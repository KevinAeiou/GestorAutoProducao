package com.kevin.gestorproducao.ui.activity;

import static org.junit.Assert.assertEquals;

import com.kevin.gestorproducao.model.TrabalhoProducao;

import org.junit.Test;

public class TrabalhoEspecificoActivityTest {
    @Test
    public void deve_retornarVerdadeiro_QuandoTrabalhoPossuiAtributoTrabalhoNecessario (){
        TrabalhoProducao trabalho = new TrabalhoProducao("1", "teste1","teste1", "teste1","teste1","teste1",0,0,"teste1",1,false);
        boolean verificacao = trabalho.possuiTrabalhosNecessarios();
        assertEquals(true, verificacao);
    }
    @Test
    public void deve_retornarFalso_QuandoTrabalhoPossuiAtributoTrabalhoNecessarioVazio (){
        TrabalhoProducao trabalho = new TrabalhoProducao("1", "teste1","teste1", "teste1","teste1","",0,0,"teste1",1,false);
        boolean verificacao = trabalho.possuiTrabalhosNecessarios();
        assertEquals(false, verificacao);
    }
    @Test
    public void deve_retornarFalso_QuandoTrabalhoPossuiAtributoTrabalhoNecessarioNulo (){
        TrabalhoProducao trabalho = new TrabalhoProducao("1", "teste1","teste1", "teste1","teste1",null,0,0,"teste1",1,false);
        boolean verificacao = trabalho.possuiTrabalhosNecessarios();
        assertEquals(false, verificacao);
    }
}