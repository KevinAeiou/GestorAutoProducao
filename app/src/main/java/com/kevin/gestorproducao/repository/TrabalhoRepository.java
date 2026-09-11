package com.kevin.gestorproducao.repository;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_LISTA_TRABALHO;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.gestorproducao.dao.TrabalhoDao;
import com.kevin.gestorproducao.model.Trabalho;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TrabalhoRepository {
    private final DatabaseReference referenciaTrabalho;
    private final MutableLiveData<Resource<ArrayList<Trabalho>>> trabalhosEncontrados;
    private static volatile TrabalhoRepository instancia;
    private ValueEventListener ouvinteTrabalho;
    private final TrabalhoDao trabalhoDao;
    private final ProfissaoRepository profissaoRepository;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);
    private static Map<String, String> mapaProfissoes = new HashMap<>();

    public TrabalhoRepository(Context context) {
        this.referenciaTrabalho = FirebaseDatabase.getInstance().getReference(CHAVE_LISTA_TRABALHO);
        this.trabalhosEncontrados = new MutableLiveData<>();
        this.trabalhoDao = new TrabalhoDao(context);
        this.profissaoRepository = ProfissaoRepository.getInstance(context);
    }

    public static synchronized TrabalhoRepository getInstancia(Context context) {
        if (instancia == null) {
            instancia = new TrabalhoRepository(context);
        }
        return instancia;
    }

    public LiveData<Resource<Void>> modificaTrabalho(Trabalho trabalho) {
        MutableLiveData<Resource<Void>> liveData = new  MutableLiveData<>();

        mapaProfissoes = profissaoRepository.recuperaMapaProfissoesLocal();

        backgroundExecutor.execute(() -> {

            mapaProfissoes = mapaProfissoesInvertido();

            String idProfissao = mapaProfissoes.get(trabalho.getProfissao());
            if (idProfissao != null) {
                trabalho.setProfissao(idProfissao);
            }

            referenciaTrabalho.child(trabalho.getId()).setValue(trabalho).addOnCompleteListener(
                backgroundExecutor, task -> {
                    if (task.isSuccessful()) {
                        try {
                            trabalhoDao.modificaTrabalho(trabalho);
                            liveData.postValue(new Resource<>(null, null));

                        } catch (RuntimeException e) {
                            liveData.postValue(new Resource<>(null, e.getMessage()));
                        }
                        return;
                    }

                    Exception exception = task.getException();
                    String erro = recuperaErro(exception, "Erro desconhecido ao modificar trabalho");
                    liveData.postValue(new Resource<>(null, erro));
                }
            );
        });

        return liveData;
    }

    private Map<String, String> mapaProfissoesInvertido() {
        Map<String, String> mapaInvertido = new HashMap<>();

        for (Map.Entry<String, String> entry : mapaProfissoes.entrySet()) {
            String id = entry.getKey();
            String nome = entry.getValue();

            mapaInvertido.put(nome, id);
        }
        return mapaInvertido;
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }

    public LiveData<Resource<Void>> insereTrabalho(Trabalho trabalho) {
        MutableLiveData<Resource<Void>> liveData = new  MutableLiveData<>();
        mapaProfissoes = profissaoRepository.recuperaMapaProfissoesLocal();
        mapaProfissoes = mapaProfissoesInvertido();

        String idProfissao = mapaProfissoes.get(trabalho.getProfissao());
        if (idProfissao != null) {
            trabalho.setProfissao(idProfissao);
        }

        referenciaTrabalho.child(trabalho.getId()).setValue(trabalho).addOnCompleteListener(
            backgroundExecutor,
            task -> {
            if (task.isSuccessful()) {
                try {
                    trabalhoDao.insereTrabalho(trabalho);
                    liveData.postValue(new Resource<>(null, null));

                } catch (RuntimeException e) {
                    liveData.postValue(new Resource<>(null, e.getMessage()));
                }
                return;
            }

            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao inserir trabalho");
            liveData.postValue(new Resource<>(null, erro));
        });

        return liveData;
    }

    public LiveData<Resource<Void>> removeTrabalho(Trabalho trabalho) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        referenciaTrabalho.child(trabalho.getId()).removeValue().addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                try {
                    trabalhoDao.removerTrabalho(trabalho);

                    liveData.postValue(new Resource<>(null, null));
                } catch (Exception e) {
                    liveData.postValue(new Resource<>(null, e.getMessage()));
                }
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao remover trabalho");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }
    public LiveData<Resource<ArrayList<Trabalho>>> recuperaTrabalhos() {
        backgroundExecutor.execute(() -> {
            try {
                ArrayList<Trabalho> trabalhos = new ArrayList<>(trabalhoDao.recuperaTrabalhos());
                trabalhosEncontrados.postValue(new Resource<>(trabalhos, null));
            } catch (RuntimeException e) {
                trabalhosEncontrados.postValue(new Resource<>(null, e.getMessage()));
            }
        });

        return trabalhosEncontrados;
    }

    public LiveData<Resource<Void>> sincronizaTrabalhos() {
        ArrayList<Trabalho> trabalhosServidor = new ArrayList<>();
        MutableLiveData<Resource<Void>> liveData = new  MutableLiveData<>();

        ouvinteTrabalho = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trabalhosServidor.clear();
                for (DataSnapshot dn : snapshot.getChildren()) {
                    Trabalho trabalho = dn.getValue(Trabalho.class);
                    if (trabalho != null) {
                        trabalhosServidor.add(trabalho);
                    }
                }

                backgroundExecutor.execute(() -> {
                    try {
                        trabalhoDao.substituirTodos(trabalhosServidor);

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

        referenciaTrabalho.addListenerForSingleValueEvent(ouvinteTrabalho);
        return liveData;
    }

    public String trabalhoEspecificoExiste(Trabalho trabalho) {
        return trabalhoDao.trabalhoJaExiste(trabalho);
    }

    public LiveData<Resource<ArrayList<Trabalho>>> recuperaTrabalhosNecessarios(Trabalho trabalho) {
        MutableLiveData<Resource<ArrayList<Trabalho>>> liveData = new MutableLiveData<>();

        backgroundExecutor.execute(() -> {
            try {
                ArrayList<Trabalho> trabalhos = new ArrayList<>(trabalhoDao.recuperaTrabalhosNecessarios(trabalho));
                liveData.postValue(new Resource<>(trabalhos, null));
            } catch (RuntimeException e) {
                liveData.postValue(new Resource<>(null, e.getMessage()));
            }
        });

        return liveData;
    }

    public Trabalho recuperaTrabalhoPorId(String id) {
        return trabalhoDao.recuperaTrabalhoPorId(id);
    }

    public void removeOuvinte() {
        if (referenciaTrabalho != null && ouvinteTrabalho != null) {
            referenciaTrabalho.removeEventListener(ouvinteTrabalho);
        }
    }

    public Trabalho recuperaTrabalhoPorNome(String nome) {
        return trabalhoDao.recuperaTrabalhoPorNome(nome);
    }

    public Trabalho recuperaTrabalhoProducaoRecursos(Trabalho trabalho) {
        return trabalhoDao.recuperaTrabalhoProducaoRecursos(trabalho);
    }

    public ArrayList<Trabalho> recuperaMaisVendidos(String idPersonagem) {
        return trabalhoDao.recuperaMaisVendidos(idPersonagem);
    }

    public ArrayList<Trabalho> recuperaTrabalhosComuns(
        int nivelProducao,
       String profissao
    ) {
        return trabalhoDao.recuperaTrabalhosComuns(nivelProducao, profissao);
    }

    public Trabalho recuperaTrabalhoPorIdTrabalhoNecessario(String idTrabalho) {
        return trabalhoDao.recuperaTrabalhoPorIdTrabalhoNecessario(idTrabalho);
    }

    public ArrayList<Trabalho> recuperaTrabalhosNecessariosPorId(List<String> trabalhosNecessarios) {
        return trabalhoDao.recuperaTrabalhosNecessariosPorId(trabalhosNecessarios);
    }
}
