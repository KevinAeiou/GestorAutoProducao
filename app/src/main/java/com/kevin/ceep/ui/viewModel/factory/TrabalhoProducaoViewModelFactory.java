package com.kevin.ceep.ui.viewModel.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kevin.ceep.repository.TrabalhoProducaoRepository;
import com.kevin.ceep.ui.viewModel.TrabalhoProducaoViewModel;

public class TrabalhoProducaoViewModelFactory implements ViewModelProvider.Factory {
    private final String idPersonagem;

    public TrabalhoProducaoViewModelFactory(String idPersonagem) {
        this.idPersonagem = idPersonagem;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TrabalhoProducaoViewModel.class)) {
            return (T) new TrabalhoProducaoViewModel(TrabalhoProducaoRepository.getInstance(idPersonagem));
        }
        throw new IllegalArgumentException("Classe ViewModel desconhecida");
    }
}
