package com.kevin.gestorproducao.db;

import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.TABLE_PERSONAGENS;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static org.junit.Assert.assertEquals;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Regressão do achado C3: onUpgrade não pode mais apagar os dados do usuário.
 */
@RunWith(RobolectricTestRunner.class)
public class DbHelperTest {

    @Test
    public void deve_ManterDadosExistentes_QuandoBancoForAtualizado() {
        DbHelper dbHelper = new DbHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(COLUMN_NAME_ID, "personagem-teste");
        valores.put(COLUMN_NAME_NOME, "Personagem de Teste");
        db.insert(TABLE_PERSONAGENS, null, valores);

        dbHelper.onUpgrade(db, DbHelper.DATABASE_VERSION - 1, DbHelper.DATABASE_VERSION);

        try (Cursor cursor = db.rawQuery(
            "SELECT " + COLUMN_NAME_NOME + " FROM " + TABLE_PERSONAGENS +
                " WHERE " + COLUMN_NAME_ID + " = ?",
            new String[]{"personagem-teste"}
        )) {
            assertEquals(1, cursor.getCount());
            cursor.moveToFirst();
            assertEquals("Personagem de Teste", cursor.getString(0));
        }
    }
}
