package com.kevin.ceep.model;

import static org.junit.Assert.assertEquals;
import android.content.Context;
import org.junit.Before;
import org.robolectric.RuntimeEnvironment;
import org.testng.annotations.Test;

public class ProfissaoTest {
    private final Profissao BRACELETES = new Profissao();
    private final Profissao CAPOTES = new Profissao();
    private final Profissao ANEIS = new Profissao();
    private final Profissao AMULETOS = new Profissao();
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
    }

    @Test
    public void deve_RetornarNivelUm_QuandoXpAtualIgualADezenove() {
        int nivel = BRACELETES.getNivel(context);
        assertEquals(1, nivel);
    }
    @Test
    public void deve_RetornarXpMaximoVinte_QuandoNivelUm() {
        int xpMaximo = BRACELETES.getXpMaximo(1);
        assertEquals(20, xpMaximo);
    }

    @Test
    public void deve_RetornarUm_QuandoXpAtualIgualADezenove() {
        int xpNecessario = BRACELETES.getXpNecessario(20);
        assertEquals(1, xpNecessario);
    }

    @Test
    public void deve_RetornarDezenove_QuandoXpAtualIgualADezenove() {
        int xpRestante = BRACELETES.getXpRestante(1,1);
        assertEquals(19, xpRestante);
    }
    @Test
    public void deve_RetornarNivelDois_QuandoXpAtualIgualACentoENoventaENove() {
        int nivel = CAPOTES.getNivel(context);
        assertEquals(2, nivel);
    }
    @Test
    public void deve_RetornarDuzentos_QuandoNivelDois() {
        int xpMaximo = CAPOTES.getXpMaximo(2);
        assertEquals(200, xpMaximo);
    }

    @Test
    public void deve_RetornarUm_QuandoXpAtualIgualACentoENoventaENove() {
        int xpNecessario = CAPOTES.getXpNecessario(200);
        assertEquals(1, xpNecessario);
    }

    @Test
    public void deve_RetornarCentoESententaEOito_QuandoXpAtualIgualACentoENoventaENove() {
        int xpRestante = CAPOTES.getXpRestante(2,1);
        assertEquals(178, xpRestante);
    }
    @Test
    public void deve_RetornarNivelVinteESeis_QuandoXpAtualIgualACentoEOitocentoETrintaMilEUm() {
        int nivel = AMULETOS.getNivel(context);
        assertEquals(26, nivel);
    }
    @Test
    public void deve_RetornarNivelVinteESeis_QuandoXpAtualIgualACentoEOitocentoETrintaMil() {
        int nivel = ANEIS.getNivel(context);
        assertEquals(26, nivel);
    }
    @Test
    public void deve_RetornarXpMaximoOitocentosETrintaMil_QuandoNivelVinteESeis() {
        int xpMaximo = ANEIS.getXpMaximo(26);
        assertEquals(830000, xpMaximo);
    }

    @Test
    public void deve_RetornarXpNecessarioZero_QuandoXpAtualIgualAOitocentoETrintaMil() {
        int xpNecessario = ANEIS.getXpNecessario(830000);
        assertEquals(0, xpNecessario);
    }

    @Test
    public void deve_RetornarZero_QuandoXpAtualIgualAOitocentoETrintaMil() {
        int xpRestante = ANEIS.getXpRestante(26,0);
        assertEquals(0, xpRestante);
    }
}