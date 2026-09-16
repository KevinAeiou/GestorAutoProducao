package com.kevin.gestorproducao.automacao.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NavegadorJogoTest {
    private final AlvoNavegacao alvo = new AlvoNavegacao("Joalheria", "Fazendo anel de bronze");

    @Test
    public void deve_RequererIntervencaoManual_QuandoEstadoForLogin() {
        AcaoNavegacao acao = NavegadorJogo.decidirProximaAcao(EstadoTela.LOGIN, textoVazio(), alvo);

        assertEquals(AcaoNavegacao.Tipo.REQUER_INTERVENCAO_MANUAL, acao.getTipo());
    }

    @Test
    public void deve_TocarAncoraAvancar_QuandoEstadoForNoticias() {
        AcaoNavegacao acao = NavegadorJogo.decidirProximaAcao(EstadoTela.NOTICIAS, textoVazio(), alvo);

        assertEquals(AcaoNavegacao.Tipo.TOQUE_ANCORA, acao.getTipo());
        assertEquals(CoordenadasReferencia.Ancora.BOTAO_AVANCAR_NOTICIAS, acao.getAncora());
    }

    @Test
    public void deve_TocarPosicaoDoTrabalho_QuandoEncontradoNaListaDeTrabalhos() {
        TextoDetectado texto = criaTexto(new TextoDetectado.Linha(
            "Fazendo anel de bronze", new Retangulo(10, 20, 110, 40)
        ));

        AcaoNavegacao acao = NavegadorJogo.decidirProximaAcao(
            EstadoTela.ARTESANATO_LISTA_TRABALHOS, texto, alvo
        );

        assertEquals(AcaoNavegacao.Tipo.TOQUE_POSICAO, acao.getTipo());
        assertEquals(60, acao.getPosicao().centroX());
        assertEquals(30, acao.getPosicao().centroY());
    }

    @Test
    public void deve_RetornarAlvoNaoEncontrado_QuandoTrabalhoNaoEstaNaLista() {
        TextoDetectado texto = criaTexto(new TextoDetectado.Linha(
            "Fazendo anel de ferro", new Retangulo(0, 0, 10, 10)
        ));

        AcaoNavegacao acao = NavegadorJogo.decidirProximaAcao(
            EstadoTela.ARTESANATO_LISTA_TRABALHOS, texto, alvo
        );

        assertEquals(AcaoNavegacao.Tipo.ALVO_NAO_ENCONTRADO, acao.getTipo());
        assertNull(acao.getPosicao());
    }

    @Test
    public void deve_TentarAbrirMenu_QuandoTelaForDesconhecida() {
        AcaoNavegacao acao = NavegadorJogo.decidirProximaAcao(EstadoTela.DESCONHECIDA, textoVazio(), alvo);

        assertEquals(AcaoNavegacao.Tipo.TOQUE_ANCORA, acao.getTipo());
        assertEquals(CoordenadasReferencia.Ancora.BOTAO_MENU_JOGO, acao.getAncora());
    }

    private TextoDetectado textoVazio() {
        return new TextoDetectado(new ArrayList<>());
    }

    private TextoDetectado criaTexto(TextoDetectado.Linha... linhas) {
        List<TextoDetectado.Linha> lista = new ArrayList<>();
        for (TextoDetectado.Linha linha : linhas) {
            lista.add(linha);
        }
        return new TextoDetectado(lista);
    }
}
