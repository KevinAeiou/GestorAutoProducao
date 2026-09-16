package com.kevin.gestorproducao.utilitario;

import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.PeriodoFiltro;
import com.kevin.gestorproducao.model.Trabalho;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    public static String formatarData(Long timestamp) {
        if (timestamp == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Resolve o tipo de período (dia/semana/mês/ano/personalizado) selecionado no filtro para um
    // intervalo [inicio, fim] em millis. dataReferencia ancora em que dia/semana/mês/ano cai o
    // período (null = o atual, a partir de agora); ignorado no caso PERSONALIZADO, onde o
    // intervalo já vem escolhido manualmente pelo usuário.
    public static long[] calcularIntervaloPeriodo(
        PeriodoFiltro tipoPeriodo,
        Long dataReferencia,
        Long dataInicioPersonalizada,
        Long dataFimPersonalizada
    ) {
        if (tipoPeriodo == PeriodoFiltro.PERSONALIZADO) {
            // Enquanto o usuário não confirmou um intervalo personalizado, nenhum trabalho
            // concluído deve corresponder — evita mostrar "todos" antes da escolha (ou reaplicar
            // um intervalo antigo ao reabrir o tipo Personalizado).
            if (dataInicioPersonalizada == null || dataFimPersonalizada == null) {
                return new long[]{Long.MAX_VALUE, Long.MIN_VALUE};
            }

            return new long[]{dataInicioPersonalizada, dataFimPersonalizada};
        }

        Calendar calendario = Calendar.getInstance();
        if (dataReferencia != null) {
            calendario.setTimeInMillis(dataReferencia);
        }
        zerarHora(calendario);

        if (tipoPeriodo == PeriodoFiltro.SEMANA) {
            calendario.set(Calendar.DAY_OF_WEEK, calendario.getFirstDayOfWeek());
        } else if (tipoPeriodo == PeriodoFiltro.MES) {
            calendario.set(Calendar.DAY_OF_MONTH, 1);
        } else if (tipoPeriodo == PeriodoFiltro.ANO) {
            calendario.set(Calendar.DAY_OF_YEAR, 1);
        }

        long inicio = calendario.getTimeInMillis();

        if (tipoPeriodo == PeriodoFiltro.SEMANA) {
            calendario.add(Calendar.WEEK_OF_YEAR, 1);
        } else if (tipoPeriodo == PeriodoFiltro.MES) {
            calendario.add(Calendar.MONTH, 1);
        } else if (tipoPeriodo == PeriodoFiltro.ANO) {
            calendario.add(Calendar.YEAR, 1);
        } else {
            calendario.add(Calendar.DAY_OF_MONTH, 1);
        }

        long fim = calendario.getTimeInMillis() - 1;

        return new long[]{inicio, fim};
    }

    // Janela móvel terminando agora — usada para pré-preencher o filtro "Personalizado" com um
    // intervalo relativo (ex.: últimos 6 meses) em vez de obrigar o usuário a escolher na mão.
    public static long[] calcularIntervaloUltimosMeses(int meses) {
        long fim = System.currentTimeMillis();

        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(fim);
        calendario.add(Calendar.MONTH, -meses);

        return new long[]{calendario.getTimeInMillis(), fim};
    }

    private static void zerarHora(Calendar calendario) {
        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);
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

