package com.kevin.ceep.model;

import static com.kevin.ceep.utilitario.Utilitario.geraIdAleatorio;

import androidx.annotation.NonNull;

public class Recurso {
    private String id;

    public Recurso() {
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
        return "Recurso{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                '}';
    }
}
