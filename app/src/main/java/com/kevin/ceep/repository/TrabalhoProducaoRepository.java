package com.kevin.ceep.repository;

import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_EXPERIENCIA;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NIVEL;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME_PRODUCAO;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_PROFISSAO;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_RARIDADE;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_TRABALHO_NECESSARIO;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ESTADO;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID_PERSONAGEM;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID_TRABALHO;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_LICENCA;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_RECORRENCIA;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.TABLE_TRABALHOS_PRODUCAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.kevin.ceep.db.contracts.TrabalhoDbContract;
import com.kevin.ceep.model.TrabalhoProducao;

import java.util.ArrayList;
import java.util.Objects;

public class TrabalhoProducaoRepository {
    private static final String CHAVE_PRODUCAO = "Producao";
    private final DatabaseReference minhaReferenciaProducao;
    private final SQLiteDatabase dbLeitura, dbModificacao;
    private final String idPersonagem;
    public TrabalhoProducaoRepository(Context context, String idPersonagem) {
        this.minhaReferenciaProducao = FirebaseDatabase.getInstance().getReference(CHAVE_PRODUCAO)
            .child(idPersonagem);
        DbHelper dbHelper = DbHelper.getInstance(context);
        this.dbLeitura = dbHelper.getReadableDatabase();
        this.dbModificacao = dbHelper.getWritableDatabase();
        this.idPersonagem = idPersonagem;
    }

    public LiveData<Resource<Void>> modificaTrabalhoProducao(TrabalhoProducao trabalhoModificado) {
        Log.d("ciclo", "Modifica trabalho de produção no servidor");
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferenciaProducao.child(trabalhoModificado.getId()).setValue(trabalhoModificado).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("ciclo", "Trabalho de produção modificado no servidor com sucesso");
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_LICENCA, trabalhoModificado.getTipoLicenca());
                values.put(COLUMN_NAME_ESTADO, trabalhoModificado.getEstado());
                values.put(COLUMN_NAME_RECORRENCIA, trabalhoModificado.getRecorrencia());
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {trabalhoModificado.getId()};
                long newRowId = dbModificacao.update(TABLE_TRABALHOS_PRODUCAO, values, selection, selectionArgs);
                if (newRowId == -1) {
                    liveData.setValue(new Resource<>(null, "Erro ao modificar trabalho produção"));
                    Log.d("ciclo", "Erro ao modificar trabalho no banco");
                    return;
                }
                liveData.setValue(new Resource<>(null, null));
                Log.d("ciclo", "Trabalho de produção modificado no banco com sucesso");
                return;
            }
            if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).toString()));
            }
        });
        return liveData;
    }

    public LiveData<Resource<Void>> insereTrabalhoProducao(TrabalhoProducao novoTrabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferenciaProducao.child(novoTrabalho.getId()).setValue(novoTrabalho).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = defineConteudo(novoTrabalho);
                long newRowId = dbModificacao.insert(TABLE_TRABALHOS_PRODUCAO, null, values);
                if (newRowId == -1) {
                    liveData.setValue(new Resource<>(null, "Erro ao adicionar novo trabalho a lista"));
                    return;
                }
                liveData.setValue(new Resource<>(null, null));
                return;
            }
            if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).getMessage()));
            }
        });
        return liveData;
    }

    @NonNull
    private ContentValues defineConteudo(TrabalhoProducao novoTrabalho) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID, novoTrabalho.getId());
        values.put(COLUMN_NAME_ID_PERSONAGEM, idPersonagem);
        values.put(COLUMN_NAME_ID_TRABALHO, novoTrabalho.getIdTrabalho());
        values.put(COLUMN_NAME_ESTADO, novoTrabalho.getEstado());
        values.put(COLUMN_NAME_LICENCA, novoTrabalho.getTipoLicenca());
        values.put(COLUMN_NAME_RECORRENCIA, novoTrabalho.getRecorrencia());
        return values;
    }

    public LiveData<Resource<Void>> removeTrabalhoProducao(TrabalhoProducao trabalhoProducao) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferenciaProducao.child(trabalhoProducao.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {trabalhoProducao.getId()};
                long resultado = dbModificacao.delete(TABLE_TRABALHOS_PRODUCAO, selection, selectionArgs);
                if (resultado == -1) {
                    liveData.setValue(new Resource<>(null, "Erro ao remover trabalho da lista"));
                    return;
                }
                liveData.setValue(new Resource<>(null, null));
                return;
            }
            if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).getMessage()));
            }
        });
        return liveData;
    }

    public LiveData<Resource<ArrayList<TrabalhoProducao>>> pegaTodosTrabalhosProducao() {
        MutableLiveData<Resource<ArrayList<TrabalhoProducao>>> trabalhosProducaoEncontrados = new MutableLiveData<>();
        String sql = "SELECT " +
            "Lista_desejo.id, " +
            "trabalhos.id, " +
            "trabalhos.nome, " +
            "trabalhos.nomeProducao, " +
            "trabalhos.experiencia, " +
            "trabalhos.nivel, " +
            "trabalhos.profissao, " +
            "trabalhos.raridade, " +
            "trabalhos.trabalhoNecessario, " +
            "Lista_desejo.recorrencia, " +
            "Lista_desejo.tipo_licenca, " +
            "Lista_desejo.estado\n" +
            "FROM Lista_desejo\n" +
            "INNER JOIN trabalhos\n" +
            "ON Lista_desejo.idTrabalho == trabalhos.id\n" +
            "WHERE Lista_desejo.idPersonagem == ?" +
            "ORDER BY " +
            "trabalhos.profissao, " +
            "Lista_desejo.estado, " +
            "trabalhos.raridade, " +
            "trabalhos.nivel;";
        String[] selectionArgs = {idPersonagem};
        Cursor cursor = dbLeitura.rawQuery(sql, selectionArgs);
        ArrayList<TrabalhoProducao> trabalhosProducao = new ArrayList<>();
        while (cursor.moveToNext()) {
            TrabalhoProducao trabalhoProducao = new TrabalhoProducao();
            trabalhoProducao.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            trabalhoProducao.setIdTrabalho(cursor.getString(cursor.getColumnIndexOrThrow(TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID)));
            trabalhoProducao.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
            trabalhoProducao.setNomeProducao(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO)));
            trabalhoProducao.setExperiencia(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)));
            trabalhoProducao.setNivel(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL)));
            trabalhoProducao.setProfissao(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_PROFISSAO)));
            trabalhoProducao.setRaridade(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE)));
            trabalhoProducao.setTrabalhoNecessario(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TRABALHO_NECESSARIO)));
            trabalhoProducao.setRecorrencia(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_RECORRENCIA)) == 1);
            trabalhoProducao.setTipoLicenca(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LICENCA)));
            trabalhoProducao.setEstado(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ESTADO)));
            trabalhosProducao.add(trabalhoProducao);
        }
        cursor.close();
        trabalhosProducaoEncontrados.setValue(new Resource<>(trabalhosProducao, null));
        return trabalhosProducaoEncontrados;
    }

    public LiveData<Resource<Void>> sincronizaTrabalhosProducao() {
        ArrayList<TrabalhoProducao> trabalhosProducaoServidor = new ArrayList<>();
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferenciaProducao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trabalhosProducaoServidor.clear();
                for (DataSnapshot dn:snapshot.getChildren()) {
                    TrabalhoProducao trabalhoProducao = dn.getValue(TrabalhoProducao.class);
                    if (trabalhoProducao == null) continue;
                    trabalhosProducaoServidor.add(trabalhoProducao);
                    String selection = "SELECT " + COLUMN_NAME_ID +
                        " FROM "+ TABLE_TRABALHOS_PRODUCAO +
                        " WHERE "+ COLUMN_NAME_ID + " == ?";
                    String[] selectionArgs = {trabalhoProducao.getId()};
                    Cursor cursor = dbLeitura.rawQuery(
                        selection,
                        selectionArgs
                    );
                    ContentValues values = defineConteudo(trabalhoProducao);
                    if (cursor.getCount() == 0) {
                        dbModificacao.insert(TABLE_TRABALHOS_PRODUCAO, null, values);
                        continue;
                    }
                    cursor.close();
                    String selection2 = COLUMN_NAME_ID + " LIKE ?";
                    dbModificacao.update(TABLE_TRABALHOS_PRODUCAO, values, selection2, selectionArgs);
                }
                String selection =
                    "SELECT " + COLUMN_NAME_ID +
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
