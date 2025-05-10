package com.kevin.ceep.ui.viewModel.factory;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kevin.ceep.repository.RecursosProducaoRepository;
import com.kevin.ceep.ui.viewModel.RecursosProducaoViewModel;

public class RecursosProducaoViewModelFactory implements ViewModelProvider.Factory {
    private final String idPersonagem;
    private final Context contexto;
    public RecursosProducaoViewModelFactory(String id, Context contexto) {
        this.idPersonagem = id;
        this.contexto = contexto;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RecursosProducaoViewModel.class)) {
            return (T) new RecursosProducaoViewModel(RecursosProducaoRepository.getInstancia(idPersonagem, contexto));
        }
        throw new IllegalArgumentException("Classe ViewModel desconhecida");
    }
}
