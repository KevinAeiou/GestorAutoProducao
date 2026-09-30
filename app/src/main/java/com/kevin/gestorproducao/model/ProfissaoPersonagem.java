package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.ui.activity.Constantes.EXPERIENCIAS;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfissaoPersonagem extends ProfissaoBase implements Serializable {
    private Integer experiencia;
    private boolean prioridade;
    private final List<Integer> xpNiveis;

    public ProfissaoPersonagem(){
        super();
        ArrayList<Integer> listaTemporaria = new ArrayList<>(EXPERIENCIAS);
        this.xpNiveis = Collections.unmodifiableList(listaTemporaria);
    }

    public Integer getExperiencia() {
        return experiencia;
    }

    public boolean isPrioridade() {
        return prioridade;
    }

    @Exclude
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

    @Exclude
    public int getXpNecessario() {
        int nivelAtual = getNivel();
        if (nivelAtual == 1) return getXpMaximo();
        return getXpMaximo() - getXpMaximo(nivelAtual - 1);
    }

    @Exclude
    public int getXpMaximo(int nivelAtual) {
        if (nivelAtual == 0) nivelAtual = getNivel();
        if (xpNiveis.isEmpty()) return 0;
        return xpNiveis.get(nivelAtual-1);
    }

    @Exclude
    public int getXpMaximo() {
        return getXpMaximo(0);
    }

    public void setExperiencia(int experiencia) {
        if (experiencia > 1195000) experiencia = 1195000;
        this.experiencia = experiencia;
    }

    public void setPrioridade(boolean prioridade) {
        this.prioridade = prioridade;
    }

    @Exclude
    public int getExperienciaRelativa() {
        int nivel = getNivel();
        if (nivel == 1) return experiencia;
        return experiencia - getXpMaximo(nivel - 1);
    }

    @Exclude
    public int getNivelProducao() {
        return calcularNivelProducao(getNivel());
    }

    private int calcularNivelProducao(int nivelProfissao) {
        if (nivelProfissao == 1) return 1;
        if (nivelProfissao == 8) return 8;
        int nivelAjustado = nivelProfissao;

        if (nivelProfissao >= 9) {
            nivelAjustado--;
        }
        int base = 10;
        int incremento = ((nivelAjustado  - 2) / 2) * 2;
        return base + incremento;
    }
}

