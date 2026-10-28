package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.utilitario.Utilitario.geraIdAleatorio;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Personagem extends Usuario implements Serializable {

    private boolean estado;
    private boolean uso;
    private boolean autoProducao;
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

    public boolean isAutoProducao() {
        return autoProducao;
    }

    public void setAutoProducao(boolean autoProducao) {
        this.autoProducao = autoProducao;
    }

    /**
     * Senha do personagem, enviada ao Firebase sem criptografia (será criptografada futuramente).
     * Sobrescreve o {@code @Exclude} de {@link Usuario#getSenha()}, que segue valendo para o login.
     */
    @Override
    public String getSenha() {
        return super.getSenha();
    }

    /**
     * Recebe a senha do personagem vinda do Firebase; sobrescreve o {@code @Exclude} de
     * {@link Usuario#setSenha(String)}.
     */
    @Override
    public void setSenha(String senha) {
        super.setSenha(senha);
    }

    @NonNull
    @Override
    public String toString() {
        return getNome();
    }
}
