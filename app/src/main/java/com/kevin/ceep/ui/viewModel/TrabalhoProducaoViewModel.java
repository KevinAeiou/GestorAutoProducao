package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.repository.TrabalhoProducaoRepository;

import java.util.ArrayList;

public class TrabalhoProducaoViewModel extends ViewModel {
    private final TrabalhoProducaoRepository repository;
    public TrabalhoProducaoViewModel(TrabalhoProducaoRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<Void>> modificaTrabalhoProducao(TrabalhoProducao trabalhoModificado) {
        return repository.modificaTrabalhoProducao(trabalhoModificado);
    }
    public LiveData<Resource<Void>> insereTrabalhoProducao(TrabalhoProducao novoTrabalho) {
        return repository.insereTrabalhoProducao(novoTrabalho);
    }
    public LiveData<Resource<Void>> deletaTrabalhoProducao(TrabalhoProducao trabalhoDeletado) {
        return repository.removeTrabalhoProducao(trabalhoDeletado);
    }

    public LiveData<Resource<ArrayList<TrabalhoProducao>>> pegaTodosTrabalhosProducao() {
        return repository.pegaTodosTrabalhosProducao();
    }

    public LiveData<Resource<Void>> sicronizaTrabalhosProducao() {
        return repository.sincronizaTrabalhosProducao();
    }

    public void removeReferenciaTrabalhoEspecifico(Trabalho trabalho) {
        repository.removeReferenciaTrabalhoEspecifico(trabalho);
    }
}
