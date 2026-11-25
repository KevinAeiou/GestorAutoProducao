package com.kevin.gestorproducao.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlanejamentoProducaoServiceTest {

    @Test
    public void deve_PermitirMelhorados_QuandoNivelImparMenorQueOito() {
        assertTrue(PlanejamentoProducaoService.permiteProduzirMelhorados(1));
        assertTrue(PlanejamentoProducaoService.permiteProduzirMelhorados(3));
        assertTrue(PlanejamentoProducaoService.permiteProduzirMelhorados(5));
        assertTrue(PlanejamentoProducaoService.permiteProduzirMelhorados(7));
    }

    @Test
    public void deve_NegarMelhorados_QuandoNivelParMenorQueOito() {
        assertFalse(PlanejamentoProducaoService.permiteProduzirMelhorados(2));
        assertFalse(PlanejamentoProducaoService.permiteProduzirMelhorados(4));
        assertFalse(PlanejamentoProducaoService.permiteProduzirMelhorados(6));
    }

    @Test
    public void deve_PermitirMelhorados_QuandoNivelParAPartirDeOito() {
        assertTrue(PlanejamentoProducaoService.permiteProduzirMelhorados(8));
        assertTrue(PlanejamentoProducaoService.permiteProduzirMelhorados(10));
        assertTrue(PlanejamentoProducaoService.permiteProduzirMelhorados(28));
    }

    @Test
    public void deve_NegarMelhorados_QuandoNivelImparMaiorQueOito() {
        assertFalse(PlanejamentoProducaoService.permiteProduzirMelhorados(9));
        assertFalse(PlanejamentoProducaoService.permiteProduzirMelhorados(11));
        assertFalse(PlanejamentoProducaoService.permiteProduzirMelhorados(27));
    }
}
