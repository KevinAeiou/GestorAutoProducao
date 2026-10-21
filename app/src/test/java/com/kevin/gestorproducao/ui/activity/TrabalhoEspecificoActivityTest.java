package com.kevin.gestorproducao.ui.activity;

import static org.junit.Assert.assertEquals;

import com.kevin.gestorproducao.model.TrabalhoProducao;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TrabalhoEspecificoActivityTest {
    @Test
    public void deve_retornarVerdadeiro_QuandoTrabalhoPossuiAtributoTrabalhoNecessario (){
        TrabalhoProducao trabalho = new TrabalhoProducao();
        Map<String, Boolean> necessarios = new HashMap<>();
        necessarios.put("teste1", false);
        trabalho.setNecessarios(necessarios);
        boolean verificacao = trabalho.possuiTrabalhosNecessarios();
        assertEquals(true, verificacao);
    }
    @Test
    public void deve_retornarFalso_QuandoTrabalhoPossuiAtributoTrabalhoNecessarioVazio (){
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.setNecessarios(new HashMap<>());
        boolean verificacao = trabalho.possuiTrabalhosNecessarios();
        assertEquals(false, verificacao);
    }
    @Test
    public void deve_retornarFalso_QuandoTrabalhoPossuiAtributoTrabalhoNecessarioNulo (){
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.setNecessarios(null);
        boolean verificacao = trabalho.possuiTrabalhosNecessarios();
        assertEquals(false, verificacao);
    }
}
