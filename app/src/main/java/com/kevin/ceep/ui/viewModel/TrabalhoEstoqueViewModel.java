package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.repository.TrabalhoEstoqueRepository;

import java.util.ArrayList;

public class TrabalhoEstoqueViewModel extends ViewModel {
    private final TrabalhoEstoqueRepository repository;

    public TrabalhoEstoqueViewModel(TrabalhoEstoqueRepository repository) {
        this.repository = repository;
    }
    public LiveData<Resource<ArrayList<TrabalhoEstoque>>> recuperaTrabalhosEstoque() {
        return repository.recuperaEstoque();
    }

    public LiveData<Resource<Void>> modificaTrabalhoEstoque(TrabalhoEstoque trabalhoEstoqueModificado) {
        return repository.modificaTrabalhoEstoque(trabalhoEstoqueModificado);
    }

    public LiveData<Resource<TrabalhoEstoque>> recuperaTrabalhoEstoquePorIdTrabalho(String idTrabalho) {
        return repository.recuperaTrabalhoEstoquePorIdTrabalho(idTrabalho);
    }

    public LiveData<Resource<Void>> insereTrabalhoEstoque(TrabalhoEstoque trabalhoEstoque) {
        return repository.insereTrabalhoEstoque(trabalhoEstoque);
    }
    public LiveData<Resource<Void>> removeTrabalhoEstoque(TrabalhoEstoque trabalhoRemovido) {
        return repository.removeTrabalhoEstoque(trabalhoRemovido);
    }

    public LiveData<Resource<Void>> removeReferenciaTrabalhoEspecifico(Trabalho trabalho) {
        return repository.removeReferenciaTrabalhoEspecifico(trabalho);
    }

    public void removeOuvinte() {
        repository.removeOuvinte();
    }
}
