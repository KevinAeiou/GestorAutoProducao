package com.kevin.ceep.ui.viewModel.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kevin.ceep.repository.FirebaseAuthRepository;
import com.kevin.ceep.ui.viewModel.AutenticacaoViewModel;

public class AutenticacaoViewModelFactor implements ViewModelProvider.Factory {
    private final FirebaseAuthRepository repository;

    public AutenticacaoViewModelFactor(FirebaseAuthRepository repository) {
        this.repository = repository;
    }
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AutenticacaoViewModel(repository);
    }
}
