package com.kevin.ceep.model;

import static com.kevin.ceep.utilitario.Utilitario.geraIdAleatorio;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

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
        quantidade = 1;
        valor = 0;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrabalhoVendido that = (TrabalhoVendido) o;
        return quantidade == that.quantidade &&
            valor == that.valor &&
            Objects.equals(idPersonagem, that.idPersonagem) &&
            Objects.equals(idTrabalho, that.idTrabalho) &&
            Objects.equals(descricao, that.descricao) &&
            Objects.equals(dataVenda, that.dataVenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPersonagem, idTrabalho, descricao, dataVenda, quantidade, valor);
    }

    @NonNull
    @Override
    public String toString() {
        return "TrabalhoVendido{" +
                "idPersonagem='" + idPersonagem + '\'' +
                ", idTrabalho='" + idTrabalho + '\'' +
                ", descricao='" + descricao + '\'' +
                ", dataVenda='" + dataVenda + '\'' +
                ", quantidade=" + quantidade +
                ", valor=" + valor +
                '}';
    }
}
