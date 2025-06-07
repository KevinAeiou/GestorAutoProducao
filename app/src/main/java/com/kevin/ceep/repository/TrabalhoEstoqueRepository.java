package com.kevin.ceep.repository;

import static com.kevin.ceep.repository.TrabalhoProducaoRepository.destroyInstance;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_ESTOQUE;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_LISTA_TRABALHO;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TrabalhoEstoqueRepository {
    private static TrabalhoEstoqueRepository instancia;
    private DatabaseReference referenciaEstoqueIdPersonagem;
    private DatabaseReference referenciaEstoque;
    private DatabaseReference referenciaTrabalhos;
    private String idPersonagem;
    private ValueEventListener ouvinteEstoque;
    private ValueEventListener ouvinteRecuperaEstoque;
    private ValueEventListener ouvinteRecuperaEstoqueIdTrabalho;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);
    private MutableLiveData<Resource<ArrayList<TrabalhoEstoque>>> estoqueEncontrado;

    public TrabalhoEstoqueRepository(String idPersonagem) {
        this.idPersonagem = idPersonagem;
        FirebaseDatabase meuBanco = FirebaseDatabase.getInstance();
        this.referenciaEstoqueIdPersonagem = meuBanco.getReference(CHAVE_ESTOQUE).child(idPersonagem);
        this.referenciaTrabalhos = meuBanco.getReference(CHAVE_LISTA_TRABALHO);
    }
    public TrabalhoEstoqueRepository() {
        this.referenciaEstoque= FirebaseDatabase.getInstance().getReference(CHAVE_ESTOQUE);
    }

    public static TrabalhoEstoqueRepository getInstance(String idPersonagem) {
        if (instancia == null || !instancia.idPersonagem.equals(idPersonagem)) {
            destroyInstance();
            instancia = new TrabalhoEstoqueRepository(idPersonagem);
        }
        return instancia;
    }

    public LiveData<Resource<Void>> modificaTrabalhoEstoque(TrabalhoEstoque trabalho) {
        TrabalhoEstoque trabalhoModificado= new TrabalhoEstoque();
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (trabalhoInvalido(trabalho)) {
            liveData.postValue(new Resource<>(null, "Trabalho inválido"));
            return liveData;
        }
        trabalhoModificado.setId(trabalho.getId());
        trabalhoModificado.setIdTrabalho(trabalho.getIdTrabalho());
        trabalhoModificado.setQuantidade(trabalho.getQuantidade());
        referenciaEstoqueIdPersonagem.child(trabalhoModificado.getId()).setValue(trabalhoModificado).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao remover trabalho no estoque");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
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

    public LiveData<Resource<ArrayList<TrabalhoEstoque>>> recuperaEstoque() {
        estoqueEncontrado = new MutableLiveData<>();
        ouvinteRecuperaEstoque = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<TrabalhoEstoque> estoque = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    TrabalhoEstoque trabalho = ds.getValue(TrabalhoEstoque.class);
                    if (trabalho == null) continue;
                    estoque.add(trabalho);
                }
                if (estoque.isEmpty()) {
                    estoqueEncontrado.postValue(new Resource<>(estoque, null));
                    return;
                }
                List<TrabalhoEstoque> trabalhosServidor = Collections.synchronizedList(new ArrayList<>());
                ArrayList<Task<DataSnapshot>> tarefas = new ArrayList<>(estoque.size());
                for (TrabalhoEstoque trabalho : estoque) {
                    tarefas.add(referenciaTrabalhos.child(trabalho.getIdTrabalho()).get());
                }
                Tasks.whenAllSuccess(tarefas).addOnCompleteListener(backgroundExecutor, tarefasCombinadas -> {
                    if (tarefasCombinadas.isSuccessful()) {
                        for (int i = 0; i < tarefasCombinadas.getResult().size(); i ++) {
                            DataSnapshot ds = (DataSnapshot) tarefasCombinadas.getResult().get(i);
                            Trabalho trabalho = ds.getValue(Trabalho.class);
                            if (trabalho == null) return;
                            TrabalhoEstoque trabalhoEstoque1 = defineAtributosTrabalho(estoque.get(i), trabalho);
                            trabalhosServidor.add(trabalhoEstoque1);
                        }
                        trabalhosServidor.sort(Comparator.comparing(TrabalhoEstoque::getProfissao).thenComparing(TrabalhoEstoque::getRaridade).thenComparing(TrabalhoEstoque::getNivel));
                        estoqueEncontrado.postValue(new Resource<>(new ArrayList<>(trabalhosServidor), null));
                        return;
                    }
                    Exception exception = tarefasCombinadas.getException();
                    String erro = recuperaErro(exception, "Erro desconhecido ao recuperar estoque");
                    estoqueEncontrado.postValue(new Resource<>( null, erro));
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                estoqueEncontrado.postValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaEstoqueIdPersonagem.addValueEventListener(ouvinteRecuperaEstoque);
        return estoqueEncontrado;
    }

    private TrabalhoEstoque defineAtributosTrabalho(TrabalhoEstoque trabalhoEstoque, Trabalho trabalho) {
        trabalhoEstoque.setNome(trabalho.getNome());
        trabalhoEstoque.setRaridade(trabalho.getRaridade());
        trabalhoEstoque.setNivel(trabalho.getNivel());
        trabalhoEstoque.setProfissao(trabalho.getProfissao());
        return trabalhoEstoque;
    }

    public LiveData<Resource<TrabalhoEstoque>> recuperaTrabalhoEstoquePorIdTrabalho(String idTrabalho) {
        MutableLiveData<Resource<TrabalhoEstoque>> liveData = new MutableLiveData<>();
        if (idTrabalho == null || idTrabalho.isEmpty()) {
            liveData.postValue(new Resource<>(null, "Id trabalho inválido"));
            return liveData;
        }
        ouvinteRecuperaEstoqueIdTrabalho = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Log.d("estoque", "Snapshot: " + ds);
                    if (ds.exists()) {
                        TrabalhoEstoque trabalho = ds.getValue(TrabalhoEstoque.class);
                        Log.d("estoque", "Trabalho estoque: " + trabalho);
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
        referenciaEstoqueIdPersonagem.addListenerForSingleValueEvent(ouvinteRecuperaEstoqueIdTrabalho);
        return liveData;
    }

    public LiveData<Resource<Void>> insereTrabalhoEstoque(@NonNull TrabalhoEstoque trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (trabalhoInvalido(trabalho)) {
            liveData.postValue(new Resource<>(null, "Trabalho inválido"));
            return liveData;
        }
        referenciaEstoqueIdPersonagem.child(trabalho.getId()).setValue(trabalho).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao inserir trabalho no estoque");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    public LiveData<Resource<Void>> removeTrabalhoEstoque(TrabalhoEstoque trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (idTrabalhoInvalido(trabalho)) {
            liveData.postValue(new Resource<>(null, "Id do trabalho inválido"));
            return liveData;
        }
        referenciaEstoqueIdPersonagem.child(trabalho.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                liveData.setValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao remover trabalho no estoque");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    public LiveData<Resource<Void>> removeReferenciaTrabalhoEspecifico(Trabalho trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (trabalho == null || trabalho.getId() == null || trabalho.getId().isEmpty()) {
            liveData.postValue(new Resource<>(null, "Id do trabalho inválido"));
            return liveData;
        }
        List<Task<Void>> tarefas = new ArrayList<>();
        ouvinteEstoque = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn : snapshot.getChildren()) {
                    referenciaEstoqueIdPersonagem = referenciaEstoque.child(Objects.requireNonNull(dn.getKey()));
                    for (DataSnapshot dn2 : dn.getChildren()) {
                        TrabalhoEstoque trabalhoEncontrado = dn2.getValue(TrabalhoEstoque.class);
                        assert trabalhoEncontrado != null;
                        if (trabalhoEncontrado.getIdTrabalho().equals(trabalho.getId())) {
                            tarefas.add(referenciaEstoqueIdPersonagem.child(trabalho.getId()).removeValue());
                        }
                    }
                }
                Tasks.whenAllComplete(tarefas).addOnCompleteListener(backgroundExecutor, task -> {
                    if (task.isSuccessful()) {
                        liveData.postValue(new Resource<>(null, null));
                        return;
                    }
                    Exception exception = task.getException();
                    String erro = recuperaErro(exception, "Erro desconhecido ao remover a referência do trabalho no estoque");
                    liveData.postValue(new Resource<>(null, erro));
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.postValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaEstoque.addListenerForSingleValueEvent(ouvinteEstoque);
        return liveData;
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
}
