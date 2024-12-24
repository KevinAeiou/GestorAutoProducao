package com.kevin.ceep.db.contracts;

import android.provider.BaseColumns;

public class TrabalhoProducaoContract {
    private TrabalhoProducaoContract() {}
    public static class TrabalhoProducaoEntry implements BaseColumns {
        public static final String TABLE_TRABALHOS_PRODUCAO = "Lista_desejo";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_ID_TRABALHO = "idTrabalho";
        public static final String COLUMN_NAME_ID_PERSONAGEM = "idPersonagem";
        public static final String COLUMN_NAME_LICENCA = "tipo_licenca";
        public static final String COLUMN_NAME_ESTADO = "estado";
        public static final String COLUMN_NAME_RECORRENCIA = "recorrencia";
    }
}
