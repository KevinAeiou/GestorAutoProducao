package com.kevin.gestorproducao.automacao.motor;

// Decide, a partir da tela atual e do alvo (profissão + trabalho procurados), qual o próximo
// gesto para se aproximar da tela "Lista de Trabalhos" da profissão certa e, quando já está
// nela, tocar no trabalho procurado. Nunca digita nada: uma tela de LOGIN é tratada como
// intervenção manual — a automação não guarda nem digita a senha do jogo.
public final class NavegadorJogo {
    private NavegadorJogo() {}

    public static AcaoNavegacao decidirProximaAcao(
        EstadoTela estadoAtual,
        TextoDetectado texto,
        AlvoNavegacao alvo
    ) {
        switch (estadoAtual) {
            case LOGIN:
                return AcaoNavegacao.requerIntervencaoManual();

            case NOTICIAS:
                return AcaoNavegacao.tocarAncora(CoordenadasReferencia.Ancora.BOTAO_AVANCAR_NOTICIAS);

            case SELECAO_PERSONAGEM:
                return AcaoNavegacao.tocarAncora(CoordenadasReferencia.Ancora.BOTAO_JOGAR);

            case MENU_ABERTO:
                return AcaoNavegacao.tocarAncora(CoordenadasReferencia.Ancora.OPCAO_MENU_PERSONAGEM);

            case MENU_PERSONAGEM:
                return AcaoNavegacao.tocarAncora(CoordenadasReferencia.Ancora.OPCAO_MENU_ARTESANATO);

            case ARTESANATO_PRODUCOES_ATUAIS:
                return AcaoNavegacao.tocarAncora(CoordenadasReferencia.Ancora.BOTAO_VOLTAR);

            case ARTESANATO_LISTA_PROFISSOES:
                return tocarTextoOuNaoEncontrado(texto, alvo.getNomeProfissao());

            case ARTESANATO_LISTA_TRABALHOS:
                return tocarTextoOuNaoEncontrado(texto, alvo.getNomeTrabalho());

            case CONFIRMACAO_INICIAR_PRODUCAO:
                return AcaoNavegacao.tocarAncora(CoordenadasReferencia.Ancora.BOTAO_CONFIRMAR_PRODUCAO);

            case MUNDO_JOGO:
            case SPLASH:
            case DESCONHECIDA:
            default:
                return AcaoNavegacao.tocarAncora(CoordenadasReferencia.Ancora.BOTAO_MENU_JOGO);
        }
    }

    private static AcaoNavegacao tocarTextoOuNaoEncontrado(TextoDetectado texto, String textoAlvo) {
        TextoDetectado.Linha linha = texto.encontra(textoAlvo);

        return linha != null
            ? AcaoNavegacao.tocarPosicao(linha.getPosicao())
            : AcaoNavegacao.alvoNaoEncontrado();
    }
}
