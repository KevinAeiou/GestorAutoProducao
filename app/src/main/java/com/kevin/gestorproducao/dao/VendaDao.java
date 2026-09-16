package com.kevin.gestorproducao.dao;

import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_PERSONAGEM;
import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_TRABALHO;
import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_QUANTIDADE;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoEntry.TABLE_PROFISSOES;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_CRIADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_EXPERIENCIA;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_MODIFICADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NIVEL;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_PROFISSAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_RARIDADE;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.TABLE_TRABALHOS;
import static com.kevin.gestorproducao.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_DESCRICAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_VALOR;
import static com.kevin.gestorproducao.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.TABLE_TRABALHOS_VENDIDOS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.kevin.gestorproducao.db.DbHelper;
import com.kevin.gestorproducao.model.TrabalhoVendido;

import java.util.ArrayList;

public class VendaDao extends BaseDao {
    public VendaDao(Context context) {
        super(DbHelper.getInstance(context).getWritableDatabase());
    }

    public void substituirTodos(ArrayList<TrabalhoVendido> vendas, String idPersonagem) {
        executaEmTransacao(() -> {
            db.delete(
                TABLE_TRABALHOS_VENDIDOS,
                COLUMN_NAME_ID_PERSONAGEM + " = ? ",
                new String[]{idPersonagem}
            );

            for (TrabalhoVendido venda : vendas) {
                ContentValues values = getContentValues(idPersonagem, venda);

                db.insert(TABLE_TRABALHOS_VENDIDOS, null, values);
            }
        });
    }

    @NonNull
    private static ContentValues getContentValues(String idPersonagem, TrabalhoVendido venda) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_ID, venda.getId());
        values.put(COLUMN_NAME_ID_PERSONAGEM, idPersonagem);
        values.put(COLUMN_NAME_ID_TRABALHO, venda.getIdTrabalho());
        values.put(COLUMN_NAME_DESCRICAO, venda.getDescricao());
        values.put(COLUMN_NAME_CRIADO_EM, venda.getCriadoEm());
        values.put(COLUMN_NAME_MODIFICADO_EM, venda.getModificadoEm());
        values.put(COLUMN_NAME_QUANTIDADE, venda.getQuantidade());
        values.put(COLUMN_NAME_VALOR, venda.getValor());
        return values;
    }

    public ArrayList<TrabalhoVendido> recuperaVendasPorTrabalho(
        String idPersonagem,
        String idTrabalho
    ) {
        ArrayList<TrabalhoVendido> vendas = new ArrayList<>();

        String query = "SELECT " +
            "v." + COLUMN_NAME_ID + ", " +
            "v." + COLUMN_NAME_ID_TRABALHO + ", " +
            "v." + COLUMN_NAME_DESCRICAO + ", " +
            "v." + COLUMN_NAME_CRIADO_EM + ", " +
            "v." + COLUMN_NAME_MODIFICADO_EM + ", " +
            "v." + COLUMN_NAME_QUANTIDADE + ", " +
            "v." + COLUMN_NAME_VALOR + ", " +
            "t." + COLUMN_NAME_NOME + ", " +
            "t." + COLUMN_NAME_NIVEL + ", " +
            "t." + COLUMN_NAME_RARIDADE + ", " +
            "p." + COLUMN_NAME_NOME + " AS profissao_nome " +
            "FROM " + TABLE_TRABALHOS_VENDIDOS + " v " +
            "LEFT JOIN " + TABLE_TRABALHOS + " t ON v." +
            COLUMN_NAME_ID_TRABALHO + " = t." + COLUMN_NAME_ID + " " +
            "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." +
            COLUMN_NAME_PROFISSAO + " = p." + COLUMN_NAME_ID + " " +
            "WHERE v." + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
            "AND v." + COLUMN_NAME_ID_TRABALHO + " = ? " +
            "ORDER BY " +
            "v." + COLUMN_NAME_CRIADO_EM + " DESC, " +
            "t." + COLUMN_NAME_NOME + " ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{idPersonagem, idTrabalho})) {
            if (cursor.moveToFirst()) {
                do {
                    TrabalhoVendido venda = new TrabalhoVendido();

                    venda.setId(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
                    );
                    venda.setIdTrabalho(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID_TRABALHO))
                    );
                    venda.setNome(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME))
                    );
                    venda.setNivel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL))
                    );
                    venda.setProfissao(
                        cursor.getString(cursor.getColumnIndexOrThrow("profissao_nome"))
                    );
                    venda.setRaridade(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE))
                    );
                    venda.setValor(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_VALOR))
                    );
                    venda.setQuantidade(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_QUANTIDADE))
                    );

                    int indexCriado = cursor.getColumnIndexOrThrow(COLUMN_NAME_CRIADO_EM);
                    if (!cursor.isNull(indexCriado)) {
                        venda.setCriadoEm(cursor.getLong(indexCriado));
                    }

                    int indexModificado = cursor.getColumnIndexOrThrow(COLUMN_NAME_MODIFICADO_EM);
                    if (!cursor.isNull(indexModificado)) {
                        venda.setModificadoEm(cursor.getLong(indexModificado));
                    }

                    venda.setDescricao(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DESCRICAO))
                    );

                    vendas.add(venda);
                } while (cursor.moveToNext());
            }
        }

        return vendas;
    }

    public ArrayList<TrabalhoVendido> recuperaMaisVendidos(
        String idPersonagem,
        Long dataInicio,
        Long dataFim
    ) {
        ArrayList<TrabalhoVendido> trabalhos = new ArrayList<>();

        StringBuilder query = new StringBuilder(
            "SELECT " +
                "t." + COLUMN_NAME_ID + ", " +
                "t." + COLUMN_NAME_NOME + ", " +
                "t." + COLUMN_NAME_NIVEL + ", " +
                "t." + COLUMN_NAME_EXPERIENCIA + ", " +
                "t." + COLUMN_NAME_RARIDADE + ", " +
                "SUM(v." + COLUMN_NAME_QUANTIDADE + ") AS total_quantidade, " +
                "SUM(v." + COLUMN_NAME_VALOR + ") AS total_valor, " +
                "p." + COLUMN_NAME_NOME + " AS profissao_nome " +
                "FROM " + TABLE_TRABALHOS_VENDIDOS + " v " +
                "INNER JOIN " + TABLE_TRABALHOS + " t ON v." +
                COLUMN_NAME_ID_TRABALHO + " = t." + COLUMN_NAME_ID + " " +
                "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
                " = p." + COLUMN_NAME_ID + " " +
                "WHERE v." + COLUMN_NAME_ID_PERSONAGEM + " = ? "
        );

        ArrayList<String> argumentos = new ArrayList<>();
        argumentos.add(idPersonagem);

        // v.criadoEm é a data em que a venda foi registrada — filtra as vendas agregadas por
        // período antes do GROUP BY, então "total_quantidade"/"total_valor" já refletem só o
        // que foi vendido dentro do intervalo selecionado.
        if (dataInicio != null && dataFim != null) {
            query.append("AND v.").append(COLUMN_NAME_CRIADO_EM).append(" BETWEEN ? AND ? ");
            argumentos.add(String.valueOf(dataInicio));
            argumentos.add(String.valueOf(dataFim));
        }

        query.append("GROUP BY t.").append(COLUMN_NAME_ID).append(" ")
            .append("ORDER BY total_quantidade DESC, t.").append(COLUMN_NAME_NOME).append(" ASC");

        try (Cursor cursor = db.rawQuery(query.toString(), argumentos.toArray(new String[0]))) {
            if (cursor.moveToFirst()) {
                do {
                    TrabalhoVendido trabalho = new TrabalhoVendido();

                    trabalho.setId(cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)
                    ));
                    trabalho.setNome(cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)
                    ));
                    trabalho.setNivel(cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL)
                    ));
                    trabalho.setExperiencia(cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)
                    ));
                    trabalho.setRaridade(cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE)
                    ));
                    trabalho.setQuantidade(cursor.getInt(
                        cursor.getColumnIndexOrThrow("total_quantidade")
                    ));
                    trabalho.setProfissao(cursor.getString(
                        cursor.getColumnIndexOrThrow("profissao_nome")
                    ));
                    trabalho.setValor(cursor.getInt(
                        cursor.getColumnIndexOrThrow("total_valor")
                    ));

                    trabalhos.add(trabalho);
                } while (cursor.moveToNext());
            }
        }

        return trabalhos;
    }

    public void removeTrabalho(TrabalhoVendido trabalho) {
        String selection = COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {trabalho.getId()};

        removeEmTransacao(TABLE_TRABALHOS_VENDIDOS, selection, selectionArgs);
    }

    public void modificaVenda(TrabalhoVendido trabalho, String idPersonagem) {
        ContentValues values = getContentValues(idPersonagem, trabalho);
        String selection = COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {trabalho.getId()};

        atualizaEmTransacao(TABLE_TRABALHOS_VENDIDOS, values, selection, selectionArgs);
    }

    public void insereVenda(TrabalhoVendido trabalho, String idPersonagem) {
        ContentValues values = getContentValues(idPersonagem, trabalho);

        insereEmTransacao(TABLE_TRABALHOS_VENDIDOS, values);
    }

    public void removeVendas(String idPersonagem) {
        String whereClause = COLUMN_NAME_ID_PERSONAGEM + " = ? ";
        String[] whereArgs = {idPersonagem};

        removeEmTransacao(TABLE_TRABALHOS_VENDIDOS, whereClause, whereArgs);
    }
}
