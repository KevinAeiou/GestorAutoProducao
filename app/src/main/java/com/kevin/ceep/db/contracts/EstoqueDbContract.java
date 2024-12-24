package com.kevin.ceep.db.contracts;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_LISTA_ESTOQUE;

import android.provider.BaseColumns;

public class EstoqueDbContract {
    public EstoqueDbContract() {}
    public static class EstoqueEntry implements BaseColumns {
        public static final String TABLE_ESTOQUE = CHAVE_LISTA_ESTOQUE;
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_ID_TRABALHO = "idTrabalho";
        public static final String COLUMN_NAME_ID_PERSONAGEM = "idPersonagem";
        public static final String COLUMN_NAME_QUANTIDADE = "quantidade";

    }
}
