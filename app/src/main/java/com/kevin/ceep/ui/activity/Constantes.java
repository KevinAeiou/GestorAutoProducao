package com.kevin.ceep.ui.activity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface Constantes {

    String CHAVE_PERSONAGENS = "Personagens";
    String CHAVE_USUARIOS2 = "Usuarios2";
    String CHAVE_ESTOQUE = "Estoque";
    String CHAVE_PROFISSOES = "Profissoes";
    String CHAVE_PRODUCAO = "Producao";
    String CHAVE_VENDAS = "Vendas";
    String CHAVE_LISTA_PERSONAGEM = "Lista_personagem";
    String CHAVE_LISTA_ESTOQUE = "Lista_estoque";
    String CHAVE_LISTA_TRABALHO = "Lista_trabalhos";
    String CHAVE_LISTA_PROFISSOES = "Lista_profissoes";
    String CHAVE_ID_PERSONAGEM = "idPersonagem";
    String CHAVE_NOVO_TRABALHO = "Novo trabalho";
    String CHAVE_RECURSO = "Recursos";
    String CHAVE_LISTA_RECURSOS = "Lista_recursos";
    String CHAVE_LICENCA_INICIANTE = "Licença de Artesanato de Iniciante";
    int CODIGO_TRABALHO_PARA_PRODUZIR = 0;
    int CODIGO_TRABALHO_PRODUZINDO = 1;
    int CODIGO_TRABALHO_FEITO = 2;
    int CODIGO_REQUISICAO_INVALIDA = -1;
    int CODIGO_REQUISICAO_INSERE_TRABALHO = 1;
    int CODIGO_REQUISICAO_ALTERA_TRABALHO = 2;
    int CODIGO_REQUISICAO_INSERE_TRABALHO_PRODUCAO = 1;
    int CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO = 3;
    int CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE = 2;
    int CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS = 4;
    int CODIGO_REQUISICAO_ALTERA_VENDAS = 5;
    List<Integer> EXPERIENCIAS = Collections.unmodifiableList(Arrays.asList(
            20, 200, 540, 1250, 2550, 4700, 7990, 12770, 19440, 28440,
            40270, 55450, 74570, 98250, 127180, 156110, 185040, 215000,
            245000, 300000, 375000, 470000, 585000, 705000, 830000,
            996000, 1195000, 1195000
    ));
}
