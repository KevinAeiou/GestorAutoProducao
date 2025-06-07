package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.RecursoAvancado;
import com.kevin.ceep.repository.RecursosProducaoRepository;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.ui.viewModel.singleEvent.SingleLiveEvent;

import java.util.ArrayList;

public class RecursosProducaoViewModel extends ViewModel {
    private final RecursosProducaoRepository repository;
    private final SingleLiveEvent<Resource<ArrayList<RecursoAvancado>>> recuperaRecursosResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> insercaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> modificacaoResultado = new SingleLiveEvent<>();
    public RecursosProducaoViewModel(RecursosProducaoRepository repository) {
        this.repository = repository;
    }
    public SingleLiveEvent<Resource<ArrayList<RecursoAvancado>>> getRecuperaRecursosResultado() {
        return recuperaRecursosResultado;
    }

    public SingleLiveEvent<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }

    public SingleLiveEvent<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }
    public void recuperaRecursos() {
        Observer<? super Resource<ArrayList<RecursoAvancado>>> observer = new Observer<Resource<ArrayList<RecursoAvancado>>>() {
            @Override
            public void onChanged(Resource<ArrayList<RecursoAvancado>> recursosEncontrados) {
                recuperaRecursosResultado.setValue(recursosEncontrados);
                repository.recuperaRecursos().removeObserver(this);
            }
        };
        repository.recuperaRecursos().observeForever(observer);
    }
    public void insereListaRecursos() {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                insercaoResultado.setValue(resultado);
                repository.insereNovosRecursos().removeObserver(this);
            }
        };
        repository.insereNovosRecursos().observeForever(observer);
    }
    public void modificaListaRecursos(ArrayList<RecursoAvancado> recursos) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                modificacaoResultado.setValue(voidResource);
                repository.modificaListaRecursos(recursos).removeObserver(this);
            }
        };
        repository.modificaListaRecursos(recursos).observeForever(observer);
    }
    public void removeOuvinte() {
        repository.removeOuvinte();
    }

}
