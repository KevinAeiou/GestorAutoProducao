package com.kevin.gestorproducao.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.kevin.gestorproducao.model.TrabalhoChanceVenda;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class AnaliseVendaServiceTest {
    private static final long AGORA = 1_000_000_000_000L;
    private static final long DIA = 24L * 60 * 60 * 1000;

    @Test
    public void deve_RetornarListaVazia_QuandoNaoHaTrabalhos() {
        assertTrue(AnaliseVendaService.ordenarPorChanceVenda(new ArrayList<>(), AGORA).isEmpty());
    }

    @Test
    public void deve_DarScoreMaximo_QuandoUnicoTrabalhoFoiVendido() {
        TrabalhoChanceVenda unico = trabalho("A", 5, AGORA - DIA);

        AnaliseVendaService.ordenarPorChanceVenda(lista(unico), AGORA);

        assertEquals(1.0, unico.getScore(), 1e-9);
    }

    @Test
    public void deve_DarScoreZero_QuandoUnicoTrabalhoNuncaFoiVendido() {
        TrabalhoChanceVenda unico = trabalho("A", 0, null);

        AnaliseVendaService.ordenarPorChanceVenda(lista(unico), AGORA);

        assertEquals(0.0, unico.getScore(), 1e-9);
    }

    @Test
    public void deve_OrdenarPorRecenciaEFrequencia() {
        TrabalhoChanceVenda melhor = trabalho("Melhor", 10, AGORA - DIA);
        TrabalhoChanceVenda pior = trabalho("Pior", 1, AGORA - 10 * DIA);
        TrabalhoChanceVenda medio = trabalho("Medio", 5, AGORA - 5 * DIA);

        ArrayList<TrabalhoChanceVenda> resultado =
            AnaliseVendaService.ordenarPorChanceVenda(lista(pior, medio, melhor), AGORA);

        assertEquals(Arrays.asList(melhor, medio, pior), resultado);
        assertEquals(1.0, melhor.getScore(), 1e-9);
        assertEquals(0.0, pior.getScore(), 1e-9);
    }

    @Test
    public void deve_NormalizarScoreEntreZeroEUm_ComPesosIguais() {
        // recência: a=1, b=0 | frequência: a=0, b=1 -> ambos 0,5
        TrabalhoChanceVenda a = trabalho("A", 1, AGORA - DIA);
        TrabalhoChanceVenda b = trabalho("B", 3, AGORA - 3 * DIA);

        AnaliseVendaService.ordenarPorChanceVenda(lista(a, b), AGORA);

        assertEquals(0.5, a.getScore(), 1e-9);
        assertEquals(0.5, b.getScore(), 1e-9);
    }

    @Test
    public void deve_DesempatarPorQuantidade_QuandoScoresIguais() {
        TrabalhoChanceVenda a = trabalho("A", 1, AGORA - DIA);
        TrabalhoChanceVenda b = trabalho("B", 3, AGORA - 3 * DIA);

        ArrayList<TrabalhoChanceVenda> resultado =
            AnaliseVendaService.ordenarPorChanceVenda(lista(a, b), AGORA);

        assertEquals(Arrays.asList(b, a), resultado);
    }

    @Test
    public void deve_DesempatarPorNome_QuandoScoreEQuantidadeIguais() {
        TrabalhoChanceVenda x = trabalho("X", 2, AGORA - DIA);
        TrabalhoChanceVenda y = trabalho("Y", 2, AGORA - DIA);

        ArrayList<TrabalhoChanceVenda> resultado =
            AnaliseVendaService.ordenarPorChanceVenda(lista(y, x), AGORA);

        assertEquals(Arrays.asList(x, y), resultado);
    }

    @Test
    public void deve_ColocarTrabalhosNuncaVendidosPorUltimo_ComScoreZero() {
        TrabalhoChanceVenda parado = trabalho("Parado", 0, null);
        TrabalhoChanceVenda vendido = trabalho("Vendido", 2, AGORA - DIA);

        ArrayList<TrabalhoChanceVenda> resultado =
            AnaliseVendaService.ordenarPorChanceVenda(lista(parado, vendido), AGORA);

        assertEquals(Arrays.asList(vendido, parado), resultado);
        assertEquals(0.0, parado.getScore(), 1e-9);
    }

    @Test
    public void deve_OrdenarPorNome_QuandoNenhumTrabalhoFoiVendido() {
        TrabalhoChanceVenda b = trabalho("B", 0, null);
        TrabalhoChanceVenda a = trabalho("A", 0, null);

        ArrayList<TrabalhoChanceVenda> resultado =
            AnaliseVendaService.ordenarPorChanceVenda(lista(b, a), AGORA);

        assertEquals(Arrays.asList(a, b), resultado);
    }

    @Test
    public void deve_DarScoreMaximo_QuandoVendasIguaisNoMesmoMomento() {
        TrabalhoChanceVenda a = trabalho("A", 4, AGORA - DIA);
        TrabalhoChanceVenda b = trabalho("B", 4, AGORA - DIA);

        AnaliseVendaService.ordenarPorChanceVenda(lista(a, b), AGORA);

        // recência 1 e frequência 1 (quantidades iguais e > 0)
        assertEquals(1.0, a.getScore(), 1e-9);
        assertEquals(1.0, b.getScore(), 1e-9);
    }

    private static ArrayList<TrabalhoChanceVenda> lista(TrabalhoChanceVenda... trabalhos) {
        return new ArrayList<>(Arrays.asList(trabalhos));
    }

    private static TrabalhoChanceVenda trabalho(String nome, int quantidade, Long ultimaVendaEm) {
        TrabalhoChanceVenda trabalho = new TrabalhoChanceVenda();
        trabalho.setNome(nome);
        trabalho.setQuantidadeVendidaPeriodo(quantidade);
        trabalho.setUltimaVendaEm(ultimaVendaEm);
        return trabalho;
    }
}
