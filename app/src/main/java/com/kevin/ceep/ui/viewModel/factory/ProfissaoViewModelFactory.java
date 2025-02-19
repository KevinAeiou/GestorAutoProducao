package com.kevin.ceep.ui.viewModel.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kevin.ceep.repository.ProfissaoRepository;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;

public class ProfissaoViewModelFactory implements ViewModelProvider.Factory {
    private final ProfissaoRepository repository;

    public ProfissaoViewModelFactory(ProfissaoRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProfissaoViewModel(repository);
    }
}
