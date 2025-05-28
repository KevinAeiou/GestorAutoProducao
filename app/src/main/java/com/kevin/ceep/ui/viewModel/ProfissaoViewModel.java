package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.ProfissaoRepository;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.ui.viewModel.singleEvent.SingleLiveEvent;

import java.util.ArrayList;

public class ProfissaoViewModel extends ViewModel {
    private final ProfissaoRepository repository;
    private final SingleLiveEvent<Resource<Void>> insercaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> modificacaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<ArrayList<Profissao>>> recuperacaoProfissoes = new SingleLiveEvent<>();

    public ProfissaoViewModel(ProfissaoRepository repository) {
        this.repository = repository;
    }

    public SingleLiveEvent<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }

    public SingleLiveEvent<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }
    public SingleLiveEvent<Resource<ArrayList<Profissao>>> getRecuperacaoProfissoes() {
        return recuperacaoProfissoes;
    }
    public Profissao retornaProfissaoModificada(
            ArrayList<Profissao> profissoes,
            TrabalhoProducao trabalhoModificado
    ) {
        return repository.retornaProfissaoModificada(profissoes,trabalhoModificado);
    }

    public void recuperaProfissoes() {
        Observer<? super Resource<ArrayList<Profissao>>> observer = new Observer<Resource<ArrayList<Profissao>>>() {
            @Override
            public void onChanged(Resource<ArrayList<Profissao>> resultado) {
                recuperacaoProfissoes.setValue(resultado);
                repository.recuperaProfissoes().removeObserver(this);
            }
        };
        repository.recuperaProfissoes().observeForever(observer);
    }

    public void modificaExperienciaProfissao(
            Profissao profissao
    ) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                modificacaoResultado.setValue(resultado);
                repository.modificaProfissao(profissao).removeObserver(this);
            }
        };
        repository.modificaProfissao(profissao).observeForever(observer);
    }

    public void insereProfissoes() {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                insercaoResultado.setValue(resultado);
                repository.insereNovasProfissoes().removeObserver(this);
            }
        };
        repository.insereNovasProfissoes().observeForever(observer);
    }

    public void removeOuvinte() {
        repository.removeOuvinte();
    }
}
