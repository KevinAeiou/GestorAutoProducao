package com.kevin.gestorproducao.utilitario;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.kevin.gestorproducao.R;

import java.text.NumberFormat;
import java.util.Locale;

public class Formatador {

    private static final Locale LOCALE_BR =
            new Locale("pt", "BR");

    public static String formatarMilhar(int valor) {
        return NumberFormat
                .getInstance(LOCALE_BR)
                .format(valor);
    }

    public static String formatarMilhar(long valor) {
        return NumberFormat
                .getInstance(LOCALE_BR)
                .format(valor);
    }

    // Achado M7: mapeamento raridade -> cor estava duplicado (com pequenas divergências) em
    // 4 adapters. Um deles (ListaTrabalhoEspecificoNovaProducaoAdapter) nem chegava a checar
    // null antes do switch, um NullPointerException em potencial se algum dia um Trabalho
    // chegasse com raridade nula — o null-check aqui cobre esse caso para todo mundo.
    public static int corPorRaridade(Context context, String raridade) {
        if (raridade == null) {
            return ContextCompat.getColor(context, R.color.cor_texto_raridade_comum);
        }

        switch (raridade) {
            case "Melhorado":
                return ContextCompat.getColor(context, R.color.cor_texto_raridade_melhorado);
            case "Raro":
                return ContextCompat.getColor(context, R.color.cor_texto_raridade_raro);
            case "Especial":
                return ContextCompat.getColor(context, R.color.cor_texto_raridade_especial);
            default:
                return ContextCompat.getColor(context, R.color.cor_texto_raridade_comum);
        }
    }
}
