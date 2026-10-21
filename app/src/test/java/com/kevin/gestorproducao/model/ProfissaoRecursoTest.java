package com.kevin.gestorproducao.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ProfissaoRecursoTest {

    @Test
    public void deve_EncontrarProfissaoPorChave_IgnorandoAcentoCaixaEEspaco() {
        assertEquals(Profissao.ANEIS, Profissao.fromKey("Anéis"));
        assertEquals(Profissao.ARMAS_CORPO_A_CORPO, Profissao.fromKey("Armas Corpo a Corpo"));
    }

    @Test
    public void deve_RetornarNulo_QuandoChaveDeProfissaoInvalida() {
        assertNull(Profissao.fromKey(null));
        assertNull(Profissao.fromKey("desconhecida"));
    }

    @Test
    public void deve_EncontrarRecursoPorChave_IgnorandoCaixa() {
        assertEquals(Recurso.MOLDE_DO_APRENDIZ, Recurso.fromKey("MoldeDoAprendiz"));
    }

    @Test
    public void deve_RetornarNulo_QuandoChaveDeRecursoInvalida() {
        assertNull(Recurso.fromKey(null));
        assertNull(Recurso.fromKey("desconhecido"));
    }
}
