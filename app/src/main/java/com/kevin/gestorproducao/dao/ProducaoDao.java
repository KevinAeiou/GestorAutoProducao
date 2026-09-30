package com.kevin.gestorproducao.dao;

import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_TRABALHO;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoEntry.TABLE_PROFISSOES;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_CRIADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_EXPERIENCIA;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_MODIFICADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NIVEL;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME_PRODUCAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_PROFISSAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_RARIDADE;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.TABLE_TRABALHOS;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ESTADO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_FINALIZADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID_PERSONAGEM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_INICIADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_LICENCA;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_RECORRENCIA;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.TABLE_TRABALHOS_PRODUCAO;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PARA_PRODUZIR;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PRODUZINDO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.kevin.gestorproducao.db.DbHelper;
import com.kevin.gestorproducao.model.TrabalhoProducao;

import java.util.ArrayList;
import java.util.List;

public class ProducaoDao extends BaseDao {
    public ProducaoDao(Context context) {
        super(DbHelper.getInstance(context).getWritableDatabase());
    }

    public TrabalhoProducao recuperaProducaoParaProduzirProduzindoPorId(
        String idPersonagem,
        String idTrabalho
    ) {
        String query =
            "SELECT " + COLUMN_NAME_ID + ", " + COLUMN_NAME_ID_TRABALHO + " " +
            "FROM " + TABLE_TRABALHOS_PRODUCAO + " " +
            "WHERE " + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
                "AND " + COLUMN_NAME_ID_TRABALHO + " = ? " +
                "AND (" +
                    COLUMN_NAME_ESTADO + " = 0 " +
                    "OR " + COLUMN_NAME_ESTADO + " = 1 " +
                ") " +
            "LIMIT 1";

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem, idTrabalho})) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            TrabalhoProducao trabalho = new TrabalhoProducao();

            trabalho.setId(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
            );
            trabalho.setIdTrabalho(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID_TRABALHO))
            );

            return trabalho;
        }
    }

    public void substituirTodas(
        ArrayList<TrabalhoProducao> producoes,
        String idPersonagem
    ) {
        executaEmTransacao(() -> {
            String selection = COLUMN_NAME_ID_PERSONAGEM + " LIKE ?";
            String[] selectionArgs = {idPersonagem};
            db.delete(TABLE_TRABALHOS_PRODUCAO, selection, selectionArgs);

            for (TrabalhoProducao trabalho : producoes) {
                ContentValues values = getContentValues(idPersonagem, trabalho);

                db.insert(TABLE_TRABALHOS_PRODUCAO, null, values);
            }
        });
    }

    @NonNull
    private static ContentValues getContentValues(String idPersonagem, TrabalhoProducao trabalho) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_ID, trabalho.getId());
        values.put(COLUMN_NAME_ID_TRABALHO, trabalho.getIdTrabalho());
        values.put(COLUMN_NAME_ID_PERSONAGEM, idPersonagem);
        values.put(COLUMN_NAME_LICENCA, trabalho.getTipoLicenca());
        values.put(COLUMN_NAME_ESTADO, trabalho.getEstado());
        values.put(COLUMN_NAME_RECORRENCIA, trabalho.getRecorrencia());
        values.put(COLUMN_NAME_INICIADO_EM, trabalho.getIniciadoEm());
        values.put(COLUMN_NAME_FINALIZADO_EM, trabalho.getFinalizadoEm());
        values.put(COLUMN_NAME_CRIADO_EM, trabalho.getCriadoEm());
        values.put(COLUMN_NAME_MODIFICADO_EM, trabalho.getModificadoEm());

        return values;
    }

    public ArrayList<TrabalhoProducao> recuperaProducoes(String idPersonagem) {
        ArrayList<TrabalhoProducao> producoes = new ArrayList<>();

        String query =
            "SELECT p." + COLUMN_NAME_ID + ", " +
            "p." + COLUMN_NAME_ID_TRABALHO + ", " +
            "p." + COLUMN_NAME_CRIADO_EM + ", " +
            "p." + COLUMN_NAME_MODIFICADO_EM + ", " +
            "p." + COLUMN_NAME_INICIADO_EM + ", " +
            "p." + COLUMN_NAME_FINALIZADO_EM + ", " +
            "t." + COLUMN_NAME_NOME + ", " +
            "t." + COLUMN_NAME_NOME_PRODUCAO + ", " +
            "t." + COLUMN_NAME_NIVEL + ", " +
            "t." + COLUMN_NAME_EXPERIENCIA + ", " +
            "t." + COLUMN_NAME_RARIDADE + ", " +
            "pf." + COLUMN_NAME_NOME + " AS profissao_nome, " +
            "p." + COLUMN_NAME_LICENCA + ", " +
            "p." + COLUMN_NAME_RECORRENCIA + ", " +
            "p." + COLUMN_NAME_ESTADO + " " +
            "FROM " + TABLE_TRABALHOS_PRODUCAO + " p " +
            "LEFT JOIN " + TABLE_TRABALHOS + " t ON p." + COLUMN_NAME_ID_TRABALHO +
            " = t." + COLUMN_NAME_ID + " " +
            "LEFT JOIN " + TABLE_PROFISSOES + " pf ON t." + COLUMN_NAME_PROFISSAO +
            " = pf." + COLUMN_NAME_ID + " " +
            "WHERE p." + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
            "ORDER BY " +
                "p." + COLUMN_NAME_ESTADO + ", "+
                "profissao_nome ASC, " +
                "t." + COLUMN_NAME_RARIDADE + " ASC, " +
                "t." + COLUMN_NAME_NIVEL + " ASC, " +
                "t." + COLUMN_NAME_NOME + " ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem})) {
            while (cursor.moveToNext()) {
                TrabalhoProducao trabalho = new TrabalhoProducao();

                trabalho.setId(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
                );
                trabalho.setIdTrabalho(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID_TRABALHO))
                );
                trabalho.setNome(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME))
                );
                trabalho.setNomeProducao(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO))
                );
                trabalho.setNivel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL))
                );
                trabalho.setExperiencia(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA))
                );
                trabalho.setRaridade(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE))
                );
                trabalho.setProfissao(
                    cursor.getString(cursor.getColumnIndexOrThrow("profissao_nome"))
                );
                trabalho.setTipoLicenca(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LICENCA))
                );
                trabalho.setRecorrencia(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_RECORRENCIA)) == 1
                );
                trabalho.setEstado(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ESTADO))
                );

                int indexCriado = cursor.getColumnIndexOrThrow(COLUMN_NAME_CRIADO_EM);
                if (!cursor.isNull(indexCriado)) {
                    trabalho.setCriadoEm(cursor.getLong(indexCriado));
                }

                int indexModificado = cursor.getColumnIndexOrThrow(COLUMN_NAME_MODIFICADO_EM);
                if (!cursor.isNull(indexModificado)) {
                    trabalho.setModificadoEm(cursor.getLong(indexModificado));
                }

                int indexIniciado = cursor.getColumnIndexOrThrow(COLUMN_NAME_INICIADO_EM);
                if (!cursor.isNull(indexIniciado)) {
                    trabalho.setIniciadoEm(cursor.getLong(indexIniciado));
                }

                int indexFinalizado = cursor.getColumnIndexOrThrow(COLUMN_NAME_FINALIZADO_EM);
                if (!cursor.isNull(indexFinalizado)) {
                    trabalho.setFinalizadoEm(cursor.getLong(indexFinalizado));
                }

                producoes.add(trabalho);
            }
        }

        return producoes;
    }

    public int recuperaQuantidadeProducaoParaProduzirPorId(
        String idPersonagem,
        String idTrabalho
    ) {
        String query =
            "SELECT COUNT(*) AS total " +
                "FROM " + TABLE_TRABALHOS_PRODUCAO + " " +
                "WHERE " + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
                "AND " + COLUMN_NAME_ID_TRABALHO + " = ? " +
                "AND " + COLUMN_NAME_ESTADO + " = " + CODIGO_TRABALHO_PARA_PRODUZIR;

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem, idTrabalho})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("total"));
            }
        }

        return 0;
    }

    public int recuperaQuantidadeProducaoProduzindoPorId(
        String idPersonagem,
        String idTrabalho
    ) {
        String query =
            "SELECT COUNT(*) AS total " +
                "FROM " + TABLE_TRABALHOS_PRODUCAO + " " +
                "WHERE " + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
                "AND " + COLUMN_NAME_ID_TRABALHO + " = ? " +
                "AND " + COLUMN_NAME_ESTADO + " = " + CODIGO_TRABALHO_PRODUZINDO;

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem, idTrabalho})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("total"));
            }
        }

        return 0;
    }

    public void insereProducao(TrabalhoProducao trabalho, String idPersonagem) {
        ContentValues values = getContentValues(idPersonagem, trabalho);

        insereEmTransacao(TABLE_TRABALHOS_PRODUCAO, values);
    }

    public void removeProducao(TrabalhoProducao trabalho) {
        String selection = COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {trabalho.getId()};

        removeEmTransacao(TABLE_TRABALHOS_PRODUCAO, selection, selectionArgs);
    }

    public void removeProducoesPorIds(List<String> ids) {
        if (ids.isEmpty()) return;

        executaEmTransacao(() -> {
            for (String id : ids) {
                db.delete(TABLE_TRABALHOS_PRODUCAO, COLUMN_NAME_ID + " = ?", new String[]{id});
            }
        });
    }

    public void modificaProducao(TrabalhoProducao trabalho, String idPersonagem) {
        ContentValues values = getContentValues(idPersonagem, trabalho);
        String selection = COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {trabalho.getId()};

        atualizaEmTransacao(TABLE_TRABALHOS_PRODUCAO, values, selection, selectionArgs);
    }

    public void removeProducoes(String idPersonagem) {
        String selection = COLUMN_NAME_ID_PERSONAGEM + " LIKE ?";
        String[] selectionArgs = {idPersonagem};

        removeEmTransacao(TABLE_TRABALHOS_PRODUCAO, selection, selectionArgs);
    }
}
