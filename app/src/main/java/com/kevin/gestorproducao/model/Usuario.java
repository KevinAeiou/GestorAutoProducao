package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.utilitario.Utilitario.geraIdAleatorio;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Usuario implements Serializable {


    private String id;
    private String nome;
    private String email;
    private String senha;
    private String tipo;

    public Usuario() {
        this.id = geraIdAleatorio();
    }

    public String getNome() {
        return nome;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    @Exclude
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isAdministrador() {
        return tipo != null && tipo.equalsIgnoreCase("super");
    }
}
