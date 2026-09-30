package com.kevin.gestorproducao.rules.exception;

public class ProducaoException extends Exception{
    private final TipoErroProducao tipo;

    public enum TipoErroProducao {
        SEM_RECURSOS,
        TRABALHO_SEM_DEPENDENCIA,
        DADO_INVALIDO,
        SEM_TRABALHO_COMUM
    }

    public ProducaoException(TipoErroProducao tipo, String mensagem) {
        super(mensagem);
        this.tipo = tipo;
    }

    public TipoErroProducao getTipo() {
        return tipo;
    }
}
