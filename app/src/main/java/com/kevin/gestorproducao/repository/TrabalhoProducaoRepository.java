package com.kevin.gestorproducao.repository;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_PRODUCAO;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.gestorproducao.dao.ProducaoDao;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.helper.FirebaseTimeoutHelper;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class  TrabalhoProducaoRepository {
    private final DatabaseReference referenciaProducao;
    private ValueEventListener ouvinteProducaoIdPersonagem, ouvinteProducao;
    private final MutableLiveData<Resource<ArrayList<TrabalhoProducao>>> producoesEncontradas;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);
    private static volatile TrabalhoProducaoRepository instancia;
    private final ProducaoDao producaoDao;

    public TrabalhoProducaoRepository(Context context) {
        this.referenciaProducao = FirebaseDatabase.getInstance().getReference(CHAVE_PRODUCAO);
        this.producaoDao = new ProducaoDao(context);
        this.producoesEncontradas = new MutableLiveData<>();
    }

    public static synchronized TrabalhoProducaoRepository getInstance(
        Context context
    ) {
        if (instancia == null) {
            instancia = new TrabalhoProducaoRepository(context);
        }

        return instancia;
    }

    public static synchronized void destroyInstance() {
        if (instancia != null) {
            instancia = null;
        }
    }

    public LiveData<Resource<Void>> modificaTrabalhoProducao(
        TrabalhoProducao trabalho,
        String idPersonagem
    ) {
        if (producaoInvalida(trabalho)) {
            MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
            liveData.postValue(new Resource<>(null, "Produção inválida"));
            return liveData;
        }

        return FirebaseTimeoutHelper.execute(callback -> referenciaProducao
            .child(idPersonagem)
            .child(trabalho.getId())
            .setValue(trabalho)
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        producaoDao.modificaProducao(trabalho, idPersonagem);
                        callback.sucesso();

                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }

                callback.erro(
                    recuperaErro(
                        task.getException(),
                        "Erro desconhecido ao modificar produção"
                    )
                );
            })
        );
    }

    public LiveData<Resource<Void>> insereTrabalhoProducao(
        TrabalhoProducao trabalho,
        String idPersonagem
    ) {
        if (idTrabalhoInvalido(trabalho) || producaoInvalida(trabalho)) {
            MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
            liveData.postValue(new Resource<>(null, "Produção inválida"));
            return liveData;
        }

        return FirebaseTimeoutHelper.execute(callback -> referenciaProducao
            .child(idPersonagem)
            .child(trabalho.getId())
            .setValue(trabalho)
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        producaoDao.insereProducao(trabalho, idPersonagem);
                        callback.sucesso();

                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }

                    return;
                }

                callback.erro(
                    recuperaErro(
                        task.getException(),
                        "Erro desconhecido ao inserir produção"
                    )
                );
            })
        );
    }

    public LiveData<Resource<Void>> removeTrabalhoProducao(
        TrabalhoProducao trabalho,
        String idPersonagem
    ) {
        if (idTrabalhoInvalido(trabalho)) {
            MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
            liveData.postValue(new Resource<>(null, "Id produção inválido"));

            return liveData;
        }

        return FirebaseTimeoutHelper.execute(callback -> referenciaProducao
            .child(idPersonagem)
            .child(trabalho.getId())
            .removeValue()
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        producaoDao.removeProducao(trabalho);
                        callback.sucesso();

                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }

                callback.erro(
                    recuperaErro(
                        task.getException(),
                        "Erro desconhecido ao remover produção"
                    )
                );
            })
        );
    }

    public void removeReferenciaTrabalhoEspecifico(String idTrabalho) {
        ouvinteProducao = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot producaoSnapshot: snapshot.getChildren()) {
                    for (DataSnapshot registroSnapshot : producaoSnapshot.getChildren()) {

                        String idTrabalhoFirebase = registroSnapshot.child("idTrabalho").getValue(String.class);

                        if (idTrabalhoFirebase != null && idTrabalhoFirebase.equals(idTrabalho)) {
                            registroSnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("repositorioProducao", "Erro: " + error.getMessage());
            }
        };

        referenciaProducao.addListenerForSingleValueEvent(ouvinteProducao);
    }

    public LiveData<Resource<ArrayList<TrabalhoProducao>>> recuperaProducoes(String idPersonagem) {
        backgroundExecutor.execute(() -> {
            try {
                ArrayList<TrabalhoProducao> producoes = new ArrayList<>(
                    producaoDao.recuperaProducoes(idPersonagem)
                );
                producoesEncontradas.postValue(new Resource<>(producoes, null));
            } catch (RuntimeException e) {
                producoesEncontradas.postValue(new Resource<>(null, e.getMessage()));
            }
        });

        return producoesEncontradas;
    }

    public void removeObservador() {
        if (referenciaProducao != null && ouvinteProducaoIdPersonagem != null) {
            referenciaProducao.removeEventListener(ouvinteProducaoIdPersonagem);
        }
        if (referenciaProducao != null && ouvinteProducao != null) {
            referenciaProducao.removeEventListener(ouvinteProducao);
        }
    }

    public TrabalhoProducao recuperaProducaoParaProduzirProduzindoPorId(
        String idPersonagem,
        String idTrabalho
    ) {
        return producaoDao.recuperaProducaoParaProduzirProduzindoPorId(idPersonagem, idTrabalho);
    }

    public LiveData<Resource<Void>> sincronizaProducao(@Nullable String idPersonagem) {
        ArrayList<TrabalhoProducao> producoes = new ArrayList<>();
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        ouvinteProducaoIdPersonagem = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                producoes.clear();

                for (DataSnapshot dn : snapshot.getChildren()) {
                    TrabalhoProducao trabalho = dn.getValue(TrabalhoProducao.class);
                    if (trabalho != null) {
                        producoes.add(trabalho);
                    }
                }

                backgroundExecutor.execute(() -> {
                    try {
                        producaoDao.substituirTodas(producoes, idPersonagem);

                        liveData.postValue(new Resource<>(null, null));
                    } catch (Exception e) {
                        liveData.postValue(new Resource<>(null, e.getMessage()));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(new Resource<>(null, error.getMessage()));
            }
        };

        if (idPersonagem == null || idPersonagem.isEmpty()) return liveData;

        referenciaProducao.child(idPersonagem).addListenerForSingleValueEvent(ouvinteProducaoIdPersonagem);
        return liveData;
    }

    public int recuperaQuantidadeProducaoParaProduzirPorId(
        String idPersonagem,
        String idTrabalho
    ) {
        return producaoDao.recuperaQuantidadeProducaoParaProduzirPorId(idPersonagem, idTrabalho);
    }

    public int recuperaQuantidadeProducaoProduzindoPorId(
        String idPersonagem,
        String idTrabalho
    ) {
        return producaoDao.recuperaQuantidadeProducaoProduzindoPorId(idPersonagem, idTrabalho);
    }

    public LiveData<Resource<Void>> removeProducoes(String idPersonagem) {
        return FirebaseTimeoutHelper.execute(callback -> referenciaProducao
            .child(idPersonagem)
            .removeValue()
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        producaoDao.removeProducoes(idPersonagem);
                        callback.sucesso();
                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao remover produções");
                callback.erro(erro);
            })
        );
    }

    private String recuperaErro(Exception exception, String erroPadrao) {
        if (exception == null) {
            return erroPadrao;
        }

        if (exception instanceof FirebaseNetworkException) {
            return "Erro de conexão";
        }

        String mensagem = exception.getMessage();

        return mensagem == null || mensagem.isEmpty()
                ? erroPadrao
                : mensagem;
    }

    private boolean producaoInvalida(TrabalhoProducao trabalho) {
        return trabalho.getIdTrabalho() == null || trabalho.getIdTrabalho().isEmpty() ||
                trabalho.getTipoLicenca() == null || trabalho.getTipoLicenca().isEmpty() ||
                trabalho.getEstado() == null ||
                trabalho.getExperiencia() == null ||
                trabalho.getRecorrencia() == null;
    }

    private boolean idTrabalhoInvalido(TrabalhoProducao trabalho) {
        return trabalho == null || trabalho.getId() == null || trabalho.getId().isEmpty();
    }
}
