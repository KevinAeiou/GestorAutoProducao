package com.kevin.gestorproducao.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class TrabalhoChanceVenda extends Trabalho implements Serializable {
    private String idTrabalho;
    private int estoqueAtual;
    private int quantidadeVendidaPeriodo;
    private Long ultimaVendaEm;
    private double score;
    private boolean ofertado;

    public String getIdTrabalho() {
        return idTrabalho;
    }

    public void setIdTrabalho(String idTrabalho) {
        this.idTrabalho = idTrabalho;
    }

    public int getEstoqueAtual() {
        return estoqueAtual;
    }

    public void setEstoqueAtual(int estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public int getQuantidadeVendidaPeriodo() {
        return quantidadeVendidaPeriodo;
    }

    public void setQuantidadeVendidaPeriodo(int quantidadeVendidaPeriodo) {
        this.quantidadeVendidaPeriodo = quantidadeVendidaPeriodo;
    }

    public Long getUltimaVendaEm() {
        return ultimaVendaEm;
    }

    public void setUltimaVendaEm(Long ultimaVendaEm) {
        this.ultimaVendaEm = ultimaVendaEm;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public boolean isOfertado() {
        return ofertado;
    }

    public void setOfertado(boolean ofertado) {
        this.ofertado = ofertado;
    }

    public boolean nuncaVendidoNoPeriodo() {
        return ultimaVendaEm == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrabalhoChanceVenda that = (TrabalhoChanceVenda) o;
        return estoqueAtual == that.estoqueAtual &&
            ofertado == that.ofertado &&
            quantidadeVendidaPeriodo == that.quantidadeVendidaPeriodo &&
            Double.compare(that.score, score) == 0 &&
            Objects.equals(idTrabalho, that.idTrabalho) &&
            Objects.equals(ultimaVendaEm, that.ultimaVendaEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTrabalho, estoqueAtual, quantidadeVendidaPeriodo, ultimaVendaEm, score, ofertado);
    }

    @NonNull
    @Override
    public String toString() {
        return "TrabalhoChanceVenda{" +
            "idTrabalho='" + idTrabalho + '\'' +
            ", estoqueAtual=" + estoqueAtual +
            ", quantidadeVendidaPeriodo=" + quantidadeVendidaPeriodo +
            ", ultimaVendaEm=" + ultimaVendaEm +
            ", score=" + score +
            ", ofertado=" + ofertado +
            '}';
    }
}
