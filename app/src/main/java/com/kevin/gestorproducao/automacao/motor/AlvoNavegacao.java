package com.kevin.gestorproducao.automacao.motor;

public class AlvoNavegacao {
    private final String nomeProfissao;
    private final String nomeTrabalho;

    public AlvoNavegacao(String nomeProfissao, String nomeTrabalho) {
        this.nomeProfissao = nomeProfissao;
        this.nomeTrabalho = nomeTrabalho;
    }

    public String getNomeProfissao() {
        return nomeProfissao;
    }

    public String getNomeTrabalho() {
        return nomeTrabalho;
    }
}
