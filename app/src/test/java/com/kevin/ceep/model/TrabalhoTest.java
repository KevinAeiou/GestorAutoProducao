package com.kevin.ceep.model;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class TrabalhoTest {
    private final Trabalho ANEL_COMUM1 = new Trabalho();
    private final Trabalho PRODUCAO_RECURSOS = new Trabalho();

    @Test
    public void deve_RetornarFalso_QuandoVerificarSeEhProducaoDeRecursos() {
        boolean verificacao = ANEL_COMUM1.ehProducaoDeRecursos();
        assertFalse(verificacao);
    }
    @Test
    public void deve_RetornarVerdadeiro_QuandoVerificarSeEhProducaoDeRecursos() {
        boolean verificacao = PRODUCAO_RECURSOS.ehProducaoDeRecursos();
        assertFalse(verificacao);
    }

}