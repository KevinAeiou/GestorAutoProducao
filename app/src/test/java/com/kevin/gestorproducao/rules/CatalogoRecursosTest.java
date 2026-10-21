package com.kevin.gestorproducao.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kevin.gestorproducao.model.Profissao;
import com.kevin.gestorproducao.model.Recurso;

import org.junit.Test;

import java.util.Map;

public class CatalogoRecursosTest {

    @Test
    public void deve_PossuirTodasAsProfissoesNoCatalogo() {
        for (Profissao profissao : Profissao.values()) {
            Map<Recurso, Integer> recursos = CatalogoRecursos.getCatalogo().get(profissao);
            assertNotNull("Profissão sem catálogo: " + profissao, recursos);
            assertFalse(recursos.isEmpty());
        }
    }

    @Test
    public void deve_TerSomenteQuantidadesPositivas() {
        for (Map<Recurso, Integer> recursos : CatalogoRecursos.getCatalogo().values()) {
            for (int quantidade : recursos.values()) {
                assertTrue(quantidade > 0);
            }
        }
    }

    @Test
    public void deve_RetornarQuantidadeDoCatalogo_IgnorandoAcentoECaixa() {
        assertEquals(2, CatalogoRecursos.getQuantidade("Anéis", "Molde do Aprendiz"));
        assertEquals(1, CatalogoRecursos.getQuantidade("aneis", "pepitadecobre"));
    }

    @Test
    public void deve_RetornarUm_QuandoProfissaoOuRecursoDesconhecidos() {
        assertEquals(1, CatalogoRecursos.getQuantidade("Inexistente", "Molde do Aprendiz"));
        assertEquals(1, CatalogoRecursos.getQuantidade("Anéis", "Inexistente"));
    }

    @Test
    public void deve_RetornarUm_QuandoRecursoNaoPertenceAProfissao() {
        assertEquals(1, CatalogoRecursos.getQuantidade("Anéis", "Varinha de Madeira"));
    }
}
