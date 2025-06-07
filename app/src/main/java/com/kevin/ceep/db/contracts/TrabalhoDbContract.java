package com.kevin.ceep.db.contracts;

import android.provider.BaseColumns;

public class TrabalhoDbContract {
    private TrabalhoDbContract() {}

    public static class TrabalhoEntry implements BaseColumns {
        public static final String TABLE_TRABALHOS = "trabalhos";
        public static final String COLUMN_NAME_ID = "ID"; //0
        public static final String COLUMN_NAME_NOME = "nome"; //1
        public static final String COLUMN_NAME_NOME_PRODUCAO = "nomeProducao"; //2
        public static final String COLUMN_NAME_EXPERIENCIA = "experiencia"; //3
        public static final String COLUMN_NAME_NIVEL = "nivel"; //4
        public static final String COLUMN_NAME_PROFISSAO = "profissao"; //5
        public static final String COLUMN_NAME_RARIDADE = "raridade"; //6
        public static final String COLUMN_NAME_TRABALHO_NECESSARIO = "trabalhoNecessario"; //7
    }
}
