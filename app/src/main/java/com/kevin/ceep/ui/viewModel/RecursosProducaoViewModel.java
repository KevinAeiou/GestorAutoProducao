package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.RecursoAvancado;
import com.kevin.ceep.repository.RecursosProducaoRepository;
import com.kevin.ceep.repository.Resource;

import java.util.ArrayList;

public class RecursosProducaoViewModel extends ViewModel {
    private final RecursosProducaoRepository repository;

    public RecursosProducaoViewModel(RecursosProducaoRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<ArrayList<RecursoAvancado>>> recuperaRecursos() {
        return repository.recuperaRecursos();
    }

    public LiveData<Resource<Void>> insereListaRecursos() {
        return repository.insereNovosRecursos();
    }

    public void removeOuvinte() {
        repository.removeOuvinte();
    }

    public LiveData<Resource<Void>> modificaListaRecursos(ArrayList<RecursoAvancado> recursos) {
        return repository.modificaListaRecursos(recursos);
    }
}
