package com.kevin.ceep.repository;

import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_EXPERIENCIA;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NIVEL;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME_PRODUCAO;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_PROFISSAO;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_RARIDADE;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_TRABALHO_NECESSARIO;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.TABLE_TRABALHOS;
import static com.kevin.ceep.repository.TrabalhoProducaoRepository.destroyInstance;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_LISTA_TRABALHO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.ceep.db.DbHelper;
import com.kevin.ceep.model.Trabalho;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TrabalhoRepository {
    private final DatabaseReference referenciaTrabalho;
    private final SQLiteDatabase dbLeitura, dbModificacao;
    private final MutableLiveData<Resource<ArrayList<Trabalho>>> trabalhosEncontrados;
    private static volatile TrabalhoRepository instancia;
    private ValueEventListener ouvinteTrabalho;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);

    public TrabalhoRepository(Context context) {
        this.referenciaTrabalho = FirebaseDatabase.getInstance().getReference(CHAVE_LISTA_TRABALHO);
        DbHelper dbHelper = DbHelper.getInstance(context);
        this.dbLeitura = dbHelper.getReadableDatabase();
        this.dbModificacao = dbHelper.getWritableDatabase();
        this.trabalhosEncontrados = new MutableLiveData<>();
    }

    public static synchronized TrabalhoRepository getInstancia(Context context) {
        if (instancia == null) {
            destroyInstance();
            instancia = new TrabalhoRepository(context);
        }
        return instancia;
    }

    public LiveData<Resource<Void>> modificaTrabalho(Trabalho trabalhoModificado) {
        MutableLiveData<Resource<Void>> liveData = new  MutableLiveData<>();
        referenciaTrabalho.child(trabalhoModificado.getId()).setValue(trabalhoModificado).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                ContentValues values = defineTrabalhoModificado(trabalhoModificado);
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {trabalhoModificado.getId()};
                long newRowId = dbModificacao.update(TABLE_TRABALHOS, values, selection, selectionArgs);
                if (newRowId == -1) {
                    liveData.postValue(new Resource<>(null, "Erro ao adicionar novo trabalho a lista"));
                    return;
                }
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao modificar trabalho");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }

    @NonNull
    private static ContentValues defineTrabalhoModificado(Trabalho trabalhoModificado) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NOME, trabalhoModificado.getNome());
        values.put(COLUMN_NAME_NOME_PRODUCAO, trabalhoModificado.getNomeProducao());
        values.put(COLUMN_NAME_EXPERIENCIA, trabalhoModificado.getExperiencia());
        values.put(COLUMN_NAME_NIVEL, trabalhoModificado.getNivel());
        values.put(COLUMN_NAME_PROFISSAO, trabalhoModificado.getProfissao());
        values.put(COLUMN_NAME_RARIDADE, trabalhoModificado.getRaridade());
        values.put(COLUMN_NAME_TRABALHO_NECESSARIO, trabalhoModificado.getTrabalhoNecessario());
        return values;
    }

    public LiveData<Resource<Void>> insereTrabalho(Trabalho trabalho) {
        MutableLiveData<Resource<Void>> liveData = new  MutableLiveData<>();
        referenciaTrabalho.child(trabalho.getId()).setValue(trabalho).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                ContentValues values = defineNovoTrabalho(trabalho);
                long newRowId = dbModificacao.insert(TABLE_TRABALHOS, null, values);
                if (newRowId == -1) {
                    liveData.postValue(new Resource<>(null, "Erro ao adicionar novo trabalho a lista"));
                    return;
                }
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao inserir trabalho");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    @NonNull
    private static ContentValues defineNovoTrabalho(Trabalho trabalho) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID, trabalho.getId());
        values.put(COLUMN_NAME_NOME, trabalho.getNome());
        values.put(COLUMN_NAME_NOME_PRODUCAO, trabalho.getNomeProducao());
        values.put(COLUMN_NAME_EXPERIENCIA, trabalho.getExperiencia());
        values.put(COLUMN_NAME_NIVEL, trabalho.getNivel());
        values.put(COLUMN_NAME_PROFISSAO, trabalho.getProfissao());
        values.put(COLUMN_NAME_RARIDADE, trabalho.getRaridade());
        values.put(COLUMN_NAME_TRABALHO_NECESSARIO, trabalho.getTrabalhoNecessario());
        return values;
    }

    public LiveData<Resource<Void>> removeTrabalho(Trabalho trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        referenciaTrabalho.child(trabalho.getId()).removeValue().addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {trabalho.getId()};
                dbModificacao.delete(TABLE_TRABALHOS, selection, selectionArgs);
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao remover trabalho");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }
    public LiveData<Resource<ArrayList<Trabalho>>> recuperaTrabalhos() {
        Cursor cursor = dbLeitura.query(
                TABLE_TRABALHOS,
                null,
                null,
                null,
                null,
                null,
                null
            );
        ArrayList<Trabalho> trabalhos = new ArrayList<>();
        while(cursor.moveToNext()) {
            Trabalho trabalho = new Trabalho(
                    cursor.getString(1), //nome
                    cursor.getString(2), //nomeProducao
                    cursor.getString(5), //profissao
                    cursor.getString(6), //raridade
                    cursor.getString(7), //trabalhoNecessario
                    cursor.getInt(4), //nivel
                    cursor.getInt(3) //experiencia
            );
            trabalho.setId(cursor.getString(0));
            trabalhos.add(trabalho);
        }
        cursor.close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            trabalhos.sort(Comparator.comparing(Trabalho::getProfissao)
                    .thenComparing(Trabalho::getRaridade)
                    .thenComparing(Trabalho::getNivel)
                    .thenComparing(Trabalho::getNome));
        }
        trabalhosEncontrados.setValue(new Resource<>(trabalhos, null));
        return trabalhosEncontrados;
    }

    public LiveData<Resource<Void>> sincronizaTrabalhos() {
        ArrayList<Trabalho> trabalhosServidor = new ArrayList<>();
        MutableLiveData<Resource<Void>> liveData = new  MutableLiveData<>();
        ouvinteTrabalho = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trabalhosServidor.clear();
                for (DataSnapshot dn : snapshot.getChildren()) {
                    Trabalho trabalho = dn.getValue(Trabalho.class);
                    trabalhosServidor.add(trabalho);
                    String selection = COLUMN_NAME_ID + " LIKE ?";
                    String[] selectionArgs = {Objects.requireNonNull(trabalho).getId()};
                    Cursor cursor = dbLeitura.query(
                            TABLE_TRABALHOS,
                            null,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null
                    );
                    ContentValues values = defineTrabalhoModificado(trabalho);
                    if (cursor.getCount() == 0) {
                        values.put(COLUMN_NAME_ID, trabalho.getId());
                        dbModificacao.insert(TABLE_TRABALHOS, null, values);
                        Log.d("onDataChange", trabalho.getNome() + " inserido com sucesso!");
                    } else {
                        String selection2 = COLUMN_NAME_ID + " LIKE ?";
                        String[] selectionArgs2 = {trabalho.getId()};
                        dbModificacao.update(TABLE_TRABALHOS, values, selection2, selectionArgs2);
                    }
                    cursor.close();
                }
                Cursor cursor = dbLeitura.query(
                        TABLE_TRABALHOS,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                ArrayList<Trabalho> trabalhosBanco = new ArrayList<>();
                while (cursor.moveToNext()) {
                    Trabalho trabalho = new Trabalho(
                            cursor.getString(1), //nome
                            cursor.getString(2), //nomeProducao
                            cursor.getString(5), //profissao
                            cursor.getString(6), //raridade
                            cursor.getString(7), //trabalhoNecessario
                            cursor.getInt(4), //nivel
                            cursor.getInt(3) //experiencia
                    );
                    trabalho.setId(cursor.getString(0));
                    trabalhosBanco.add(trabalho);
                }
                cursor.close();
                ArrayList<Trabalho> novaLista = new ArrayList<>();
                for (Trabalho trabalhoBanco : trabalhosBanco) {
                    for (Trabalho trabalhoServidor : trabalhosServidor) {
                        if (trabalhoServidor.getId().equals(trabalhoBanco.getId())) {
                            novaLista.add(trabalhoBanco);
                        }
                    }
                }
                trabalhosBanco.removeAll(novaLista);
                for (Trabalho trabalhoBanco : trabalhosBanco) {
                    String selection = COLUMN_NAME_ID + " LIKE ?";
                    String[] selectionArgs = {trabalhoBanco.getId()};
                    dbModificacao.delete(TABLE_TRABALHOS, selection, selectionArgs);
                    Log.d("onDataChange", trabalhoBanco.getNome() + " removido com sucesso!");
                }
                liveData.setValue(new Resource<>(null, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaTrabalho.addListenerForSingleValueEvent(ouvinteTrabalho);
        return liveData;
    }

    public boolean trabalhoEspecificoExiste(Trabalho trabalho) {
        String selection = "SELECT " + COLUMN_NAME_ID +
                " FROM " + TABLE_TRABALHOS +
                " WHERE " + COLUMN_NAME_NOME + " == ?" +
                " AND " + COLUMN_NAME_NOME_PRODUCAO + " == ?" +
                " AND " + COLUMN_NAME_NIVEL + " == ?" +
                " AND " + COLUMN_NAME_EXPERIENCIA + " == ?" +
                " AND " + COLUMN_NAME_PROFISSAO + " == ?" +
                " AND " + COLUMN_NAME_RARIDADE + " == ?";
        String[] selectionArgs = {
                trabalho.getNome(),
                trabalho.getNomeProducao(),
                String.valueOf(trabalho.getNivel()),
                String.valueOf(trabalho.getExperiencia()),
                trabalho.getProfissao(),
                trabalho.getRaridade()};
        Cursor cursor = dbLeitura.rawQuery(
                selection,
                selectionArgs
        );
        boolean trabalhoEncontrado = cursor.getCount() == 1;
        cursor.close();
        return trabalhoEncontrado;
    }

    public LiveData<Resource<ArrayList<Trabalho>>> recuperaTrabalhosNecessarios(Trabalho trabalho) {
        MutableLiveData<Resource<ArrayList<Trabalho>>> liveData = new MutableLiveData<>();
        ArrayList<Trabalho> trabalhosEncontrados = new ArrayList<>();
        String selection = "SELECT *"+
                " FROM " + TABLE_TRABALHOS +
                " WHERE " + COLUMN_NAME_PROFISSAO + " == ?" +
                " AND " + COLUMN_NAME_NIVEL + " == ?" +
                " AND " + COLUMN_NAME_RARIDADE + " == ?";
        String[] selectionArgs = {trabalho.getProfissao(), String.valueOf(trabalho.getNivel()), trabalho.getRaridade()};
        Cursor cursor = dbLeitura.rawQuery(selection, selectionArgs);
        while (cursor.moveToNext()) {
            Trabalho trabalhoEncontrado = new Trabalho();
            trabalhoEncontrado.setId(cursor.getString(0));
            trabalhoEncontrado.setNome(cursor.getString(1));
            trabalhoEncontrado.setNomeProducao(cursor.getString(2));
            trabalhoEncontrado.setExperiencia(cursor.getInt(3));
            trabalhoEncontrado.setNivel(cursor.getInt(4));
            trabalhoEncontrado.setProfissao(cursor.getString(5));
            trabalhoEncontrado.setRaridade(cursor.getString(6));
            trabalhoEncontrado.setTrabalhoNecessario(cursor.getString(7));
            trabalhosEncontrados.add(trabalhoEncontrado);
        }
        cursor.close();
        liveData.setValue(new Resource<>(trabalhosEncontrados, null));
        return liveData;
    }

    public LiveData<Resource<Trabalho>> recuperaTrabalhoPorId(String id) {
        MutableLiveData<Resource<Trabalho>> liveData = new MutableLiveData<>();
        String selection = "SELECT *"+
                " FROM " + TABLE_TRABALHOS +
                " WHERE " + COLUMN_NAME_ID + " == ?" +
                " LIMIT 1";
        String[] selectionArgs = {id};
        Cursor cursor = dbLeitura.rawQuery(selection, selectionArgs);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Trabalho trabalhoEncontrado = new Trabalho();
            trabalhoEncontrado.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            trabalhoEncontrado.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
            trabalhoEncontrado.setNomeProducao(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO)));
            trabalhoEncontrado.setExperiencia(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)));
            trabalhoEncontrado.setNivel(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL)));
            trabalhoEncontrado.setProfissao(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_PROFISSAO)));
            trabalhoEncontrado.setRaridade(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE)));
            trabalhoEncontrado.setTrabalhoNecessario(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TRABALHO_NECESSARIO)));
            liveData.setValue(new Resource<>(trabalhoEncontrado, null));
            cursor.close();
            return liveData;
        }
        cursor.close();
        liveData.setValue(new Resource<>(null, "Não encontrado"));
        return liveData;
    }

    public void removeOuvinte() {
        if (referenciaTrabalho != null && ouvinteTrabalho != null) {
            referenciaTrabalho.removeEventListener(ouvinteTrabalho);
        }
    }
}
