package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.repository.Resource;

import java.util.ArrayList;

public class TrabalhosVendidosViewModel extends ViewModel {
    private final TrabalhoVendidoRepository repository;

    public TrabalhosVendidosViewModel(TrabalhoVendidoRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<ArrayList<TrabalhoVendido>>> recuperaVendas() {
        return repository.recuperaVendas();
    }
    public LiveData<Resource<Void>> removeVenda(TrabalhoVendido trabalhoRemovido) {
        return repository.removeTrabalho(trabalhoRemovido);
    }

    public LiveData<Resource<Void>> modificaTrabalhoVendido(TrabalhoVendido trabalho) {
        return repository.modificaTrabalhoVendido(trabalho);
    }

    public void removeReferenciaTrabalhoEspecfico(Trabalho trabalho) {
        repository.removeReferenciaTrabalhoEspecfico(trabalho);
    }

    public LiveData<Resource<Void>> insereVenda(TrabalhoVendido trabalho) {
        return repository.insereVenda(trabalho);
    }

    public void removeOuvinte() {
        repository.removeOuvinte();
    }
}
