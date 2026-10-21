package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_LICENCA_INICIANTE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_FEITO;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PARA_PRODUZIR;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PRODUZINDO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TrabalhoProducaoTest {

    @Test
    public void deve_NascerParaProduzirESemRecorrencia() {
        TrabalhoProducao trabalho = new TrabalhoProducao();

        assertTrue(trabalho.ehProduzir());
        assertFalse(trabalho.ehProduzindo());
        assertFalse(trabalho.ehFeito());
        assertFalse(trabalho.getRecorrencia());
        assertNotNull(trabalho.getId());
    }

    @Test
    public void deve_MarcarInicio_QuandoPassarDeParaProduzirParaProduzindo() {
        TrabalhoProducao trabalho = new TrabalhoProducao();

        trabalho.atualizarEstado(CODIGO_TRABALHO_PRODUZINDO);

        assertTrue(trabalho.ehProduzindo());
        assertNotNull(trabalho.getIniciadoEm());
        assertNull(trabalho.getFinalizadoEm());
    }

    @Test
    public void deve_MarcarFim_QuandoPassarDeProduzindoParaFeito() {
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.atualizarEstado(CODIGO_TRABALHO_PRODUZINDO);

        trabalho.atualizarEstado(CODIGO_TRABALHO_FEITO);

        assertTrue(trabalho.ehFeito());
        assertNotNull(trabalho.getIniciadoEm());
        assertNotNull(trabalho.getFinalizadoEm());
    }

    @Test
    public void deve_NaoMarcarFim_QuandoPularDiretoDeParaProduzirParaFeito() {
        TrabalhoProducao trabalho = new TrabalhoProducao();

        trabalho.atualizarEstado(CODIGO_TRABALHO_FEITO);

        assertTrue(trabalho.ehFeito());
        assertNull(trabalho.getFinalizadoEm());
    }

    @Test
    public void deve_PreservarInicio_QuandoJaIniciadoAnteriormente() {
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.setIniciadoEm(123L);

        trabalho.atualizarEstado(CODIGO_TRABALHO_PRODUZINDO);

        assertEquals(Long.valueOf(123L), trabalho.getIniciadoEm());
    }

    @Test
    public void deve_PreservarFim_QuandoJaFinalizadoAnteriormente() {
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.setEstado(CODIGO_TRABALHO_PRODUZINDO);
        trabalho.setFinalizadoEm(456L);

        trabalho.atualizarEstado(CODIGO_TRABALHO_FEITO);

        assertEquals(Long.valueOf(456L), trabalho.getFinalizadoEm());
    }

    @Test
    public void deve_PermitirVoltarEstado_SemAlterarInicio() {
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.atualizarEstado(CODIGO_TRABALHO_PRODUZINDO);
        Long iniciado = trabalho.getIniciadoEm();

        trabalho.atualizarEstado(CODIGO_TRABALHO_PARA_PRODUZIR);

        assertTrue(trabalho.ehProduzir());
        assertEquals(iniciado, trabalho.getIniciadoEm());
    }

    @Test
    public void deve_AplicarBonusDeUmVirgulaCinco_QuandoLicencaDeIniciante() {
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.setExperiencia(100);
        trabalho.setTipoLicenca(CHAVE_LICENCA_INICIANTE);

        assertEquals(Integer.valueOf(150), trabalho.getExperiencia());
    }

    @Test
    public void deve_ManterExperiencia_QuandoOutraLicenca() {
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.setExperiencia(100);
        trabalho.setTipoLicenca("Outra licença");

        assertEquals(Integer.valueOf(100), trabalho.getExperiencia());
    }
}
