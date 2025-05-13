package com.kevin.ceep.repository;

import static com.kevin.ceep.repository.TrabalhoProducaoRepository.destroyInstance;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_LISTA_TRABALHO;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_VENDAS;

import android.util.Log;

import androidx.annotation.NonNull;
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
import com.kevin.ceep.model.TrabalhoVendido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TrabalhoVendidoRepository {
    private static volatile TrabalhoVendidoRepository instancia;
    private DatabaseReference referenciaVendasIdPersonagem;
    private DatabaseReference referenciaVendas;
    private DatabaseReference referenciaTrabalhos;
    private ValueEventListener ouvinteVenda, ouvinteVendaIdPersonagem;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);
    private String idPersonagem;

    public TrabalhoVendidoRepository(String idPersonagem) {
        FirebaseDatabase meuBanco = FirebaseDatabase.getInstance();
        this.idPersonagem = idPersonagem;
        this.referenciaVendasIdPersonagem = meuBanco.getReference(CHAVE_VENDAS)
                .child(this.idPersonagem);
        this.referenciaTrabalhos = meuBanco.getReference(CHAVE_LISTA_TRABALHO);
    }
    public TrabalhoVendidoRepository() {
        this.referenciaVendas = FirebaseDatabase.getInstance().getReference(CHAVE_VENDAS);
    }
    public static synchronized TrabalhoVendidoRepository getInstance(String idPersonagem) {
        if (instancia == null || !instancia.idPersonagem.equals(idPersonagem)) {
            destroyInstance();
            instancia = new TrabalhoVendidoRepository(idPersonagem);
        }
        return instancia;
    }
    public LiveData<Resource<ArrayList<TrabalhoVendido>>> recuperaVendas() {
        MutableLiveData<Resource<ArrayList<TrabalhoVendido>>> vendasEncontradas = new MutableLiveData<>();
        ouvinteVendaIdPersonagem = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<TrabalhoVendido> vendas = new ArrayList<>();
                for (DataSnapshot dn : snapshot.getChildren()) {
                    TrabalhoVendido trabalho = dn.getValue(TrabalhoVendido.class);
                    if (trabalho == null) continue;
                    vendas.add(trabalho);
                }
                if (vendas.isEmpty()) {
                    vendasEncontradas.postValue(new Resource<>(vendas, null));
                    return;
                }
                List<TrabalhoVendido> vendasServidor = Collections.synchronizedList(new ArrayList<>());
                ArrayList<Task<DataSnapshot>> tarefas = new ArrayList<>(vendas.size());
                for (TrabalhoVendido trabalho : vendas) {
                    tarefas.add(referenciaTrabalhos.child(trabalho.getIdTrabalho()).get());
                }
                Tasks.whenAllSuccess(tarefas).addOnCompleteListener(backgroundExecutor, tarefasCombinadas -> {
                    if (tarefasCombinadas.isSuccessful()) {
                        for (int i = 0; i < tarefasCombinadas.getResult().size(); i++) {
                            DataSnapshot ds = (DataSnapshot) tarefasCombinadas.getResult().get(i);
                            Trabalho trabalho = ds.getValue(Trabalho.class);
                            if (trabalho == null) return;
                            TrabalhoVendido venda = defineAtributosTrabalho(vendas.get(i), trabalho);
                            vendasServidor.add(venda);
                        }
                        vendasEncontradas.postValue(new Resource<>(new ArrayList<>(vendasServidor), null));
                        return;
                    }
                    vendasEncontradas.postValue(new Resource<>(null, tarefasCombinadas.getException().getMessage()));
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                vendasEncontradas.postValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaVendasIdPersonagem.addListenerForSingleValueEvent(ouvinteVendaIdPersonagem);
        return vendasEncontradas;
    }

    private TrabalhoVendido defineAtributosTrabalho(TrabalhoVendido venda, Trabalho trabalho) {
        venda.setNome(trabalho.getNome());
        venda.setRaridade(trabalho.getRaridade());
        return venda;
    }

    public LiveData<Resource<Void>> removeTrabalho(TrabalhoVendido trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (idVendaInvalida(trabalho)) {
            liveData.setValue(new Resource<>(null, "Venda está nula ou id vazio!"));
            return liveData;
        }
        referenciaVendasIdPersonagem.child(trabalho.getId())
                .removeValue()
                .addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erroEncontrado = recuperaErro(exception, "Erro desconhecido ao remover venda");
            liveData.postValue(new Resource<>(null, erroEncontrado));
        });
        return liveData;
    }

    public LiveData<Resource<Void>> modificaVenda(TrabalhoVendido trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (vendaInvalida(trabalho)) {
            liveData.setValue(new Resource<>(null, "Venda inválida!"));
            return liveData;
        }
        referenciaVendasIdPersonagem.child(trabalho.getId()).setValue(trabalho).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
            } else {
                Exception exception = task.getException();
                String erroRecuperado = recuperaErro(exception, "Erro desconhecido ao modificar venda");
                liveData.postValue(new Resource<>(null, erroRecuperado));
            }
        });
        return liveData;
    }
    public void removeReferenciaTrabalhoEspecfico(Trabalho trabalho) {
        ouvinteVenda = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn: snapshot.getChildren()) {
                    referenciaVendasIdPersonagem= referenciaVendas.child(Objects.requireNonNull(dn.getKey()));
                    for (DataSnapshot dn2: dn.getChildren()) {
                        TrabalhoVendido trabalhoEncontrado= dn2.getValue(TrabalhoVendido.class);
                        assert trabalhoEncontrado != null;
                        if (trabalhoEncontrado.getIdTrabalho().equals(trabalho.getId())){
                            removeTrabalho(trabalhoEncontrado);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("repositorioVendas", "Erro: " + error.getMessage());
            }
        };
        referenciaVendas.addListenerForSingleValueEvent(ouvinteVenda);
    }

    public LiveData<Resource<Void>> insereVenda(TrabalhoVendido trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (vendaInvalida(trabalho)) {
            liveData.setValue(new Resource<>(null, "Venda inválida!"));
            return liveData;
        }
        referenciaVendasIdPersonagem.child(trabalho.getId()).setValue(trabalho).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
            } else {
                Exception exception = task.getException();
                String erroRecuperado = recuperaErro(exception, "Erro desconhecido ao inserir venda");
                liveData.postValue(new Resource<>(null, erroRecuperado));
            }
        });
        return liveData;
    }

    private static boolean vendaInvalida(TrabalhoVendido trabalho) {
        return idVendaInvalida(trabalho) || trabalho.getDataVenda() == null || trabalho.getDescricao() == null || trabalho.getDescricao().isEmpty() || trabalho.getDataVenda().isEmpty();
    }

    private String recuperaErro(Exception exception, String erroPadrao) {
        return exception == null ? erroPadrao : exception.getMessage();
    }

    private static boolean idVendaInvalida(TrabalhoVendido trabalho) {
        return trabalho == null || trabalho.getId() == null || trabalho.getId().isEmpty();
    }

    public void removeOuvinte() {
        if (referenciaVendasIdPersonagem != null && ouvinteVendaIdPersonagem != null) {
            referenciaVendasIdPersonagem.removeEventListener(ouvinteVendaIdPersonagem);
        }
        if (referenciaVendas != null && ouvinteVenda != null) {
            referenciaVendas.removeEventListener(ouvinteVenda);
        }
    }
}
