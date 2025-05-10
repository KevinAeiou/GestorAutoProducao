package com.kevin.ceep.repository;

import static com.kevin.ceep.ui.activity.Constantes.CHAVE_LISTA_TRABALHO;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_PRODUCAO;

import android.os.Build;

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
import com.kevin.ceep.model.TrabalhoProducao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class  TrabalhoProducaoRepository {
    private final DatabaseReference referenciaTrabalhos;
    private final String idPersonagem;
    private DatabaseReference referenciaProducaoIdPersonagem;
    private ValueEventListener ouvinteProducaoIdPersonagem, ouvinteProducao;
    private DatabaseReference referenciaProducao;
    private MutableLiveData<Resource<ArrayList<TrabalhoProducao>>> trabalhosEncontrados;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);
    private static volatile TrabalhoProducaoRepository instancia;

    public TrabalhoProducaoRepository(String idPersonagem) {
        this.referenciaProducaoIdPersonagem = FirebaseDatabase.getInstance().getReference(CHAVE_PRODUCAO)
            .child(idPersonagem);
        this.referenciaTrabalhos = FirebaseDatabase.getInstance().getReference(CHAVE_LISTA_TRABALHO);
        this.idPersonagem = idPersonagem;
    }

    public static synchronized TrabalhoProducaoRepository getInstance(String idPersonagem) {
        if (instancia == null || !instancia.idPersonagem.equals(idPersonagem)) {
            destroyInstance();
            instancia = new TrabalhoProducaoRepository(idPersonagem);
        }
        return instancia;
    }

    public static synchronized void destroyInstance() {
        if (instancia != null) {
            instancia = null;
        }
    }

    public LiveData<Resource<Void>> modificaTrabalhoProducao(TrabalhoProducao trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (trabalhoInvalido(trabalho)) {
            liveData.postValue(new Resource<>(null, "Produção inválida"));
            return liveData;
        }
        referenciaProducaoIdPersonagem.child(trabalho.getId()).setValue(trabalho).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao modificar produção");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }

    private boolean trabalhoInvalido(TrabalhoProducao trabalho) {
        return trabalho.getIdTrabalho() == null || trabalho.getIdTrabalho().isEmpty() ||
            trabalho.getTipoLicenca() == null || trabalho.getTipoLicenca().isEmpty() ||
            trabalho.getEstado() == null ||
            trabalho.getRecorrencia() == null;
    }

    public LiveData<Resource<Void>> insereTrabalhoProducao(TrabalhoProducao trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (idTrabalhoInvalido(trabalho) || trabalhoInvalido(trabalho)) {
            liveData.postValue(new Resource<>(null, "Produção inválida"));
            return liveData;
        }
        referenciaProducaoIdPersonagem.child(trabalho.getId()).setValue(trabalho).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao inserir produção");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    public LiveData<Resource<Void>> removeTrabalhoProducao(TrabalhoProducao trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (idTrabalhoInvalido(trabalho)) {
            liveData.postValue(new Resource<>(null, "Id produção inválido"));
            return liveData;
        }
        referenciaProducaoIdPersonagem.child(trabalho.getId()).removeValue().addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao remover produção");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    private boolean idTrabalhoInvalido(TrabalhoProducao trabalho) {
        return trabalho == null || trabalho.getId() == null || trabalho.getId().isEmpty();
    }

    public LiveData<Resource<Void>> removeReferenciaTrabalhoEspecifico(Trabalho trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (trabalho == null || trabalho.getId() == null || trabalho.getId().isEmpty()) {
            liveData.setValue(new Resource<>(null, "Id produção inválido"));
            return  liveData;
        }
        referenciaProducao = FirebaseDatabase.getInstance().getReference(CHAVE_PRODUCAO);
        List<Task<Void>> tarefas = new ArrayList<>();
        ouvinteProducao = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn : snapshot.getChildren()) {
                    referenciaProducaoIdPersonagem = referenciaProducao.child(Objects.requireNonNull(dn.getKey()));
                    for (DataSnapshot dn2 : dn.getChildren()) {
                        TrabalhoProducao trabalhoEncontrado = dn2.getValue(TrabalhoProducao.class);
                        assert trabalhoEncontrado != null;
                        if (trabalhoEncontrado.getIdTrabalho().equals(trabalho.getId())) {
                            tarefas.add(referenciaProducaoIdPersonagem.child(trabalho.getId()).removeValue());
                        }
                    }
                }
                Tasks.whenAllComplete(tarefas).addOnCompleteListener(backgroundExecutor, task -> {
                    if (task.isSuccessful()) {
                        liveData.postValue(new Resource<>(null, null));
                        return;
                    }
                    Exception exception = task.getException();
                    String erro = recuperaErro(exception, "Erro desconhecido ao remover referência produção");
                    liveData.postValue(new Resource<>(null, erro));
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.postValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaProducao.addListenerForSingleValueEvent(ouvinteProducao);
        return liveData;
    }

    public LiveData<Resource<ArrayList<TrabalhoProducao>>> recuperaTrabalhosServidor() {
        trabalhosEncontrados = new MutableLiveData<>();
        ouvinteProducaoIdPersonagem = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<TrabalhoProducao> trabalhos = new ArrayList<>();
                for (DataSnapshot dn : snapshot.getChildren()) {
                    TrabalhoProducao trabalhoProducao = dn.getValue(TrabalhoProducao.class);
                    if (trabalhoProducao == null) continue;
                    trabalhos.add(trabalhoProducao);
                }
                if (trabalhos.isEmpty()) {
                    trabalhosEncontrados.postValue(new Resource<>(trabalhos, null));
                    return;
                }
                List<TrabalhoProducao> trabalhosServidor = Collections.synchronizedList(new ArrayList<>());
                ArrayList<Task<DataSnapshot>> tarefas = new ArrayList<>(trabalhos.size());
                for (TrabalhoProducao trabalhoProducao : trabalhos) {
                    tarefas.add(referenciaTrabalhos.child(trabalhoProducao.getIdTrabalho()).get());
                }
                Tasks.whenAllSuccess(tarefas).addOnCompleteListener(backgroundExecutor, tarefasCombinadas -> {
                    if (tarefasCombinadas.isSuccessful()) {
                        for (int i = 0; i < tarefasCombinadas.getResult().size(); i++) {
                            DataSnapshot ds = (DataSnapshot) tarefasCombinadas.getResult().get(i);
                            Trabalho trabalho = ds.getValue(Trabalho.class);
                            if (trabalho == null) continue;
                            TrabalhoProducao trabalhoProducao = defineAtributosTrabalho(trabalhos.get(i), trabalho);
                            trabalhosServidor.add(trabalhoProducao);
                        }
                        trabalhosServidor.sort(Comparator.comparing(TrabalhoProducao::getEstado)
                                .thenComparing(TrabalhoProducao::getProfissao)
                                .thenComparing(TrabalhoProducao::getRaridade)
                                .thenComparing(TrabalhoProducao::getNivel)
                                .thenComparing(TrabalhoProducao::getNome));
                        trabalhosEncontrados.postValue(new Resource<>(new ArrayList<>(trabalhosServidor), null));
                        return;
                    }
                    trabalhosEncontrados.postValue(new Resource<>(null, tarefasCombinadas.getException().getMessage()));
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    trabalhosEncontrados.postValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaProducaoIdPersonagem.addValueEventListener(ouvinteProducaoIdPersonagem);
        return trabalhosEncontrados;
    }

    @NonNull
    private static TrabalhoProducao defineAtributosTrabalho(TrabalhoProducao trabalhoProducao, Trabalho trabalho) {
        trabalhoProducao.setNome(trabalho.getNome());
        trabalhoProducao.setNomeProducao(trabalho.getNomeProducao());
        trabalhoProducao.setProfissao(trabalho.getProfissao());
        trabalhoProducao.setRaridade(trabalho.getRaridade());
        trabalhoProducao.setTrabalhoNecessario(trabalho.getTrabalhoNecessario());
        trabalhoProducao.setNivel(trabalho.getNivel());
        trabalhoProducao.setExperiencia(trabalho.getExperiencia());
        return trabalhoProducao;
    }

    public void removeOuvinte() {
        if (referenciaProducaoIdPersonagem != null && ouvinteProducaoIdPersonagem != null) {
            referenciaProducaoIdPersonagem.removeEventListener(ouvinteProducaoIdPersonagem);
        }
        if (referenciaProducao != null && ouvinteProducao != null) {
            referenciaProducao.removeEventListener(ouvinteProducao);
        }
    }
}
