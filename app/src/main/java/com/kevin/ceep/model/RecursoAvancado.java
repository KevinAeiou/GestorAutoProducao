package com.kevin.ceep.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class RecursoAvancado extends Recurso {
    private int quantidade;
    private int valor;
    public RecursoAvancado() {
        quantidade = 0;
        valor = 0;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecursoAvancado that = (RecursoAvancado) o;
        return quantidade == that.quantidade && valor == that.valor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantidade, valor);
    }

    @NonNull
    @Override
    public String toString() {
        return "RecursoAvancado{" +
                "id=" + getId() +
                ", nome=" + getNome() +
                ", quantidade=" + quantidade +
                ", valor=" + valor +
                '}';
    }
}
