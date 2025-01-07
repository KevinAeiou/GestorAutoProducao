package com.kevin.ceep.ui.viewModel.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.ui.viewModel.TrabalhosVendidosViewModel;

public class TrabalhosVendidosViewModelFactory implements ViewModelProvider.Factory {
    private final TrabalhoVendidoRepository repository;

    public TrabalhosVendidosViewModelFactory(TrabalhoVendidoRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TrabalhosVendidosViewModel(repository);
    }
}
