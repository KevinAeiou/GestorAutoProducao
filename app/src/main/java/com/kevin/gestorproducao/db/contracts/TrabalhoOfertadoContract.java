package com.kevin.gestorproducao.db.contracts;

import android.provider.BaseColumns;

public class TrabalhoOfertadoContract {
    private TrabalhoOfertadoContract() {}
    public static class TrabalhoOfertadoEntry implements BaseColumns {
        public static final String TABLE_TRABALHOS_OFERTADOS = "Lista_ofertados";
    }
}
