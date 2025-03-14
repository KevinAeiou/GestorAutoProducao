package com.kevin.ceep.model;

import static com.kevin.ceep.utilitario.Utilitario.geraIdAleatorio;

import java.io.Serializable;

public class TrabalhoEstoque extends Trabalho implements Serializable {
    private Integer quantidade;
    private String idTrabalho;
    public TrabalhoEstoque() {
        super();
        super.setId(geraIdAleatorio());
    }
    public Integer getQuantidade() {
        return quantidade;
    }
    public void setQuantidade(int novaQuantidade) {
        if (novaQuantidade < 0) novaQuantidade = 0;
        this.quantidade = novaQuantidade;
    }
    public String getIdTrabalho() {
        return idTrabalho;
    }
    public void setIdTrabalho(String trabalhoId) {
        this.idTrabalho = trabalhoId;
    }
}
