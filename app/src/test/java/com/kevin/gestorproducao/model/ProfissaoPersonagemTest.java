package com.kevin.gestorproducao.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ProfissaoPersonagemTest {
    private final ProfissaoPersonagem BRACELETES = new ProfissaoPersonagem();
    private final ProfissaoPersonagem CAPOTES = new ProfissaoPersonagem();
    private final ProfissaoPersonagem ANEIS = new ProfissaoPersonagem();
    private final ProfissaoPersonagem AMULETOS = new ProfissaoPersonagem();

    @Before
    public void configuraExperiencia() {
        // Achado A7: o teste original nunca chamava setExperiencia antes de usar estes
        // objetos — como o campo é Integer (não int), getNivel()/getXpRestante() comparavam
        // "experiencia < ..." com null e lançavam NullPointerException em toda execução.
        // Isso nunca foi percebido porque a classe também usava @org.testng.annotations.Test
        // sem o projeto configurar useTestNG() no Gradle, então o `./gradlew test` nunca
        // executava nenhum destes casos.
        BRACELETES.setExperiencia(19);
        CAPOTES.setExperiencia(199);
        ANEIS.setExperiencia(830000);
        AMULETOS.setExperiencia(830001);
    }

    @Test
    public void deve_RetornarNivelUm_QuandoXpAtualIgualADezenove() {
        int nivel = BRACELETES.getNivel();
        assertEquals(1, nivel);
    }
    @Test
    public void deve_RetornarXpMaximoVinte_QuandoNivelUm() {
        int xpMaximo = BRACELETES.getXpMaximo();
        assertEquals(20, xpMaximo);
    }

    @Test
    public void deve_RetornarDezenove_QuandoXpAtualIgualADezenove() {
        int xpRestante = BRACELETES.getXpRestante(1,1);
        assertEquals(19, xpRestante);
    }
    @Test
    public void deve_RetornarNivelDois_QuandoXpAtualIgualACentoENoventaENove() {
        int nivel = CAPOTES.getNivel();
        assertEquals(2, nivel);
    }
    @Test
    public void deve_RetornarDuzentos_QuandoNivelDois() {
        int xpMaximo = CAPOTES.getXpMaximo();
        assertEquals(200, xpMaximo);
    }

    @Test
    public void deve_RetornarCentoESententaEOito_QuandoXpAtualIgualACentoENoventaENove() {
        int xpRestante = CAPOTES.getXpRestante(2,1);
        assertEquals(178, xpRestante);
    }
    @Test
    public void deve_RetornarNivelVinteESeis_QuandoXpAtualIgualAOitocentoETrintaMilEUm() {
        int nivel = AMULETOS.getNivel();
        assertEquals(26, nivel);
    }
    @Test
    public void deve_RetornarNivelVinteESeis_QuandoXpAtualIgualAOitocentoETrintaMil() {
        int nivel = ANEIS.getNivel();
        assertEquals(26, nivel);
    }
    @Test
    public void deve_RetornarXpMaximoNovecentosENoventaESeisMil_QuandoNivelVinteESeis() {
        // Achado A7: o teste original esperava 830000 para o nível 26, mas getXpMaximo()
        // devolve o teto do nível atual (xpNiveis[nivel-1] = 996000), não o piso — o mesmo
        // valor que já era usado corretamente nos testes de nível 1 e 2 abaixo. Corrigido o
        // valor esperado para bater com o comportamento real do método, que está certo.
        int xpMaximo = ANEIS.getXpMaximo();
        assertEquals(996000, xpMaximo);
    }

    @Test
    public void deve_RetornarZero_QuandoXpAtualIgualAOitocentoETrintaMil() {
        int xpRestante = ANEIS.getXpRestante(26,0);
        assertEquals(0, xpRestante);
    }
}
