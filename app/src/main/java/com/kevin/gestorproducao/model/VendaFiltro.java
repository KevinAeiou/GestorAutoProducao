package com.kevin.gestorproducao.model;

public class VendaFiltro {
    private final String idPersonagem;
    private final String idTrabalho;

    public VendaFiltro(String idPersonagem, String idTrabalho) {
        this.idPersonagem = idPersonagem;
        this.idTrabalho = idTrabalho;
    }

    public String getIdPersonagem() {
        return idPersonagem;
    }

    public String getIdTrabalho() {
        return idTrabalho;
    }
}