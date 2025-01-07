package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.repository.Resource;

import java.util.ArrayList;

public class ProdutosVendidosViewModel extends ViewModel {
    private final TrabalhoVendidoRepository repository;

    public ProdutosVendidosViewModel(TrabalhoVendidoRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<ArrayList<TrabalhoVendido>>> pegaTodosTrabalhosVendidos() {
        return repository.pegaTodosTrabalhosVendidos();
    }
    public LiveData<Resource<Void>> removeTrabalhoVendido(TrabalhoVendido trabalhoRemovido) {
        return repository.removeTrabalho(trabalhoRemovido);
    }
    public LiveData<Resource<Void>> sincronizaTrabalhos() {
        return repository.sincronizaTrabalhos();
    }
}
