package com.kevin.ceep.model;

import static com.kevin.ceep.utilitario.Utilitario.geraIdAleatorio;

import java.io.Serializable;

public class TrabalhoVendido extends Trabalho implements Serializable {
    private String idPersonagem;
    private String idTrabalho;
    private String descricao;
    private String dataVenda;
    private int quantidade;
    private int valor;
    public TrabalhoVendido() {
        super();
        super.setId(geraIdAleatorio());
    }
    public String getDescricao() {
        return descricao;
    }

    public String getDataVenda() {
        return dataVenda;
    }

    public String getIdPersonagem() {
        return idPersonagem;
    }
    public String getIdTrabalho() {
        return idTrabalho;
    }
    public void setIdPersonagem(String idPersonagem) {
        this.idPersonagem = idPersonagem;
    }
    public int getQuantidade() {
        return quantidade;
    }
    public int getValor() {
        return valor;
    }
    public void setIdTrabalho(String idTrabalho) {
        this.idTrabalho = idTrabalho;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setDataVenda(String dataVenda) {
        this.dataVenda = dataVenda;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }
}
