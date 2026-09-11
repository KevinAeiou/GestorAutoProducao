package com.kevin.gestorproducao.repository;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_PROFISSOES;

import android.content.Context;

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
import com.kevin.gestorproducao.dao.ProfissaoDao;
import com.kevin.gestorproducao.dao.ProfissaoPersonagemDao;
import com.kevin.gestorproducao.model.ProfissaoBase;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProfissaoPersonagemRepository {
    private final DatabaseReference referenciaProfissoesPersonagem;
    private static volatile ProfissaoPersonagemRepository instancia;
    private ValueEventListener ouvinteProfissoesPersonagem;
    private final ProfissaoPersonagemDao profissaoPersonagemDao;
    private final Executor backGroundExecutor = Executors.newFixedThreadPool(2);
    private final ProfissaoDao profissaoDao;

    public ProfissaoPersonagemRepository(Context context) {
        FirebaseDatabase meuBanco= FirebaseDatabase.getInstance();

        this.profissaoPersonagemDao = new ProfissaoPersonagemDao(context);
        this.referenciaProfissoesPersonagem = meuBanco.getReference(CHAVE_PROFISSOES);
        this.profissaoDao = new ProfissaoDao(context);
    }
    public static synchronized ProfissaoPersonagemRepository getInstance(Context context) {
        if (instancia == null) {
            instancia = new ProfissaoPersonagemRepository(context);
        }
        return instancia;
    }

    public LiveData<Resource<ArrayList<ProfissaoPersonagem>>> recuperaProfissoesPersonagem(
        String idPersonagem
    ) {
        ArrayList<ProfissaoPersonagem> profissoesPersonagem = new ArrayList<>(
            profissaoPersonagemDao.recuperaProfissoes(idPersonagem)
        );
        MutableLiveData<Resource<ArrayList<ProfissaoPersonagem>>> profissoes = new MutableLiveData<>();

        profissoes.setValue(new Resource<>(profissoesPersonagem, null));

        return profissoes;
    }

    public LiveData<Resource<Void>> modificaProfissaoPersonagem(
        ProfissaoPersonagem profissao,
        String idPersonagem
    ) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        ProfissaoPersonagem profissaoModificada = new ProfissaoPersonagem();

        profissaoModificada.setId(profissao.getId());
        profissaoModificada.setExperiencia(profissao.getExperiencia());
        profissaoModificada.setPrioridade(profissao.isPrioridade());

        referenciaProfissoesPersonagem.child(idPersonagem).child(profissao.getId()).setValue(
            profissaoModificada
        ).addOnCompleteListener(backGroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        profissaoPersonagemDao.modificaProfissao(profissao, idPersonagem);
                        liveData.postValue(new Resource<>(null, null));
                    } catch (RuntimeException e) {
                        liveData.postValue(new Resource<>(null, e.getMessage()));
                    }
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao modificar profissão");
                liveData.postValue(new Resource<>(null, erro));
        });

        return liveData;
    }

    public ProfissaoPersonagem recuperaProfissaoPorNome(
        String idPersonagem,
        String nomeProfissao
    ) {
        return profissaoPersonagemDao.recuperaProfissaoPorNome(
            idPersonagem,
            nomeProfissao
        );
    }

    public LiveData<Resource<Void>> sincronizaProfissoes(String idPersonagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        ArrayList<ProfissaoPersonagem> profissoes = new ArrayList<>();

        ouvinteProfissoesPersonagem = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                profissoes.clear();

                for (DataSnapshot dn : snapshot.getChildren()) {
                    ProfissaoPersonagem profissao = dn.getValue(ProfissaoPersonagem.class);

                    if (profissao != null) {
                        profissoes.add(profissao);
                    }
                }

                backGroundExecutor.execute(() -> {
                    try {
                        profissaoPersonagemDao.substituirTodos(profissoes, idPersonagem);

                        liveData.postValue(new Resource<>(null, null));
                    } catch (Exception e) {
                        liveData.postValue(new Resource<>(null, e.getMessage()));
                    }
                });

                liveData.setValue(new Resource<>(null, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(new Resource<>(null, error.getMessage()));
            }
        };

        if (idPersonagem == null) return liveData;

        referenciaProfissoesPersonagem.child(idPersonagem).addListenerForSingleValueEvent(ouvinteProfissoesPersonagem);
        return liveData;
    }

    private String recuperaErro(Exception exception, String erroPadrao) {
        return exception == null ? erroPadrao : exception.getMessage();
    }

    public void removeOuvinte() {
        if (referenciaProfissoesPersonagem != null && ouvinteProfissoesPersonagem != null) {
            referenciaProfissoesPersonagem.removeEventListener(ouvinteProfissoesPersonagem);
        }
    }

    public ArrayList<ProfissaoPersonagem> recuperaProfissoesPriorizadas(String idPersonagem) {
        return profissaoPersonagemDao.recuperaProfissoesPriorizadas(idPersonagem);
    }

    public LiveData<Resource<Void>> insereProfissaoEmTodosPersonagens(ProfissaoBase profissao) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        ValueEventListener ouvinte = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Task<Void>> tarefas = new ArrayList<>();

                for (DataSnapshot personagemSnapshot : snapshot.getChildren()) {

                    String idPersonagem = personagemSnapshot.getKey();

                    if (idPersonagem == null) continue;

                    ProfissaoPersonagem novaProfissao = new ProfissaoPersonagem();

                    novaProfissao.setId(profissao.getId());
                    novaProfissao.setExperiencia(0);
                    novaProfissao.setPrioridade(false);

                    Task<Void> tarefa = referenciaProfissoesPersonagem
                        .child(idPersonagem)
                        .child(profissao.getId())
                        .setValue(novaProfissao);

                    tarefas.add(tarefa);
                }

                Tasks.whenAllComplete(tarefas).addOnCompleteListener(
                    backGroundExecutor, task -> {
                        if (task.isSuccessful()) {
                            liveData.postValue(new Resource<>(null, null));
                            return;
                        }

                        Exception exception = task.getException();
                        String erro = recuperaErro(
                            exception,
                            "Erro ao inserir profissão nos personagens"
                        );

                        liveData.postValue(new Resource<>(null, erro));
                    }
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.postValue(new Resource<>(null, error.getMessage()));
            }
        };

        referenciaProfissoesPersonagem.addListenerForSingleValueEvent(ouvinte);

        return liveData;
    }

    public LiveData<Resource<Void>> removeProfissaoDeTodosPersonagens(ProfissaoBase profissao) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        ValueEventListener ouvinte = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Task<Void>> tarefas = new ArrayList<>();

                for (DataSnapshot personagemSnapshot : snapshot.getChildren()) {

                    String idPersonagem = personagemSnapshot.getKey();

                    if (idPersonagem == null) continue;

                    Task<Void> tarefa = referenciaProfissoesPersonagem.child(idPersonagem)
                        .child(profissao.getId())
                        .removeValue();

                    tarefas.add(tarefa);
                }

                Tasks.whenAllComplete(tarefas).addOnCompleteListener(
                    task -> {

                        if (task.isSuccessful()) {
                            liveData.postValue(new Resource<>(null, null));

                            return;
                        }

                        Exception exception = task.getException();

                        String erro = recuperaErro(
                            exception,
                            "Erro ao remover profissão dos personagens"
                        );

                        liveData.postValue(new Resource<>(null, erro));
                    }
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.postValue(new Resource<>(null, error.getMessage()));
            }
        };

        referenciaProfissoesPersonagem.addListenerForSingleValueEvent(ouvinte);

        return liveData;
    }

    public void insereProfissoesNovoPersonagem(String idPersonagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        backGroundExecutor.execute(() -> {
            try {
                ArrayList<Task<Void>> tarefas = new ArrayList<>();

                ArrayList<ProfissaoBase> profissoes = new ArrayList<>(
                    profissaoDao.recuperaProfissoesBase()
                );

                ArrayList<ProfissaoPersonagem> profissoesPersonagem = new ArrayList<>();

                for (ProfissaoBase profissaoPersonagem : profissoes) {
                    ProfissaoPersonagem profissao = new ProfissaoPersonagem();

                    profissao.setId(profissaoPersonagem.getId());
                    profissao.setExperiencia(0);
                    profissao.setPrioridade(false);

                    Task<Void> tarefa = referenciaProfissoesPersonagem
                        .child(idPersonagem)
                        .child(profissaoPersonagem.getId())
                        .setValue(profissao);

                    profissoesPersonagem.add(profissao);
                    tarefas.add(tarefa);
                }

                Tasks.whenAllComplete(tarefas).addOnCompleteListener(
                    backGroundExecutor, task -> {
                        if (task.isSuccessful()) {
                            try {
                                profissaoPersonagemDao.insereProfissoes(
                                    idPersonagem,
                                    profissoesPersonagem
                                );
                                liveData.postValue(new Resource<>(null, null));

                            } catch (RuntimeException e) {
                                liveData.postValue(new Resource<>(null, e.getMessage()));
                            }

                            return;
                        }

                        Exception exception = task.getException();

                        String erro = recuperaErro(
                            exception,
                            "Erro ao inserir profissões do novo personagem"
                        );

                        liveData.postValue(new Resource<>(null, erro));
                    }
                );

            } catch (Exception e) {
                liveData.postValue(new Resource<>(null, e.getMessage()));
            }
        });
    }

    public void removeProfissoesPersonagem(String idPersonagem) {
        referenciaProfissoesPersonagem.child(idPersonagem).removeValue().addOnCompleteListener(
            backGroundExecutor, task -> {
                if (task.isSuccessful()) {
                    profissaoPersonagemDao.removeProfissoes(idPersonagem);
                }
            }
        );
    }
}
