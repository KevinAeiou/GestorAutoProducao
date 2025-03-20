package com.kevin.ceep.ui.viewModel.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kevin.ceep.repository.ProfissaoRepository;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;

public class ProfissaoViewModelFactory implements ViewModelProvider.Factory {
    private final String idPersonagem;

    public  ProfissaoViewModelFactory(String idPersonagem) {
        this.idPersonagem= idPersonagem;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfissaoViewModel.class)) {
            return (T) new ProfissaoViewModel(new ProfissaoRepository(idPersonagem));
        }
        throw new IllegalArgumentException("Classe ViewModel desconhecida");
    }
}
