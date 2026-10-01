package com.kevin.gestorproducao.repository;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_ESTOQUE;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.gestorproducao.dao.EstoqueDao;
import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.repository.helper.FirebaseTimeoutHelper;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TrabalhoEstoqueRepository {
    private static volatile TrabalhoEstoqueRepository instancia;
    private final DatabaseReference referenciaEstoque;
    private final DatabaseReference referenciaEstoqueIdPersonagem;
    private ValueEventListener ouvinteEstoque;
    private ValueEventListener ouvinteRecuperaEstoque;
    private ValueEventListener ouvinteRecuperaEstoqueIdTrabalho;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);
    private final MutableLiveData<Resource<ArrayList<TrabalhoEstoque>>> estoqueEncontrado;
    private final EstoqueDao estoqueDao;

    public TrabalhoEstoqueRepository(Context context) {
        FirebaseDatabase meuBanco = FirebaseDatabase.getInstance();
        this.referenciaEstoqueIdPersonagem = meuBanco.getReference(CHAVE_ESTOQUE);
        this.referenciaEstoque = meuBanco.getReference(CHAVE_ESTOQUE);
        this.estoqueDao = new EstoqueDao(context);
        this.estoqueEncontrado = new MutableLiveData<>();
    }

    public static synchronized TrabalhoEstoqueRepository getInstance(Context context) {
        if (instancia == null) {
            instancia = new TrabalhoEstoqueRepository(context);
        }
        return instancia;
    }

    public LiveData<Resource<Void>> modificaEstoque(
        TrabalhoEstoque trabalho,
        String idPersonagem)
    {
        TrabalhoEstoque trabalhoModificado= new TrabalhoEstoque();
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        if (trabalhoInvalido(trabalho)) {
            liveData.postValue(new Resource<>(null, "Trabalho inválido"));
            return liveData;
        }

        trabalhoModificado.setId(trabalho.getId());
        trabalhoModificado.setIdTrabalho(trabalho.getIdTrabalho());
        trabalhoModificado.setQuantidade(trabalho.getQuantidade());

        return FirebaseTimeoutHelper.execute(callback -> referenciaEstoqueIdPersonagem
            .child(idPersonagem)
            .child(trabalhoModificado.getId())
            .setValue(trabalhoModificado)
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        estoqueDao.modificaEstoque(trabalho, idPersonagem);
                        callback.sucesso();

                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao remover trabalho no estoque");
                callback.erro(erro);
            })
        );
    }

    private boolean trabalhoInvalido(TrabalhoEstoque trabalho) {
        return idTrabalhoInvalido(trabalho) || trabalho.getIdTrabalho() == null || trabalho.getIdTrabalho().isEmpty() || trabalho.getQuantidade() == null;
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }

    private static boolean idTrabalhoInvalido(TrabalhoEstoque trabalho) {
        return trabalho == null || trabalho.getId() == null || trabalho.getId().isEmpty();
    }

    public LiveData<Resource<ArrayList<TrabalhoEstoque>>> recuperaEstoque(String idPersonagem) {
        backgroundExecutor.execute(() -> {
            try {
                ArrayList<TrabalhoEstoque> estoque = new ArrayList<>(estoqueDao.recuperaEstoque(idPersonagem));
                estoqueEncontrado.postValue(new Resource<>(estoque, null));
            } catch (RuntimeException e) {
                estoqueEncontrado.postValue(new Resource<>(null, e.getMessage()));
            }
        });

        return estoqueEncontrado;
    }

    public LiveData<Resource<TrabalhoEstoque>> recuperaTrabalhoEstoquePorIdTrabalho(
        String idTrabalho,
        String idPersonagem
    ) {
        MutableLiveData<Resource<TrabalhoEstoque>> liveData = new MutableLiveData<>();
        if (idTrabalho == null || idTrabalho.isEmpty()) {
            liveData.postValue(new Resource<>(null, "Id trabalho inválido"));
            return liveData;
        }
        ouvinteRecuperaEstoqueIdTrabalho = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        TrabalhoEstoque trabalho = ds.getValue(TrabalhoEstoque.class);
                        if (trabalho == null) continue;
                        if (trabalho.getIdTrabalho().equals(idTrabalho)) {
                            liveData.postValue(new Resource<>(trabalho, null));
                            return;
                        }
                    }
                }
                liveData.postValue(new Resource<>(null, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.postValue(new Resource<>(null, error.getMessage()));
            }
        };

        referenciaEstoqueIdPersonagem.child(idPersonagem).addListenerForSingleValueEvent(ouvinteRecuperaEstoqueIdTrabalho);
        return liveData;
    }

    public LiveData<Resource<Void>> insereEstoque(
        @NonNull TrabalhoEstoque trabalho,
        String idPersonagem
    ) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (trabalhoInvalido(trabalho)) {
            liveData.postValue(new Resource<>(null, "Trabalho inválido"));
            return liveData;
        }
        return FirebaseTimeoutHelper.execute(callback -> referenciaEstoqueIdPersonagem
            .child(idPersonagem)
            .child(trabalho.getId())
            .setValue(trabalho)
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        estoqueDao.insereEstoque(trabalho, idPersonagem);
                        callback.sucesso();
                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao inserir trabalho no estoque");
                callback.erro(erro);
            })
        );
    }

    public LiveData<Resource<Void>> removeTrabalhoEstoque(
        TrabalhoEstoque trabalho,
        String idPersonagem
    ) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (idTrabalhoInvalido(trabalho)) {
            liveData.postValue(new Resource<>(null, "Id do trabalho inválido"));
            return liveData;
        }

        return FirebaseTimeoutHelper.execute(callback -> referenciaEstoqueIdPersonagem
            .child(idPersonagem)
            .child(trabalho.getId())
            .removeValue()
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        estoqueDao.removeEstoque(trabalho);
                        callback.sucesso();

                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao remover trabalho no estoque");
                callback.erro(erro);
            })
        );
    }

    public void removeReferenciaTrabalhoEspecifico(String idTrabalho) {
        ouvinteEstoque = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot estoqueSnapshot: snapshot.getChildren()) {
                    for (DataSnapshot registroSnapshot : estoqueSnapshot.getChildren()) {

                        String idTrabalhoFirebase = registroSnapshot.child("idTrabalho").getValue(String.class);

                        if (idTrabalhoFirebase != null && idTrabalhoFirebase.equals(idTrabalho)) {
                            registroSnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("repositorioEstoque", "Erro: " + error.getMessage());
            }
        };

        referenciaEstoque.addListenerForSingleValueEvent(ouvinteEstoque);
    }

    public void removeOuvinte() {
        if (referenciaEstoque != null && ouvinteEstoque != null) {
            referenciaEstoque.removeEventListener(ouvinteEstoque);
        }
        if (referenciaEstoqueIdPersonagem != null && ouvinteRecuperaEstoque != null) {
            referenciaEstoqueIdPersonagem.removeEventListener(ouvinteRecuperaEstoque);
        }
        if (referenciaEstoqueIdPersonagem != null && ouvinteRecuperaEstoqueIdTrabalho != null) {
            referenciaEstoqueIdPersonagem.removeEventListener(ouvinteRecuperaEstoqueIdTrabalho);
        }
    }

    public LiveData<Resource<Void>> sincronizaEstoque(String idPersonagem) {
        ArrayList<TrabalhoEstoque> estoque = new ArrayList<>();
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        ouvinteRecuperaEstoque = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                estoque.clear();

                for (DataSnapshot dn : snapshot.getChildren()) {
                    TrabalhoEstoque trabalho = dn.getValue(TrabalhoEstoque.class);
                    if (trabalho != null) {
                        estoque.add(trabalho);
                    }
                }

                backgroundExecutor.execute(() -> {
                    try {
                        estoqueDao.substituirTodas(estoque, idPersonagem);

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

        referenciaEstoqueIdPersonagem.child(idPersonagem).addListenerForSingleValueEvent(ouvinteRecuperaEstoque);
        return liveData;
    }

    public TrabalhoEstoque recuperaTrabalhoPorId(@Nullable String idPersonagem, String idTrabalho) {
        return estoqueDao.recuperaTrabalhoPorId(idPersonagem, idTrabalho);
    }

    public ArrayList<TrabalhoEstoque> recuperaEstoqueComNomes(String idPersonagem) {
        return estoqueDao.recuperaEstoqueComNomes(idPersonagem);
    }

    public LiveData<Resource<Void>> removeEstoque(String idPersonagem) {
        return FirebaseTimeoutHelper.execute(callback -> referenciaEstoqueIdPersonagem
            .child(idPersonagem)
            .removeValue()
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        estoqueDao.removeEstoques(idPersonagem);
                        callback.sucesso();
                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao remover estoque");
                callback.erro(erro);
            })
        );
    }
}
