package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.utilitario.Utilitario.comparaString;

import androidx.annotation.StringRes;

import com.kevin.gestorproducao.R;

public enum Profissao {
    ANEIS("aneis", R.string.stringProfissaoAneis),
    CAPOTES("capotes", R.string.stringProfissaoCapotes),
    AMULETOS("amuletos", R.string.stringProfissaoAmuletos),
    BRACELETES("braceletes", R.string.stringProfissaoBraceletes),
    ARMADURAS_LEVES("armadurasleves", R.string.stringProfissaoArmaduraLeve),
    ARMADURAS_DE_TECIDO("armadurasdetecido", R.string.stringProfissaoArmaduraTecido),
    ARMADURAS_PESADAS("armaduraspesadas", R.string.stringProfissaoArmaduraPesada),
    ARMAS_CORPO_A_CORPO("armascorpoacorpo", R.string.stringProfissaoArmasCorpoCorpo),
    ARMAS_DE_LONGO_ALCANCE("armasdelongoalcance", R.string.stringProfissaoArmaLongoAlcance);

    private final String key;
    @StringRes
    private final int label;

    Profissao(String key, int label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() {
        return key;
    }
    public int getLabel() {
        return label;
    }

    public static Profissao fromKey(String key) {
        if (key == null) return null;

        for (Profissao profissao : values()) {
            if (comparaString(profissao.getKey(), key)) {
                return profissao;
            }
        }
        return null;
    }
}
