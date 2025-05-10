package com.kevin.ceep.repository;

import static com.kevin.ceep.repository.TrabalhoProducaoRepository.destroyInstance;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_ESTOQUE;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_PERSONAGENS;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_PRODUCAO;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_PROFISSOES;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_USUARIOS2;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_VENDAS;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.ceep.model.Personagem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PersonagemRepository {
    private final DatabaseReference referenciaPersonagens;
    private final DatabaseReference referenciaUsuarios;
    private final String idUsuario;
    private ValueEventListener ouvintePersonagem, ouvinteUsuario;
    private static volatile PersonagemRepository instancia;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);

    public PersonagemRepository() {
        this.idUsuario = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.referenciaPersonagens = FirebaseDatabase.getInstance().getReference(CHAVE_PERSONAGENS);
        this.referenciaUsuarios = FirebaseDatabase.getInstance().getReference(CHAVE_USUARIOS2).child(idUsuario).child(CHAVE_PERSONAGENS);
    }

    public static synchronized PersonagemRepository getInstance() {
        String idUsuario = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (instancia == null || instancia.idUsuario.equals(idUsuario)) {
            destroyInstance();
            instancia = new PersonagemRepository();
        }
        return instancia;
    }

    @NonNull
    public MutableLiveData<Resource<ArrayList<Personagem>>> recuperaPersonagensServidor(ArrayList<String> idsPersonagens) {
        MutableLiveData<Resource<ArrayList<Personagem>>> personagensEncontrados = new MutableLiveData<>();
        ouvintePersonagem = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Personagem> personagensServidor = new ArrayList<>();
                for (DataSnapshot dn: snapshot.getChildren()) {
                    Personagem personagem = dn.getValue(Personagem.class);
                    if (personagem == null) return;
                    boolean encontrado= idsPersonagens.stream().anyMatch(personagem.getId()::equals);
                    if (encontrado) personagensServidor.add(personagem);
                }
                personagensEncontrados.postValue(new Resource<>(personagensServidor, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                personagensEncontrados.postValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaPersonagens.addValueEventListener(ouvintePersonagem);
        return personagensEncontrados;
    }

    public LiveData<Resource<ArrayList<String>>> pegaIdsPersonagens() {
        MutableLiveData<Resource<ArrayList<String>>> livedata = new MutableLiveData<>();
        ouvinteUsuario = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> ids= new ArrayList<>();
                for (DataSnapshot dn: snapshot.getChildren()) {
                    ids.add(dn.getKey());
                }
                livedata.setValue(new Resource<>(ids, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                livedata.setValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaUsuarios.addListenerForSingleValueEvent(ouvinteUsuario);
        return livedata;
    }

    public LiveData<Resource<Void>> modificaPersonagem(Personagem personagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (personagemInvalido(personagem)) {
            liveData.postValue(new Resource<>(null, "Personagem inválido"));
            return liveData;
        }
        referenciaPersonagens.child(personagem.getId()).setValue(personagem).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao modificar personagem");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    public LiveData<Resource<Void>> inserePersonagem(Personagem personagem) {
       MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
       if (personagemInvalido(personagem)) {
           liveData.postValue(new Resource<>(null, "Personagem inválido"));
           return liveData;
       }
       referenciaUsuarios.child(personagem.getId()).setValue(true).addOnCompleteListener(backgroundExecutor, task -> {
           if (task.isSuccessful()) {
                referenciaPersonagens.child(personagem.getId()).setValue(personagem).addOnCompleteListener(backgroundExecutor, task1 -> {
                    if (task1.isSuccessful()) {
                        liveData.postValue(new Resource<>(null, null));
                        return;
                    }
                    referenciaUsuarios.child(personagem.getId()).removeValue().addOnCompleteListener(backgroundExecutor, task2 -> {
                        if (task2.isSuccessful()) {
                            Exception exception = task1.getException();
                            String erro = recuperaErro(exception, "Erro desconhecido ao inserir personagem");
                            liveData.postValue(new Resource<>(null, erro));
                        }
                    });
                });
                return;
           }
           Exception exception = task.getException();
           String erro = recuperaErro(exception, "Erro desconhecido ao inserir personagem");
           liveData.postValue(new Resource<>(null, erro));
       });
        return liveData;
    }

    private boolean personagemInvalido(Personagem personagem) {
        return personagem == null || personagem.getId() == null || personagem.getId().isEmpty() ||
            personagem.getNome() == null || personagem.getNome().isEmpty() ||
            personagem.getEmail() == null || personagem.getEmail().isEmpty() ||
            personagem.getSenha() == null || personagem.getSenha().isEmpty();
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }

    public LiveData<Resource<Void>> removePersonagem(Personagem personagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (idPersonagemInavalido(personagem)) {
            liveData.postValue(new Resource<>(null, "Erro id personagem inválido"));
            return liveData;
        }
        referenciaPersonagens.child(personagem.getId()).removeValue().addOnCompleteListener(backgroundExecutor, task -> {
           if (task.isSuccessful()) {
               FirebaseDatabase meuBanco = FirebaseDatabase.getInstance();
               List<Task<Void>> tarefas = new ArrayList<>();
               tarefas.add(meuBanco.getReference(CHAVE_ESTOQUE).child(personagem.getId()).removeValue());
               tarefas.add(meuBanco.getReference(CHAVE_PROFISSOES).child(personagem.getId()).removeValue());
               tarefas.add(meuBanco.getReference(CHAVE_PRODUCAO).child(personagem.getId()).removeValue());
               tarefas.add(meuBanco.getReference(CHAVE_VENDAS).child(personagem.getId()).removeValue());
               tarefas.add(referenciaUsuarios.child(personagem.getId()).removeValue());
               Tasks.whenAllComplete(tarefas).addOnCompleteListener(backgroundExecutor, task1 -> {
                  if (task1.isSuccessful()) {
                    liveData.postValue(new Resource<>(null, null));
                    return;
                  }
                   Exception exception = task1.getException();
                   String erro = recuperaErro(exception, "Erro desconhecido ao remover personagem");
                   liveData.postValue(new Resource<>(null, erro));
               });
               return;
           }
           Exception exception = task.getException();
           String erro = recuperaErro(exception, "Erro desconhecido ao remover personagem");
           liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    private boolean idPersonagemInavalido(Personagem personagem) {
        return personagem == null || personagem.getId() == null || personagem.getId().isEmpty();
    }

    public void removeOuvinte() {
        if (referenciaPersonagens != null && ouvintePersonagem != null) {
            referenciaPersonagens.removeEventListener(ouvintePersonagem);
        }
        if (referenciaUsuarios != null && ouvinteUsuario != null) {
            referenciaUsuarios.removeEventListener(ouvinteUsuario);
        }
    }
}
