package com.kevin.gestorproducao.repository;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_LISTA_RECURSOS;
import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_RECURSO;

import android.annotation.SuppressLint;
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
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.RecursoComum;
import com.kevin.gestorproducao.model.RecursoComumAvancado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RecursosProducaoRepository {
    private final DatabaseReference referenciaRecursos;
    private final DatabaseReference referenciaListaRecursos;
    private final Context meuContexto;
    private static volatile RecursosProducaoRepository instancia;
    private final MutableLiveData<Resource<ArrayList<RecursoComumAvancado>>> recursosEncontrados;
    private final Executor backGroundExecutor = Executors.newFixedThreadPool(2);
    private ValueEventListener ouvinteRecursoAvancado;
    private ValueEventListener ouvinteRecurso;

    public RecursosProducaoRepository(Context context) {
        FirebaseDatabase meuBanco = FirebaseDatabase.getInstance();
        referenciaRecursos = meuBanco.getReference(CHAVE_RECURSO);
        referenciaListaRecursos = meuBanco.getReference(CHAVE_LISTA_RECURSOS);
        recursosEncontrados = new MutableLiveData<>();
        meuContexto = context;
    }

    public static synchronized RecursosProducaoRepository getInstance(Context contexto) {
        if (instancia == null) {
            instancia = new RecursosProducaoRepository(contexto);
        }
        return instancia;
    }

    public LiveData<Resource<ArrayList<RecursoComumAvancado>>> recuperaRecursos(String idPersonagem) {
        ouvinteRecursoAvancado = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<RecursoComumAvancado> recursos = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dn : snapshot.getChildren()) {
                        RecursoComumAvancado recursoAvancado = dn.getValue(RecursoComumAvancado.class);
                        assert recursoAvancado != null;
                        recursos.add(recursoAvancado);
                    }
                    List<RecursoComumAvancado> recursosServidor = Collections.synchronizedList(new ArrayList<>());
                    ArrayList<Task<DataSnapshot>> tarefas = new ArrayList<>(recursos.size());
                    for (RecursoComumAvancado recursoAvancado : recursos) {
                        tarefas.add(referenciaListaRecursos.child(recursoAvancado.getId()).get());
                    }
                    Tasks.whenAllSuccess(tarefas).addOnCompleteListener(backGroundExecutor, tarefasCombinadas -> {
                       if (tarefasCombinadas.isSuccessful()) {
                           for (int i = 0; i < tarefasCombinadas.getResult().size(); i ++) {
                               DataSnapshot ds = (DataSnapshot) tarefasCombinadas.getResult().get(i);
                               RecursoComum recursoComum = ds.getValue(RecursoComum.class);
                               if (recursoComum == null) continue;
                               RecursoComumAvancado recursoAvancado = recursos.get(i);
                               recursoAvancado.setNome(recursoComum.getNome());
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

        if (idPersonagem == null) return recursosEncontrados;
        referenciaRecursos.child(idPersonagem).addListenerForSingleValueEvent(ouvinteRecursoAvancado);
        return recursosEncontrados;
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }

    public LiveData<Resource<Void>> insereNovosRecursos(String idPersonagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        ouvinteRecurso = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<RecursoComum> recursoComums = new ArrayList<>();
                    for (DataSnapshot dn : snapshot.getChildren()) {
                        RecursoComum recursoComum = dn.getValue(RecursoComum.class);
                        if (recursoComum == null) continue;
                        recursoComums.add(recursoComum);
                    }
                    ArrayList<Task<Void>> tarefas = new ArrayList<>(recursoComums.size());
                    for (RecursoComum recursoComum : recursoComums) {
                        RecursoComumAvancado recursoAvancado = new RecursoComumAvancado();
                        recursoAvancado.setId(recursoComum.getId());
                        tarefas.add(referenciaRecursos.child(idPersonagem).child(recursoAvancado.getId()).setValue(recursoAvancado));
                    }
                    Tasks.whenAllComplete(tarefas).addOnCompleteListener(tarefasCombinadas -> {
                       if (tarefasCombinadas.isSuccessful()) {
                           liveData.postValue(new Resource<>(null, null));
                           return;
                       }
                        Exception exception = tarefasCombinadas.getException();
                        String erro = recuperaErro(exception, "Erro desconhecido ao inserir novos recursoComums");
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
                RecursoComum novoRecursoComum = new RecursoComum();
                novoRecursoComum.setNome(item);
                referenciaListaRecursos.child(novoRecursoComum.getId()).setValue(novoRecursoComum);
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

    public LiveData<Resource<Void>> modificaListaRecursos(
        ArrayList<RecursoComumAvancado> recursos,
        String idPersonagem
    ) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        ArrayList<Task<Void>> tarefas = new ArrayList<>(recursos.size());
        for (RecursoComumAvancado recurso : recursos) {
            tarefas.add(referenciaRecursos.child(idPersonagem).child(recurso.getId()).setValue(recurso));
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
