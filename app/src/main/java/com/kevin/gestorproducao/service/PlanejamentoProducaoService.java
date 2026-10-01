package com.kevin.gestorproducao.service;

import static com.kevin.gestorproducao.rules.exception.ProducaoException.TipoErroProducao.TRABALHO_SEM_DEPENDENCIA;

import android.content.Context;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.Profissao;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.model.Recurso;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.Resource;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;
import com.kevin.gestorproducao.rules.CatalogoRecursos;
import com.kevin.gestorproducao.rules.exception.ProducaoException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

    // Meta de trabalhos (estoque + fila) por trabalho comum de cada profissão priorizada.
    // Ainda fixa em 1; ponto de partida para torná-la configurável por profissão.
    private static final int META_POR_TRABALHO_COMUM = 1;
    private static final int INSUMO_INDISPONIVEL = -1;

    private Consumer<String> ouvinteFalhaGravacao;

    // Chamado (na thread principal) quando uma gravação feita pelo planejamento falha depois
    // de aceita, já que o resultado do Firebase só chega de forma assíncrona.
    public void setOuvinteFalhaGravacao(Consumer<String> ouvinteFalhaGravacao) {
        this.ouvinteFalhaGravacao = ouvinteFalhaGravacao;
    }

    public ResumoPlanejamento incluirComunsProfissoesPriorizadas() {
        ResumoPlanejamento resumo = new ResumoPlanejamento();
        ArrayList<ProfissaoPersonagem> profissoes =
            profissaoPersonagemRepo.recuperaProfissoesPriorizadas(idPersonagem);

        // Cada profissão roda isolada: um problema em uma não impede as demais.
        for (ProfissaoPersonagem profissao : profissoes) {
            try {
                processaProfissaoPriorizada(profissao, resumo);
            } catch (RuntimeException e) {
                resumo.registraAviso(profissao.getNome() + ": erro inesperado (" + e.getMessage() + ")");
            }
        }

        return resumo;
    }

    private void processaProfissaoPriorizada(
        ProfissaoPersonagem profissao,
        ResumoPlanejamento resumo
    ) {
        int nivelProducao = profissao.getNivelProducao();
        ArrayList<Trabalho> trabalhosComuns =
            trabalhoRepo.recuperaTrabalhosComuns(nivelProducao, profissao.getNome());

        if (trabalhosComuns.isEmpty()) {
            resumo.registraAviso(
                "Trabalho comum de (" + profissao.getNome() + ") nível (" + nivelProducao + ") não encontrado"
            );
            return;
        }

        // Estoque e fila entram na decisão: só falta produzir o que a meta ainda não cobre.
        int[] totais = new int[trabalhosComuns.size()];
        int faltante = 0;
        for (int i = 0; i < trabalhosComuns.size(); i++) {
            totais[i] = quantidadeEmEstoqueEFila(trabalhosComuns.get(i));
            faltante += Math.max(0, META_POR_TRABALHO_COMUM - totais[i]);
        }

        if (faltante == 0) return;

        if (nivelProducao != 1 && nivelProducao != 8) {
            Trabalho trabalhoComum = trabalhosComuns.get(0);
            int maxProduzivel = calculaMaxProduzivel(trabalhoComum);

            if (maxProduzivel == INSUMO_INDISPONIVEL) {
                resumo.registraAviso(profissao.getNome() + ": catálogo de recursos indisponível");
                return;
            }

            if (maxProduzivel <= 0) {
                enfileiraProducaoDeRecursos(trabalhoComum, resumo);
                resumo.registraAguardandoInsumo(profissao.getNome());
                return;
            }

            faltante = Math.min(faltante, maxProduzivel);
        }

        String licenca = licencaPara(profissao);

        // Sempre no trabalho de menor total; o empate segue a ordem da consulta (determinística).
        while (faltante > 0) {
            int escolhido = -1;
            for (int i = 0; i < totais.length; i++) {
                if (totais[i] >= META_POR_TRABALHO_COMUM) continue;
                if (escolhido == -1 || totais[i] < totais[escolhido]) escolhido = i;
            }

            if (escolhido == -1) break;

            Trabalho trabalho = trabalhosComuns.get(escolhido);
            TrabalhoProducao nova = new TrabalhoProducao();
            nova.setIdTrabalho(trabalho.getId());
            nova.setExperiencia(trabalho.getExperiencia());
            nova.setTipoLicenca(licenca);

            if (gravaProducao(nova)) {
                resumo.registraAdicionado();
            } else {
                resumo.registraAviso(profissao.getNome() + ": produção de '" + trabalho.getNome() + "' inválida");
            }

            totais[escolhido]++;
            faltante--;
        }
    }

    private int quantidadeEmEstoqueEFila(Trabalho trabalho) {
        int quantidadeEstoque = 0;
        TrabalhoEstoque emEstoque = estoqueRepo.recuperaTrabalhoPorId(idPersonagem, trabalho.getId());
        if (emEstoque != null) {
            quantidadeEstoque = emEstoque.getQuantidade();
        }

        return quantidadeEstoque
            + producaoRepo.recuperaQuantidadeProducaoParaProduzirPorId(idPersonagem, trabalho.getId())
            + producaoRepo.recuperaQuantidadeProducaoProduzindoPorId(idPersonagem, trabalho.getId());
    }

    // Quantas produções comuns o estoque de recursos comporta. Recurso sem registro no estoque
    // conta como 0 (antes era ignorado e liberava a produção sem insumo).
    private int calculaMaxProduzivel(Trabalho trabalhoComum) {
        int nivel = trabalhoComum.getNivel();

        Profissao profissaoEnum = Profissao.fromKey(trabalhoComum.getProfissao());
        if (profissaoEnum == null) return INSUMO_INDISPONIVEL;

        Map<Recurso, Integer> recursos = CatalogoRecursos.getCatalogo().get(profissaoEnum);
        if (recursos == null || recursos.isEmpty()) return INSUMO_INDISPONIVEL;

        List<Map.Entry<Recurso, Integer>> lista = new ArrayList<>(recursos.entrySet());

        int offset = (nivel >= 16) ? 3 : 0;
        if (lista.size() < offset + 3) return INSUMO_INDISPONIVEL;

        int primario = 4 + (nivel > 16 ? nivel - 10 : nivel - 6);
        int[] quantidadesBase = { primario, primario - 1, primario - 2 };

        int maxProduzivel = Integer.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            Recurso recurso = lista.get(offset + i).getKey();
            Trabalho recursoProducao = trabalhoRepo.recuperaTrabalhoPorNome(recurso.getKey());
            if (recursoProducao == null) continue;

            TrabalhoEstoque recursoEstoque = estoqueRepo.recuperaTrabalhoPorId(idPersonagem, recursoProducao.getId());
            int emEstoque = recursoEstoque == null ? 0 : recursoEstoque.getQuantidade();

            maxProduzivel = Math.min(maxProduzivel, emEstoque / quantidadesBase[i]);
        }

        return maxProduzivel;
    }

    private void enfileiraProducaoDeRecursos(Trabalho trabalhoComum, ResumoPlanejamento resumo) {
        Trabalho producaoEmMassaRecursos = trabalhoRepo.recuperaTrabalhoProducaoRecursos(trabalhoComum);
        if (producaoEmMassaRecursos == null) return;

        int quantidadeEmMassa = producaoRepo.recuperaQuantidadeProducaoParaProduzirPorId(
            idPersonagem,
            producaoEmMassaRecursos.getId()
        );
        if (quantidadeEmMassa != 0) return;

        TrabalhoProducao novaProducao = new TrabalhoProducao();
        novaProducao.setIdTrabalho(producaoEmMassaRecursos.getId());
        novaProducao.setExperiencia(producaoEmMassaRecursos.getExperiencia());
        novaProducao.setTipoLicenca(context.getString(R.string.licencaAprendiz));

        if (gravaProducao(novaProducao)) {
            resumo.registraAdicionado();
        }
    }

    // Grava e acompanha o resultado: uma falha assíncrona é repassada ao ouvinte.
    private boolean gravaProducao(TrabalhoProducao producao) {
        if (producao.getIdTrabalho() == null || producao.getIdTrabalho().isEmpty()) return false;

        LiveData<Resource<Void>> resultado = producaoRepo.insereTrabalhoProducao(producao, idPersonagem);

        if (resultado != null && Looper.myLooper() == Looper.getMainLooper()) {
            resultado.observeForever(new Observer<Resource<Void>>() {
                @Override
                public void onChanged(Resource<Void> recurso) {
                    resultado.removeObserver(this);
                    if (recurso != null && recurso.getErro() != null && ouvinteFalhaGravacao != null) {
                        ouvinteFalhaGravacao.accept(recurso.getErro());
                    }
                }
            });
        }

        return true;
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
