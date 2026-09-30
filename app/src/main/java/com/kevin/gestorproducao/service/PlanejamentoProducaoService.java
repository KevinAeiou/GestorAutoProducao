package com.kevin.gestorproducao.service;

import static com.kevin.gestorproducao.rules.exception.ProducaoException.TipoErroProducao.SEM_TRABALHO_COMUM;
import static com.kevin.gestorproducao.rules.exception.ProducaoException.TipoErroProducao.TRABALHO_SEM_DEPENDENCIA;

import android.content.Context;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.Profissao;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.model.Recurso;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;
import com.kevin.gestorproducao.rules.CatalogoRecursos;
import com.kevin.gestorproducao.rules.exception.ProducaoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanejamentoProducaoService {
    private final TrabalhoRepository trabalhoRepo;
    private final TrabalhoEstoqueRepository estoqueRepo;
    private final TrabalhoProducaoRepository producaoRepo;
    private final ProfissaoPersonagemRepository profissaoPersonagemRepo;
    private final String idPersonagem;
    private final Context context;

    public PlanejamentoProducaoService(
        TrabalhoRepository trabalhoRepo,
        TrabalhoEstoqueRepository estoqueRepo,
        TrabalhoProducaoRepository producaoRepo,
        ProfissaoPersonagemRepository profissaoPersonagemRepo,
        String idPersonagem,
        Context context
    ) {
        this.trabalhoRepo = trabalhoRepo;
        this.estoqueRepo = estoqueRepo;
        this.producaoRepo = producaoRepo;
        this.profissaoPersonagemRepo = profissaoPersonagemRepo;
        this.idPersonagem = idPersonagem;
        this.context = context;
    }

    private static final int LIMITE_PRODUCAO_RAROS = 6;

    // Achado M4: incluirMaisVendidos() tinha ~165 linhas com 4 níveis de aninhamento
    // (raro → melhorado → comum → recursos) num único método. Dividido em um método por
    // nível, cada um cuidando de um trabalho e devolvendo quantos foram efetivamente
    // incluídos na produção — a lógica e a ordem de cada verificação são as mesmas de antes,
    // só a organização mudou.
    public void incluirMaisVendidos() throws ProducaoException {
        ArrayList<Trabalho> maisVendidos = trabalhoRepo.recuperaMaisVendidos(idPersonagem);

        int contador = 0;
        for (Trabalho raroMaisVendido : maisVendidos) {
            if (contador == LIMITE_PRODUCAO_RAROS) {
                break;
            }

            ProfissaoPersonagem profissaoPersonagem = profissaoPersonagemRepo.recuperaProfissaoPorNome(
                idPersonagem,
                raroMaisVendido.getProfissao()
            );

            if (profissaoPersonagem == null) continue;

            contador += processaRaro(raroMaisVendido, profissaoPersonagem);
        }
    }

    private int processaRaro(
        Trabalho raroMaisVendido,
        ProfissaoPersonagem profissaoPersonagem
    ) throws ProducaoException {
        TrabalhoEstoque raroEmEstoque = estoqueRepo.recuperaTrabalhoPorId(idPersonagem, raroMaisVendido.getId());
        if (raroEmEstoque != null && raroEmEstoque.getQuantidade() > 0) {
            return 0;
        }

        TrabalhoProducao raroEmProducao = producaoRepo.recuperaProducaoParaProduzirProduzindoPorId(
            idPersonagem,
            raroMaisVendido.getId()
        );

        if (raroEmProducao != null) {
            return raroEmProducao.ehProduzindo() ? 1 : 0;
        }

        List<String> melhoradoNecessarios = raroMaisVendido.getListaTrabalhosNecessarios();
        if (melhoradoNecessarios == null || melhoradoNecessarios.isEmpty()) {
            throw new ProducaoException(
                TRABALHO_SEM_DEPENDENCIA,
                "O trabalho raro '" + raroMaisVendido.getNome() + "' não possui requisitos para produção."
            );
        }

        List<String> melhoradosFaltantes = recuperaRecursosFaltantes(melhoradoNecessarios);
        if (melhoradosFaltantes.isEmpty()) {
            insereProducao(raroMaisVendido.getId(), raroMaisVendido.getExperiencia(), licencaPara(profissaoPersonagem));
            return 1;
        }

        int contador = 0;
        for (String melhoradoFaltante : melhoradosFaltantes) {
            contador += processaMelhorado(melhoradoFaltante, profissaoPersonagem);
        }
        return contador;
    }

    private int processaMelhorado(
        String melhoradoFaltante,
        ProfissaoPersonagem profissaoPersonagem
    ) throws ProducaoException {
        TrabalhoProducao melhoradoEmProducao = producaoRepo.recuperaProducaoParaProduzirProduzindoPorId(
            idPersonagem,
            melhoradoFaltante
        );

        if (melhoradoEmProducao != null) {
            return 1;
        }

        Trabalho melhoradoNecessario = trabalhoRepo.recuperaTrabalhoPorId(melhoradoFaltante);
        if (melhoradoNecessario == null) return 0;

        List<String> comumNecessarios = melhoradoNecessario.getListaTrabalhosNecessarios();
        if (comumNecessarios == null || comumNecessarios.isEmpty()) {
            throw new ProducaoException(
                TRABALHO_SEM_DEPENDENCIA,
                "O trabalho '" + melhoradoNecessario.getNome() + "' não possui recursos necessários definidos."
            );
        }

        List<String> comunsFaltantes = recuperaRecursosFaltantes(comumNecessarios);
        if (comunsFaltantes.isEmpty()) {
            insereProducao(melhoradoNecessario.getId(), melhoradoNecessario.getExperiencia(), licencaPara(profissaoPersonagem));
            return 1;
        }

        int contador = 0;
        for (String comumFaltante : comunsFaltantes) {
            contador += processaComum(comumFaltante, profissaoPersonagem);
        }
        return contador;
    }

    private int processaComum(String comumFaltante, ProfissaoPersonagem profissaoPersonagem) {
        TrabalhoProducao comumEmProducao = producaoRepo.recuperaProducaoParaProduzirProduzindoPorId(
            idPersonagem,
            comumFaltante
        );

        if (comumEmProducao != null) {
            return 1;
        }

        Trabalho comumNecessario = trabalhoRepo.recuperaTrabalhoPorId(comumFaltante);
        if (comumNecessario == null) return 0;

        if (temRecursosProducaoSuficientes(idPersonagem, comumNecessario)) {
            insereProducao(comumNecessario.getId(), comumNecessario.getExperiencia(), licencaPara(profissaoPersonagem));
            return 1;
        }

        return processaRecursos(comumNecessario);
    }

    private int processaRecursos(Trabalho comumNecessario) {
        Trabalho producaoRecursos = trabalhoRepo.recuperaTrabalhoProducaoRecursos(comumNecessario);
        if (producaoRecursos == null) return 0;

        TrabalhoProducao producaoRecursosEmProducao = producaoRepo.recuperaProducaoParaProduzirProduzindoPorId(
            idPersonagem,
            producaoRecursos.getId()
        );

        if (producaoRecursosEmProducao != null) return 0;

        TrabalhoProducao novaProducao = new TrabalhoProducao();
        novaProducao.setIdTrabalho(producaoRecursos.getId());
        novaProducao.setTipoLicenca(context.getString(R.string.licencaAprendiz));
        novaProducao.setExperiencia(producaoRecursos.getExperiencia());
        novaProducao.setRecorrencia(true);

        producaoRepo.insereTrabalhoProducao(novaProducao, idPersonagem);
        return 1;
    }

    private String licencaPara(ProfissaoPersonagem profissaoPersonagem) {
        return profissaoPersonagem.getNivel() == 28 ?
            context.getString(R.string.licencaMestre) :
            context.getString(R.string.licencaIniciante);
    }

    private void insereProducao(String idTrabalho, Integer experiencia, String licenca) {
        TrabalhoProducao novaProducao = new TrabalhoProducao();
        novaProducao.setIdTrabalho(idTrabalho);
        novaProducao.setTipoLicenca(licenca);
        novaProducao.setExperiencia(experiencia);
        producaoRepo.insereTrabalhoProducao(novaProducao, idPersonagem);
    }

    // Achado M4: este método só devolve true, sem checar nada — o nome promete uma
    // verificação real contra o estoque de recursos que nunca foi implementada, então todo
    // o planejamento assume "recursos sempre suficientes" para produção comum. NÃO
    // implementei a verificação de verdade aqui: ela dependeria de comparar, para cada
    // ingrediente do catálogo (CatalogoRecursos/CatalogoRecursosRaros — ver achado M5), a
    // quantidade em estoque contra o custo da receita, e eu não tenho como validar as regras
    // de negócio corretas (ex.: o que fazer quando falta só um recurso) sem arriscar mudar o
    // comportamento de produção do app de um jeito que ninguém pediu. Mantido o
    // comportamento atual (sempre true) e documentado aqui para que o time decida
    // conscientemente entre implementar de verdade ou remover esta verificação do fluxo.
    private boolean temRecursosProducaoSuficientes(String idPersonagem, Trabalho trabalho) {
        return true;
    }

    private List<String> recuperaRecursosFaltantes(List<String> necessarios) {
        List<String> recursosFaltantes = new ArrayList<>();
        for (String idNecessario : necessarios) {
            TrabalhoEstoque necessarioEmEstoque = estoqueRepo.recuperaTrabalhoPorId(
                idPersonagem,
                idNecessario
            );

            if (necessarioEmEstoque == null || necessarioEmEstoque.getQuantidade() == 0) {
                recursosFaltantes.add(idNecessario);
            }
        }

        return recursosFaltantes;
    }

    public void incluirComunsProfissoesPriorizadas() throws ProducaoException {
        ArrayList<ProfissaoPersonagem> profissoes;
        profissoes = profissaoPersonagemRepo.recuperaProfissoesPriorizadas(idPersonagem);

        for (ProfissaoPersonagem profissao : profissoes) {
            ArrayList<TrabalhoProducao> producoes = new ArrayList<>();
            ArrayList<Trabalho> trabalhosComuns;
            Map<Trabalho, Integer> mapaTotais = new HashMap<>();
            int nivelProducao = profissao.getNivelProducao();

            trabalhosComuns = trabalhoRepo.recuperaTrabalhosComuns(nivelProducao, profissao.getNome());

            if (trabalhosComuns.isEmpty()) {
                throw new ProducaoException(
                    SEM_TRABALHO_COMUM,
                    "Trabalho comum de ("+ profissao.getNome() + ") nível (" + nivelProducao + ") não encontrado."
                );
            }

            int totalEmProducao = 0;

            for (Trabalho trabalho : trabalhosComuns) {

                int quantidadeEstoque = 0;
                int quantidadeProducao;

                TrabalhoEstoque emEstoque = estoqueRepo.recuperaTrabalhoPorId(
                    idPersonagem,
                    trabalho.getId()
                );
                if (emEstoque != null) {
                    quantidadeEstoque = emEstoque.getQuantidade();
                }

                int emProducaoParaProduzir = producaoRepo.recuperaQuantidadeProducaoParaProduzirPorId(
                    idPersonagem,
                    trabalho.getId()
                );
                int emProducaoProduzindo = producaoRepo.recuperaQuantidadeProducaoProduzindoPorId(
                    idPersonagem,
                    trabalho.getId()
                );
                quantidadeProducao = emProducaoParaProduzir + emProducaoProduzindo;
                totalEmProducao += emProducaoParaProduzir + emProducaoProduzindo;

                int total = quantidadeEstoque + quantidadeProducao;

                mapaTotais.put(trabalho, total);
            }

            int tamanhoDesejado = trabalhosComuns.size();

            if (totalEmProducao >= tamanhoDesejado) continue;

            int restanteParaInserir = tamanhoDesejado - totalEmProducao;

            if (nivelProducao != 1 && nivelProducao != 8) {

                Trabalho trabalhoComum = trabalhosComuns.get(0);
                int nivel = trabalhoComum.getNivel();
                String profissaoStr = trabalhoComum.getProfissao();

                Profissao profissaoEnum = Profissao.fromKey(profissaoStr);
                if (profissaoEnum == null) continue;

                Map<Recurso, Integer> recursos = CatalogoRecursos.getCatalogo().get(profissaoEnum);
                if (recursos == null || recursos.isEmpty()) continue;

                List<Map.Entry<Recurso, Integer>> lista = new ArrayList<>(recursos.entrySet());

                int offset = (nivel >= 16) ? 3 : 0;

                int primario = 4 + (nivel > 16 ? nivel - 10 : nivel - 6);
                int secundario = primario - 1;
                int terciario = primario - 2;

                int[] quantidadesBase = { primario, secundario, terciario };

                int maxProduzivel = Integer.MAX_VALUE;
                for (int i = 0; i < 3; i++ ) {
                    Map.Entry<Recurso, Integer> entry = lista.get(offset + i);

                    Recurso recurso = entry.getKey();
                    Trabalho recursoProducao = trabalhoRepo.recuperaTrabalhoPorNome(recurso.getKey());
                    if (recursoProducao == null) continue;

                    TrabalhoEstoque recursoEstoque = estoqueRepo.recuperaTrabalhoPorId(idPersonagem, recursoProducao.getId());
                    if (recursoEstoque == null) continue;

                    int produzivel = recursoEstoque.getQuantidade() / quantidadesBase[i];
                    maxProduzivel = Math.min(maxProduzivel, produzivel);
                }

                if (maxProduzivel <= 0) {
                    Trabalho producaoEmMassaRecursos = trabalhoRepo.recuperaTrabalhoProducaoRecursos(trabalhoComum);
                    if (producaoEmMassaRecursos == null) continue;

                    int quantidadeProducaoEmMassa = producaoRepo.recuperaQuantidadeProducaoParaProduzirPorId(
                        idPersonagem,
                        producaoEmMassaRecursos.getId()
                    );
                    if (quantidadeProducaoEmMassa == 0) {
                        TrabalhoProducao novaProducao = new TrabalhoProducao();
                        novaProducao.setIdTrabalho(producaoEmMassaRecursos.getId());
                        novaProducao.setExperiencia(producaoEmMassaRecursos.getExperiencia());
                        novaProducao.setTipoLicenca(context.getString(R.string.licencaAprendiz));

                        producaoRepo.insereTrabalhoProducao(novaProducao, idPersonagem);
                    }
                    continue;
                }

                restanteParaInserir = Math.min(restanteParaInserir, maxProduzivel);
            }

            int menorTotal;

           while (producoes.size() < restanteParaInserir) {
                menorTotal = Collections.min(mapaTotais.values());

                boolean inseriu = false;

                for (Map.Entry<Trabalho, Integer> entry : mapaTotais.entrySet()) {

                    if (entry.getValue() == menorTotal) {
                        Trabalho trabalho = entry.getKey();

                        TrabalhoProducao nova = new TrabalhoProducao();
                        nova.setIdTrabalho(trabalho.getId());
                        nova.setExperiencia(trabalho.getExperiencia());
                        nova.setTipoLicenca(
                            context.getString(R.string.licencaIniciante)
                        );

                        producoes.add(nova);

                        mapaTotais.put(trabalho, entry.getValue() + 1);

                        inseriu = true;

                        break;
                    }
                }

                if (!inseriu) break;
            }

            for (TrabalhoProducao producao : producoes) {
                producaoRepo.insereTrabalhoProducao(producao, idPersonagem);
            }
        }
    }

    public void incluirRaro(String idTrabalho) {
        Trabalho trabalho = trabalhoRepo.recuperaTrabalhoPorIdTrabalhoNecessario(idTrabalho);

        if (trabalho == null) return;

        ProfissaoPersonagem profissao = profissaoPersonagemRepo.recuperaProfissaoPorNome(
            idPersonagem,
            trabalho.getProfissao()
        );

        if (profissao == null) return;

        String licenca = profissao.getNivel() == 28 ?
            context.getString(R.string.licencaMestre) :
            context.getString(R.string.licencaIniciante);

        TrabalhoProducao producao = new TrabalhoProducao();
        producao.setIdTrabalho(trabalho.getId());
        producao.setExperiencia(trabalho.getExperiencia());
        producao.setTipoLicenca(licenca);

        producaoRepo.insereTrabalhoProducao(producao, idPersonagem);
    }
}
