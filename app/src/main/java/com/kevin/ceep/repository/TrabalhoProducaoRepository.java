package com.kevin.ceep.repository;

import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ESTADO;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID_PERSONAGEM;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID_TRABALHO;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_LICENCA;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_RECORRENCIA;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.TABLE_TRABALHOS_PRODUCAO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_LISTA_DESEJO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_LISTA_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_USUARIOS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.kevin.ceep.model.TrabalhoProducao;

import java.util.ArrayList;
import java.util.Objects;

public class TrabalhoProducaoRepository {
    private final DatabaseReference minhaReferenciaListaDeDesejos;
    private final SQLiteDatabase dbLeitura, dbModificacao;
    private final String idPersonagem;
    private final MutableLiveData<Resource<ArrayList<TrabalhoProducao>>> trabalhosProducaoEncontrados;

    public TrabalhoProducaoRepository(Context context, String personagemID) {
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.minhaReferenciaListaDeDesejos = FirebaseDatabase.getInstance().getReference(CHAVE_USUARIOS)
                .child(usuarioID).child(CHAVE_LISTA_PERSONAGEM)
                .child(personagemID).child(CHAVE_LISTA_DESEJO);
        DbHelper dbHelper = DbHelper.getInstance(context);
        this.dbLeitura = dbHelper.getReadableDatabase();
        this.dbModificacao = dbHelper.getWritableDatabase();
        this.idPersonagem = personagemID;
        this.trabalhosProducaoEncontrados = new MutableLiveData<>();
    }

    public LiveData<Resource<Void>> modificaTrabalhoProducao(TrabalhoProducao trabalhoModificado) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferenciaListaDeDesejos.child(trabalhoModificado.getId()).setValue(trabalhoModificado).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_NAME_LICENCA, trabalhoModificado.getTipo_licenca());
                        values.put(COLUMN_NAME_ESTADO, trabalhoModificado.getEstado());
                        values.put(COLUMN_NAME_RECORRENCIA, trabalhoModificado.getRecorrencia());
                        String selection = COLUMN_NAME_ID + " LIKE ?";
                        String[] selectionArgs = {trabalhoModificado.getId()};
                        long newRowId = dbModificacao.update(TABLE_TRABALHOS_PRODUCAO, values, selection, selectionArgs);
                        if (newRowId == -1) {
                            liveData.setValue(new Resource<>(null, "Erro ao modificar trabalho produção"));
                        } else {
                            liveData.setValue(new Resource<>(null, null));
                        }
                    } else if (task.isCanceled()) {
                        liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
                    }
                });
        return liveData;
    }

    public LiveData<Resource<Void>> insereTrabalhoProducao(TrabalhoProducao novoTrabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferenciaListaDeDesejos.child(novoTrabalho.getId())
                .setValue(novoTrabalho).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_NAME_ID, novoTrabalho.getId());
                        values.put(COLUMN_NAME_ID_PERSONAGEM, idPersonagem);
                        values.put(COLUMN_NAME_ID_TRABALHO, novoTrabalho.getIdTrabalho());
                        values.put(COLUMN_NAME_ESTADO, novoTrabalho.getEstado());
                        values.put(COLUMN_NAME_LICENCA, novoTrabalho.getTipo_licenca());
                        values.put(COLUMN_NAME_RECORRENCIA, novoTrabalho.getRecorrencia());
                        long newRowId = dbModificacao.insert(TABLE_TRABALHOS_PRODUCAO, null, values);
                        if (newRowId == -1) {
                            liveData.setValue(new Resource<>(null, "Erro ao adicionar novo trabalho a lista"));
                        } else {
                            liveData.setValue(new Resource<>(null, null));
                        }
                    } else if (task.isCanceled()) {
                        liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).getMessage()));
                    }
                });
        return liveData;
    }
    public LiveData<Resource<Void>> removeTrabalhoProducao(TrabalhoProducao trabalhoProducao) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferenciaListaDeDesejos.child(trabalhoProducao.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {trabalhoProducao.getId()};
                dbModificacao.delete(TABLE_TRABALHOS_PRODUCAO, selection, selectionArgs);
                liveData.setValue(new Resource<>(null, null));
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).getMessage()));
            }
        });
        return liveData;
    }

    public LiveData<Resource<ArrayList<TrabalhoProducao>>> pegaTodosTrabalhosProducao() {
        String sql = "SELECT Lista_desejo.id, trabalhos.id, trabalhos.nome, trabalhos.nomeProducao, trabalhos.experiencia, trabalhos.nivel, trabalhos.profissao, trabalhos.raridade, trabalhos.trabalhoNecessario, Lista_desejo.recorrencia, Lista_desejo.tipo_licenca, Lista_desejo.estado\n" +
                "FROM Lista_desejo\n" +
                "INNER JOIN trabalhos\n" +
                "ON Lista_desejo.idTrabalho == trabalhos.id\n" +
                "WHERE Lista_desejo.idPersonagem == ?" +
                "ORDER BY trabalhos.profissao, Lista_desejo.estado, trabalhos.raridade, trabalhos.nivel;";
        String[] selectionArgs = {idPersonagem};
        Cursor cursor = dbLeitura.rawQuery(
                sql,
                selectionArgs
        );
        ArrayList<TrabalhoProducao> trabalhosProducao = new ArrayList<>();
        while (cursor.moveToNext()) {
            TrabalhoProducao trabalhoProducao = new TrabalhoProducao();
            trabalhoProducao.setId(cursor.getString(0 ));
            trabalhoProducao.setIdTrabalho(cursor.getString(1));
            trabalhoProducao.setNome(cursor.getString(2));
            trabalhoProducao.setNomeProducao(cursor.getString(3));
            trabalhoProducao.setExperiencia(cursor.getInt(4));
            trabalhoProducao.setNivel(cursor.getInt(5));
            trabalhoProducao.setProfissao(cursor.getString(6));
            trabalhoProducao.setRaridade(cursor.getString(7));
            trabalhoProducao.setTrabalhoNecessario(cursor.getString(8));
            trabalhoProducao.setRecorrencia(cursor.getInt(9) == 1);
            trabalhoProducao.setTipo_licenca(cursor.getString(10));
            trabalhoProducao.setEstado(cursor.getInt(11));
            trabalhosProducao.add(trabalhoProducao);
        }
        cursor.close();
        trabalhosProducaoEncontrados.setValue(new Resource<>(trabalhosProducao, null));
        return trabalhosProducaoEncontrados;
    }

    public LiveData<Resource<Void>> sincronizaTrabalhosProducao() {
        ArrayList<TrabalhoProducao> trabalhosProducaoServidor = new ArrayList<>();
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferenciaListaDeDesejos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trabalhosProducaoServidor.clear();
                for (DataSnapshot dn:snapshot.getChildren()) {
                    TrabalhoProducao trabalhoProducao = dn.getValue(TrabalhoProducao.class);
                    trabalhosProducaoServidor.add(trabalhoProducao);
                    String selection = COLUMN_NAME_ID + " LIKE ?";
                    String[] selectionArgs = {Objects.requireNonNull(trabalhoProducao).getId()};
                    Cursor cursor = dbLeitura.query(
                            TABLE_TRABALHOS_PRODUCAO,
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
                    values.put(COLUMN_NAME_ID, trabalhoProducao.getId());
                    values.put(COLUMN_NAME_ID_PERSONAGEM, idPersonagem);
                    values.put(COLUMN_NAME_ID_TRABALHO, trabalhoProducao.getIdTrabalho());
                    values.put(COLUMN_NAME_ESTADO, trabalhoProducao.getEstado());
                    values.put(COLUMN_NAME_LICENCA, trabalhoProducao.getTipo_licenca());
                    values.put(COLUMN_NAME_RECORRENCIA, trabalhoProducao.getRecorrencia());
                    if (contadorLinhas == 0) {
                        dbModificacao.insert(TABLE_TRABALHOS_PRODUCAO, null, values);
                    } else if (contadorLinhas == 1) {
                        selection = COLUMN_NAME_ID + " LIKE ?";
                        selectionArgs = new String[]{trabalhoProducao.getId()};
                        dbModificacao.update(TABLE_TRABALHOS_PRODUCAO, values, selection, selectionArgs);
                    }
                }
                String selection = "SELECT " + COLUMN_NAME_ID +
                        " FROM "+ TABLE_TRABALHOS_PRODUCAO +
                        " WHERE "+ COLUMN_NAME_ID_PERSONAGEM + " == ?";
                String[] selectionArgs = {idPersonagem};
                Cursor cursor = dbLeitura.rawQuery(
                        selection,
                        selectionArgs
                );
                ArrayList<TrabalhoProducao> trabalhosProducaoBanco = new ArrayList<>();
                while (cursor.moveToNext()) {
                    TrabalhoProducao trabalhoProducao = new TrabalhoProducao();
                    trabalhoProducao.setId(cursor.getString(0));
                    trabalhosProducaoBanco.add(trabalhoProducao);
                }
                cursor.close();
                ArrayList<TrabalhoProducao> novaLista = new ArrayList<>();
                for (TrabalhoProducao trabalhoBanco : trabalhosProducaoBanco) {
                    for (TrabalhoProducao trabalhoServidor : trabalhosProducaoServidor) {
                        if (trabalhoBanco.getId().equals(trabalhoServidor.getId())) {
                            novaLista.add(trabalhoBanco);
                        }
                    }
                }
                trabalhosProducaoBanco.removeAll(novaLista);
                for (TrabalhoProducao trabalhoProducao : trabalhosProducaoBanco) {
                    String selection2 = COLUMN_NAME_ID + " LIKE ?";
                    String[] selectionArgs2 = {trabalhoProducao.getId()};
                    dbModificacao.delete(TABLE_TRABALHOS_PRODUCAO, selection2, selectionArgs2);
                }
                liveData.setValue(new Resource<>(null, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(new Resource<>(null, error.getMessage()));
            }
        });
        return liveData;
    }
}
