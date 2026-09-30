package com.kevin.gestorproducao.model;

public class BaseEntity {
    protected Long criadoEm;
    protected Long modificadoEm;

    public void marcarCriacao() {
        long agora = System.currentTimeMillis();
        this.criadoEm = agora;
        this.modificadoEm = agora;
    }

    public void marcarModificacao() {
        this.modificadoEm = System.currentTimeMillis();
    }

    public Long getCriadoEm() {
        return criadoEm;
    }

    public Long getModificadoEm() {
        return modificadoEm;
    }

    public void setCriadoEm(Long criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setModificadoEm(Long modificadoEm) {
        this.modificadoEm = modificadoEm;
    }
}
