package com.kevin.ceep.repository;

import static com.kevin.ceep.repository.TrabalhoProducaoRepository.destroyInstance;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_LISTA_PROFISSOES;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_PROFISSOES;

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
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.TrabalhoProducao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProfissaoRepository {
    private final DatabaseReference referenciaProfissoes;
    private final DatabaseReference referenciaListaProfissoes;
    private static volatile ProfissaoRepository instancia;
    private final String idPersonagem;
    private ValueEventListener ouvinteListaProfissoes;
    private ValueEventListener ouvinteProfissoes;
    private final Executor backGroundExecutor = Executors.newFixedThreadPool(2);

    public ProfissaoRepository(String idPersonagem) {
        FirebaseDatabase meuBanco= FirebaseDatabase.getInstance();
        this.idPersonagem = idPersonagem;
        this.referenciaProfissoes = meuBanco.getReference(CHAVE_PROFISSOES).child(idPersonagem);
        this.referenciaListaProfissoes = meuBanco.getReference(CHAVE_LISTA_PROFISSOES);
    }
    public static synchronized ProfissaoRepository getInstance(String idPersonagem) {
        if (instancia == null || !instancia.idPersonagem.equals(idPersonagem)) {
            destroyInstance();
            instancia = new ProfissaoRepository(idPersonagem);
        }
        return instancia;
    }
    public Profissao retornaProfissaoModificada(ArrayList<Profissao> profissoes, TrabalhoProducao trabalho) {
        for (Profissao profissao : profissoes) {
            if (profissao.getNome().equals(trabalho.getProfissao())) {
                return profissao;
            }
        }
        return null;
    }

    public LiveData<Resource<ArrayList<Profissao>>> recuperaProfissoes() {
        MutableLiveData<Resource<ArrayList<Profissao>>> profissoesRecuperadas = new MutableLiveData<>();
        ouvinteProfissoes = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Profissao> profissoes = new ArrayList<>();
                for (DataSnapshot dn : dataSnapshot.getChildren()) {
                    Profissao profissao = dn.getValue(Profissao.class);
                    assert profissao != null;
                    profissoes.add(profissao);
                }
                if (profissoes.isEmpty()) {
                    profissoesRecuperadas.postValue(new Resource<>(profissoes, null));
                    return;
                }
                List<Profissao> profissoesServidor = Collections.synchronizedList(new ArrayList<>());
                ArrayList<Task<DataSnapshot>> tarefas = new ArrayList<>(profissoes.size());
                for (Profissao profissao : profissoes) {
                    tarefas.add(referenciaListaProfissoes.child(profissao.getId()).get());
                }
                Tasks.whenAllSuccess(tarefas).addOnCompleteListener(backGroundExecutor, tarefasCombinadas -> {
                    if (tarefasCombinadas.isSuccessful()) {
                        for (int i = 0; i < tarefasCombinadas.getResult().size(); i++) {
                            DataSnapshot ds = (DataSnapshot) tarefasCombinadas.getResult().get(i);
                            Profissao profissao = ds.getValue(Profissao.class);
                            if (profissao == null) continue;
                            Profissao profissao1 = profissoes.get(i);
                            profissao1.setNome(profissao.getNome());
                            profissoesServidor.add(profissao1);
                        }
                        profissoesServidor.sort(Comparator.comparing(Profissao::getExperiencia).reversed());
                        profissoesRecuperadas.postValue(new Resource<>(new ArrayList<>(profissoesServidor), null));
                        return;
                    }
                    Exception exception = tarefasCombinadas.getException();
                    String erro = recuperaErro(exception, "Erro desconhecido ao recuperar profissões");
                    profissoesRecuperadas.postValue(new Resource<>(null, erro));
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                profissoesRecuperadas.postValue(new Resource<>(null, databaseError.getMessage()));
            }
        };
        referenciaProfissoes.addValueEventListener(ouvinteProfissoes);
        return profissoesRecuperadas;
    }

    public LiveData<Resource<Void>> insereNovasProfissoes() {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        ouvinteListaProfissoes = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Task<Void>> tarefas = new ArrayList<>();
                for (DataSnapshot dn : snapshot.getChildren()) {
                    Profissao valor = dn.getValue(Profissao.class);
                    if (valor == null) continue;
                    Profissao profissao = new Profissao();
                    profissao.setId(valor.getId());
                    profissao.setExperiencia(0);
                    profissao.setPrioridade(false);
                    tarefas.add(referenciaProfissoes.child(profissao.getId()).setValue(profissao));
                }
                Tasks.whenAllComplete(tarefas).addOnCompleteListener(backGroundExecutor, task -> {
                   if (task.isSuccessful()) {
                       liveData.postValue(new Resource<>(null, null));
                       return;
                   }
                   Exception exception = task.getException();
                   String erro = recuperaErro(exception, "Erro ao inserir profissões");
                   liveData.postValue(new Resource<>(null, erro));
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.postValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaListaProfissoes.addListenerForSingleValueEvent(ouvinteListaProfissoes);
        return liveData;
    }

    public LiveData<Resource<Void>> modificaProfissao(Profissao profissaoModificada) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        Profissao profissao= new Profissao();
        profissao.setId(profissaoModificada.getId());
        profissao.setExperiencia(profissaoModificada.getExperiencia());
        profissao.setPrioridade(profissaoModificada.isPrioridade());
        referenciaProfissoes.child(profissaoModificada.getId()).setValue(profissao).addOnCompleteListener(backGroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao modificar profissão");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    private String recuperaErro(Exception exception, String erroPadrao) {
        return exception == null ? erroPadrao : exception.getMessage();
    }

    public void removeOuvinte() {
        if (referenciaProfissoes != null && ouvinteProfissoes != null) {
            referenciaProfissoes.removeEventListener(ouvinteProfissoes);
        }
        if (referenciaListaProfissoes != null && ouvinteListaProfissoes != null) {
            referenciaListaProfissoes.removeEventListener(ouvinteListaProfissoes);
        }
    }
}
