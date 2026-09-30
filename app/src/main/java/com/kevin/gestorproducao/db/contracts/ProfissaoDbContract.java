package com.kevin.gestorproducao.db.contracts;

import android.provider.BaseColumns;

public class ProfissaoDbContract {

    private ProfissaoDbContract() {}

    public static class ProfissaoEntry implements BaseColumns {
        public static final String TABLE_PROFISSOES = "profissoes";
    }

    public static class ProfissaoPersonagemEntry implements BaseColumns {
        public static final String TABLE_PROFISSOES_PERSONAGEM = "profissoes_personagem";
        public static final String COLUMN_NAME_EXPERIENCIA = "experiencia";
        public static final String COLUMN_NAME_PRIORIDADE = "prioridade";
    }
}
