package com.kevin.gestorproducao.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de uma rodada de planejamento das profissões priorizadas: o que foi adicionado,
 * quais profissões ficaram aguardando insumo e os avisos que antes interrompiam o processo.
 */
public class ResumoPlanejamento {
    private int adicionados;
    private final List<String> aguardandoInsumo = new ArrayList<>();
    private final List<String> avisos = new ArrayList<>();

    void registraAdicionado() {
        adicionados++;
    }

    void registraAguardandoInsumo(String profissao) {
        aguardandoInsumo.add(profissao);
    }

    void registraAviso(String aviso) {
        avisos.add(aviso);
    }

    public int getAdicionados() {
        return adicionados;
    }

    public List<String> getAguardandoInsumo() {
        return aguardandoInsumo;
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public boolean temNovidades() {
        return adicionados > 0 || !aguardandoInsumo.isEmpty() || !avisos.isEmpty();
    }

    public String paraMensagem() {
        List<String> partes = new ArrayList<>();

        if (adicionados > 0) {
            partes.add(adicionados + (adicionados == 1 ? " produção adicionada" : " produções adicionadas"));
        }

        if (!aguardandoInsumo.isEmpty()) {
            partes.add("Aguardando insumo: " + String.join(", ", aguardandoInsumo));
        }

        partes.addAll(avisos);

        return String.join(" · ", partes);
    }
}
