package com.kevin.gestorproducao.service;

import com.kevin.gestorproducao.model.TrabalhoChanceVenda;

import java.util.ArrayList;
import java.util.Comparator;

// Score de "chance de venda" = RFM simplificado (sem eixo monetário): combina recência (há
// quanto tempo foi a última venda) e frequência (quanto foi vendido no período), cada um
// normalizado 0-1 dentro do próprio conjunto retornado pela consulta. Trabalhos sem nenhuma
// venda no período recebem score de recência 0, mas continuam na lista (ordenados por último) —
// útil para identificar itens parados.
public class AnaliseVendaService {
    private static final double PESO_RECENCIA = 0.5;
    private static final double PESO_FREQUENCIA = 0.5;
    private static final double MILLIS_POR_DIA = 24d * 60 * 60 * 1000;

    private AnaliseVendaService() {}

    public static ArrayList<TrabalhoChanceVenda> ordenarPorChanceVenda(
        ArrayList<TrabalhoChanceVenda> trabalhos,
        long agora
    ) {
        if (trabalhos.isEmpty()) return trabalhos;

        double diasMin = Double.MAX_VALUE;
        double diasMax = -Double.MAX_VALUE;
        int quantidadeMin = Integer.MAX_VALUE;
        int quantidadeMax = Integer.MIN_VALUE;

        for (TrabalhoChanceVenda trabalho : trabalhos) {
            if (!trabalho.nuncaVendidoNoPeriodo()) {
                double dias = diasDesdeUltimaVenda(trabalho, agora);
                diasMin = Math.min(diasMin, dias);
                diasMax = Math.max(diasMax, dias);
            }

            int quantidade = trabalho.getQuantidadeVendidaPeriodo();
            quantidadeMin = Math.min(quantidadeMin, quantidade);
            quantidadeMax = Math.max(quantidadeMax, quantidade);
        }

        for (TrabalhoChanceVenda trabalho : trabalhos) {
            double scoreRecencia = calculaScoreRecencia(trabalho, agora, diasMin, diasMax);
            double scoreFrequencia = calculaScoreFrequencia(trabalho, quantidadeMin, quantidadeMax);

            trabalho.setScore(PESO_RECENCIA * scoreRecencia + PESO_FREQUENCIA * scoreFrequencia);
        }

        trabalhos.sort(
            Comparator
                .comparingDouble(TrabalhoChanceVenda::getScore).reversed()
                .thenComparing(
                    Comparator.comparingInt(TrabalhoChanceVenda::getQuantidadeVendidaPeriodo).reversed()
                )
                .thenComparing(TrabalhoChanceVenda::getNome)
        );

        return trabalhos;
    }

    private static double calculaScoreRecencia(
        TrabalhoChanceVenda trabalho,
        long agora,
        double diasMin,
        double diasMax
    ) {
        if (trabalho.nuncaVendidoNoPeriodo()) return 0;

        if (diasMax <= diasMin) return 1;

        double dias = diasDesdeUltimaVenda(trabalho, agora);

        return 1 - ((dias - diasMin) / (diasMax - diasMin));
    }

    private static double calculaScoreFrequencia(
        TrabalhoChanceVenda trabalho,
        int quantidadeMin,
        int quantidadeMax
    ) {
        if (quantidadeMax <= quantidadeMin) {
            return trabalho.getQuantidadeVendidaPeriodo() > 0 ? 1 : 0;
        }

        return (double) (trabalho.getQuantidadeVendidaPeriodo() - quantidadeMin) /
            (quantidadeMax - quantidadeMin);
    }

    private static double diasDesdeUltimaVenda(TrabalhoChanceVenda trabalho, long agora) {
        return (agora - trabalho.getUltimaVendaEm()) / MILLIS_POR_DIA;
    }
}
