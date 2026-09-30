package com.kevin.gestorproducao.automacao;

import android.content.Context;
import android.os.Handler;

import com.kevin.gestorproducao.automacao.captura.CapturaTelaManager;
import com.kevin.gestorproducao.automacao.motor.AcaoNavegacao;
import com.kevin.gestorproducao.automacao.motor.AlvoNavegacao;
import com.kevin.gestorproducao.automacao.motor.CoordenadasReferencia;
import com.kevin.gestorproducao.automacao.motor.DetectorEstadoTela;
import com.kevin.gestorproducao.automacao.motor.EstadoTela;
import com.kevin.gestorproducao.automacao.motor.NavegadorJogo;
import com.kevin.gestorproducao.automacao.motor.Retangulo;
import com.kevin.gestorproducao.automacao.motor.TextoDetectado;
import com.kevin.gestorproducao.automacao.ocr.LeitorTextoTela;
import com.kevin.gestorproducao.automacao.util.LiveDataOneShot;
import com.kevin.gestorproducao.dao.PersonagemDao;
import com.kevin.gestorproducao.dao.ProducaoDao;
import com.kevin.gestorproducao.model.Personagem;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;
import com.kevin.gestorproducao.service.ProducaoServicosFactory;
import com.kevin.gestorproducao.service.ServicosProducaoPersonagem;
import com.kevin.gestorproducao.ui.activity.Constantes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

// Lógica do loop de automação: para cada personagem com autoProducao=true, percorre seus
// TrabalhoProducao pendentes e usa NavegadorJogo/DetectorEstadoTela para navegar até iniciar
// cada um. Ao confirmar visualmente que o jogo aceitou, grava o novo estado reaproveitando o
// mesmo caminho de escrita usado pelo swipe manual em ListaTrabalhosProducaoFragment
// (TrabalhoProducaoRepository.modificaTrabalhoProducao + ProducaoFluxoService.processarPosModificacao).
public class AutomacaoOrquestrador {
    private static final long INTERVALO_PASSO_MS = 800;
    private static final int MAX_TENTATIVAS_POR_ACAO = 8;

    private final Context context;
    private final Handler handler;
    private final CapturaTelaManager capturaTelaManager;
    private final LeitorTextoTela leitorTextoTela;
    private final PersonagemDao personagemDao;
    private final ProducaoDao producaoDao;
    private final TrabalhoProducaoRepository producaoRepository;
    private final TrabalhoRepository trabalhoRepository;
    private final TrabalhoEstoqueRepository estoqueRepository;
    private final ProfissaoPersonagemRepository profissaoPersonagemRepository;

    private volatile boolean rodando;
    private Runnable aoConcluir;
    private Deque<Personagem> personagensPendentes;
    private Personagem personagemAtual;
    private Deque<TrabalhoProducao> trabalhosPendentesPersonagem;
    private TrabalhoProducao trabalhoAtual;
    private boolean aguardandoConfirmacaoProducao;
    private boolean progressoNesteCiclo;
    private boolean existePersonagemComAutoProducao;
    private int tentativasPassoAtual;

    public AutomacaoOrquestrador(
        Context context,
        Handler handler,
        CapturaTelaManager capturaTelaManager
    ) {
        this.context = context.getApplicationContext();
        this.handler = handler;
        this.capturaTelaManager = capturaTelaManager;
        this.leitorTextoTela = new LeitorTextoTela(handler::post);
        // Leitura direta pelo DAO (e não por PersonagemRepository/TrabalhoProducaoRepository):
        // essas duas leituras de repository reaproveitam um MutableLiveData de instância única
        // por Repository (Resource<...>), então observá-las aqui correria o risco de pegar um
        // valor publicado por uma chamada concorrente feita pela UI (usuário navegando enquanto
        // a automação roda). Os DAOs são apenas SQLite local, seguros para chamar direto aqui,
        // já que este orquestrador todo roda numa HandlerThread dedicada, fora da main thread.
        this.personagemDao = new PersonagemDao(this.context);
        this.producaoDao = new ProducaoDao(this.context);
        this.producaoRepository = TrabalhoProducaoRepository.getInstance(this.context);
        this.trabalhoRepository = TrabalhoRepository.getInstancia(this.context);
        this.estoqueRepository = TrabalhoEstoqueRepository.getInstance(this.context);
        this.profissaoPersonagemRepository = ProfissaoPersonagemRepository.getInstance(this.context);
    }

    public void iniciar(Runnable aoConcluir) {
        this.aoConcluir = aoConcluir;
        rodando = true;

        iniciarCiclo();
    }

    // Um "ciclo" é uma volta completa por todos os personagens com auto produção. Um trabalho
    // não encontrado não é descartado (Trabalho não some da lista de pendentes no banco) — só é
    // pulado nesta volta, então uma nova volta o tenta de novo, para o caso de ter ficado
    // disponível nesse meio tempo. Para não girar para sempre em trabalhos que nunca serão
    // encontrados, um ciclo inteiro sem nenhum início bem-sucedido encerra a automação.
    private void iniciarCiclo() {
        if (!rodando) return;

        progressoNesteCiclo = false;

        AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
            AutomacaoStatus.Fase.RODANDO, null, null, "Buscando personagens com auto produção…"
        ));

        carregarPersonagensPendentes();
    }

    public void parar() {
        if (!rodando) return;

        rodando = false;
        leitorTextoTela.fechar();

        AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
            AutomacaoStatus.Fase.PARADO, null, null, null
        ));
    }

    private void carregarPersonagensPendentes() {
        if (!rodando) return;

        ArrayList<Personagem> personagens = personagemDao.recuperaPersonagens();
        personagensPendentes = new ArrayDeque<>();

        for (Personagem personagem : personagens) {
            if (personagem.isAutoProducao()) {
                personagensPendentes.add(personagem);
            }
        }

        existePersonagemComAutoProducao = !personagensPendentes.isEmpty();

        avancarParaProximoPersonagem();
    }

    private void avancarParaProximoPersonagem() {
        if (!rodando) return;

        if (personagensPendentes.isEmpty()) {
            finalizarCiclo();
            return;
        }

        personagemAtual = personagensPendentes.poll();
        carregarTrabalhosPendentes();
    }

    private void finalizarCiclo() {
        if (!existePersonagemComAutoProducao) {
            concluir("Nenhum personagem com auto produção está ativada.");
            return;
        }

        if (progressoNesteCiclo) {
            iniciarCiclo();
            return;
        }

        concluir("Nenhum trabalho pendente pôde ser iniciado agora.");
    }

    private void carregarTrabalhosPendentes() {
        if (!rodando) return;

        ArrayList<TrabalhoProducao> producoes = producaoDao.recuperaProducoes(personagemAtual.getId());
        trabalhosPendentesPersonagem = new ArrayDeque<>();

        for (TrabalhoProducao trabalho : producoes) {
            if (trabalho.ehProduzir()) {
                trabalhosPendentesPersonagem.add(trabalho);
            }
        }

        avancarParaProximoTrabalho();
    }

    private void avancarParaProximoTrabalho() {
        if (!rodando) return;

        if (trabalhosPendentesPersonagem.isEmpty()) {
            avancarParaProximoPersonagem();
            return;
        }

        trabalhoAtual = trabalhosPendentesPersonagem.poll();
        tentativasPassoAtual = 0;
        aguardandoConfirmacaoProducao = false;

        AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
            AutomacaoStatus.Fase.RODANDO,
            personagemAtual.getNome(),
            trabalhoAtual.getNome(),
            "Procurando " + trabalhoAtual.getNome() + "…"
        ));

        agendarPasso();
    }

    private void agendarPasso() {
        if (!rodando) return;

        handler.postDelayed(this::executarPasso, INTERVALO_PASSO_MS);
    }

    private void executarPasso() {
        if (!rodando) return;

        capturaTelaManager.capturarFrame(frame -> {
            if (!rodando) return;

            if (frame == null) {
                registrarTentativaFalha("não foi possível capturar a tela");
                return;
            }

            leitorTextoTela.reconhecer(frame, this::processarLeitura);
        });
    }

    private void processarLeitura(TextoDetectado texto) {
        if (!rodando) return;

        EstadoTela estadoAtual = DetectorEstadoTela.detectar(texto);

        boolean voltouParaListaAposConfirmar = aguardandoConfirmacaoProducao && (
            estadoAtual == EstadoTela.ARTESANATO_LISTA_TRABALHOS
                || estadoAtual == EstadoTela.ARTESANATO_PRODUCOES_ATUAIS
        );

        if (voltouParaListaAposConfirmar) {
            marcarTrabalhoIniciado();
            return;
        }

        AlvoNavegacao alvo = new AlvoNavegacao(trabalhoAtual.getProfissao(), trabalhoAtual.getNome());
        AcaoNavegacao acao = NavegadorJogo.decidirProximaAcao(estadoAtual, texto, alvo);

        if (acao.getTipo() == AcaoNavegacao.Tipo.REQUER_INTERVENCAO_MANUAL) {
            AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
                AutomacaoStatus.Fase.ERRO,
                personagemAtual.getNome(),
                trabalhoAtual.getNome(),
                "É necessário fazer login manualmente no jogo antes de iniciar a automação"
            ));
            parar();
            return;
        }

        if (acao.getTipo() == AcaoNavegacao.Tipo.ALVO_NAO_ENCONTRADO) {
            AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
                AutomacaoStatus.Fase.RODANDO,
                personagemAtual.getNome(),
                trabalhoAtual.getNome(),
                trabalhoAtual.getNome() + " não está disponível agora, pulando"
            ));
            avancarParaProximoTrabalho();
            return;
        }

        aguardandoConfirmacaoProducao = estadoAtual == EstadoTela.CONFIRMACAO_INICIAR_PRODUCAO;

        executarAcao(acao);
    }

    private void executarAcao(AcaoNavegacao acao) {
        Retangulo posicao = acao.getTipo() == AcaoNavegacao.Tipo.TOQUE_ANCORA
            ? CoordenadasReferencia.resolver(acao.getAncora(), capturaTelaManager.getLargura(), capturaTelaManager.getAltura())
            : acao.getPosicao();

        AutomacaoAccessibilityService.tocar(posicao.centroX(), posicao.centroY(), handler, sucesso -> {
            if (!sucesso) {
                registrarTentativaFalha("serviço de acessibilidade indisponível");
                return;
            }

            tentativasPassoAtual = 0;
            agendarPasso();
        });
    }

    private void registrarTentativaFalha(String motivo) {
        tentativasPassoAtual++;

        if (tentativasPassoAtual >= MAX_TENTATIVAS_POR_ACAO) {
            AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
                AutomacaoStatus.Fase.RODANDO,
                personagemAtual.getNome(),
                trabalhoAtual.getNome(),
                "Não foi possível avançar em " + trabalhoAtual.getNome() + " (" + motivo + "), pulando"
            ));
            avancarParaProximoTrabalho();
            return;
        }

        agendarPasso();
    }

    private void marcarTrabalhoIniciado() {
        aguardandoConfirmacaoProducao = false;

        int estadoAnterior = trabalhoAtual.getEstado();
        trabalhoAtual.atualizarEstado(Constantes.CODIGO_TRABALHO_PRODUZINDO);
        trabalhoAtual.marcarModificacao();

        LiveDataOneShot.observar(
            producaoRepository.modificaTrabalhoProducao(trabalhoAtual, personagemAtual.getId()),
            resultado -> {
                if (!rodando) return;

                if (resultado.getErro() != null) {
                    AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
                        AutomacaoStatus.Fase.RODANDO,
                        personagemAtual.getNome(),
                        trabalhoAtual.getNome(),
                        "Erro ao registrar início: " + resultado.getErro()
                    ));
                    avancarParaProximoTrabalho();
                    return;
                }

                ServicosProducaoPersonagem servicos = ProducaoServicosFactory.cria(
                    trabalhoRepository,
                    estoqueRepository,
                    producaoRepository,
                    profissaoPersonagemRepository,
                    personagemAtual.getId(),
                    context
                );

                servicos.getProducaoFluxoService().processarPosModificacao(trabalhoAtual, estadoAnterior);

                progressoNesteCiclo = true;

                AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
                    AutomacaoStatus.Fase.RODANDO,
                    personagemAtual.getNome(),
                    trabalhoAtual.getNome(),
                    trabalhoAtual.getNome() + " iniciado com sucesso"
                ));

                avancarParaProximoTrabalho();
            }
        );
    }

    private void concluir(String mensagem) {
        rodando = false;
        leitorTextoTela.fechar();

        AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
            AutomacaoStatus.Fase.CONCLUIDO, null, null, mensagem
        ));

        if (aoConcluir != null) {
            aoConcluir.run();
        }
    }
}
