package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.repository.TrabalhoRepository;

import java.util.ArrayList;

public class ListaNovaProducaoViewModel extends ViewModel {
    private final TrabalhoRepository trabalhoRepository;
    public ListaNovaProducaoViewModel(TrabalhoRepository repository) {
        this.trabalhoRepository = repository;
    }

    public LiveData<Resource<ArrayList<Trabalho>>> pegaTodosTrabalhos() {
        return trabalhoRepository.recuperaTrabalhos();
    }

    public void removeOuvinte() {
        trabalhoRepository.removeOuvinte();
    }
}
