package com.kevin.ceep.dao;

import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_AUTO_PRODUCAO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_EMAIL;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ESPACO_PRODUCAO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ESTADO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ID;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_NOME;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_SENHA;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_USO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.TABLE_PERSONAGENS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        DbHelper dbHelper = DbHelper.getInstance(context);
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
            boolean estado = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ESTADO)) == 1;
            boolean uso = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_USO)) == 1;
            boolean autoProducao = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_AUTO_PRODUCAO)) == 1;
            Personagem personagem = new Personagem ();
            personagem.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            personagem.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
            personagem.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_EMAIL)));
            personagem.setSenha(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_SENHA)));
            personagem.setEstado(estado);
            personagem.setUso(uso);
            personagem.setAutoProducao(autoProducao);
            personagem.setEspacoProducao(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ESPACO_PRODUCAO)));
            Log.d("personagem", "PERSONAGEM ENCONTRADO NO BANCO: " + personagem.getNome());
            personagens.add(personagem);
        }
        cursor.close();
        return personagens;
    }

    public boolean modificaNomePersonagem(Personagem personagem) {
        dbModifica.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_NOME, personagem.getNome());
            String selection = COLUMN_NAME_ID + " LIKE ?";
            String[] selectionArgs = {personagem.getId()};
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
