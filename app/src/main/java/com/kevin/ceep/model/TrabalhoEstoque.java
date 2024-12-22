package com.kevin.ceep.model;

import static com.kevin.ceep.utilitario.Utilitario.geraIdAleatorio;

import java.io.Serializable;

public class TrabalhoEstoque extends Trabalho implements Serializable {
    private Integer quantidade;
    private String trabalhoId;
    public TrabalhoEstoque() {
        super();
        super.setId(geraIdAleatorio());
    }
    public Integer getQuantidade() {
        return quantidade;
    }
    public void setQuantidade(int novaQuantidade) {
        this.quantidade = novaQuantidade;
    }
    public String getTrabalhoId() {
        return trabalhoId;
    }
    public void setTrabalhoId(String trabalhoId) {
        this.trabalhoId = trabalhoId;
    }
}
