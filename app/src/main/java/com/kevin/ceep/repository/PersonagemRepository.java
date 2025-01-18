package com.kevin.ceep.repository;

import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_EMAIL;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ESPACO_PRODUCAO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ESTADO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ID;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ID_USUARIO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_NOME;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_SENHA;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_USO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.TABLE_PERSONAGENS;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_LISTA_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_USUARIOS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.ceep.db.DbHelper;
import com.kevin.ceep.model.Personagem;

import java.util.ArrayList;
import java.util.Objects;

public class PersonagemRepository {
    private final DatabaseReference minhaReferencia;
    private final String usuarioID;
    private final SQLiteDatabase dbModifica, dbLeitura;
    private final MutableLiveData<Resource<ArrayList<Personagem>>> personagensEncontrados;

    public PersonagemRepository(Context context) {
        this.usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.minhaReferencia = FirebaseDatabase.getInstance().getReference(CHAVE_USUARIOS)
                .child(usuarioID).child(CHAVE_LISTA_PERSONAGEM);
        DbHelper dbHelper = new DbHelper(context);
        this.dbModifica = dbHelper.getWritableDatabase();
        this.dbLeitura = dbHelper.getReadableDatabase();
        this.personagensEncontrados = new MutableLiveData<>();
    }

    public LiveData<Resource<Void>> sincronizaPersonagens() {
        ArrayList<Personagem> personagensServidor = new ArrayList<>();
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferencia.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                personagensServidor.clear();
                for (DataSnapshot dn:snapshot.getChildren()){
                    Personagem personagem = dn.getValue(Personagem.class);
                    if (personagem != null){
                        personagensServidor.add(personagem);
                        String selection = COLUMN_NAME_ID + " LIKE ?";
                        String[] selectionArgs = {personagem.getId()};
                        Cursor cursor = dbLeitura.query(
                                TABLE_PERSONAGENS,
                                null,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                null
                        );
                        int contadorLinhas = 0;
                        while(cursor.moveToNext()) {
                            contadorLinhas += 1;
                        }
                        cursor.close();
                        ContentValues values = new ContentValues();
                        if (contadorLinhas == 0) {
                            values.put(COLUMN_NAME_ID, personagem.getId());
                            values.put(COLUMN_NAME_ID_USUARIO, usuarioID);
                            values.put(COLUMN_NAME_NOME, personagem.getNome());
                            values.put(COLUMN_NAME_EMAIL, personagem.getEmail());
                            values.put(COLUMN_NAME_SENHA, personagem.getSenha());
                            values.put(COLUMN_NAME_ESTADO, personagem.getEstado());
                            values.put(COLUMN_NAME_USO, personagem.getUso());
                            values.put(COLUMN_NAME_ESPACO_PRODUCAO, personagem.getEspacoProducao());
                            dbModifica.insert(TABLE_PERSONAGENS, null, values);
                        } else if (contadorLinhas == 1) {
                            values.put(COLUMN_NAME_ID, personagem.getId());
                            values.put(COLUMN_NAME_ID_USUARIO, usuarioID);
                            values.put(COLUMN_NAME_NOME, personagem.getNome());
                            values.put(COLUMN_NAME_EMAIL, personagem.getEmail());
                            values.put(COLUMN_NAME_SENHA, personagem.getSenha());
                            values.put(COLUMN_NAME_ESTADO, personagem.getEstado());
                            values.put(COLUMN_NAME_USO, personagem.getUso());
                            values.put(COLUMN_NAME_ESPACO_PRODUCAO, personagem.getEspacoProducao());
                            selection = COLUMN_NAME_ID + " LIKE ?";
                            selectionArgs = new String[]{personagem.getId()};
                            dbModifica.update(TABLE_PERSONAGENS, values, selection, selectionArgs);
                        }
                    }
                    Cursor cursor = dbLeitura.query(
                            TABLE_PERSONAGENS,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );
                    ArrayList<Personagem> personagensBanco = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        Personagem personagem1 = new Personagem();
                        personagem1.setId(cursor.getString(0));
                        personagensBanco.add(personagem1);
                    }
                    cursor.close();
                    ArrayList<Personagem> novaLista = new ArrayList<>();
                    for (Personagem personagemBanco : personagensBanco) {
                        for (Personagem personagemServidor : personagensServidor) {
                            if (personagemBanco.getId().equals(personagemServidor.getId())) {
                                novaLista.add(personagemBanco);
                            }
                        }
                    }
                    personagensBanco.removeAll(novaLista);
                    for (Personagem personagem1 : personagensBanco) {
                        String selection = COLUMN_NAME_ID + " LIKE ?";
                        String[] selectionArgs = {personagem1.getId()};
                        dbModifica.delete(TABLE_PERSONAGENS, selection, selectionArgs);
                    }
                    liveData.setValue(new Resource<>(null, null));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return liveData;
    }
    public LiveData<Resource<ArrayList<Personagem>>> pegaTodosPersonagens() {
        String selection = "SELECT * " +
                " FROM " + TABLE_PERSONAGENS +
                " WHERE " + COLUMN_NAME_ID_USUARIO + " == ?";
        String[] selectionArgs = {usuarioID};
        Cursor cursor = dbLeitura.rawQuery(
                selection,
                selectionArgs
        );
        ArrayList<Personagem> personagens = new ArrayList<>();
        while (cursor.moveToNext()) {
            boolean estado = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ESTADO)) == 1;
            boolean uso = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_USO)) == 1;
            Personagem personagem = new Personagem ();
            personagem.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            personagem.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
            personagem.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_EMAIL)));
            personagem.setSenha(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_SENHA)));
            personagem.setEstado(estado);
            personagem.setUso(uso);
            personagem.setEspacoProducao(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ESPACO_PRODUCAO)));
            personagens.add(personagem);
        }
        cursor.close();
        personagensEncontrados.setValue(new Resource<>(personagens, null));
        return personagensEncontrados;
    }
    public LiveData<Resource<Void>> modificaPersonagem(Personagem personagemModificado) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferencia.child(personagemModificado.getId()).child(COLUMN_NAME_NOME).setValue(personagemModificado.getNome()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_NOME, personagemModificado.getNome());
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {personagemModificado.getId()};
                long newRowId = dbModifica.update(TABLE_PERSONAGENS, values, selection, selectionArgs);
                if (newRowId == -1) {
                    liveData.setValue(new Resource<>(null, "Erro ao modificar "+personagemModificado.getNome()+" no banco"));
                } else {
                    liveData.setValue(new Resource<>(null, null));
                }
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
            }
        });
        minhaReferencia.child(personagemModificado.getId()).child(COLUMN_NAME_EMAIL).setValue(personagemModificado.getEmail()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_EMAIL, personagemModificado.getEmail());
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {personagemModificado.getId()};
                long newRowId = dbModifica.update(TABLE_PERSONAGENS, values, selection, selectionArgs);
                if (newRowId == -1) {
                    liveData.setValue(new Resource<>(null, "Erro ao modificar "+personagemModificado.getNome()+" no banco"));
                } else {
                    liveData.setValue(new Resource<>(null, null));
                }
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
            }
        });
        minhaReferencia.child(personagemModificado.getId()).child(COLUMN_NAME_SENHA).setValue(personagemModificado.getSenha()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_SENHA, personagemModificado.getSenha());
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {personagemModificado.getId()};
                long newRowId = dbModifica.update(TABLE_PERSONAGENS, values, selection, selectionArgs);
                if (newRowId == -1) {
                    liveData.setValue(new Resource<>(null, "Erro ao modificar "+personagemModificado.getNome()+" no banco"));
                } else {
                    liveData.setValue(new Resource<>(null, null));
                }
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
            }
        });
        minhaReferencia.child(personagemModificado.getId()).child(COLUMN_NAME_ESTADO).setValue(personagemModificado.getEstado()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_ESTADO, personagemModificado.getEstado());
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {personagemModificado.getId()};
                long newRowId = dbModifica.update(TABLE_PERSONAGENS, values, selection, selectionArgs);
                if (newRowId == -1) {
                    Log.d("modificaPersonagem", "Erro ao modificar estado para: "+personagemModificado.getEstado());
                    liveData.setValue(new Resource<>(null, "Erro ao modificar "+personagemModificado.getNome()+" no banco"));
                } else {
                    Log.d("modificaPersonagem", "Estado modificado para: "+personagemModificado.getEstado());
                    liveData.setValue(new Resource<>(null, null));
                }
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
            }
        });
        minhaReferencia.child(personagemModificado.getId()).child(COLUMN_NAME_USO).setValue(personagemModificado.getUso()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_USO, personagemModificado.getUso());
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {personagemModificado.getId()};
                long newRowId = dbModifica.update(TABLE_PERSONAGENS, values, selection, selectionArgs);
                if (newRowId == -1) {
                    liveData.setValue(new Resource<>(null, "Erro ao modificar "+personagemModificado.getNome()+" no banco"));
                } else {
                    liveData.setValue(new Resource<>(null, null));
                }
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
            }
        });
        minhaReferencia.child(personagemModificado.getId()).child(COLUMN_NAME_ESPACO_PRODUCAO).setValue(personagemModificado.getEspacoProducao()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_ESPACO_PRODUCAO, personagemModificado.getEspacoProducao());
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {personagemModificado.getId()};
                long newRowId = dbModifica.update(TABLE_PERSONAGENS, values, selection, selectionArgs);
                if (newRowId == -1) {
                    liveData.setValue(new Resource<>(null, "Erro ao modificar "+personagemModificado.getNome()+" no banco"));
                } else {
                    liveData.setValue(new Resource<>(null, null));
                }
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
            }
        });
        return liveData;
    }

    public LiveData<Resource<Void>> inserePersonagem(Personagem novoPersonagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferencia.child(novoPersonagem.getId()).setValue(novoPersonagem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_ID, novoPersonagem.getId());
                values.put(COLUMN_NAME_ID_USUARIO, usuarioID);
                values.put(COLUMN_NAME_NOME, novoPersonagem.getNome());
                values.put(COLUMN_NAME_EMAIL, novoPersonagem.getEmail());
                values.put(COLUMN_NAME_SENHA, novoPersonagem.getSenha());
                values.put(COLUMN_NAME_ESTADO, novoPersonagem.getEstado());
                values.put(COLUMN_NAME_USO, novoPersonagem.getUso());
                values.put(COLUMN_NAME_ESPACO_PRODUCAO, novoPersonagem.getEspacoProducao());
                long newRowId = dbModifica.insert(TABLE_PERSONAGENS, null, values);
                if (newRowId == -1) {
                    liveData.setValue(new Resource<>(null, "Erro ao inserir "+novoPersonagem.getNome()+" no banco"));
                } else {
                    liveData.setValue(new Resource<>(null, null));
                }
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
            }
        });
        return liveData;
    }
}
