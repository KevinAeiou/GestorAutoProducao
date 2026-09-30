package com.kevin.gestorproducao.db.contracts;

import android.provider.BaseColumns;

public class TrabalhoVendidoContract {
    private  TrabalhoVendidoContract() {}
    public static class TrabalhoVendidoEntry implements BaseColumns {
        public static final String TABLE_TRABALHOS_VENDIDOS = "Lista_vendas";
        public static final String COLUMN_NAME_DESCRICAO = "descricao";
        public static final String COLUMN_NAME_VALOR = "valor";
    }
}
