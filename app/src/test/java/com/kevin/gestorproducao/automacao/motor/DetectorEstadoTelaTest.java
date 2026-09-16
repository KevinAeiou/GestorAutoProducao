package com.kevin.gestorproducao.automacao.motor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class DetectorEstadoTelaTest {

    @Test
    public void deve_DetectarListaDeTrabalhos_QuandoTextoContemProduzir() {
        assertEquals(
            EstadoTela.ARTESANATO_LISTA_TRABALHOS,
            DetectorEstadoTela.detectar(criaTexto("Fazendo anel de bronze", "Produzir"))
        );
    }

    @Test
    public void deve_DetectarListaDeProfissoes_QuandoTextoContemArtesanatoENivel() {
        assertEquals(
            EstadoTela.ARTESANATO_LISTA_PROFISSOES,
            DetectorEstadoTela.detectar(criaTexto("Artesanato", "Nível 12", "Joalheria"))
        );
    }

    @Test
    public void deve_DetectarConfirmacao_QuandoTextoContemIniciarProducaoECancelar() {
        assertEquals(
            EstadoTela.CONFIRMACAO_INICIAR_PRODUCAO,
            DetectorEstadoTela.detectar(criaTexto("Iniciar produção", "Cancelar"))
        );
    }

    @Test
    public void deve_RetornarDesconhecida_QuandoNenhumaPalavraChaveBater() {
        assertEquals(
            EstadoTela.DESCONHECIDA,
            DetectorEstadoTela.detectar(criaTexto("Vida", "100/100"))
        );
    }

    @Test
    public void deve_IgnorarAcentuacaoEMaiusculas_AoDetectar() {
        assertEquals(
            EstadoTela.NOTICIAS,
            DetectorEstadoTela.detectar(criaTexto("AVANCAR"))
        );
    }

    private TextoDetectado criaTexto(String... linhas) {
        List<TextoDetectado.Linha> lista = new java.util.ArrayList<>();

        for (String linha : linhas) {
            lista.add(new TextoDetectado.Linha(linha, new Retangulo(0, 0, 10, 10)));
        }

        return new TextoDetectado(lista);
    }
}
