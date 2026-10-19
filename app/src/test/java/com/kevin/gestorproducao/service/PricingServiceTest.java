package com.kevin.gestorproducao.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PricingServiceTest {

    @Test
    public void deve_CobrirTaxaDeVendaEUmaOferta_QuandoTaxaIgualAZero() {
        // 1000 / (1 - 0,10 - 0,10) = 1250
        assertEquals(1250, PricingService.calculaValorLucro(0, 1000, 1));
    }

    @Test
    public void deve_CobrirOfertasAdicionais_QuandoItemExpiraSemVender() {
        // 1000 / (1 - 0,10 - 0,30) = 1667 (arredondado para cima)
        assertEquals(1667, PricingService.calculaValorLucro(0, 1000, 3));
    }

    @Test
    public void deve_AplicarLucroSobreProducao_QuandoTaxaIgualACem() {
        assertEquals(2500, PricingService.calculaValorLucro(100, 1000, 1));
    }

    @Test
    public void deve_RetornarZero_QuandoTaxasConsomemTodoOValor() {
        assertEquals(0, PricingService.calculaValorLucro(0, 1000, 9));
    }

    @Test
    public void deve_RetornarZero_QuandoValorIgualAoNecessarioParaCobrirTaxas() {
        assertEquals(0, PricingService.calculaTaxa(1250, 1000, 1));
    }

    @Test
    public void deve_RetornarCem_QuandoLiquidoEhODobroDaProducao() {
        assertEquals(100, PricingService.calculaTaxa(2500, 1000, 1));
    }

    @Test
    public void deve_RetornarNegativo_QuandoLiquidoEhMenorQueAProducao() {
        assertEquals(-50, PricingService.calculaTaxa(625, 1000, 1));
    }

    @Test
    public void deve_TratarOfertasInvalidasComoUma() {
        assertEquals(1250, PricingService.calculaValorLucro(0, 1000, 0));
    }

    @Test
    public void deve_UsarLicencaNovato_QuandoAprendizEhMaisCaro() {
        // aprendiz: (4*500+80)/2 = 1040 por 18 itens (57,7/item) > novato: 80 por 9 itens (8,9/item)
        assertEquals(89, PricingService.calculaValorProducaoRecurso(9, false, 500, 0, 10));
    }

    @Test
    public void deve_UsarLicencaAprendiz_QuandoMaisBarata() {
        // aprendiz: (4*10+80)/2 = 60 por 18 itens (3,33/item) < novato: 8,89/item
        assertEquals(34, PricingService.calculaValorProducaoRecurso(9, false, 10, 0, 10));
    }

    @Test
    public void deve_MultiplicarPelaQuantidade_NoCustoDoRecurso() {
        assertEquals(2 * PricingService.calculaValorProducaoRecurso(2, false, 500, 0, 5),
            PricingService.calculaValorProducaoRecurso(2, false, 500, 0, 10));
    }

    @Test
    public void deve_CustarMetadeDoLoteDeLicencasAprendiz_SemDuplicar() {
        // 2 licenças = 4*100 + 80 = 480 -> 240 cada
        assertEquals(240, PricingService.calculaValorProducaoRecurso(2, true, 100, 0, 1));
        assertEquals(480, PricingService.calculaValorProducaoRecurso(2, true, 100, 0, 2));
    }

    @Test
    public void deve_SomarMateriaisDoLoteAoCustoDaLicenca() {
        // lote de 9 itens, materiais 4*100 = 400. novato: (80+400)/9 = 53,3/item;
        // aprendiz: (240+400)/18 = 35,6/item -> 10 itens = 355,6 -> 356
        assertEquals(356, PricingService.calculaValorProducaoRecurso(9, false, 100, 400, 10));
    }
}
