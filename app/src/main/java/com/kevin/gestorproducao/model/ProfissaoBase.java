package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.utilitario.Utilitario.geraIdAleatorio;

import java.io.Serializable;

public class ProfissaoBase implements Serializable {
    private String id;
    private String nome;

    public ProfissaoBase(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public ProfissaoBase() {
        geraNovoId();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void geraNovoId() {
        this.id= geraIdAleatorio();
    }
}