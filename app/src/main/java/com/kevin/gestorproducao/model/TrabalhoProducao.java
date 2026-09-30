package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_LICENCA_INICIANTE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_FEITO;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PARA_PRODUZIR;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PRODUZINDO;
import static com.kevin.gestorproducao.utilitario.Utilitario.geraIdAleatorio;
import static com.kevin.gestorproducao.utilitario.Utilitario.limpaString;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.kevin.gestorproducao.rules.CatalogoRecursos;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@IgnoreExtraProperties
public class TrabalhoProducao extends Trabalho implements Serializable {
    private String idTrabalho;
    private String tipoLicenca;
    private Integer estado;
    private Boolean recorrencia;
    protected Long iniciadoEm;
    protected Long finalizadoEm;

    public TrabalhoProducao() {
        super();
        super.setId(geraIdAleatorio());
        estado = CODIGO_TRABALHO_PARA_PRODUZIR;
        recorrencia = false;
        this.marcarCriacao();
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

    public void setIniciadoEm(Long iniciadoEm) {
        this.iniciadoEm = iniciadoEm;
    }

    public void setFinalizadoEm(Long finalizadoEm) {
        this.finalizadoEm = finalizadoEm;
    }

    public String getIdTrabalho() {
        return idTrabalho;
    }

    public Long getIniciadoEm() {
        return iniciadoEm;
    }

    public Long getFinalizadoEm() {
        return finalizadoEm;
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
    public boolean ehFeito() {
        return estado == CODIGO_TRABALHO_FEITO;
    }

    @Exclude
    @Override
    public Integer getExperiencia() {
        if (tipoLicenca.equals(CHAVE_LICENCA_INICIANTE)) {
            return (int) (super.getExperiencia() * 1.5);
        }
        return super.getExperiencia();
    }

    @Exclude
    public Map<String, Integer> getMapMateriaisNecessarios(Context context) {
        Map<String, Integer> resultado = new LinkedHashMap<>();

        String profissaoStr = this.getProfissao();

        Profissao profissao = Profissao.fromKey(limpaString(profissaoStr));
        Map<Recurso, Integer> base = CatalogoRecursos.getCatalogo().get(profissao);

        if (base == null) return resultado;

        for (Map.Entry<Recurso, Integer> entry: base.entrySet()) {
            Recurso recurso = entry.getKey();
            int qtd = recuperaQuantidadeMaximaRecursos(context);

            resultado.put(recurso.getKey(), qtd);
        }

        return resultado;
    }

    public void marcarIniciado() {
        this.iniciadoEm = System.currentTimeMillis();
    }

    public void marcarFinalizado() {
        this.finalizadoEm = System.currentTimeMillis();
    }

    public void atualizarEstado(int novoEstado) {
        if (this.iniciadoEm == null &&
            this.ehProduzir() &&
            novoEstado == CODIGO_TRABALHO_PRODUZINDO
        ) {
            this.marcarIniciado();
        }

        if (this.finalizadoEm == null &&
            this.ehProduzindo() &&
            novoEstado == CODIGO_TRABALHO_FEITO
        ) {
            this.marcarFinalizado();
        }

        this.estado = novoEstado;
    }
}
