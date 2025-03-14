package com.kevin.ceep.repository;

import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_AUTO_PRODUCAO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_EMAIL;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ESPACO_PRODUCAO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ESTADO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ID;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ID_USUARIO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_NOME;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_SENHA;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_USO;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.TABLE_PERSONAGENS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.ceep.dao.PersonagemDao;
import com.kevin.ceep.db.DbHelper;
import com.kevin.ceep.db.contracts.EstoqueDbContract;
import com.kevin.ceep.model.Personagem;

import java.util.ArrayList;
import java.util.Objects;

public class PersonagemRepository {
    private static final String CHAVE_PERSONAGENS = "Personagens";
    private static final String CHAVE_USUARIOS2 = "Usuarios2";
    private final DatabaseReference referenciaPersonagens;
    private final DatabaseReference referenciaUsuarios;
    private final String usuarioID;
    private final SQLiteDatabase dbModifica, dbLeitura;
    private final PersonagemDao personagemDao;
    private final MutableLiveData<Resource<ArrayList<Personagem>>> personagensEncontrados;

    public PersonagemRepository(Context context) {
        this.usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.referenciaPersonagens = FirebaseDatabase.getInstance().getReference(CHAVE_PERSONAGENS);
        this.referenciaUsuarios = FirebaseDatabase.getInstance().getReference(CHAVE_USUARIOS2).child(usuarioID).child(CHAVE_PERSONAGENS);
        DbHelper dbHelper = DbHelper.getInstance(context);
        this.dbModifica = dbHelper.getWritableDatabase();
        this.dbLeitura = dbHelper.getReadableDatabase();
        this.personagensEncontrados = new MutableLiveData<>();
        this.personagemDao = new PersonagemDao(context);
    }



    public LiveData<Resource<Void>> sincronizaPersonagens(ArrayList<Personagem> personagensServidor) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        for (Personagem personagem: personagensServidor) {
            String sql= "SELECT " + COLUMN_NAME_ID + " FROM " + TABLE_PERSONAGENS + " WHERE " + COLUMN_NAME_ID + " == ?";
            String[] argumentos= {personagem.getId()};
            Cursor cursor = dbLeitura.rawQuery(
                    sql,
                    argumentos
            );
            ContentValues values = defineValorPersonagem(personagem);
            Log.d("personagem", "Resultado consulta sql: "+cursor.getCount());
            if (cursor.getCount() == 0) {
                Log.d("personagem", "Personagem inserido: "+personagem.getNome());
                dbModifica.insert(TABLE_PERSONAGENS, null, values);
            } else if (cursor.getCount() == 1) {
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = new String[]{personagem.getId()};
                Log.d("personagem", "Personagem modificado: "+personagem.getNome());
                dbModifica.update(TABLE_PERSONAGENS, values, selection, selectionArgs);
            }
            cursor.close();
            removePersonagens(personagensServidor);
            liveData.setValue(new Resource<>(null, null));
        }
        return liveData;
    }

    private void removePersonagens(ArrayList<Personagem> personagensServidor) {
        String sql= "SELECT " + COLUMN_NAME_ID + " FROM " + TABLE_PERSONAGENS;
        Cursor cursor = dbLeitura.rawQuery(sql, null);
        ArrayList<Personagem> personagensBanco = new ArrayList<>();
        ArrayList<Personagem> novaLista = new ArrayList<>();
        while (cursor.moveToNext()) {
            Personagem personagem = new Personagem();
            personagem.setId(cursor.getString(0));
            personagensBanco.add(personagem);
            for (Personagem personagemServidor : personagensServidor) {
                if (personagem.getId().equals(personagemServidor.getId())) {
                    novaLista.add(personagem);
                }
            }
        }
        cursor.close();
        personagensBanco.removeAll(novaLista);
        for (Personagem personagem : personagensBanco) {
            String selection = COLUMN_NAME_ID + " LIKE ?";
            String[] selectionArgs = {personagem.getId()};
            Log.d("personagem", "Personagem removido: "+personagem.getNome());
            dbModifica.delete(TABLE_PERSONAGENS, selection, selectionArgs);
        }
    }

    @NonNull
    private ContentValues defineValorPersonagem(Personagem personagem) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID, personagem.getId());
        values.put(COLUMN_NAME_ID_USUARIO, usuarioID);
        values.put(COLUMN_NAME_NOME, personagem.getNome());
        values.put(COLUMN_NAME_EMAIL, personagem.getEmail());
        values.put(COLUMN_NAME_SENHA, personagem.getSenha());
        values.put(COLUMN_NAME_ESTADO, personagem.getEstado());
        values.put(COLUMN_NAME_USO, personagem.getUso());
        values.put(COLUMN_NAME_AUTO_PRODUCAO, personagem.isAutoProducao());
        values.put(COLUMN_NAME_ESPACO_PRODUCAO, personagem.getEspacoProducao());
        return values;
    }

    @NonNull
    public MutableLiveData<Resource<ArrayList<Personagem>>> pegaPersonagensServidor(ArrayList<String> idsPersonagens) {
        ArrayList<Personagem> personagensServidor = new ArrayList<>();
        MutableLiveData<Resource<ArrayList<Personagem>>> liveData = new MutableLiveData<>();
        referenciaPersonagens.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn: snapshot.getChildren()) {
                    Personagem personagem = dn.getValue(Personagem.class);
                    if (personagem == null) return;
                    boolean encontrado= idsPersonagens.stream().anyMatch(personagem.getId()::equals);
                    if (encontrado) personagensServidor.add(personagem);
                }
                liveData.setValue(new Resource<>(personagensServidor, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(new Resource<>(null, error.getMessage()));
            }
        });
        return liveData;
    }

    public LiveData<Resource<ArrayList<String>>> pegaIdsPersonagens() {
        MutableLiveData<Resource<ArrayList<String>>> livedata = new MutableLiveData<>();
        ArrayList<String> ids= new ArrayList<>();
        referenciaUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn: snapshot.getChildren()) {
                    Log.d("personagem", "ID PERSONAGEM: " + dn.getKey());
                    ids.add(dn.getKey());
                }
                livedata.setValue(new Resource<>(ids, null));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                livedata.setValue(new Resource<>(null, error.getMessage()));
            }
        });
        return livedata;
    }

    public LiveData<Resource<ArrayList<Personagem>>> pegaPersonagensBanco() {
        ArrayList<Personagem> personagens = personagemDao.pegaPersonagens();
        personagensEncontrados.setValue(new Resource<>(personagens, null));
        return personagensEncontrados;
    }
    public LiveData<Resource<Void>> modificaPersonagem(Personagem personagemModificado) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        referenciaPersonagens.child(personagemModificado.getId()).child(COLUMN_NAME_NOME).setValue(personagemModificado.getNome()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (personagemDao.modificaNomePersonagem(personagemModificado)) {
                    liveData.setValue(new Resource<>(null, null));
                } else {
                    liveData.setValue(new Resource<>(null, "Erro ao modificar "+personagemModificado.getNome()+" no banco: " + personagemDao.pegaErro()));
                }
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
            }
        });
        referenciaPersonagens.child(personagemModificado.getId()).child(COLUMN_NAME_EMAIL).setValue(personagemModificado.getEmail()).addOnCompleteListener(task -> {
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
        referenciaPersonagens.child(personagemModificado.getId()).child(COLUMN_NAME_SENHA).setValue(personagemModificado.getSenha()).addOnCompleteListener(task -> {
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
        referenciaPersonagens.child(personagemModificado.getId()).child(COLUMN_NAME_ESTADO).setValue(personagemModificado.getEstado()).addOnCompleteListener(task -> {
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
        referenciaPersonagens.child(personagemModificado.getId()).child(COLUMN_NAME_USO).setValue(personagemModificado.getUso()).addOnCompleteListener(task -> {
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
        referenciaPersonagens.child(personagemModificado.getId()).child(COLUMN_NAME_AUTO_PRODUCAO).setValue(personagemModificado.isAutoProducao()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_AUTO_PRODUCAO, personagemModificado.isAutoProducao());
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
        referenciaPersonagens.child(personagemModificado.getId()).child(COLUMN_NAME_ESPACO_PRODUCAO).setValue(personagemModificado.getEspacoProducao()).addOnCompleteListener(task -> {
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
        referenciaPersonagens.child(novoPersonagem.getId()).setValue(novoPersonagem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = defineValorPersonagem(novoPersonagem);
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

    public LiveData<Resource<Void>> removePersonagem(Personagem personagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        referenciaPersonagens.child(personagem.getId()).removeValue().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               String selection = EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID + " LIKE ?";
               String[] selectionArgs = {personagem.getId()};
               long linhaRemovida = dbModifica.delete(TABLE_PERSONAGENS, selection, selectionArgs);
               if (linhaRemovida == -1) {
                   liveData.setValue(new Resource<>(null, "Erro ao remover personagem"));
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
