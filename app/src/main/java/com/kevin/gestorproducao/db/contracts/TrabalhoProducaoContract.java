package com.kevin.gestorproducao.db.contracts;

import android.provider.BaseColumns;

public class TrabalhoProducaoContract {
    private TrabalhoProducaoContract() {}
    public static class TrabalhoProducaoEntry implements BaseColumns {
        public static final String TABLE_TRABALHOS_PRODUCAO = "Lista_desejo";
        public static final String COLUMN_NAME_ID_PERSONAGEM = "idPersonagem";
        public static final String COLUMN_NAME_LICENCA = "tipo_licenca";
        public static final String COLUMN_NAME_ESTADO = "estado";
        public static final String COLUMN_NAME_RECORRENCIA = "recorrencia";
        public static final String COLUMN_NAME_INICIADO_EM = "iniciadoEm";
        public static final String COLUMN_NAME_FINALIZADO_EM = "finalizadoEm";
    }
}
