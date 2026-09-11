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

    public void incluirMaisVendidos() throws ProducaoException {
        ArrayList<Trabalho> maisVendidos = trabalhoRepo.recuperaMaisVendidos(idPersonagem);

        int LIMITE_PRODUCAO_RAROS = 6;
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

            TrabalhoEstoque raroEmEstoque = estoqueRepo.recuperaTrabalhoPorId(idPersonagem, raroMaisVendido.getId());
            if (raroEmEstoque == null || raroEmEstoque.getQuantidade() == 0) {
                TrabalhoProducao raroEmProducao = producaoRepo.recuperaProducaoParaProduzirProduzindoPorId(
                    idPersonagem,
                    raroMaisVendido.getId()
                );

                if (raroEmProducao == null) {
                    List<String> melhoradoNecessarios = raroMaisVendido.getListaTrabalhosNecessarios();
                    if (melhoradoNecessarios == null || melhoradoNecessarios.isEmpty()) {
                        throw new ProducaoException(
                            TRABALHO_SEM_DEPENDENCIA,
                            "O trabalho raro '" + raroMaisVendido.getNome() + "' não possui requisitos para produção."
                        );
                    }

                    List<String> melhoradosFaltantes = recuperaRecursosFaltantes(melhoradoNecessarios);
                    if (melhoradosFaltantes.isEmpty()) {
                        String licenca = profissaoPersonagem.getNivel() == 28 ?
                            context.getString(R.string.licencaMestre) :
                            context.getString(R.string.licencaIniciante);

                        TrabalhoProducao novaProducao = new TrabalhoProducao();
                        novaProducao.setIdTrabalho(raroMaisVendido.getId());
                        novaProducao.setTipoLicenca(licenca);
                        novaProducao.setExperiencia(raroMaisVendido.getExperiencia());

                        producaoRepo.insereTrabalhoProducao(novaProducao, idPersonagem);

                        contador++;
                        continue;
                    }

                    for (String melhoradoFaltante : melhoradosFaltantes) {
                        TrabalhoProducao melhoradoEmProducao = producaoRepo.recuperaProducaoParaProduzirProduzindoPorId(
                            idPersonagem,
                            melhoradoFaltante
                        );

                        if (melhoradoEmProducao == null) {
                            Trabalho melhoradoNecessario = trabalhoRepo.recuperaTrabalhoPorId(melhoradoFaltante);

                            if (melhoradoNecessario == null) continue;

                            List<String> comumNecessarios = melhoradoNecessario.getListaTrabalhosNecessarios();
                            if (comumNecessarios == null || comumNecessarios.isEmpty()) {
                                throw new ProducaoException(
                                    TRABALHO_SEM_DEPENDENCIA,
                                    "O trabalho '" + melhoradoNecessario.getNome() + "' não possui recursos necessários definidos."
                                );
                            }

                            List<String> comunsFaltantes = recuperaRecursosFaltantes(comumNecessarios);

                            if (comunsFaltantes.isEmpty()) {
                                String licenca = profissaoPersonagem.getNivel() == 28 ?
                                    context.getString(R.string.licencaMestre) :
                                    context.getString(R.string.licencaIniciante);

                                TrabalhoProducao novaProducao = new TrabalhoProducao();
                                novaProducao.setIdTrabalho(melhoradoNecessario.getId());
                                novaProducao.setTipoLicenca(licenca);
                                novaProducao.setExperiencia(melhoradoNecessario.getExperiencia());

                                producaoRepo.insereTrabalhoProducao(novaProducao, idPersonagem);
                                contador++;
                                continue;
                            }

                            for (String comumFaltante : comunsFaltantes) {
                                TrabalhoProducao comumEmProducao = producaoRepo.recuperaProducaoParaProduzirProduzindoPorId(
                                    idPersonagem,
                                    comumFaltante
                                );

                                if (comumEmProducao == null) {
                                    Trabalho comumNecessario = trabalhoRepo.recuperaTrabalhoPorId(comumFaltante);

                                    if (comumNecessario == null) continue;

                                    if (temRecursosProducaoSuficientes(idPersonagem, comumNecessario)) {
                                        String licenca = profissaoPersonagem.getNivel() == 28 ?
                                            context.getString(R.string.licencaMestre) :
                                            context.getString(R.string.licencaIniciante);

                                        TrabalhoProducao novaProducao = new TrabalhoProducao();
                                        novaProducao.setIdTrabalho(comumNecessario.getId());
                                        novaProducao.setTipoLicenca(licenca);
                                        novaProducao.setExperiencia(comumNecessario.getExperiencia());

                                        producaoRepo.insereTrabalhoProducao(novaProducao, idPersonagem);
                                        contador++;
                                        continue;
                                    }

                                    Trabalho producaoRecursos = trabalhoRepo.recuperaTrabalhoProducaoRecursos(comumNecessario);
                                    if (producaoRecursos == null) continue;

                                    TrabalhoProducao producaoRecursosEmProducao = producaoRepo.recuperaProducaoParaProduzirProduzindoPorId(
                                        idPersonagem,
                                        producaoRecursos.getId()
                                    );

                                    if (producaoRecursosEmProducao == null) {
                                        TrabalhoProducao novaProducao = new TrabalhoProducao();
                                        novaProducao.setIdTrabalho(producaoRecursos.getId());
                                        novaProducao.setTipoLicenca(context.getString(R.string.licencaAprendiz));
                                        novaProducao.setExperiencia(producaoRecursos.getExperiencia());
                                        novaProducao.setRecorrencia(true);

                                        producaoRepo.insereTrabalhoProducao(novaProducao, idPersonagem);
                                        contador++;
                                    }
                                    continue;
                                }

                                contador++;
                            }
                            continue;
                        }
                        contador++;
                    }
                    continue;
                }
                if (raroEmProducao.ehProduzindo()) {
                    contador += 1;
                }
            }
        }
    }

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
