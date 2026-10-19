package com.kevin.gestorproducao.service;

import static com.kevin.gestorproducao.utilitario.Utilitario.limpaString;

import android.content.Context;

import com.kevin.gestorproducao.model.RecursoComumAvancado;
import com.kevin.gestorproducao.model.Recurso;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.rules.CatalogoRecursos;
import com.kevin.gestorproducao.rules.CatalogoRecursosRaros;

import java.util.List;
import java.util.Map;

public class PricingService {
    private static final int MEDIA_VALOR_LICENCA_INICIANTE = 1000;
    private static final double FATOR_PERCENTUAL = 0.01;
    private static final int LICENCA_NOVATO = 80;

    private PricingService() {}

    public static class ValoresMercadoRecursos {
        public int mediaComum;
        public int mediaComposto;
        public int mediaEnergia;
        public int mediaEtereo;
    }

    public static ValoresMercadoRecursos mapeiaValoresMercado(
        Trabalho trabalhoSelecionado,
        List<RecursoComumAvancado> recursosAvancados,
        Context context
    ) {
        ValoresMercadoRecursos valores = new ValoresMercadoRecursos();

        if (trabalhoSelecionado.ehAmuletos(context) || trabalhoSelecionado.ehAneis(context) || trabalhoSelecionado.ehCapotes(context) || trabalhoSelecionado.ehBraceletes(context)) {
            for (RecursoComumAvancado recursoAvancado : recursosAvancados) {
                switch (recursoAvancado.getId()) {
                    case "e580e375-abc1-44f8-b332-774b7f1a490c":
                        valores.mediaComum = recursoAvancado.getValor();
                        continue;
                    case "94b66657-c7c6-41c0-b6f0-922614182549":
                        valores.mediaComposto = recursoAvancado.getValor();
                        continue;
                    case "c9751ecc-f528-4a80-88c3-d2a8af2804fa":
                        valores.mediaEnergia = recursoAvancado.getValor();
                        continue;
                    case "7c27a18c-fc60-484c-9545-99030a623129":
                        valores.mediaEtereo = recursoAvancado.getValor();
                        break;
                }
            }
        }

        if (trabalhoSelecionado.ehLongoAlcance(context) || trabalhoSelecionado.ehCorpoCorpo(context)) {
            for (RecursoComumAvancado recursoAvancado : recursosAvancados) {
                switch (recursoAvancado.getId()) {
                    case "b7f69638-c9b7-4c69-865e-cbacef5c45b1":
                        valores.mediaComum = recursoAvancado.getValor();
                        continue;
                    case "3a085587-5093-471d-9187-27b2370e4b38":
                        valores.mediaComposto = recursoAvancado.getValor();
                        continue;
                    case "259d5a95-72fd-4b36-b17f-c7b6a2a6897f":
                        valores.mediaEnergia = recursoAvancado.getValor();
                        continue;
                    case "2d8c434a-50eb-4269-bc70-725ded6bc7e9":
                        valores.mediaEtereo = recursoAvancado.getValor();
                        break;
                }
            }
        }

        if (trabalhoSelecionado.ehArmaduraPesada(context) || trabalhoSelecionado.ehArmaduraLeve(context) || trabalhoSelecionado.ehArmaduraTecido(context)) {
            for (RecursoComumAvancado recursoAvancado : recursosAvancados) {
                switch (recursoAvancado.getId()) {
                    case "6ac21d44-1e8d-4bf8-bd62-53248e568417":
                        valores.mediaComum = recursoAvancado.getValor();
                        continue;
                    case "6250e394-4a82-4ccb-b697-c788b9094c41":
                        valores.mediaComposto = recursoAvancado.getValor();
                        continue;
                    case "b2f158f9-5b52-444a-a27b-7ac1284063c6":
                        valores.mediaEnergia = recursoAvancado.getValor();
                        continue;
                    case "e12c1346-9343-414e-a0b5-631e494423b2":
                        valores.mediaEtereo = recursoAvancado.getValor();
                        break;
                }
            }
        }

        return valores;
    }

    public static int calculaValorProducaoComum(
        Trabalho trabalhoSelecionado,
        Context context,
        int mediaValorRecursoUnitarioComumMercado,
        int mediaValorRecursoUnitarioCompostoMercado
    ) {
        int quantidadeMaximaRecursos = trabalhoSelecionado.recuperaQuantidadeMaximaRecursos(context);
        int quantidadeTotalRecursos = quantidadeMaximaRecursos * 3 + 3;
        int quantidadeMaximaRecursosProduzido = trabalhoSelecionado.getNivel() > 14 ? 24 : 18;
        int quantidadeRecursosNecessarios = trabalhoSelecionado.getNivel() > 14 ? 8 : 4;
        int valorRecursoUnitario = (mediaValorRecursoUnitarioCompostoMercado * quantidadeRecursosNecessarios) / quantidadeMaximaRecursosProduzido;
        int valorLicencaComum = 80;
        int valorLicencaAprendiz = mediaValorRecursoUnitarioComumMercado * 4 / 2 + 80;
        double resultado = (double) quantidadeTotalRecursos / quantidadeMaximaRecursosProduzido;
        int quantidadeLicencaAprendizUtilizada = (int) Math.max(Math.round(resultado), 1);
        int valorLicencas = valorLicencaComum + (valorLicencaAprendiz * quantidadeLicencaAprendizUtilizada);
        int valorRecursoTotal = quantidadeTotalRecursos * valorRecursoUnitario;
        return valorRecursoTotal + valorLicencas;
    }

    public static int quantidadeProduzidaPorTrabalho(Trabalho trabalho) {
        if (trabalho.ehLicencaProducaoAprendiz()) return 2;
        if (trabalho.ehRaro()) {
            Map<Recurso, Integer> recursos = CatalogoRecursosRaros.getRecursos(
                trabalho.getProfissao(),
                trabalho.getNome()
            );
            int total = 0;
            for (int quantidade : recursos.values()) total += quantidade;
            return Math.max(total, 1);
        }
        return Math.max(CatalogoRecursos.getQuantidade(trabalho.getProfissao(), trabalho.getNome()), 1);
    }

    public static int calculaCustoMateriaisRecurso(
        Trabalho trabalho,
        int mediaValorRecursoUnitarioComumMercado,
        int mediaValorRecursoUnitarioCompostoMercado
    ) {
        if (!trabalho.ehRaro() || trabalho.ehLicencaProducaoAprendiz()) return 0;
        String nome = limpaString(trabalho.getNome());
        if (nome.contains("avancados")) return 8 * mediaValorRecursoUnitarioCompostoMercado;
        return 4 * mediaValorRecursoUnitarioComumMercado;
    }

    public static int calculaValorProducaoRecurso(
        int quantidadePorTrabalho,
        boolean ehLicencaAprendiz,
        int mediaValorRecursoUnitarioComumMercado,
        int custoMateriaisPorTrabalho,
        int quantidadeDesejada
    ) {
        if (quantidadeDesejada <= 0 || quantidadePorTrabalho <= 0) return 0;
        double custoLicencaAprendiz = (4.0 * mediaValorRecursoUnitarioComumMercado + LICENCA_NOVATO) / 2;
        double custoUnitario;
        if (ehLicencaAprendiz) {
            custoUnitario = custoLicencaAprendiz;
        } else {
            double comNovato = (double) (LICENCA_NOVATO + custoMateriaisPorTrabalho) / quantidadePorTrabalho;
            double comAprendiz = (custoLicencaAprendiz + custoMateriaisPorTrabalho) / (quantidadePorTrabalho * 2);
            custoUnitario = Math.min(comNovato, comAprendiz);
        }
        return (int) Math.ceil(custoUnitario * quantidadeDesejada - 1e-9);
    }

    public static int calculaValorProducaoMelhorado(
        Trabalho trabalhoSelecionado,
        Context context,
        int valorProducaoComum,
        int mediaValorRecursoUnitarioEnergiaMercado
    ) {
        List<String> listaTrabalhosNecessarios = trabalhoSelecionado.getListaTrabalhosNecessarios();
        int quantidadeTrabalhosComunsNecessarios = listaTrabalhosNecessarios.size();
        int quantidadeRecursoEnerga = trabalhoSelecionado.recuperaQuantidadeMaximaRecursosEnergia(context);
        return (valorProducaoComum * quantidadeTrabalhosComunsNecessarios) + (mediaValorRecursoUnitarioEnergiaMercado * quantidadeRecursoEnerga) + MEDIA_VALOR_LICENCA_INICIANTE;
    }

    public static int calculaValorProducaoRaro(
        Trabalho trabalhoSelecionado,
        Context context,
        int valorProducaoMelhorado,
        int mediaValorRecursoUnitarioEtereoMercado
    ) {
        int quantidadeRecursoEtereo = trabalhoSelecionado.recuperaQuantidadeMaximaRecursosEtereo(context);
        return valorProducaoMelhorado + (mediaValorRecursoUnitarioEtereoMercado * quantidadeRecursoEtereo) + MEDIA_VALOR_LICENCA_INICIANTE;
    }

    public static final double TAXA_VENDA = 0.10;
    public static final double TAXA_OFERTA = 0.10;
    public static final int OFERTAS_PADRAO = 1;

    // Fração do preço anunciado que sobra ao vendedor após a venda e todas as ofertas.
    public static double fatorLiquido(int quantidadeOfertas) {
        int ofertas = Math.max(quantidadeOfertas, 1);
        return Math.max(1 - TAXA_VENDA - TAXA_OFERTA * ofertas, 0);
    }

    public static int calculaTaxa(int novoValorLucro, int valorProducao, int quantidadeOfertas) {
        if (valorProducao == 0) return 0;
        double valorLiquido = novoValorLucro * fatorLiquido(quantidadeOfertas);
        double taxa = (valorLiquido / valorProducao - 1) * 100;
        return (int) Math.round(taxa);
    }

    public static int calculaValorLucro(int novaTaxa, int valorProducao, int quantidadeOfertas) {
        double fator = fatorLiquido(quantidadeOfertas);
        if (fator <= 0) return 0;
        double valorDesejado = valorProducao * (1 + novaTaxa * FATOR_PERCENTUAL);
        return (int) Math.max(Math.ceil(valorDesejado / fator - 1e-9), 0);
    }
}
