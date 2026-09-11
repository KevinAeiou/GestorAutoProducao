package com.kevin.gestorproducao.repository;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_LISTA_PROFISSOES;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.gestorproducao.dao.ProfissaoDao;
import com.kevin.gestorproducao.model.ProfissaoBase;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProfissaoRepository {
    private static volatile ProfissaoRepository instancia;
    private final DatabaseReference referenciaListaProfissoes;
    private ValueEventListener ouvinteListaProfissoes;
    private final ProfissaoDao profissaoDao;
    private final ProfissaoPersonagemRepository profissaoPersonagemRepository;
    private final Executor backGroundExecutor = Executors.newFixedThreadPool(2);

    public ProfissaoRepository(Context context) {
        FirebaseDatabase meuBanco= FirebaseDatabase.getInstance();
        this.referenciaListaProfissoes = meuBanco.getReference(CHAVE_LISTA_PROFISSOES);
        this.profissaoDao = new ProfissaoDao(context);
        this.profissaoPersonagemRepository = ProfissaoPersonagemRepository.getInstance(context);
    }

    public static synchronized ProfissaoRepository getInstance(Context context) {
        if (instancia == null) {
            instancia = new ProfissaoRepository(context);
        }

        return instancia;
    }

    public LiveData<Resource<ArrayList<ProfissaoBase>>> recuperaProfissoes() {
        MutableLiveData<Resource<ArrayList<ProfissaoBase>>> profissoesRecuperadas = new MutableLiveData<>();

        ouvinteListaProfissoes = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ProfissaoBase> profissoes = new ArrayList<>();

                for (DataSnapshot dn : snapshot.getChildren()) {
                    ProfissaoBase profissaoPersonagem = dn.getValue(ProfissaoBase.class);
                    assert profissaoPersonagem != null;
                    profissoes.add(profissaoPersonagem);
                }

                profissoesRecuperadas.postValue(new Resource<>(profissoes, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                profissoesRecuperadas.postValue(new Resource<>(null, error.getMessage()));
            }
        };

        referenciaListaProfissoes.addValueEventListener(ouvinteListaProfissoes);
        return profissoesRecuperadas;
    }

    public Map<String, String> recuperaMapaProfissoesLocal() {
        return profissaoDao.recuperaMapaProfissoes();
    }

    public LiveData<Resource<Void>> sincronizaProfissoes() {
        ArrayList<ProfissaoBase> profissoesServidor = new ArrayList<>();
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        ouvinteListaProfissoes = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                profissoesServidor.clear();

                for (DataSnapshot dn : snapshot.getChildren()) {
                    ProfissaoBase profissao = dn.getValue(ProfissaoBase.class);
                    if (profissao != null) {
                        profissoesServidor.add(profissao);
                    }
                }

                backGroundExecutor.execute(() -> {
                    try {
                        profissaoDao.substituirTodas(profissoesServidor);

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

        referenciaListaProfissoes.addListenerForSingleValueEvent(ouvinteListaProfissoes);
        return liveData;
    }

    public LiveData<Resource<Void>> modificaProfissao(ProfissaoBase profissao) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        referenciaListaProfissoes.child(profissao.getId()).setValue(profissao).addOnCompleteListener(
            backGroundExecutor, task -> {
               if (task.isSuccessful()) {
                   try {
                        profissaoDao.modificaProfissao(profissao);
                        liveData.postValue(new Resource<>(null, null));
                   } catch (RuntimeException e) {
                       liveData.postValue(new Resource<>(null, e.getMessage()));
                   }
                   return;
               }

               Exception exception = task.getException();
               String erro = recuperaErro(
                   exception,
                   "Erro desconhecido ao modificar profissão"
               );

               liveData.postValue(new Resource<>(null, erro));
            }
        );

        return liveData;
    }

    private String recuperaErro(Exception exception, String erroPadrao) {
        return exception == null ? erroPadrao : exception.getMessage();
    }

    public void removeOuvinte() {
        if (referenciaListaProfissoes != null && ouvinteListaProfissoes != null) {
            referenciaListaProfissoes.removeEventListener(ouvinteListaProfissoes);
        }
    }

    public LiveData<Resource<Void>> insereProfissao(
        ProfissaoBase profissao
    ) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        referenciaListaProfissoes.child(profissao.getId()).setValue(profissao).addOnCompleteListener(
            task -> {
                if (!task.isSuccessful()) {
                    Exception exception = task.getException();

                    String erro = recuperaErro(
                        exception,
                        "Erro desconhecido ao inserir profissão"
                    );

                    liveData.postValue(new Resource<>(null, erro));

                    return;
                }

                LiveData<Resource<Void>> source = profissaoPersonagemRepository
                    .insereProfissaoEmTodosPersonagens(profissao);

                profissaoDao.insereProfissao(profissao);

                Observer<Resource<Void>> observer = new Observer<Resource<Void>>() {
                    @Override
                    public void onChanged(
                        Resource<Void> resultado
                    ) {

                        if (resultado == null) return;

                        liveData.postValue(resultado);

                        source.removeObserver(this);
                    }
                };

                source.observeForever(observer);
            }
        );

        return liveData;
    }

    public LiveData<Resource<Void>> removeProfissao(ProfissaoBase profissao) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        referenciaListaProfissoes.child(profissao.getId()).removeValue().addOnCompleteListener(
            task -> {
                if (!task.isSuccessful()) {
                    Exception exception = task.getException();

                    String erro = recuperaErro(
                        exception,
                        "Erro ao remover profissão"
                    );

                    liveData.postValue(new Resource<>(null, erro));

                    return;
                }

                profissaoDao.removeProfissao(profissao);

                LiveData<Resource<Void>> source = profissaoPersonagemRepository
                    .removeProfissaoDeTodosPersonagens(profissao);

                Observer<Resource<Void>> observer = new Observer<Resource<Void>>() {
                    @Override
                    public void onChanged(Resource<Void> resultado) {
                        if (resultado == null) return;

                        liveData.postValue(resultado);

                        source.removeObserver(this);
                    }
                };

                source.observeForever(observer);
            }
        );

        return liveData;
    }
}
