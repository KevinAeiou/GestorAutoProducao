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
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.setId(trabalhoModificado.getId());
        trabalho.setIdTrabalho(trabalhoModificado.getIdTrabalho());
        trabalho.setTipoLicenca(trabalhoModificado.getTipoLicenca());
        trabalho.setEstado(trabalhoModificado.getEstado());
        trabalho.setRecorrencia(trabalhoModificado.getRecorrencia());
        return repository.modificaTrabalhoProducao(trabalho);
    }
    public LiveData<Resource<Void>> insereTrabalhoProducao(TrabalhoProducao trabalho) {
        return repository.insereTrabalhoProducao(trabalho);
    }
    public LiveData<Resource<Void>> removeTrabalhoProducao(TrabalhoProducao trabalho) {
        return repository.removeTrabalhoProducao(trabalho);
    }

    public LiveData<Resource<Void>> removeReferenciaTrabalhoEspecifico(Trabalho trabalho) {
        return repository.removeReferenciaTrabalhoEspecifico(trabalho);
    }

    public LiveData<Resource<ArrayList<TrabalhoProducao>>> recuperaTrabalhosServidor() {
        return repository.recuperaTrabalhosServidor();
    }

    public void removeOuvinte() {
        repository.removeOuvinte();
    }
}
