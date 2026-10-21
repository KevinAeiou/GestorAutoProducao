package com.kevin.gestorproducao.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ResumoPlanejamentoTest {

    @Test
    public void deve_NaoTerNovidades_QuandoVazio() {
        ResumoPlanejamento resumo = new ResumoPlanejamento();

        assertFalse(resumo.temNovidades());
        assertEquals("", resumo.paraMensagem());
    }

    @Test
    public void deve_TerNovidades_QuandoAlgoFoiRegistrado() {
        ResumoPlanejamento adicionado = new ResumoPlanejamento();
        adicionado.registraAdicionado();
        assertTrue(adicionado.temNovidades());

        ResumoPlanejamento aguardando = new ResumoPlanejamento();
        aguardando.registraAguardandoInsumo("Anéis");
        assertTrue(aguardando.temNovidades());

        ResumoPlanejamento aviso = new ResumoPlanejamento();
        aviso.registraAviso("Algo aconteceu");
        assertTrue(aviso.temNovidades());
    }

    @Test
    public void deve_UsarSingular_QuandoUmaProducaoAdicionada() {
        ResumoPlanejamento resumo = new ResumoPlanejamento();
        resumo.registraAdicionado();

        assertEquals("1 produção adicionada", resumo.paraMensagem());
    }

    @Test
    public void deve_UsarPlural_QuandoVariasProducoesAdicionadas() {
        ResumoPlanejamento resumo = new ResumoPlanejamento();
        resumo.registraAdicionado();
        resumo.registraAdicionado();

        assertEquals(2, resumo.getAdicionados());
        assertEquals("2 produções adicionadas", resumo.paraMensagem());
    }

    @Test
    public void deve_JuntarPartesComSeparador() {
        ResumoPlanejamento resumo = new ResumoPlanejamento();
        resumo.registraAdicionado();
        resumo.registraAguardandoInsumo("Anéis");
        resumo.registraAguardandoInsumo("Capotes");
        resumo.registraAviso("Estoque baixo");

        assertEquals(
            "1 produção adicionada · Aguardando insumo: Anéis, Capotes · Estoque baixo",
            resumo.paraMensagem()
        );
    }
}
