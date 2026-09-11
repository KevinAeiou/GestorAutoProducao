package com.kevin.gestorproducao.utilitario;

import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.Trabalho;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Utilitario {
    public static String removeAcentos(String string) {
        if (string == null) {
            return "";
        }
        return Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }
    public static String limpaString(String string) {
        return removeAcentos(string).toLowerCase().replace(" ","");
    }
    public static boolean comparaString(String string1, String string2) {
        return limpaString(string1).equals(limpaString(string2));
    }
    public static boolean stringContemString(String string1, String string2) {
        return removeAcentos(string1).toLowerCase().replace(" ","").contains(removeAcentos(string2).toLowerCase().replace(" ",""));
    }

    public static String geraIdAleatorio() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String formatarTimestamp(Long timestamp) {
        if (timestamp == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    public static Integer extrairNivel(String texto) {
        String numeros = texto.replaceAll("\\D+", "");

        if (numeros.isEmpty()) {
            return null;
        }

        return Integer.parseInt(numeros);
    }

    public static String extrairDescricao(String texto) {
        return texto.replaceAll("\\d+", "").trim();
    }

    // Achado M2: esse filtro por descrição/profissões/raridades/nível estava copiado em 6
    // telas — Trabalho, TrabalhoEstoque, TrabalhoProducao e TrabalhoVendido compartilham esses
    // 4 getters via a superclasse Trabalho, o que permite um único método genérico aqui. Cada
    // tela mantém, à parte, os filtros que só fazem sentido para ela (ex.: estado de produção
    // em ListaTrabalhosProducaoFragment) e qualquer pós-processamento próprio (ordenação,
    // gráfico etc.).
    public static <T extends Trabalho> ArrayList<T> filtrarTrabalhos(
        List<T> trabalhos,
        FiltroTrabalho filtroAtual
    ) {
        ArrayList<T> filtrados = new ArrayList<>();

        if (filtroAtual == null) {
            filtrados.addAll(trabalhos);
            return filtrados;
        }

        String descricaoFiltro = filtroAtual.getDescricao() != null
            ? filtroAtual.getDescricao().toLowerCase()
            : "";

        Integer nivelFiltro = filtroAtual.getNivel();

        boolean temDescricao = !descricaoFiltro.isEmpty();
        boolean temProfissoes = filtroAtual.getProfissoes() != null && !filtroAtual.getProfissoes().isEmpty();
        boolean temRaridades = filtroAtual.getRaridades() != null && !filtroAtual.getRaridades().isEmpty();
        boolean temNivel = nivelFiltro != null;

        for (T trabalho : trabalhos) {
            boolean match = true;

            if (temDescricao) {
                match &= trabalho.getNome() != null &&
                    stringContemString(trabalho.getNome(), descricaoFiltro);
            }

            if (temProfissoes) {
                match &= filtroAtual.getProfissoes().stream()
                    .anyMatch(profissao ->
                        trabalho.getProfissao() != null &&
                            trabalho.getProfissao().equalsIgnoreCase(profissao.getNome())
                    );
            }

            if (temRaridades) {
                match &= filtroAtual.getRaridades().stream()
                    .anyMatch(raridade ->
                        trabalho.getRaridade() != null &&
                            trabalho.getRaridade().equalsIgnoreCase(raridade)
                    );
            }

            if (temNivel) {
                match &= trabalho.getNivel().equals(nivelFiltro);
            }

            if (match) {
                filtrados.add(trabalho);
            }
        }

        return filtrados;
    }
}

