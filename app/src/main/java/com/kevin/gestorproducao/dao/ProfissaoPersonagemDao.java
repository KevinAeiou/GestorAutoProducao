package com.kevin.gestorproducao.dao;

import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoEntry.TABLE_PROFISSOES;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoPersonagemEntry.COLUMN_NAME_EXPERIENCIA;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoPersonagemEntry.COLUMN_NAME_PRIORIDADE;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoPersonagemEntry.TABLE_PROFISSOES_PERSONAGEM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID_PERSONAGEM;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.kevin.gestorproducao.db.DbHelper;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;

import java.util.ArrayList;

public class ProfissaoPersonagemDao {
    private final SQLiteDatabase db;


    public ProfissaoPersonagemDao(Context context) {
        DbHelper dbHelper = DbHelper.getInstance(context);

        this.db = dbHelper.getWritableDatabase();
    }

    public ProfissaoPersonagem recuperaProfissaoPorNome(
        String idPersonagem,
        String nomeProfissao
    ) {
        String query =
            "SELECT pp." + COLUMN_NAME_EXPERIENCIA + ", " +
                "pp." + COLUMN_NAME_PRIORIDADE + ", " +
                "p." + COLUMN_NAME_NOME + ", " +
                "pp." + COLUMN_NAME_ID +
                " FROM " + TABLE_PROFISSOES_PERSONAGEM + " pp " +
                " INNER JOIN " + TABLE_PROFISSOES + " p " +
                " ON pp." + COLUMN_NAME_ID + " = p." + COLUMN_NAME_ID +
                " WHERE pp." + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
                " AND p." + COLUMN_NAME_NOME + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem, nomeProfissao})) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            ProfissaoPersonagem profissao = new ProfissaoPersonagem();
            profissao.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            profissao.setExperiencia(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)));
            profissao.setPrioridade(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_PRIORIDADE)) == 1);
            profissao.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));

            return profissao;
        }
    }

    public void substituirTodos(
        ArrayList<ProfissaoPersonagem> profissoes,
        String idPersonagem
    ) {
        db.beginTransaction();

        try {
            db.delete(
                TABLE_PROFISSOES_PERSONAGEM,
                COLUMN_NAME_ID_PERSONAGEM +"= ?",
                new String[]{idPersonagem}
            );

            for (ProfissaoPersonagem profissao : profissoes) {
                ContentValues values = getContentValues(profissao, idPersonagem);

                db.insert(TABLE_PROFISSOES_PERSONAGEM, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<ProfissaoPersonagem> recuperaProfissoes(String idPersonagem) {
        ArrayList<ProfissaoPersonagem> profissoes = new ArrayList<>();

        String query =
            "SELECT pp." + COLUMN_NAME_EXPERIENCIA + ", " +
                "pp." + COLUMN_NAME_PRIORIDADE + ", " +
                "p." + COLUMN_NAME_NOME + ", " +
                "pp." + COLUMN_NAME_ID +
                " FROM " + TABLE_PROFISSOES_PERSONAGEM + " pp " +
                " INNER JOIN " + TABLE_PROFISSOES + " p " +
                " ON pp." + COLUMN_NAME_ID + " = p." + COLUMN_NAME_ID +
                " WHERE pp." + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
                " ORDER BY pp." + COLUMN_NAME_EXPERIENCIA + " DESC, " +
                "p." + COLUMN_NAME_NOME + " ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem})) {
            while (cursor.moveToNext()) {
                ProfissaoPersonagem profissao = new ProfissaoPersonagem();

                profissao.setId(
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)
                    )
                );

                profissao.setExperiencia(
                    cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)
                    )
                );

                profissao.setPrioridade(
                    cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME_PRIORIDADE)
                    ) == 1
                );

                profissao.setNome(
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)
                    )
                );

                profissoes.add(profissao);
            }

        }

        return profissoes;
    }

    public ArrayList<ProfissaoPersonagem> recuperaProfissoesPriorizadas(String idPersonagem) {
        ArrayList<ProfissaoPersonagem> profissoes = new ArrayList<>();

        String query =
            "SELECT pp." + COLUMN_NAME_EXPERIENCIA + ", " +
                "pp." + COLUMN_NAME_PRIORIDADE + ", " +
                "p." + COLUMN_NAME_NOME + ", " +
                "pp." + COLUMN_NAME_ID + " " +
            "FROM " + TABLE_PROFISSOES_PERSONAGEM + " pp " +
            "INNER JOIN " + TABLE_PROFISSOES + " p " +
            "ON pp." + COLUMN_NAME_ID + " = p." + COLUMN_NAME_ID + " " +
            "WHERE pp." + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
            "AND pp." + COLUMN_NAME_PRIORIDADE + " = 1 " +
            "ORDER BY pp." + COLUMN_NAME_EXPERIENCIA + " DESC, p." + COLUMN_NAME_NOME + " ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem})) {
            while (cursor.moveToNext()) {
                ProfissaoPersonagem profissao = new ProfissaoPersonagem();

                profissao.setId(cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)
                ));

                profissao.setExperiencia(cursor.getInt(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)
                ));

                profissao.setPrioridade(cursor.getInt(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_PRIORIDADE)
                ) == 1);

                profissao.setNome(cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)
                ));

                profissoes.add(profissao);
            }
        }

        return profissoes;
    }

    public void modificaProfissao(
        ProfissaoPersonagem profissao,
        String idPersonagem
    ) {
        db.beginTransaction();

        try {
            ContentValues values = getContentValues(profissao, idPersonagem);

            String selection = COLUMN_NAME_ID + " = ? AND " + COLUMN_NAME_ID_PERSONAGEM + " = ?";
            String[] selectionArgs = {profissao.getId(), idPersonagem};

            db.update(
                TABLE_PROFISSOES_PERSONAGEM,
                values,
                selection,
                selectionArgs
            );

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @NonNull
    private static ContentValues getContentValues(
        ProfissaoPersonagem profissao,
        String idPersonagem
    ) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_ID, profissao.getId());
        values.put(COLUMN_NAME_ID_PERSONAGEM, idPersonagem);
        values.put(COLUMN_NAME_EXPERIENCIA, profissao.getExperiencia());
        values.put(COLUMN_NAME_PRIORIDADE, profissao.isPrioridade() ? 1 : 0);

        return values;
    }

    public void insereProfissoes(
        String idPersonagem,
        ArrayList<ProfissaoPersonagem> profissoes
    ) {
        db.beginTransaction();

        try {
            for (ProfissaoPersonagem profissao : profissoes) {
                ContentValues values = getContentValues(profissao, idPersonagem);

                db.insert(TABLE_PROFISSOES_PERSONAGEM, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void removeProfissoes(String idPersonagem) {
        db.beginTransaction();

        try {
            db.delete(
                TABLE_PROFISSOES_PERSONAGEM,
                COLUMN_NAME_ID_PERSONAGEM +"= ?",
                new String[]{idPersonagem}
            );

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
