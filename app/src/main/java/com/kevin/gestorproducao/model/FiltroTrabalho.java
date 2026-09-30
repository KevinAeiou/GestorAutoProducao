package com.kevin.gestorproducao.model;

import java.util.List;

public class FiltroTrabalho {
    private final String descricao;
    private final List<ProfissaoBase> profissoes;
    private final List<String> raridades;
    private final int estado;

    private final Integer nivel;

    public FiltroTrabalho(
        String descricao,
        List<ProfissaoBase> profissoes,
        List<String> raridades,
        int estado,
        Integer nivel
    ) {
        this.descricao = descricao;
        this.profissoes = profissoes;
        this.raridades = raridades;
        this.estado = estado;
        this.nivel = nivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public List<ProfissaoBase> getProfissoes() {
        return profissoes;
    }

    public List<String> getRaridades() {
        return raridades;
    }

    public int getEstado() {
        return estado;
    }

    public Integer getNivel() {
        return nivel;
    }
}
