package com.kevin.gestorproducao.utilitario;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class TemaUtil {

    private static final String PREFS_NOME = "user_preferences";
    private static final String CHAVE_TEMA = "modo_noturno_selecionado";

    public static void aplicaTemaSalvo(Context context) {
        AppCompatDelegate.setDefaultNightMode(pegaModoSalvo(context));
    }

    public static void salvaEAplicaTema(Context context, int modoNoturno) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NOME, Context.MODE_PRIVATE);
        preferences.edit().putInt(CHAVE_TEMA, modoNoturno).apply();

        AppCompatDelegate.setDefaultNightMode(modoNoturno);
    }

    public static int pegaModoSalvo(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NOME, Context.MODE_PRIVATE);

        return preferences.getInt(CHAVE_TEMA, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static int pegaIndiceOpcaoSelecionada(Context context) {
        int modo = pegaModoSalvo(context);

        if (modo == AppCompatDelegate.MODE_NIGHT_NO) return 0;
        if (modo == AppCompatDelegate.MODE_NIGHT_YES) return 1;

        return 2;
    }

    public static int pegaModoNoturnoPorIndice(int indice) {
        switch (indice) {
            case 0:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case 1:
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}
