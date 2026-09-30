package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.utilitario.Utilitario.geraIdAleatorio;

import com.kevin.gestorproducao.rules.CatalogoRecursos;

import java.io.Serializable;

public class TrabalhoEstoque extends Trabalho implements Serializable {
    private Integer quantidade;
    private String idTrabalho;

    public TrabalhoEstoque() {
        super();
        super.setId(geraIdAleatorio());
        quantidade = 1;
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

    public void incrementaQuantidade() {
        if (this.ehProducaoDeRecursos()) {
            if (this.ehComum()) {
                this.quantidade = CatalogoRecursos.getQuantidade(getProfissao(), getNome());
            }
            return;
        }

        quantidade += 1;
    }

    public void decrementaQuantidade(int quantidade) {
        this.quantidade -= quantidade;
    }
}
