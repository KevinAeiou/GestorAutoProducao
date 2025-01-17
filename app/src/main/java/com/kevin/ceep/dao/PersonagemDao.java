package com.kevin.ceep.dao;

import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ID;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_NOME;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.TABLE_PERSONAGENS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.firebase.auth.FirebaseAuth;
import com.kevin.ceep.db.DbHelper;
import com.kevin.ceep.model.Personagem;

import java.util.ArrayList;
import java.util.Objects;

public class PersonagemDao {
    private final String idUsuario;
    private String erro;
    private final SQLiteDatabase dbLeitura, dbModifica;

    public PersonagemDao(Context context) {
        this.idUsuario = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DbHelper dbHelper = new DbHelper(context);
        this.erro = null;
        this.dbLeitura = dbHelper.getReadableDatabase();
        this.dbModifica = dbHelper.getWritableDatabase();
    }
    public ArrayList<Personagem> pegaPersonagens() {
        String selection = "SELECT *  FROM Lista_personagem WHERE idUsuario == ?";
        String[] selectionArgs = {idUsuario};
        Cursor cursor = dbLeitura.rawQuery(
                selection,
                selectionArgs
        );
        ArrayList<Personagem> personagens = new ArrayList<>();
        while (cursor.moveToNext()) {
            boolean estado = cursor.getInt(5) == 1;
            boolean uso = cursor.getInt(6) == 1;
            Personagem personagem = new Personagem ();
            personagem.setId(cursor.getString(0));
            personagem.setNome(cursor.getString(2));
            personagem.setEmail(cursor.getString(3));
            personagem.setSenha(cursor.getString(4));
            personagem.setEstado(estado);
            personagem.setUso(uso);
            personagem.setEspacoProducao(cursor.getInt(7));
            personagens.add(personagem);
        }
        cursor.close();
        return personagens;
    }

    public boolean modificaNomePersonagem(Personagem personagemModificado) {
        dbModifica.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_NOME, personagemModificado.getNome());
            String selection = COLUMN_NAME_ID + " LIKE ?";
            String[] selectionArgs = {personagemModificado.getId()};
            dbModifica.update(TABLE_PERSONAGENS, values, selection, selectionArgs);
            dbModifica.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            this.erro = e.getMessage();
        } finally {
            dbModifica.endTransaction();
        }
        return false;
    }

    public String pegaErro() {
        return this.erro;
    }
}
