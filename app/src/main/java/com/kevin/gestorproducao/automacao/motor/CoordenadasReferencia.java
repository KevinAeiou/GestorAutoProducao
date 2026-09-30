package com.kevin.gestorproducao.automacao.motor;

import java.util.EnumMap;
import java.util.Map;

// Pontos de toque fixos (menu, botões de navegação) expressos como fração [0..1] da resolução
// real da tela, não pixels absolutos — a automação precisa funcionar em qualquer dispositivo.
// Os valores abaixo são um ponto de partida e precisam ser recalibrados comparando com capturas
// reais do WarSpear Online no dispositivo do usuário.
public final class CoordenadasReferencia {
    public enum Ancora {
        BOTAO_JOGAR,
        BOTAO_AVANCAR_NOTICIAS,
        BOTAO_MENU_JOGO,
        OPCAO_MENU_PERSONAGEM,
        OPCAO_MENU_ARTESANATO,
        BOTAO_CONFIRMAR_PRODUCAO,
        BOTAO_VOLTAR
    }

    private static final Map<Ancora, float[]> FRACOES = new EnumMap<>(Ancora.class);

    static {
        FRACOES.put(Ancora.BOTAO_JOGAR, new float[]{0.5f, 0.85f});
        FRACOES.put(Ancora.BOTAO_AVANCAR_NOTICIAS, new float[]{0.5f, 0.92f});
        FRACOES.put(Ancora.BOTAO_MENU_JOGO, new float[]{0.93f, 0.90f});
        FRACOES.put(Ancora.OPCAO_MENU_PERSONAGEM, new float[]{0.80f, 0.55f});
        FRACOES.put(Ancora.OPCAO_MENU_ARTESANATO, new float[]{0.80f, 0.40f});
        FRACOES.put(Ancora.BOTAO_CONFIRMAR_PRODUCAO, new float[]{0.65f, 0.70f});
        FRACOES.put(Ancora.BOTAO_VOLTAR, new float[]{0.08f, 0.08f});
    }

    private CoordenadasReferencia() {}

    public static Retangulo resolver(Ancora ancora, int larguraTela, int alturaTela) {
        float[] fracao = FRACOES.get(ancora);
        int x = Math.round(fracao[0] * larguraTela);
        int y = Math.round(fracao[1] * alturaTela);

        return new Retangulo(x, y, x, y);
    }
}
