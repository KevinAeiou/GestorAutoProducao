package com.kevin.gestorproducao.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ProfissaoTrabalho implements Serializable {
    private String nome;
    private ArrayList<Trabalho> trabalhos;
    private boolean isExpandable;
    public ProfissaoTrabalho(){}
    public ProfissaoTrabalho(String nome, ArrayList<Trabalho> trabalhos){
        this.nome = nome;
        this.trabalhos = trabalhos;
        isExpandable = false;
    }
    public String getNome() {
        return nome;
    }
    public ArrayList<Trabalho> getTrabalhos() {
        return trabalhos;
    }

    public boolean isExpandable() {
        return isExpandable;
    }
    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }
}
