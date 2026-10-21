package com.kevin.gestorproducao.utilitario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.PeriodoFiltro;
import com.kevin.gestorproducao.model.ProfissaoBase;
import com.kevin.gestorproducao.model.Trabalho;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class UtilitarioTest {

    @Test
    public void deve_RemoverAcentos() {
        assertEquals("Acao de Aco", Utilitario.removeAcentos("Ação de Aço"));
    }

    @Test
    public void deve_RetornarStringVazia_QuandoRemoverAcentosDeNulo() {
        assertEquals("", Utilitario.removeAcentos(null));
    }

    @Test
    public void deve_NormalizarString_SemAcentoEspacoOuMaiuscula() {
        assertEquals("armadurasdetecido", Utilitario.limpaString("Armaduras de Tecido"));
        assertEquals("extracaodesubstancia", Utilitario.limpaString("Extração de Substância"));
    }

    @Test
    public void deve_RetornarVazio_QuandoLimparStringNula() {
        assertEquals("", Utilitario.limpaString(null));
    }

    @Test
    public void deve_CompararStringsIgnorandoAcentoCaixaEEspaco() {
        assertTrue(Utilitario.comparaString("Armas Corpo a Corpo", "armascorpoacorpo"));
        assertFalse(Utilitario.comparaString("Anéis", "Amuletos"));
    }

    @Test
    public void deve_VerificarSeStringContemOutraIgnorandoAcentoCaixaEEspaco() {
        assertTrue(Utilitario.stringContemString("Fazendo Anéis de Aço", "ANEIS DE"));
        assertFalse(Utilitario.stringContemString("Fazendo Anéis de Aço", "bronze"));
    }

    @Test
    public void deve_GerarIdsAleatoriosDiferentes() {
        String id1 = Utilitario.geraIdAleatorio();
        String id2 = Utilitario.geraIdAleatorio();
        assertNotEquals(id1, id2);
        assertEquals(36, id1.length());
    }

    @Test
    public void deve_RetornarVazio_QuandoFormatarDataOuTimestampNulos() {
        assertEquals("", Utilitario.formatarData(null));
        assertEquals("", Utilitario.formatarTimestamp(null));
    }

    @Test
    public void deve_FormatarDataNoPadraoDiaMesAno() {
        long momento = data(2024, Calendar.MARCH, 5, 14, 30);

        assertEquals("05/03/2024", Utilitario.formatarData(momento));
        assertEquals("05/03/2024 14:30", Utilitario.formatarTimestamp(momento));
    }

    @Test
    public void deve_ExtrairNivel_QuandoTextoPossuiNumero() {
        assertEquals(Integer.valueOf(12), Utilitario.extrairNivel("Nível 12"));
    }

    @Test
    public void deve_RetornarNulo_QuandoTextoNaoPossuiNumero() {
        assertNull(Utilitario.extrairNivel("Sem nivel"));
    }

    @Test
    public void deve_ExtrairDescricao_RemovendoNumeros() {
        assertEquals("Nível", Utilitario.extrairDescricao("Nível 12"));
    }

    // --- calcularIntervaloPeriodo ---

    @Test
    public void deve_RetornarIntervaloInvertido_QuandoPersonalizadoSemDatas() {
        long[] intervalo = Utilitario.calcularIntervaloPeriodo(PeriodoFiltro.PERSONALIZADO, null, null, null);
        assertEquals(Long.MAX_VALUE, intervalo[0]);
        assertEquals(Long.MIN_VALUE, intervalo[1]);
    }

    @Test
    public void deve_RetornarIntervaloInvertido_QuandoPersonalizadoComApenasUmaData() {
        long[] intervalo = Utilitario.calcularIntervaloPeriodo(PeriodoFiltro.PERSONALIZADO, null, 10L, null);
        assertEquals(Long.MAX_VALUE, intervalo[0]);
        assertEquals(Long.MIN_VALUE, intervalo[1]);
    }

    @Test
    public void deve_RetornarDatasEscolhidas_QuandoPersonalizadoComDatas() {
        long[] intervalo = Utilitario.calcularIntervaloPeriodo(PeriodoFiltro.PERSONALIZADO, 999L, 10L, 20L);
        assertEquals(10L, intervalo[0]);
        assertEquals(20L, intervalo[1]);
    }

    @Test
    public void deve_CobrirODiaInteiro_QuandoPeriodoForDia() {
        long referencia = data(2024, Calendar.MARCH, 13, 15, 45);
        long[] intervalo = Utilitario.calcularIntervaloPeriodo(PeriodoFiltro.DIA, referencia, null, null);

        assertEquals(data(2024, Calendar.MARCH, 13, 0, 0), intervalo[0]);
        assertEquals(data(2024, Calendar.MARCH, 14, 0, 0) - 1, intervalo[1]);
    }

    @Test
    public void deve_CobrirOMesInteiro_QuandoPeriodoForMes() {
        long referencia = data(2024, Calendar.FEBRUARY, 20, 10, 0);
        long[] intervalo = Utilitario.calcularIntervaloPeriodo(PeriodoFiltro.MES, referencia, null, null);

        assertEquals(data(2024, Calendar.FEBRUARY, 1, 0, 0), intervalo[0]);
        // 2024 é bissexto: fevereiro termina no dia 29
        assertEquals(data(2024, Calendar.MARCH, 1, 0, 0) - 1, intervalo[1]);
    }

    @Test
    public void deve_CobrirOAnoInteiro_QuandoPeriodoForAno() {
        long referencia = data(2023, Calendar.JULY, 4, 8, 0);
        long[] intervalo = Utilitario.calcularIntervaloPeriodo(PeriodoFiltro.ANO, referencia, null, null);

        assertEquals(data(2023, Calendar.JANUARY, 1, 0, 0), intervalo[0]);
        assertEquals(data(2024, Calendar.JANUARY, 1, 0, 0) - 1, intervalo[1]);
    }

    @Test
    public void deve_CobrirSeteDiasComecandoNoPrimeiroDiaDaSemana_QuandoPeriodoForSemana() {
        long referencia = data(2024, Calendar.MARCH, 13, 12, 0);
        long[] intervalo = Utilitario.calcularIntervaloPeriodo(PeriodoFiltro.SEMANA, referencia, null, null);

        Calendar inicio = Calendar.getInstance();
        inicio.setTimeInMillis(intervalo[0]);
        assertEquals(inicio.getFirstDayOfWeek(), inicio.get(Calendar.DAY_OF_WEEK));
        assertTrue(intervalo[0] <= referencia && referencia <= intervalo[1]);

        inicio.add(Calendar.WEEK_OF_YEAR, 1);
        assertEquals(inicio.getTimeInMillis() - 1, intervalo[1]);
    }

    @Test
    public void deve_TerFimAgoraEInicioMesesAntes_QuandoUltimosMeses() {
        long antes = System.currentTimeMillis();
        long[] intervalo = Utilitario.calcularIntervaloUltimosMeses(6);
        long depois = System.currentTimeMillis();

        assertTrue(intervalo[1] >= antes && intervalo[1] <= depois);

        Calendar esperado = Calendar.getInstance();
        esperado.setTimeInMillis(intervalo[1]);
        esperado.add(Calendar.MONTH, -6);
        assertEquals(esperado.getTimeInMillis(), intervalo[0]);
    }

    // --- filtrarTrabalhos ---

    @Test
    public void deve_RetornarTodos_QuandoFiltroNulo() {
        List<Trabalho> trabalhos = Arrays.asList(
            trabalho("Anel", "Anéis", "Comum", 1),
            trabalho("Amuleto", "Amuletos", "Raro", 2)
        );

        assertEquals(2, Utilitario.filtrarTrabalhos(trabalhos, null).size());
    }

    @Test
    public void deve_RetornarTodos_QuandoFiltroVazio() {
        List<Trabalho> trabalhos = Arrays.asList(
            trabalho("Anel", "Anéis", "Comum", 1),
            trabalho("Amuleto", "Amuletos", "Raro", 2)
        );

        FiltroTrabalho filtro = new FiltroTrabalho("", null, null, 0, null);

        assertEquals(2, Utilitario.filtrarTrabalhos(trabalhos, filtro).size());
    }

    @Test
    public void deve_FiltrarPorDescricao_IgnorandoAcentoECaixa() {
        Trabalho anel = trabalho("Anel de Aço", "Anéis", "Comum", 1);
        Trabalho amuleto = trabalho("Amuleto de Jade", "Amuletos", "Comum", 1);

        FiltroTrabalho filtro = new FiltroTrabalho("ACO", null, null, 0, null);

        List<Trabalho> resultado = Utilitario.filtrarTrabalhos(Arrays.asList(anel, amuleto), filtro);
        assertEquals(Collections.singletonList(anel), resultado);
    }

    @Test
    public void deve_FiltrarPorProfissao() {
        Trabalho anel = trabalho("Anel", "Anéis", "Comum", 1);
        Trabalho amuleto = trabalho("Amuleto", "Amuletos", "Comum", 1);

        FiltroTrabalho filtro = new FiltroTrabalho(
            null, Collections.singletonList(new ProfissaoBase("1", "amuletos")), null, 0, null);

        List<Trabalho> resultado = Utilitario.filtrarTrabalhos(Arrays.asList(anel, amuleto), filtro);
        assertEquals(Collections.singletonList(amuleto), resultado);
    }

    @Test
    public void deve_FiltrarPorRaridade() {
        Trabalho comum = trabalho("Anel", "Anéis", "Comum", 1);
        Trabalho raro = trabalho("Anel raro", "Anéis", "Raro", 1);

        FiltroTrabalho filtro = new FiltroTrabalho(null, null, Collections.singletonList("raro"), 0, null);

        List<Trabalho> resultado = Utilitario.filtrarTrabalhos(Arrays.asList(comum, raro), filtro);
        assertEquals(Collections.singletonList(raro), resultado);
    }

    @Test
    public void deve_FiltrarPorNivel() {
        Trabalho nivel1 = trabalho("Anel 1", "Anéis", "Comum", 1);
        Trabalho nivel2 = trabalho("Anel 2", "Anéis", "Comum", 2);

        FiltroTrabalho filtro = new FiltroTrabalho(null, null, null, 0, 2);

        List<Trabalho> resultado = Utilitario.filtrarTrabalhos(Arrays.asList(nivel1, nivel2), filtro);
        assertEquals(Collections.singletonList(nivel2), resultado);
    }

    @Test
    public void deve_CombinarFiltrosComE_QuandoVariosCriteriosInformados() {
        Trabalho alvo = trabalho("Anel de Aço", "Anéis", "Raro", 3);
        Trabalho outraRaridade = trabalho("Anel de Aço", "Anéis", "Comum", 3);
        Trabalho outroNivel = trabalho("Anel de Aço", "Anéis", "Raro", 4);

        FiltroTrabalho filtro = new FiltroTrabalho(
            "aco",
            Collections.singletonList(new ProfissaoBase("1", "Anéis")),
            Collections.singletonList("Raro"),
            0,
            3
        );

        List<Trabalho> resultado = Utilitario.filtrarTrabalhos(
            Arrays.asList(alvo, outraRaridade, outroNivel), filtro);
        assertEquals(Collections.singletonList(alvo), resultado);
    }

    @Test
    public void deve_DescartarTrabalhoSemNome_QuandoFiltrarPorDescricao() {
        Trabalho semNome = trabalho(null, "Anéis", "Comum", 1);

        FiltroTrabalho filtro = new FiltroTrabalho("anel", null, null, 0, null);

        assertTrue(Utilitario.filtrarTrabalhos(new ArrayList<>(Collections.singletonList(semNome)), filtro).isEmpty());
    }

    private static long data(int ano, int mes, int dia, int hora, int minuto) {
        Calendar calendario = Calendar.getInstance();
        calendario.clear();
        calendario.set(ano, mes, dia, hora, minuto, 0);
        return calendario.getTimeInMillis();
    }

    private static Trabalho trabalho(String nome, String profissao, String raridade, int nivel) {
        Trabalho trabalho = new Trabalho();
        trabalho.setNome(nome);
        trabalho.setProfissao(profissao);
        trabalho.setRaridade(raridade);
        trabalho.setNivel(nivel);
        return trabalho;
    }
}
