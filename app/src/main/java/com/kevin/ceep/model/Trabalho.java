package com.kevin.ceep.model;

import static com.kevin.ceep.utilitario.Utilitario.geraIdAleatorio;
import static com.kevin.ceep.utilitario.Utilitario.limpaString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trabalho implements Serializable {

    private String id;
    private String nome;
    private String nomeProducao;
    private String profissao;
    private String raridade;
    private String trabalhoNecessario;
    private Integer nivel;
    private Integer experiencia;
    public Trabalho(){
        geraNovoId();
    }

    public Trabalho(String nome, String nomeProducao, String profissao, String raridade, String trabalhoNecessario, Integer nivel, Integer experiencia) {
        geraNovoId();
        this.nome = nome;
        this.nomeProducao = nomeProducao;
        this.profissao = profissao;
        this.raridade = raridade;
        this.trabalhoNecessario = trabalhoNecessario;
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

    public void setId(String novoId) {
        this.id = novoId;
    }

    public Integer getExperiencia() {
        return experiencia;
    }

    public String getTrabalhoNecessario() {
        return trabalhoNecessario;
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

    public void setTrabalhoNecessario(String trabalhoNecessario) {
        this.trabalhoNecessario = trabalhoNecessario;
    }

    public boolean possueTrabalhoNecessarioValido() {
        return this.trabalhoNecessario != null && !this.trabalhoNecessario.isEmpty();
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public void setExperiencia(Integer experiencia) {
        this.experiencia = experiencia;
    }
    public boolean ehProducaoDeRecursos() {
        ArrayList<String> listaProducaoRecursos = new ArrayList<>(List.of(
                "melhorarlicencacomum","licençadeproducaodoaprendiz","grandecolecaoderecursoscomuns",
                "grandecolecaoderecursosavancados","coletaemmassaderecursosavancados","melhoriadaessenciacomum",
                "melhoriadasubstanciacomum","melhoriadocatalizadorcomum","melhoriadaessenciacomposta",
                "melhoriadasubtanciacomposta","melhoriadocatalizadoramplificado","criaresferadoaprendiz",
                "produzindoavarinhademadeira","produzindocabecadocajadodejade","produzindocabecadecajadodeonix",
                "criaresferadoneofito","produzindoavarinhadeaço","extracaodelascas",
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
                "extracaodejadebruta","recebendoenergiainicial","adquirirpinçasdoprincipiante",
                "extracaodeonixextraordinaria","recebendoeterinicial","adquirirfuradordoaprendiz",
                "produzindotecidodelicado","extracaodesubstanciainstável","adquirirfuradordoprincipiante",
                "produzindotecidodenso","extracaodesubstanciaestável","recebendofibradebronze",
                "recebendoprata","recebendoinsigniadeestudante","recebendofibradeplatina",
                "recebendoambar","recebendodistintivodeaprendiz"
        ));
        return listaProducaoRecursos.contains(limpaString(nomeProducao));
    }

    public void geraNovoId() {
        this.id= geraIdAleatorio();
    }
}