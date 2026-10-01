package com.kevin.gestorproducao.model;

import static com.kevin.gestorproducao.utilitario.Utilitario.geraIdAleatorio;
import static com.kevin.gestorproducao.utilitario.Utilitario.limpaString;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.rules.CatalogoLicencasProducao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trabalho extends BaseEntity implements Serializable {
    // Comparada em ehProducaoDeRecursos() contra um nome já normalizado por limpaString()
    // (sem acento, minúsculo, sem espaço) — toda entrada aqui precisa nascer já normalizada,
    // senão a comparação nunca bate (era o bug do achado A1: 4 entradas tinham acento).
    static final List<String> LISTA_PRODUCAO_RECURSOS = List.of(
        "melhorarlicencacomum","licencadeproducaodoaprendiz","grandecolecaoderecursoscomuns",
        "grandecolecaoderecursosavancados","coletaemmassaderecursosavancados","melhoriadaessenciacomum",
        "melhoriadasubstanciacomum","melhoriadocatalizadorcomum","melhoriadaessenciacomposta",
        "melhoriadasubtanciacomposta","melhoriadocatalizadoramplificado","criaresferadoaprendiz",
        "produzindoavarinhademadeira","produzindocabecadocajadodejade","produzindocabecadecajadodeonix",
        "criaresferadoneofito","produzindoavarinhadeaco","extracaodelascas",
        "manipulacaodelascas","fazermodoaprendiz","preparandolascasdequartzo",
        "manipulacaodemineriodecobre","fazermodoprincipiante","adquirirtesouradoaprendiz",
        "produzindofioresistente","fazendotecidodelinho","fazendotecidodecetim",
        "comprartesouradoprincipiante","produzindofiogrosso","adquirirfacadoaprendiz",
        "recebendoescamasdaserpente","concluindocouroresistente","adquirirfacadoprincipiante",
        "recebendoescamasdolagarto","curtindocourogrosso","adquirirmarretaodoaprendiz",
        "forjandoplacasdecobre","fazendoplacasdebronze","adquirirmarretaodoprincipiante",
        "forjandoplacasdeferro","fazendoaneisdeaco","adquirirmoldedoaprendiz",
        "extracaodepepitasdecobre","recebendogemadassombras","adquirirmoldedoprincipiante",
        "extracaodepepitasdeprata","recebendogemadaluz","adquirirpincadoaprendiz",
        "extracaodejadebruta","recebendoenergiainicial","adquirirpincasdoprincipiante",
        "extracaodeonixextraordinaria","recebendoeterinicial","adquirirfuradordoaprendiz",
        "produzindotecidodelicado","extracaodesubstanciainstavel","adquirirfuradordoprincipiante",
        "produzindotecidodenso","extracaodesubstanciaestavel","recebendofibradebronze",
        "recebendoprata","recebendoinsigniadeestudante","recebendofibradeplatina",
        "recebendoambar","recebendodistintivodeaprendiz"
    );

    private String id;
    private String nome;
    private String nomeProducao;
    private String profissao;
    private String raridade;
    private Map<String, Boolean> necessarios;
    private Integer nivel;
    private Integer experiencia;

    public Trabalho(){
        geraNovoId();
    }

    public Trabalho(
        String nome,
        String nomeProducao,
        String profissao,
        String raridade,
        Map<String, Boolean> necessarios,
        Integer nivel,
        Integer experiencia
    ) {
        this.necessarios = necessarios;
        geraNovoId();
        this.nome = nome;
        this.nomeProducao = nomeProducao;
        this.profissao = profissao;
        this.raridade = raridade;
        this.nivel = nivel;
        this.experiencia = experiencia;
    }

    public String getNome() {
        return nome;
    }
    public String getNomeProducao() {
        return nomeProducao;
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public Integer getNivel() {
        return nivel;
    }

    public String getRaridade() {
        return raridade;
    }

    public String getId() {
        return id;
    }

    public Map<String, Boolean> getNecessarios() {
        return necessarios;
    }

    public void setId(String novoId) {
        this.id = novoId;
    }

    public Integer getExperiencia() {
        return experiencia;
    }

    @Exclude
    public List<String> getListaTrabalhosNecessarios() {
        if (necessarios == null) return new ArrayList<>();
        return new ArrayList<>(necessarios.keySet());
    }
    public void setNecessarios(Map<String, Boolean> necessarios) {
        this.necessarios = necessarios;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setNomeProducao(String nomeProducao) {
        this.nomeProducao = nomeProducao;
    }

    public void setRaridade(String raridade) {
        this.raridade = raridade;
    }

    public boolean possuiTrabalhosNecessarios() {
        return necessarios != null && !necessarios.isEmpty();
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public void setExperiencia(Integer experiencia) {
        this.experiencia = experiencia;
    }
    public boolean ehProducaoDeRecursos() {
        return LISTA_PRODUCAO_RECURSOS.contains(limpaString(nomeProducao)) || ehLicencaProducaoAprendiz();
    }

    // Cada tipo de profissão chama esse trabalho de um jeito; os nomes ficam em CatalogoLicencasProducao.
    public boolean ehLicencaProducaoAprendiz() {
        return CatalogoLicencasProducao.ehLicencaProducao(nomeProducao)
            || CatalogoLicencasProducao.ehLicencaProducao(nome);
    }

    public void geraNovoId() {
        this.id= geraIdAleatorio();
    }

    @NonNull
    @Override
    public String toString() {
        return nome;
    }

    public boolean ehComum() {
        return raridade.equals("Comum");
    }

    public int recuperaQuantidadeMaximaRecursos(Context context) {
        if (ehProducaoDeRecursos()) {
            return 0;
        }
        int quantidadeBase = profissao.equals(context.getString(R.string.stringProfissaoArmaLongoAlcance)) ? 1 : 0;
        Map<Integer, Integer> bonusPorNivel = recuperaMapQuantidadeMaximaRecursosProducao();
        return quantidadeBase + bonusPorNivel.getOrDefault(nivel, 0);
    }

    @NonNull
    private static Map<Integer, Integer> recuperaMapQuantidadeMaximaRecursosProducao() {
        Map<Integer, Integer> bonusPorNivel = new HashMap<>();
        bonusPorNivel.put(10, 2);
        bonusPorNivel.put(12, 4);
        bonusPorNivel.put(14, 6);
        bonusPorNivel.put(16, 2);
        bonusPorNivel.put(18, 4);
        bonusPorNivel.put(20, 6);
        bonusPorNivel.put(22, 8);
        bonusPorNivel.put(24, 10);
        bonusPorNivel.put(26, 12);
        bonusPorNivel.put(28, 14);
        bonusPorNivel.put(30, 16);
        bonusPorNivel.put(32, 18);
        bonusPorNivel.put(34, 20);
        return bonusPorNivel;
    }

    public boolean ehMelhorado() {
        return raridade.equals("Melhorado");
    }

    public int recuperaQuantidadeMaximaRecursosEnergia(Context context) {
        if (ehProducaoDeRecursos()) return 0;
        if (profissao.equals(context.getString(R.string.stringProfissaoAneis))) {
            Map<Integer, Integer> bonusPorNivel = recuperaMapQuantidadeRecursosEnergiaPorNvelProfissaAneis();
            return bonusPorNivel.getOrDefault(nivel, 0);
        }
        if (profissao.equals(context.getString(R.string.stringProfissaoArmaLongoAlcance))) {
            Map<Integer, Integer> quantidadeRecursoEnergia = recuperaMapQuantidadeRecursosEnergiaPorNivelProfissaoArmaLongoAlcance();
            return quantidadeRecursoEnergia.getOrDefault(nivel, 0);
        }
        Map<Integer, Integer> quantidadeRecursoEnergia = recuperaMapQuantidadeRecursosEnergiaPorNivelProfissaoGeral();
        return quantidadeRecursoEnergia.getOrDefault(nivel, 0);
    }

    @NonNull
    private static Map<Integer, Integer> recuperaMapQuantidadeRecursosEnergiaPorNivelProfissaoGeral() {
        Map<Integer, Integer> quantidadeRecursoEnergia = new HashMap<>();
        quantidadeRecursoEnergia.put(10, 4);
        quantidadeRecursoEnergia.put(12, 6);
        quantidadeRecursoEnergia.put(14, 8);
        quantidadeRecursoEnergia.put(16, 10);
        quantidadeRecursoEnergia.put(18, 12);
        quantidadeRecursoEnergia.put(20, 14);
        quantidadeRecursoEnergia.put(22, 16);
        quantidadeRecursoEnergia.put(24, 18);
        quantidadeRecursoEnergia.put(26, 20);
        quantidadeRecursoEnergia.put(28, 22);
        quantidadeRecursoEnergia.put(30, 24);
        quantidadeRecursoEnergia.put(32, 26);
        quantidadeRecursoEnergia.put(34, 28);
        return quantidadeRecursoEnergia;
    }

    @NonNull
    private static Map<Integer, Integer> recuperaMapQuantidadeRecursosEnergiaPorNivelProfissaoArmaLongoAlcance() {
        Map<Integer, Integer> quantidadeRecursoEnergia = new HashMap<>();
        quantidadeRecursoEnergia.put(10, 6);
        quantidadeRecursoEnergia.put(12, 10);
        quantidadeRecursoEnergia.put(14, 14);
        quantidadeRecursoEnergia.put(16, 18);
        quantidadeRecursoEnergia.put(18, 22);
        quantidadeRecursoEnergia.put(20, 26);
        quantidadeRecursoEnergia.put(22, 30);
        quantidadeRecursoEnergia.put(24, 34);
        quantidadeRecursoEnergia.put(26, 38);
        quantidadeRecursoEnergia.put(28, 40);
        quantidadeRecursoEnergia.put(30, 42);
        quantidadeRecursoEnergia.put(32, 44);
        quantidadeRecursoEnergia.put(34, 46);
        return quantidadeRecursoEnergia;
    }

    @NonNull
    private static Map<Integer, Integer> recuperaMapQuantidadeRecursosEnergiaPorNvelProfissaAneis() {
        Map<Integer, Integer> quantidadeRecursoEnergia = new HashMap<>();
        quantidadeRecursoEnergia.put(10, 2);
        quantidadeRecursoEnergia.put(12, 3);
        quantidadeRecursoEnergia.put(14, 4);
        quantidadeRecursoEnergia.put(16, 5);
        quantidadeRecursoEnergia.put(18, 6);
        quantidadeRecursoEnergia.put(20, 7);
        quantidadeRecursoEnergia.put(22, 8);
        quantidadeRecursoEnergia.put(24, 10);
        quantidadeRecursoEnergia.put(26, 12);
        quantidadeRecursoEnergia.put(28, 14);
        quantidadeRecursoEnergia.put(30, 16);
        quantidadeRecursoEnergia.put(32, 18);
        quantidadeRecursoEnergia.put(34, 20);
        return quantidadeRecursoEnergia;
    }

    public boolean ehRaro() {
        return raridade.equals("Raro");
    }

    public int recuperaQuantidadeMaximaRecursosEtereo(Context context) {
        if (ehProducaoDeRecursos())return 0;
        if (profissao.equals(context.getString(R.string.stringProfissaoArmaLongoAlcance))) {
            Map<Integer, Integer> quantidadeRecursoEtereo = recuperaMapQuantidadeRecursosEtereoPorNivelProfissaoArmaLongoAlcance();
            return quantidadeRecursoEtereo.getOrDefault(nivel, 0);
        }
        Map<Integer, Integer> quantidadeRecursoEtereo = recuperaMapQuantidadeRecursosEtereoPorNivelProfissaoGeral();
        return quantidadeRecursoEtereo.getOrDefault(nivel, 0);
    }

    private Map<Integer, Integer> recuperaMapQuantidadeRecursosEtereoPorNivelProfissaoArmaLongoAlcance() {
        Map<Integer, Integer> quantidadeRecursoEtereo = new HashMap<>();
        quantidadeRecursoEtereo.put(10, 2);
        quantidadeRecursoEtereo.put(12, 6);
        quantidadeRecursoEtereo.put(14, 10);
        quantidadeRecursoEtereo.put(16, 14);
        quantidadeRecursoEtereo.put(18, 18);
        quantidadeRecursoEtereo.put(20, 22);
        quantidadeRecursoEtereo.put(22, 26);
        quantidadeRecursoEtereo.put(24, 30);
        quantidadeRecursoEtereo.put(26, 34);
        quantidadeRecursoEtereo.put(28, 36);
        quantidadeRecursoEtereo.put(30, 38);
        quantidadeRecursoEtereo.put(32, 40);
        quantidadeRecursoEtereo.put(34, 42);
        return quantidadeRecursoEtereo;
    }

    @NonNull
    private static Map<Integer, Integer> recuperaMapQuantidadeRecursosEtereoPorNivelProfissaoGeral() {
        Map<Integer, Integer> quantidadeRecursoEtereo = new HashMap<>();
        quantidadeRecursoEtereo.put(10, 2);
        quantidadeRecursoEtereo.put(12, 4);
        quantidadeRecursoEtereo.put(14, 6);
        quantidadeRecursoEtereo.put(16, 8);
        quantidadeRecursoEtereo.put(18, 10);
        quantidadeRecursoEtereo.put(20, 12);
        quantidadeRecursoEtereo.put(22, 14);
        quantidadeRecursoEtereo.put(24, 16);
        quantidadeRecursoEtereo.put(26, 18);
        quantidadeRecursoEtereo.put(28, 20);
        quantidadeRecursoEtereo.put(30, 22);
        quantidadeRecursoEtereo.put(32, 24);
        quantidadeRecursoEtereo.put(34, 26);
        return quantidadeRecursoEtereo;
    }

    public boolean ehAmuletos(Context context) {
        return profissao.equals(context.getString(R.string.stringProfissaoAmuletos));
    }

    public boolean ehAneis(Context context) {
        return profissao.equals(context.getString(R.string.stringProfissaoAneis));
    }

    public boolean ehCapotes(Context context) {
        return profissao.equals(context.getString(R.string.stringProfissaoCapotes));
    }

    public boolean ehBraceletes(Context context) {
        return profissao.equals(context.getString(R.string.stringProfissaoBraceletes));
    }

    public boolean ehLongoAlcance(Context context) {
        return profissao.equals(context.getString(R.string.stringProfissaoArmaLongoAlcance));
    }

    public boolean ehCorpoCorpo(Context context) {
        return profissao.equals(context.getString(R.string.stringProfissaoArmasCorpoCorpo));
    }

    public boolean ehArmaduraPesada(Context context) {
        return profissao.equals(context.getString(R.string.stringProfissaoArmaduraPesada));
    }

    public boolean ehArmaduraLeve(Context context) {
        return profissao.equals(context.getString(R.string.stringProfissaoArmaduraLeve));
    }

    public boolean ehArmaduraTecido(Context context) {
        return profissao.equals(context.getString(R.string.stringProfissaoArmaduraTecido));
    }
}