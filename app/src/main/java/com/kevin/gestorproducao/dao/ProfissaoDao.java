package com.kevin.gestorproducao.dao;

import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoEntry.TABLE_PROFISSOES;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.kevin.gestorproducao.db.DbHelper;
import com.kevin.gestorproducao.model.ProfissaoBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfissaoDao extends BaseDao {

    public ProfissaoDao(Context context) {
        super(DbHelper.getInstance(context).getWritableDatabase());
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
        executaEmTransacao(() -> {
            db.delete(TABLE_PROFISSOES, null, null);

            for (ProfissaoBase profissao : profissoes) {
                ContentValues values = getContentValues(profissao);

                db.insert(TABLE_PROFISSOES, null, values);
            }
        });
    }

    @NonNull
    private static ContentValues getContentValues(ProfissaoBase profissao) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_ID, profissao.getId());
        values.put(COLUMN_NAME_NOME, profissao.getNome());

        return values;
    }

    public void modificaProfissao(ProfissaoBase profissao) {
        ContentValues values = getContentValues(profissao);

        String selection = COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {profissao.getId()};

        atualizaEmTransacao(TABLE_PROFISSOES, values, selection, selectionArgs);
    }

    public void insereProfissao(ProfissaoBase profissao) {
        ContentValues values = getContentValues(profissao);

        insereEmTransacao(TABLE_PROFISSOES, values);
    }

    public void removeProfissao(ProfissaoBase profissao) {
        String selection = COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {profissao.getId()};

        removeEmTransacao(TABLE_PROFISSOES, selection, selectionArgs);
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
