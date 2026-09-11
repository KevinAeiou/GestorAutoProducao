package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.gestorproducao.model.RecursoComumAvancado;
import com.kevin.gestorproducao.repository.RecursosProducaoRepository;
import com.kevin.gestorproducao.repository.Resource;

import java.util.ArrayList;

public class RecursosProducaoViewModel extends ViewModel {
    private final RecursosProducaoRepository repository;
    private final MutableLiveData<String> idPersonagem = new MutableLiveData<>();
    private final LiveData<Resource<ArrayList<RecursoComumAvancado>>> recursos;
    private final MediatorLiveData<Resource<Void>> insercaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> modificacaoResultado = new MediatorLiveData<>();
    public RecursosProducaoViewModel(RecursosProducaoRepository repository) {
        this.repository = repository;
        recursos = Transformations.switchMap(
            idPersonagem,
            id -> {
                if (id == null) return new MutableLiveData<>();

                return repository.recuperaRecursos(id);
            }
        );
    }

    public MediatorLiveData<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }

    public void insereListaRecursos() {
        LiveData<Resource<Void>> source = repository.insereNovosRecursos(idPersonagem.getValue());

        insercaoResultado.addSource(source, resultado -> {
            insercaoResultado.setValue(resultado);
            insercaoResultado.removeSource(source);
        });
    }
    public void modificaListaRecursos(ArrayList<RecursoComumAvancado> recursos) {
        LiveData<Resource<Void>> source = repository.modificaListaRecursos(
            recursos,
            idPersonagem.getValue()
        );

        modificacaoResultado.addSource(source, resultado -> {
            modificacaoResultado.setValue(resultado);
            modificacaoResultado.removeSource(source);
        });
    }
    public void removeObservador() {
        repository.removeOuvinte();
    }

    public void setIdPersonagem(String id) {
        if (id == null) return;

        if (id.equals(idPersonagem.getValue())) return;

        idPersonagem.setValue(id);
    }

    public LiveData<Resource<ArrayList<RecursoComumAvancado>>> getRecursos() {
        return recursos;
    }
}
