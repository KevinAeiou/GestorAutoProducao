package com.kevin.gestorproducao.automacao;

import androidx.lifecycle.MutableLiveData;

// Ponto único, observável pela UI, do andamento da automação — o serviço publica aqui em vez de
// expor sua própria instância (Service e Fragment têm ciclos de vida independentes).
public final class AutomacaoStatus {
    public enum Fase { PARADO, RODANDO, CONCLUIDO, ERRO }

    public static class Evento {
        public final Fase fase;
        public final String personagemAtual;
        public final String trabalhoAtual;
        public final String mensagem;

        public Evento(Fase fase, String personagemAtual, String trabalhoAtual, String mensagem) {
            this.fase = fase;
            this.personagemAtual = personagemAtual;
            this.trabalhoAtual = trabalhoAtual;
            this.mensagem = mensagem;
        }
    }

    public static final MutableLiveData<Evento> status = new MutableLiveData<>(
        new Evento(Fase.PARADO, null, null, null)
    );

    private AutomacaoStatus() {}

    public static void publicar(Evento evento) {
        status.postValue(evento);
    }
}
