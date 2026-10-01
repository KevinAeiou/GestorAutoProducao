package com.kevin.gestorproducao.dao;

import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_PERSONAGEM;
import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_TRABALHO;
import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_QUANTIDADE;
import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.TABLE_ESTOQUE;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoEntry.TABLE_PROFISSOES;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NIVEL;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME_PRODUCAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_PROFISSAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_RARIDADE;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.TABLE_TRABALHOS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.kevin.gestorproducao.db.DbHelper;
import com.kevin.gestorproducao.model.TrabalhoEstoque;

import java.util.ArrayList;
import java.util.List;

public class EstoqueDao extends BaseDao {
    public EstoqueDao(Context context) {
        super(DbHelper.getInstance(context).getWritableDatabase());
    }

    public void substituirTodas(
        List<TrabalhoEstoque> estoque,
        String idPersonagem
    ) {
        executaEmTransacao(() -> {
            String selection = COLUMN_NAME_ID_PERSONAGEM + " LIKE ?";
            String[] selectionArgs = {idPersonagem};
            db.delete(TABLE_ESTOQUE, selection, selectionArgs);

            for (TrabalhoEstoque trabalho : estoque) {
                ContentValues values = getContentValues(idPersonagem, trabalho);

                db.insert(TABLE_ESTOQUE, null, values);
            }
        });
    }

    @NonNull
    private static ContentValues getContentValues(
        String idPersonagem,
        TrabalhoEstoque trabalho
    ) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_ID, trabalho.getId());
        values.put(COLUMN_NAME_ID_TRABALHO, trabalho.getIdTrabalho());
        values.put(COLUMN_NAME_ID_PERSONAGEM, idPersonagem);
        values.put(COLUMN_NAME_QUANTIDADE, trabalho.getQuantidade());

        return values;
    }

    public ArrayList<TrabalhoEstoque> recuperaEstoque(String idPersonagem) {
        ArrayList<TrabalhoEstoque> estoque = new ArrayList<>();

        String query =
            "SELECT e." + COLUMN_NAME_QUANTIDADE + ", " +
            "e." + COLUMN_NAME_ID + ", " +
            "t." + COLUMN_NAME_NOME + ", " +
            "e." + COLUMN_NAME_ID_TRABALHO + ", " +
            "t." + COLUMN_NAME_NIVEL + ", " +
            "t." + COLUMN_NAME_RARIDADE + ", " +
            "p." + COLUMN_NAME_NOME + " AS profissao_nome " +
            "FROM " + TABLE_ESTOQUE + " e " +
            "LEFT JOIN " + TABLE_TRABALHOS + " t ON e." + COLUMN_NAME_ID_TRABALHO +
            " = t." + COLUMN_NAME_ID + " " +
            "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
            " = p." + COLUMN_NAME_ID + " " +
            "WHERE " + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
            "ORDER BY " +
            "profissao_nome ASC, " +
            "t." + COLUMN_NAME_RARIDADE + " ASC, " +
            "t." + COLUMN_NAME_NIVEL + " ASC, " +
            "t." + COLUMN_NAME_NOME + " ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem})) {
            while (cursor.moveToNext()) {
                TrabalhoEstoque trabalho = new TrabalhoEstoque();

                trabalho.setId(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
                );
                trabalho.setIdTrabalho(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID_TRABALHO))
                );
                trabalho.setNome(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME))
                );
                trabalho.setNivel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL))
                );
                trabalho.setProfissao(
                    cursor.getString(cursor.getColumnIndexOrThrow("profissao_nome"))
                );
                trabalho.setRaridade(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE))
                );
                trabalho.setQuantidade(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_QUANTIDADE))
                );

                estoque.add(trabalho);
            }
        }

        return estoque;
    }

    // Estoque do personagem com o nome e o nomeProducao de cada trabalho, para filtros por nome.
    public ArrayList<TrabalhoEstoque> recuperaEstoqueComNomes(String idPersonagem) {
        ArrayList<TrabalhoEstoque> estoque = new ArrayList<>();

        String query =
            "SELECT e." + COLUMN_NAME_ID + ", e." + COLUMN_NAME_ID_TRABALHO + ", e." + COLUMN_NAME_QUANTIDADE + ", " +
            "t." + COLUMN_NAME_NOME + ", t." + COLUMN_NAME_NOME_PRODUCAO + " " +
            "FROM " + TABLE_ESTOQUE + " e " +
            "INNER JOIN " + TABLE_TRABALHOS + " t ON e." + COLUMN_NAME_ID_TRABALHO +
            " = t." + COLUMN_NAME_ID + " " +
            "WHERE e." + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
            "ORDER BY e." + COLUMN_NAME_ID + " ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem})) {
            while (cursor.moveToNext()) {
                TrabalhoEstoque trabalho = new TrabalhoEstoque();
                trabalho.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
                trabalho.setIdTrabalho(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID_TRABALHO)));
                trabalho.setQuantidade(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_QUANTIDADE)));
                trabalho.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
                trabalho.setNomeProducao(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO)));
                estoque.add(trabalho);
            }
        }

        return estoque;
    }

    public TrabalhoEstoque recuperaTrabalhoPorId(String idPersonagem, String idTrabalho) {
        String query =
            "SELECT " + COLUMN_NAME_ID  + ", " + COLUMN_NAME_QUANTIDADE + ", " + COLUMN_NAME_ID_TRABALHO + " " +
            "FROM " + TABLE_ESTOQUE + " " +
            "WHERE " + COLUMN_NAME_ID_PERSONAGEM + " = ?" +  " AND " + COLUMN_NAME_ID_TRABALHO + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem, idTrabalho})) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            TrabalhoEstoque trabalho = new TrabalhoEstoque();

            trabalho.setId(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
            );
            trabalho.setIdTrabalho(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID_TRABALHO))
            );
            trabalho.setQuantidade(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_QUANTIDADE))
            );

            return trabalho;
        }
    }

    public void modificaEstoque(TrabalhoEstoque trabalho, String idPersonagem) {
        ContentValues values = getContentValues(idPersonagem, trabalho);
        String selection = COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {trabalho.getId()};

        atualizaEmTransacao(TABLE_ESTOQUE, values, selection, selectionArgs);
    }

    public void insereEstoque(TrabalhoEstoque trabalho, String idPersonagem) {
        ContentValues values = getContentValues(idPersonagem, trabalho);

        insereEmTransacao(TABLE_ESTOQUE, values);
    }

    public void removeEstoque(TrabalhoEstoque trabalho) {
        String selection = COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {trabalho.getId()};

        removeEmTransacao(TABLE_ESTOQUE, selection, selectionArgs);
    }

    public void removeEstoques(String idPersonagem) {
        String selection = COLUMN_NAME_ID_PERSONAGEM + " LIKE ?";
        String[] selectionArgs = {idPersonagem};

        removeEmTransacao(TABLE_ESTOQUE, selection, selectionArgs);
    }
}
