package com.kevin.gestorproducao.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PricingServiceTest {

    @Test
    public void deve_RetornarZero_QuandoLucroIgualAoValorDeProducaoComTaxaDeMercado() {
        int taxa = PricingService.calculaTaxa(1100, 1000);
        assertEquals(0, taxa);
    }

    @Test
    public void deve_RetornarCem_QuandoLucroEhODobroDoValorDeProducaoComTaxaDeMercado() {
        int taxa = PricingService.calculaTaxa(2200, 1000);
        assertEquals(100, taxa);
    }

    @Test
    public void deve_RetornarNegativo_QuandoLucroEhMenorQueOValorDeProducao() {
        int taxa = PricingService.calculaTaxa(550, 1000);
        assertEquals(-50, taxa);
    }

    @Test
    public void deve_RetornarValorComTaxaDeMercado_QuandoTaxaIgualAZero() {
        int lucro = PricingService.calculaValorLucro(0, 1000);
        assertEquals(1100, lucro);
    }

    @Test
    public void deve_DobrarAntesDaTaxaDeMercado_QuandoTaxaIgualACem() {
        int lucro = PricingService.calculaValorLucro(100, 1000);
        assertEquals(2200, lucro);
    }

    @Test
    public void deve_RetornarZero_QuandoTaxaTornaOLucroNegativo() {
        int lucro = PricingService.calculaValorLucro(-100, 1000);
        assertEquals(0, lucro);
    }
}
