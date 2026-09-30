package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.utilitario.Utilitario.limpaString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TrabalhoTest {

    @Test
    public void deve_RetornarFalso_QuandoNaoForProducaoDeRecursos() {
        assertFalse(criaTrabalho("Fazendo anel de bronze").ehProducaoDeRecursos());
    }

    @Test
    public void deve_RetornarVerdadeiro_QuandoForProducaoDeRecursos() {
        assertTrue(criaTrabalho("Produzindo a varinha de madeira").ehProducaoDeRecursos());
    }

    @Test
    public void deve_ReconhecerNomesComAcentoQueEstavamQuebrados() {
        // Regressão do achado A1: estas 4 entradas tinham acento na lista hardcoded e nunca
        // eram reconhecidas, mesmo com o nome idêntico ao do catálogo.
        assertTrue(criaTrabalho("Produzindo a varinha de aço").ehProducaoDeRecursos());
        assertTrue(criaTrabalho("Adquirir pinças do principiante").ehProducaoDeRecursos());
        assertTrue(criaTrabalho("Extração de substância instável").ehProducaoDeRecursos());
        assertTrue(criaTrabalho("Extração de substância estável").ehProducaoDeRecursos());
    }

    @Test
    public void deve_TerTodasEntradasDoCatalogoJaNormalizadas() {
        for (String nomeProducao : Trabalho.LISTA_PRODUCAO_RECURSOS) {
            assertEquals(nomeProducao, limpaString(nomeProducao));
        }
    }

    private Trabalho criaTrabalho(String nomeProducao) {
        Trabalho trabalho = new Trabalho();
        trabalho.setNomeProducao(nomeProducao);
        return trabalho;
    }
}
