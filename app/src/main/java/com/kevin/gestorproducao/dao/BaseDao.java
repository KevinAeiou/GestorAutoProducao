package com.kevin.gestorproducao.dao;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

// Achado M6: 7 DAOs repetiam o mesmo boilerplate de transação (beginTransaction/try/
// setTransactionSuccessful/finally/endTransaction) em cada inserir/atualizar/remover.
// Centralizado aqui — a lógica de cada operação continua na DAO específica, só a
// transação em volta dela é compartilhada.
public abstract class BaseDao {
    protected final SQLiteDatabase db;

    protected BaseDao(SQLiteDatabase db) {
        this.db = db;
    }

    protected void executaEmTransacao(Runnable operacao) {
        db.beginTransaction();

        try {
            operacao.run();

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    protected void insereEmTransacao(String tabela, ContentValues values) {
        executaEmTransacao(() -> db.insert(tabela, null, values));
    }

    protected void atualizaEmTransacao(
        String tabela,
        ContentValues values,
        String selection,
        String[] selectionArgs
    ) {
        executaEmTransacao(() -> db.update(tabela, values, selection, selectionArgs));
    }

    protected void removeEmTransacao(String tabela, String selection, String[] selectionArgs) {
        executaEmTransacao(() -> db.delete(tabela, selection, selectionArgs));
    }
}
