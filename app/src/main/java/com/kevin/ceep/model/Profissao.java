package com.kevin.ceep.model;

import static com.kevin.ceep.ui.activity.Constantes.EXPERIENCIAS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profissao implements Serializable {
    private String id;
    private String nome;
    private Integer experiencia;
    private boolean prioridade;
    private final List<Integer> xpNiveis;

    public Profissao(){
        ArrayList<Integer> listaTemporaria = new ArrayList<>(EXPERIENCIAS);
        this.xpNiveis = Collections.unmodifiableList(listaTemporaria);
    }

    public String getNome() {
        return nome;
    }

    public Integer getExperiencia() {
        return experiencia;
    }

    public boolean isPrioridade() {
        return prioridade;
    }

    public int getNivel() {
        int i;
        for (i = 0; i < xpNiveis.size() - 1; i ++){
            if (i == 0 && experiencia < xpNiveis.get(i)) {
                return i + 1;
            }
            if (experiencia >= xpNiveis.get(i) && experiencia < xpNiveis.get(i + 1)) {
                return i + 2;
            }
        }
        return i + 1;
    }

    public int getXpRestante(int nivel, int xpNecessario) {
        if (nivel == 1) {
            return experiencia;
        }
        return experiencia - xpNiveis.get(nivel-2) - xpNecessario;
    }

    public int getXpNecessario() {
        int nivelAtual = getNivel();
        if (nivelAtual == 1) return getXpMaximo();
        return getXpMaximo() - getXpMaximo(nivelAtual - 1);
    }

    public int getXpMaximo(int nivelAtual) {
        if (nivelAtual == 0) nivelAtual = getNivel();
        if (xpNiveis.isEmpty()) return 0;
        return xpNiveis.get(nivelAtual-1);
    }

    public int getXpMaximo() {
        return getXpMaximo(0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExperiencia(int experiencia) {
        if (experiencia > 1195000) experiencia = 1195000;
        this.experiencia = experiencia;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPrioridade(boolean prioridade) {
        this.prioridade = prioridade;
    }

    public int getExperienciaRelativa() {
        int nivel = getNivel();
        if (nivel == 1) return experiencia;
        return experiencia - getXpMaximo(nivel - 1);
    }
}

