package com.kevin.ceep.model;

import static com.kevin.ceep.utilitario.Utilitario.geraIdAleatorio;

import java.io.Serializable;

public class Personagem extends Usuario implements Serializable {

    private boolean estado;
    private boolean  uso;
    private int espacoProducao;

    public Personagem(){
        super();
        super.setId(geraIdAleatorio());
    }
    public boolean getEstado() {
        return estado;
    }
    public boolean getUso() {
        return uso;
    }

    public int getEspacoProducao() {
        return espacoProducao;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setUso(boolean uso) {
        this.uso = uso;
    }

    public void setEspacoProducao(int espacoProducao) {
        this.espacoProducao = espacoProducao;
    }
}
