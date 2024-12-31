package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.ProfissaoRepository;
import com.kevin.ceep.repository.Resource;

import java.util.ArrayList;

public class ProfissaoViewModel extends ViewModel {
    private final ProfissaoRepository repository;

    public ProfissaoViewModel(ProfissaoRepository repository) {
        this.repository = repository;
    }

    public Profissao retornaProfissaoModificada(ArrayList<Profissao> profissoes, TrabalhoProducao trabalhoModificado) {
        return repository.retornaProfissaoModificada(profissoes,trabalhoModificado);
    }

    public LiveData<Resource<ArrayList<Profissao>>> pegaTodasProfissoes() {
        return repository.pegaTodasProfissoes();
    }

    public LiveData<Resource<Void>> modificaExperienciaProfissao(Profissao profissaoModificada) {
        return repository.modificaProfissao(profissaoModificada);
    }
}
