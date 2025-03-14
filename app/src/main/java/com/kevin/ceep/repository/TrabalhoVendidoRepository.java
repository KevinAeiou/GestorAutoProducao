package com.kevin.ceep.repository;

import static com.kevin.ceep.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_QUANTIDADE;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_RARIDADE;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID_PERSONAGEM;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ID_TRABALHO;
import static com.kevin.ceep.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_DATA_VENDA;
import static com.kevin.ceep.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_DESCRICAO;
import static com.kevin.ceep.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_VALOR;
import static com.kevin.ceep.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.TABLE_TRABALHOS_VENDIDOS;

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
import com.kevin.ceep.model.TrabalhoVendido;

import java.util.ArrayList;

public class TrabalhoVendidoRepository {
    private static final String CHAVE_VENDAS = "Vendas";
    private final DatabaseReference referenciaVendas;
    private final SQLiteDatabase dbLeitura, dbModificacao;
    private final String idPersonagem;

    public TrabalhoVendidoRepository(Context context, String idPersonagem) {
        this.idPersonagem = idPersonagem;
        this.referenciaVendas = FirebaseDatabase.getInstance().getReference(CHAVE_VENDAS)
                .child(idPersonagem);
        DbHelper dbHelper = DbHelper.getInstance(context);
        this.dbLeitura = dbHelper.getReadableDatabase();
        this.dbModificacao = dbHelper.getWritableDatabase();
    }

    public LiveData<Resource<ArrayList<TrabalhoVendido>>> pegaTodosTrabalhosVendidos() {
        MutableLiveData<Resource<ArrayList<TrabalhoVendido>>> todosTrabalhosVendidosEncontrados = new MutableLiveData<>();
        String selection = "SELECT " +
                "Lista_vendas.id, " +
                "Lista_vendas.idPersonagem, " +
                "Lista_vendas.idTrabalho, " +
                "trabalhos.nome, " +
                "Lista_vendas.descricao, " +
                "Lista_vendas.dataVenda, " +
                "Lista_vendas.quantidade, " +
                "Lista_vendas.valor, " +
                "trabalhos.raridade\n" +
        "FROM Lista_vendas\n" +
        "LEFT JOIN trabalhos\n" +
        "ON Lista_vendas.idTrabalho == trabalhos.id\n" +
        "WHERE Lista_vendas.idPersonagem == ?\n" +
        "ORDER BY Lista_vendas.dataVenda DESC";
        String[] selectionArgs = {idPersonagem};
        Cursor cursor = dbLeitura.rawQuery(selection, selectionArgs);
        ArrayList<TrabalhoVendido> trabalhosVendidos = new ArrayList<>();
        while (cursor.moveToNext()) {
            TrabalhoVendido trabalho = new TrabalhoVendido();
            trabalho.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            trabalho.setIdPersonagem(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID_PERSONAGEM)));
            trabalho.setIdTrabalho(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID_TRABALHO)));
            trabalho.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
            trabalho.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DESCRICAO)));
            trabalho.setDataVenda(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DATA_VENDA)));
            trabalho.setQuantidade(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_QUANTIDADE)));
            trabalho.setValor(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_VALOR)));
            trabalho.setRaridade(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE)));
            trabalhosVendidos.add(trabalho);
        }
        cursor.close();
        todosTrabalhosVendidosEncontrados.setValue(new Resource<>(trabalhosVendidos, null));
        return todosTrabalhosVendidosEncontrados;
    }
    public LiveData<Resource<Void>> removeTrabalho(TrabalhoVendido trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        referenciaVendas.child(trabalho.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                liveData.setValue(new Resource<>(null, null));
            } else if (task.isCanceled()) {
                Exception exception = task.getException();
                if (exception != null) {
                    liveData.setValue(new Resource<>(null, exception.getMessage()));
                }
            }
        });
        return liveData;
    }

    public LiveData<Resource<Void>> sincronizaTrabalhos() {
        ArrayList<TrabalhoVendido> trabalhosVendidosServidor = new ArrayList<>();
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        referenciaVendas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trabalhosVendidosServidor.clear();
                for (DataSnapshot dn : snapshot.getChildren()) {
                    TrabalhoVendido trabalho = dn.getValue(TrabalhoVendido.class);
                    trabalhosVendidosServidor.add(trabalho);
                }
                for (TrabalhoVendido trabalho : trabalhosVendidosServidor) {
                    String selection = "SELECT " + COLUMN_NAME_ID +
                            " FROM " + TABLE_TRABALHOS_VENDIDOS +
                            " WHERE " + COLUMN_NAME_ID + " == ?" +
                            " LIMIT 1";
                    String[] selectionArgs = {trabalho.getId()};
                    Cursor cursor = dbLeitura.rawQuery(selection, selectionArgs);
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_NAME_ID_TRABALHO, trabalho.getIdTrabalho());
                    values.put(COLUMN_NAME_ID_PERSONAGEM, idPersonagem);
                    values.put(COLUMN_NAME_DESCRICAO, trabalho.getDescricao());
                    values.put(COLUMN_NAME_DATA_VENDA, trabalho.getDataVenda());
                    values.put(COLUMN_NAME_QUANTIDADE, trabalho.getQuantidade());
                    values.put(COLUMN_NAME_VALOR, trabalho.getValor());
                    if (cursor.getCount() == 0) {
                        values.put(COLUMN_NAME_ID, trabalho.getId());
                        dbModificacao.insert(TABLE_TRABALHOS_VENDIDOS, null, values);
                    } else {
                        String selection2 = COLUMN_NAME_ID + " LIKE ?";
                        String[] selectionArgs2 = new String[]{trabalho.getId()};
                        dbModificacao.update(TABLE_TRABALHOS_VENDIDOS, values, selection2, selectionArgs2);
                    }
                    cursor.close();
                }
                String selection = "SELECT " + COLUMN_NAME_ID +
                        " FROM " + TABLE_TRABALHOS_VENDIDOS +
                        " WHERE " + COLUMN_NAME_ID_PERSONAGEM + " == ?";
                String[] selectionArgs = {idPersonagem};
                Cursor cursor = dbLeitura.rawQuery(selection, selectionArgs);
                ArrayList<TrabalhoVendido> trabalhosVendidosBanco = new ArrayList<>();
                while (cursor.moveToNext()) {
                    TrabalhoVendido trabalho = new TrabalhoVendido();
                    trabalho.setId(cursor.getString(0));
                    trabalhosVendidosBanco.add(trabalho);
                }
                cursor.close();
                ArrayList<TrabalhoVendido> novaLista = new ArrayList<>();
                for (TrabalhoVendido trabalhoBanco : trabalhosVendidosBanco) {
                    for (TrabalhoVendido trabalhoServidor : trabalhosVendidosServidor) {
                        if (trabalhoBanco.getId().equals(trabalhoServidor.getId())) {
                            novaLista.add(trabalhoBanco);
                        }
                    }
                }
                trabalhosVendidosBanco.removeAll(novaLista);
                for (TrabalhoVendido trabalho : trabalhosVendidosBanco) {
                    String selection2 = COLUMN_NAME_ID + " LIKE ?";
                    String[] selectionArgs2 = {trabalho.getId()};
                    dbModificacao.delete(TABLE_TRABALHOS_VENDIDOS, selection2, selectionArgs2);
                    Log.d("modificaBanco", "Removido: " + trabalho.getId());
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

    public LiveData<Resource<Void>> modificaTrabalhoVendido(TrabalhoVendido trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        referenciaVendas.child(trabalho.getId()).setValue(trabalho).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_ID_PERSONAGEM, idPersonagem);
                values.put(COLUMN_NAME_ID_TRABALHO, trabalho.getIdTrabalho());
                values.put(COLUMN_NAME_DESCRICAO, trabalho.getDescricao());
                values.put(COLUMN_NAME_DATA_VENDA, trabalho.getDataVenda());
                values.put(COLUMN_NAME_QUANTIDADE, trabalho.getQuantidade());
                values.put(COLUMN_NAME_VALOR, trabalho.getValor());
                String selection = COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = {trabalho.getId()};
                dbModificacao.update(TABLE_TRABALHOS_VENDIDOS, values, selection, selectionArgs);
                liveData.setValue(new Resource<>(null, null));
            } else if (task.isCanceled()) {
                Exception exception = task.getException();
                if (exception != null) {
                    liveData.setValue(new Resource<>(null, exception.getMessage()));
                }
            }
        });
        return liveData;
    }
}
