package com.kevin.gestorproducao.repository;

import static com.kevin.gestorproducao.repository.TrabalhoProducaoRepository.destroyInstance;
import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_PERSONAGENS;
import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_USUARIOS2;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.gestorproducao.dao.PersonagemDao;
import com.kevin.gestorproducao.model.Personagem;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PersonagemRepository {
    private final DatabaseReference referenciaPersonagens;
    private final DatabaseReference referenciaUsuarios;
    private ValueEventListener ouvintePersonagem, ouvinteUsuario;
    private static volatile PersonagemRepository instancia;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);
    private final PersonagemDao personagemDao;

    public PersonagemRepository(Context context) {
        this.referenciaPersonagens = FirebaseDatabase.getInstance().getReference(CHAVE_PERSONAGENS);
        this.referenciaUsuarios = FirebaseDatabase.getInstance().getReference(CHAVE_USUARIOS2);
        this.personagemDao = new PersonagemDao(context);
    }

    public static synchronized PersonagemRepository getInstance(Context context) {
        if (instancia == null) {
            destroyInstance();
            instancia = new PersonagemRepository(context);
        }

        return instancia;
    }

    public LiveData<Resource<ArrayList<Personagem>>> recuperaPersonagens() {
        MutableLiveData<Resource<ArrayList<Personagem>>> personagensEncontrados = new MutableLiveData<>();

        ArrayList<Personagem> personagens = new ArrayList<>(personagemDao.recuperaPersonagens());

        personagensEncontrados.setValue(new Resource<>(personagens, null));

        return personagensEncontrados;
    }

    public LiveData<Resource<Void>> modificaPersonagem(Personagem personagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        if (personagemEhInvalido(personagem)) {
            liveData.postValue(new Resource<>(null, "Personagem inválido"));
            return liveData;
        }

        referenciaPersonagens.child(personagem.getId()).setValue(personagem).addOnCompleteListener(
            backgroundExecutor,
            task -> {
                if (task.isSuccessful()) {
                    try {
                        personagemDao.modificaPersonagem(personagem);
                        liveData.postValue(new Resource<>(null, null));

                    } catch (RuntimeException e) {
                        liveData.postValue(new Resource<>(null, e.getMessage()));
                    }
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao modificar personagem");
                liveData.postValue(new Resource<>(null, erro));
            }
        );

        return liveData;
    }

    public LiveData<Resource<Void>> inserePersonagemUsuario(Personagem personagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        if (personagemEhInvalido(personagem)) {
            liveData.postValue(new Resource<>(null, "Personagem inválido"));
            return liveData;
        }

        String idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        referenciaUsuarios.child(idUsuario).child(CHAVE_PERSONAGENS).child(personagem.getId()).setValue(true)
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    liveData.postValue(new Resource<>(null, null));
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao inserir personagem");

                liveData.postValue(new Resource<>(null, erro));
            }
        );

        return liveData;
    }
    public void inserePersonagem(Personagem personagem) {
        referenciaPersonagens.child(personagem.getId()).setValue(personagem).addOnCompleteListener(
            backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    personagemDao.inserePersonagem(personagem);
                }
            }
        );
    }

    private boolean personagemEhInvalido(Personagem personagem) {
        return personagem == null || personagem.getId() == null || personagem.getId().isEmpty() ||
            personagem.getNome() == null || personagem.getNome().isEmpty() ||
            personagem.getEmail() == null || personagem.getEmail().isEmpty() ||
            personagem.getSenha() == null || personagem.getSenha().isEmpty();
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }

    public LiveData<Resource<Void>> removePersonagemUsuario(String idPersonagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        if (idPersonagem == null || idPersonagem.isEmpty()) {
            liveData.setValue(new Resource<>(null, "Erro id do personagem inválido"));
            return liveData;
        }

        String idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        referenciaUsuarios.child(idUsuario).child(CHAVE_PERSONAGENS).child(idPersonagem).removeValue()
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    liveData.postValue(new Resource<>(null, null));
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao remover personagem");
                liveData.postValue(new Resource<>(null, erro));
            }
        );

        return liveData;
    }

    public LiveData<Resource<Void>> removePersonagem(String idPersonagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        if (idPersonagem == null || idPersonagem.isEmpty()) {
            liveData.setValue(new Resource<>(null, "Erro id do personagem inválido"));
            return liveData;
        }

        referenciaPersonagens.child(idPersonagem).removeValue().addOnCompleteListener(
            backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        personagemDao.removePersonagem(idPersonagem);
                        liveData.postValue(new Resource<>(null, null));
                    } catch (RuntimeException e) {
                        liveData.postValue(new Resource<>(null, e.getMessage()));
                    }
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao remover personagem");
                liveData.postValue(new Resource<>(null, erro));
            }
        );

        return liveData;
    }

    public void removeOuvinte() {
        if (referenciaPersonagens != null && ouvintePersonagem != null) {
            referenciaPersonagens.removeEventListener(ouvintePersonagem);
        }
        if (referenciaUsuarios != null && ouvinteUsuario != null) {
            referenciaUsuarios.removeEventListener(ouvinteUsuario);
        }
    }

    public LiveData<Resource<Void>> sincronizaPersonagens(String usuarioId) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        ArrayList<Personagem> personagensServidor = new ArrayList<>();

        ouvinteUsuario = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChildren()) {
                    backgroundExecutor.execute(() -> {
                        personagemDao.substituirTodos(new ArrayList<>());
                        liveData.postValue(new Resource<>(null, null));
                    });
                    return;
                }

                int total = (int) snapshot.getChildrenCount();
                AtomicInteger carregados = new AtomicInteger(0);

                personagensServidor.clear();

                for (DataSnapshot dn: snapshot.getChildren()) {

                    ouvintePersonagem = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Personagem personagem = snapshot.getValue(Personagem.class);
                            if (personagem != null) {
                                personagensServidor.add(personagem);
                            }
                            if (carregados.incrementAndGet() == total) {
                                backgroundExecutor.execute(() -> {
                                    try {
                                        personagemDao.substituirTodos(personagensServidor);

                                        liveData.postValue(new Resource<>(null, null));
                                    } catch (Exception e) {
                                        liveData.postValue(new Resource<>(null, e.getMessage()));
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            liveData.postValue(new Resource<>(null, error.getMessage()));
                        }
                    };

                    referenciaPersonagens.child(dn.getKey()).addListenerForSingleValueEvent(ouvintePersonagem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(new Resource<>(null, error.getMessage()));
            }
        };

        referenciaUsuarios.child(usuarioId).child(CHAVE_PERSONAGENS).addListenerForSingleValueEvent(ouvinteUsuario);
        return liveData;
    }
}
