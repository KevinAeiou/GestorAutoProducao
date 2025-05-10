package com.kevin.ceep.repository;

import static com.kevin.ceep.repository.TrabalhoProducaoRepository.destroyInstance;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_LISTA_RECURSOS;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_RECURSO;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.kevin.ceep.R;
import com.kevin.ceep.model.Recurso;
import com.kevin.ceep.model.RecursoAvancado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RecursosProducaoRepository {
    private final DatabaseReference referenciaRecursos;
    private final DatabaseReference referenciaListaRecursos;
    private final Context meuContexto;
    @SuppressLint("StaticFieldLeak")
    private static volatile RecursosProducaoRepository instancia;
    private final MutableLiveData<Resource<ArrayList<RecursoAvancado>>> recursosEncontrados;
    private final String idPersonagem;
    private final Executor backGroundExecutor = Executors.newFixedThreadPool(2);
    private ValueEventListener ouvinteRecursoAvancado;
    private ValueEventListener ouvinteRecurso;

    public RecursosProducaoRepository(String idPersonagem, Context context) {
        FirebaseDatabase meuBanco = FirebaseDatabase.getInstance();
        this.idPersonagem = idPersonagem;
        referenciaRecursos = meuBanco.getReference(CHAVE_RECURSO).child(idPersonagem);
        referenciaListaRecursos = meuBanco.getReference(CHAVE_LISTA_RECURSOS);
        recursosEncontrados = new MutableLiveData<>();
        meuContexto = context;
    }

    public static synchronized RecursosProducaoRepository getInstancia(String idPersonagem, Context contexto) {
        if (instancia == null || !instancia.idPersonagem.equals(idPersonagem)) {
            destroyInstance();
            instancia = new RecursosProducaoRepository(idPersonagem, contexto);
        }
        return instancia;
    }

    public LiveData<Resource<ArrayList<RecursoAvancado>>> recuperaRecursos() {
        ouvinteRecursoAvancado = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<RecursoAvancado> recursos = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dn : snapshot.getChildren()) {
                        RecursoAvancado recursoAvancado = dn.getValue(RecursoAvancado.class);
                        assert recursoAvancado != null;
                        recursos.add(recursoAvancado);
                    }
                    List<RecursoAvancado> recursosServidor = Collections.synchronizedList(new ArrayList<>());
                    ArrayList<Task<DataSnapshot>> tarefas = new ArrayList<>(recursos.size());
                    for (RecursoAvancado recursoAvancado : recursos) {
                        tarefas.add(referenciaListaRecursos.child(recursoAvancado.getId()).get());
                    }
                    Tasks.whenAllSuccess(tarefas).addOnCompleteListener(backGroundExecutor, tarefasCombinadas -> {
                       if (tarefasCombinadas.isSuccessful()) {
                           for (int i = 0; i < tarefasCombinadas.getResult().size(); i ++) {
                               DataSnapshot ds = (DataSnapshot) tarefasCombinadas.getResult().get(i);
                               Recurso recurso = ds.getValue(Recurso.class);
                               if (recurso == null) continue;
                               RecursoAvancado recursoAvancado = recursos.get(i);
                               recursoAvancado.setNome(recurso.getNome());
                               recursosServidor.add(recursoAvancado);
                           }
                           recursosEncontrados.postValue(new Resource<>(new ArrayList<>(recursosServidor), null));
                           return;
                       }
                        Exception exception = tarefasCombinadas.getException();
                        String erro = recuperaErro(exception, "Erro desconhecido ao recuperar profissões");
                        recursosEncontrados.postValue(new Resource<>(null, erro));
                    });
                    return;
                }
                recursosEncontrados.postValue(new Resource<>(recursos, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                recursosEncontrados.postValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaRecursos.addListenerForSingleValueEvent(ouvinteRecursoAvancado);
        return recursosEncontrados;
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }

    public LiveData<Resource<Void>> insereNovosRecursos() {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        ouvinteRecurso = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<Recurso> recursos = new ArrayList<>();
                    for (DataSnapshot dn : snapshot.getChildren()) {
                        Recurso recurso = dn.getValue(Recurso.class);
                        if (recurso == null) continue;
                        recursos.add(recurso);
                    }
                    ArrayList<Task<Void>> tarefas = new ArrayList<>(recursos.size());
                    for (Recurso recurso : recursos) {
                        RecursoAvancado recursoAvancado = new RecursoAvancado();
                        recursoAvancado.setId(recurso.getId());
                        tarefas.add(referenciaRecursos.child(recursoAvancado.getId()).setValue(recursoAvancado));
                    }
                    Tasks.whenAllComplete(tarefas).addOnCompleteListener(tarefasCombinadas -> {
                       if (tarefasCombinadas.isSuccessful()) {
                           liveData.postValue(new Resource<>(null, null));
                           return;
                       }
                        Exception exception = tarefasCombinadas.getException();
                        String erro = recuperaErro(exception, "Erro desconhecido ao inserir novos recursos");
                        liveData.postValue(new Resource<>(null, erro));
                    });
                    return;
                }
                insereListaRecursosBase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.postValue(new Resource<>(null, error.getMessage()));
            }
        };
        referenciaListaRecursos.addListenerForSingleValueEvent(ouvinteRecurso);
        return liveData;
    }

    @SuppressLint("ResourceType")
    public void insereListaRecursosBase() {
        String[] catalisadores = meuContexto.getResources().getStringArray(R.array.catalisadores);
        String[] essencias = meuContexto.getResources().getStringArray(R.array.essencias);
        String[] substancias = meuContexto.getResources().getStringArray(R.array.substancias);
        ArrayList<String[]> novaLista = new ArrayList<>();
        novaLista.add(catalisadores);
        novaLista.add(essencias);
        novaLista.add(substancias);
        for (String[] array : novaLista) {
            for (String item : array) {
                Recurso novoRecurso = new Recurso();
                novoRecurso.setNome(item);
                Log.d("recursos", "Recurso: " + novoRecurso);
                referenciaListaRecursos.child(novoRecurso.getId()).setValue(novoRecurso);
            }
        }
    }

    public void removeOuvinte() {
        if (referenciaRecursos != null && ouvinteRecursoAvancado != null) {
            referenciaRecursos.removeEventListener(ouvinteRecursoAvancado);
        }
        if (referenciaListaRecursos != null && ouvinteRecurso != null) {
            referenciaListaRecursos.removeEventListener(ouvinteRecurso);
        }
    }

    public LiveData<Resource<Void>> modificaListaRecursos(ArrayList<RecursoAvancado> recursos) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        ArrayList<Task<Void>> tarefas = new ArrayList<>(recursos.size());
        for (RecursoAvancado recurso : recursos) {
            tarefas.add(referenciaRecursos.child(recurso.getId()).setValue(recurso));
        }
        Tasks.whenAllComplete(tarefas).addOnCompleteListener(backGroundExecutor, tarefasCombinadas -> {
           if (tarefasCombinadas.isSuccessful()) {
               liveData.postValue(new Resource<>(null, null));
               return;
           }
           Exception exception = tarefasCombinadas.getException();
           String erro = recuperaErro(exception, "Erro desconhecido ao modificar recursos");
           liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }
}
