package com.kevin.gestorproducao.automacao.motor;

public class AcaoNavegacao {
    public enum Tipo {
        TOQUE_ANCORA,
        TOQUE_POSICAO,
        AGUARDAR,
        ALVO_NAO_ENCONTRADO,
        REQUER_INTERVENCAO_MANUAL
    }

    private final Tipo tipo;
    private final CoordenadasReferencia.Ancora ancora;
    private final Retangulo posicao;

    private AcaoNavegacao(Tipo tipo, CoordenadasReferencia.Ancora ancora, Retangulo posicao) {
        this.tipo = tipo;
        this.ancora = ancora;
        this.posicao = posicao;
    }

    public static AcaoNavegacao tocarAncora(CoordenadasReferencia.Ancora ancora) {
        return new AcaoNavegacao(Tipo.TOQUE_ANCORA, ancora, null);
    }

    public static AcaoNavegacao tocarPosicao(Retangulo posicao) {
        return new AcaoNavegacao(Tipo.TOQUE_POSICAO, null, posicao);
    }

    public static AcaoNavegacao aguardar() {
        return new AcaoNavegacao(Tipo.AGUARDAR, null, null);
    }

    public static AcaoNavegacao alvoNaoEncontrado() {
        return new AcaoNavegacao(Tipo.ALVO_NAO_ENCONTRADO, null, null);
    }

    public static AcaoNavegacao requerIntervencaoManual() {
        return new AcaoNavegacao(Tipo.REQUER_INTERVENCAO_MANUAL, null, null);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public CoordenadasReferencia.Ancora getAncora() {
        return ancora;
    }

    public Retangulo getPosicao() {
        return posicao;
    }
}
