package com.kevin.gestorproducao.automacao.motor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Classifica a tela atual do jogo a partir do texto lido por OCR, por presença de palavras-chave.
// As palavras abaixo são um ponto de partida e precisam ser conferidas/ajustadas contra capturas
// reais do WarSpear Online no dispositivo do usuário — os estados são verificados em ordem, do
// mais específico para o mais genérico, e o primeiro cujas palavras todas aparecerem vence.
// SPLASH e MUNDO_JOGO não têm texto confiável para detecção direta: ambos caem em DESCONHECIDA,
// que o NavegadorJogo trata tentando abrir o menu do jogo como ação de recuperação.
public final class DetectorEstadoTela {
    private static final Map<EstadoTela, List<String>> PALAVRAS_CHAVE = new LinkedHashMap<>();

    static {
        PALAVRAS_CHAVE.put(EstadoTela.CONFIRMACAO_INICIAR_PRODUCAO, List.of("iniciar produção", "cancelar"));
        PALAVRAS_CHAVE.put(EstadoTela.ARTESANATO_PRODUCOES_ATUAIS, List.of("produções atuais"));
        PALAVRAS_CHAVE.put(EstadoTela.ARTESANATO_LISTA_TRABALHOS, List.of("produzir"));
        PALAVRAS_CHAVE.put(EstadoTela.ARTESANATO_LISTA_PROFISSOES, List.of("artesanato", "nível"));
        PALAVRAS_CHAVE.put(EstadoTela.MENU_PERSONAGEM, List.of("artesanato", "atributos"));
        PALAVRAS_CHAVE.put(EstadoTela.MENU_ABERTO, List.of("personagem", "inventário"));
        PALAVRAS_CHAVE.put(EstadoTela.SELECAO_PERSONAGEM, List.of("jogar"));
        PALAVRAS_CHAVE.put(EstadoTela.NOTICIAS, List.of("avançar"));
        PALAVRAS_CHAVE.put(EstadoTela.LOGIN, List.of("entrar", "senha"));
    }

    private DetectorEstadoTela() {}

    public static EstadoTela detectar(TextoDetectado texto) {
        for (Map.Entry<EstadoTela, List<String>> entrada : PALAVRAS_CHAVE.entrySet()) {
            if (contemTodasPalavras(texto, entrada.getValue())) {
                return entrada.getKey();
            }
        }

        return EstadoTela.DESCONHECIDA;
    }

    private static boolean contemTodasPalavras(TextoDetectado texto, List<String> palavras) {
        for (String palavra : palavras) {
            if (!texto.contem(palavra)) {
                return false;
            }
        }

        return true;
    }
}
