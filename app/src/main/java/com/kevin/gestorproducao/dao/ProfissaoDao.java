package com.kevin.gestorproducao.dao;

import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoEntry.TABLE_PROFISSOES;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.kevin.gestorproducao.db.DbHelper;
import com.kevin.gestorproducao.model.ProfissaoBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfissaoDao {
    private final SQLiteDatabase db;

    public ProfissaoDao(Context context) {
        DbHelper dbHelper = DbHelper.getInstance(context);

        this.db = dbHelper.getWritableDatabase();
    }

    public Map<String, String> recuperaMapaProfissoes() {
        Map<String, String> mapa = new HashMap<>();

        try (Cursor cursor = db.query(
            TABLE_PROFISSOES,
            new String[]{COLUMN_NAME_ID, COLUMN_NAME_NOME},
            null,null,null,null,null
        )) {
            while (cursor.moveToNext()) {
                mapa.put(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME))
                );
            }
        }

        return mapa;
    }

    public void substituirTodas(List<ProfissaoBase> profissoes) {
        db.beginTransaction();
        try {
            db.delete(TABLE_PROFISSOES, null, null);

            for (ProfissaoBase profissao : profissoes) {
                ContentValues values = getContentValues(profissao);

                db.insert(TABLE_PROFISSOES, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @NonNull
    private static ContentValues getContentValues(ProfissaoBase profissao) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_ID, profissao.getId());
        values.put(COLUMN_NAME_NOME, profissao.getNome());

        return values;
    }

    public void modificaProfissao(ProfissaoBase profissao) {
        db.beginTransaction();

        try {
            ContentValues values = getContentValues(profissao);

            String selection = COLUMN_NAME_ID + " = ?";
            String[] selectionArgs = {profissao.getId()};

            db.update(
                TABLE_PROFISSOES,
                values,
                selection,
                selectionArgs
            );

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    public void insereProfissao(ProfissaoBase profissao) {
        db.beginTransaction();

        try {
            ContentValues values = getContentValues(profissao);

            db.insert(
                TABLE_PROFISSOES,
                null,
                values
            );

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    public void removeProfissao(ProfissaoBase profissao) {
        db.beginTransaction();

        try {
            String selection = COLUMN_NAME_ID + " = ?";
            String[] selectionArgs = {profissao.getId()};

            db.delete(
                TABLE_PROFISSOES,
                selection,
                selectionArgs
            );

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<ProfissaoBase> recuperaProfissoesBase() {
        ArrayList<ProfissaoBase> profissoes = new ArrayList<>();

        try (Cursor cursor = db.query(
            TABLE_PROFISSOES,
            new String[]{COLUMN_NAME_ID, COLUMN_NAME_NOME},
            null,
            null,
            null,
            null,
            COLUMN_NAME_NOME + " ASC"
        )) {
            while (cursor.moveToNext()) {
                ProfissaoBase profissao = new ProfissaoBase();

                profissao.setId(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
                );

                profissao.setNome(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME))
                );

                profissoes.add(profissao);
            }
        }

        return profissoes;
    }
}
