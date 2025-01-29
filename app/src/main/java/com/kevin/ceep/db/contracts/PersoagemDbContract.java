package com.kevin.ceep.db.contracts;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_LISTA_PERSONAGEM;

import android.provider.BaseColumns;

public class PersoagemDbContract {
    private PersoagemDbContract() {}
    public static class PersonagemEntry implements BaseColumns {
        public static final String TABLE_PERSONAGENS = CHAVE_LISTA_PERSONAGEM;
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_ID_USUARIO = "idUsuario";
        public static final String COLUMN_NAME_NOME = "nome";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_SENHA = "senha";
        public static final String COLUMN_NAME_ESTADO = "estado";
        public static final String COLUMN_NAME_USO = "uso";
        public static final String COLUMN_NAME_AUTO_PRODUCAO = "autoProducao";
        public static final String COLUMN_NAME_ESPACO_PRODUCAO = "espacoProducao";
    }
}
