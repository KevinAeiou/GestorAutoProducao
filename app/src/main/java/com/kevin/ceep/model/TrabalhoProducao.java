package com.kevin.ceep.model;

import static com.kevin.ceep.ui.activity.Constantes.CHAVE_LICENCA_INICIANTE;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_TRABALHO_PARA_PRODUZIR;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_TRABALHO_PRODUZINDO;
import static com.kevin.ceep.utilitario.Utilitario.geraIdAleatorio;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class TrabalhoProducao extends Trabalho implements Serializable {
    private String idTrabalho;
    private String tipoLicenca;
    private Integer estado;
    private Boolean recorrencia;

    public TrabalhoProducao() {
        super();
        super.setId(geraIdAleatorio());
    }
    public Boolean getRecorrencia() {
        return recorrencia;
    }

    public Integer getEstado() {
        return estado;
    }

    public String getTipoLicenca() {
        return tipoLicenca;
    }

    public void setTipoLicenca(String tipoLicenca) {
        this.tipoLicenca = tipoLicenca;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public void setRecorrencia(Boolean recorrencia) {
        this.recorrencia = recorrencia;
    }

    public String getIdTrabalho() {
        return idTrabalho;
    }

    public void setIdTrabalho(String idTrabalho) {
        this.idTrabalho = idTrabalho;
    }

    @NonNull
    @Override
    public String toString() {
        return getId() + " | " + idTrabalho + " | " + tipoLicenca + " | " + estado + " | " + recorrencia;
    }

    public boolean ehProduzindo() {
        return estado == CODIGO_TRABALHO_PRODUZINDO;
    }

    public boolean ehProduzir() {
        return estado == CODIGO_TRABALHO_PARA_PRODUZIR;
    }

    @Exclude
    @Override
    public Integer getExperiencia() {
        if (tipoLicenca.equals(CHAVE_LICENCA_INICIANTE)) {
            return (int) (super.getExperiencia() * 1.5);
        }
        return super.getExperiencia();
    }
}
