package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.utilitario.Utilitario.geraIdAleatorio;

import androidx.annotation.NonNull;

public class RecursoComum {
    private String id;

    public RecursoComum() {
        id = geraIdAleatorio();
    }

    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return "RecursoComum{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                '}';
    }
}
