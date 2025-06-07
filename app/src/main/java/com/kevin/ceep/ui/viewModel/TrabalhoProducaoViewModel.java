package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.repository.TrabalhoProducaoRepository;
import com.kevin.ceep.ui.viewModel.singleEvent.SingleLiveEvent;

import java.util.ArrayList;

public class TrabalhoProducaoViewModel extends ViewModel {
    private final TrabalhoProducaoRepository repository;
    private final MutableLiveData<Boolean> triggerRecuperaProducao = new MutableLiveData<>();
    private final LiveData<Resource<ArrayList<TrabalhoProducao>>> trabalhosProducao;
    private final SingleLiveEvent<Resource<Void>> modificacaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> insercaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> remocaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> remocaoReferenciaResultado = new SingleLiveEvent<>();
    public TrabalhoProducaoViewModel(TrabalhoProducaoRepository repository) {
        this.repository = repository;
        trabalhosProducao = Transformations.switchMap(
                triggerRecuperaProducao,
                trigger -> repository.recuperaTrabalhosServidor()
        );
    }
    public LiveData<Resource<ArrayList<TrabalhoProducao>>> getTrabalhosProducao() {
        return trabalhosProducao;
    }
    public SingleLiveEvent<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }
    public SingleLiveEvent<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }
    public SingleLiveEvent<Resource<Void>> getRemocaoResultado() {
        return remocaoResultado;
    }
    public SingleLiveEvent<Resource<Void>> getRemocaoReferenciaResultado() {
        return remocaoReferenciaResultado;
    }

    public void modificaTrabalhoProducao(TrabalhoProducao trabalhoModificado) {
        TrabalhoProducao trabalho = new TrabalhoProducao();
        trabalho.setId(trabalhoModificado.getId());
        trabalho.setIdTrabalho(trabalhoModificado.getIdTrabalho());
        trabalho.setTipoLicenca(trabalhoModificado.getTipoLicenca());
        trabalho.setEstado(trabalhoModificado.getEstado());
        trabalho.setRecorrencia(trabalhoModificado.getRecorrencia());
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                modificacaoResultado.setValue(resultado);
                repository.modificaTrabalhoProducao(trabalho).removeObserver(this);
            }
        };
        repository.modificaTrabalhoProducao(trabalho).observeForever(observer);
    }
    public void insereTrabalhoProducao(TrabalhoProducao trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                insercaoResultado.setValue(resultado);
                repository.insereTrabalhoProducao(trabalho).removeObserver(this);
            }
        };
        repository.insereTrabalhoProducao(trabalho).observeForever(observer);
    }
    public void removeTrabalhoProducao(TrabalhoProducao trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                remocaoResultado.setValue(resultado);
                repository.removeTrabalhoProducao(trabalho).removeObserver(this);
            }
        };
        repository.removeTrabalhoProducao(trabalho).observeForever(observer);
    }

    public void removeReferenciaTrabalhoEspecifico(Trabalho trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                remocaoReferenciaResultado.setValue(resultado);
                repository.removeReferenciaTrabalhoEspecifico(trabalho).removeObserver(this);
            }
        };
        repository.removeReferenciaTrabalhoEspecifico(trabalho).observeForever(observer);
    }

    public void recuperaTrabalhosProducao() {
        triggerRecuperaProducao.setValue(true);
    }

    public void removeOuvinte() {
        repository.removeOuvinte();
    }
}
