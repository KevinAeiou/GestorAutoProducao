package com.kevin.gestorproducao.db.contracts;

import android.provider.BaseColumns;

public class TrabalhoDbContract {
    private TrabalhoDbContract() {}

    public static class TrabalhoEntry implements BaseColumns {
        public static final String TABLE_TRABALHOS = "trabalhos";
        public static final String COLUMN_NAME_ID = "id"; //0
        public static final String COLUMN_NAME_NOME = "nome"; //1
        public static final String COLUMN_NAME_NOME_PRODUCAO = "nomeProducao"; //2
        public static final String COLUMN_NAME_EXPERIENCIA = "experiencia"; //3
        public static final String COLUMN_NAME_NIVEL = "nivel"; //4
        public static final String COLUMN_NAME_PROFISSAO = "profissao"; //5
        public static final String COLUMN_NAME_RARIDADE = "raridade"; //6
        public static final String COLUMN_NAME_TRABALHO_NECESSARIO = "trabalhoNecessario"; //7
        public static final String COLUMN_NAME_CRIADO_EM = "criadoEm";
        public static final String COLUMN_NAME_MODIFICADO_EM = "modificadoEm";
    }

    public static class TrabalhoNecessarioEntry implements BaseColumns {
        public static final String TABLE_TRABALHOS_NECESSARIOS = "trabalhos_necessarios";
        public static final String COLUMN_TRABALHO_NECESSARIO_ID = "trabalhoNecessarioId";
    }
}
