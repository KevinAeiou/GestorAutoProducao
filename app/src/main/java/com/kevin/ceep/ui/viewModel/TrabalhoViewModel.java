package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.repository.TrabalhoRepository;

import java.util.ArrayList;

public class TrabalhoViewModel extends ViewModel {
    private final TrabalhoRepository trabalhoRepository;

    public TrabalhoViewModel(TrabalhoRepository trabalhoRepository) {
        this.trabalhoRepository = trabalhoRepository;
    }

    public LiveData<Resource<Void>> insereTrabalho(Trabalho trabalho) {
        return trabalhoRepository.adicionaTrabalho(trabalho);
    }
    public LiveData<Resource<Void>> modificaTrabalho(Trabalho trabalho) {
        return trabalhoRepository.modificaTrabalho(trabalho);
    }
    public LiveData<Resource<Void>> excluiTrabalhoEspecificoServidor(Trabalho trabalhoRecebido) {
        return trabalhoRepository.removeTrabalho(trabalhoRecebido);
    }
    public LiveData<Resource<ArrayList<Trabalho>>> pegaTodosTrabalhos() {
        return trabalhoRepository.pegaTodosTrabalhos();
    }
    public LiveData<Resource<Void>> sincronizaTrabalhos() {
        return trabalhoRepository.sincronizaTrabalhos();
    }

    public boolean trabalhoEspecificoExiste(Trabalho trabalho) {
        return trabalhoRepository.trabalhoEspecificoExiste(trabalho);
    }

    public LiveData<Resource<ArrayList<Trabalho>>> pegaTrabalhosNecessarios(Trabalho trabalho) {
        return trabalhoRepository.pegaTrabalhosNecessarios(trabalho);
    }
}
