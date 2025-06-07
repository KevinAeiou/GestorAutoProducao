package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.ui.viewModel.singleEvent.SingleLiveEvent;

import java.util.ArrayList;

public class TrabalhoViewModel extends ViewModel {
    private final TrabalhoRepository repository;
    private final MutableLiveData<Boolean> triggerRecuperaTrabalhos = new MutableLiveData<>();
    private final LiveData<Resource<ArrayList<Trabalho>>> trabalhos;
    private final SingleLiveEvent<Resource<Void>> insercaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> modificacaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> remocaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> sincronizacaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<ArrayList<Trabalho>>> trabalhosNecessariosResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Trabalho>> trabalhoPorIdResultado = new SingleLiveEvent<>();
    public TrabalhoViewModel(TrabalhoRepository repository) {
        this.repository = repository;
        trabalhos = Transformations.switchMap(
                triggerRecuperaTrabalhos,
                trigger -> repository.recuperaTrabalhos()
        );
    }
    public LiveData<Resource<ArrayList<Trabalho>>> getTrabalhos() {
        return trabalhos;
    }
    public SingleLiveEvent<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }
    public SingleLiveEvent<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }
    public SingleLiveEvent<Resource<Void>> getRemocaoResultado() {
        return remocaoResultado;
    }
    public SingleLiveEvent<Resource<Void>> getSincronizacaoResultado() {
        return sincronizacaoResultado;
    }
    public SingleLiveEvent<Resource<ArrayList<Trabalho>>> getTrabalhosNecessariosResultado() {
        return trabalhosNecessariosResultado;
    }
    public SingleLiveEvent<Resource<Trabalho>> getTrabalhoPorIdResultado() {
        return trabalhoPorIdResultado;
    }
    public void insereTrabalho(Trabalho trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                insercaoResultado.setValue(voidResource);
                repository.insereTrabalho(trabalho).removeObserver(this);
            }
        };
        repository.insereTrabalho(trabalho).observeForever(observer);
    }
    public void modificaTrabalho(Trabalho trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                modificacaoResultado.setValue(resultado);
                repository.modificaTrabalho(trabalho).removeObserver(this);
            }
        };
        repository.modificaTrabalho(trabalho).observeForever(observer);
    }
    public void removeTrabalho(Trabalho trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                remocaoResultado.setValue(resultado);
                repository.removeTrabalho(trabalho).removeObserver(this);
            }
        };
        repository.removeTrabalho(trabalho).observeForever(observer);
    }
    public void recuperaTrabalhos() {
        triggerRecuperaTrabalhos.setValue(true);
    }
    public void sincronizaTrabalhos() {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                sincronizacaoResultado.setValue(resultado);
                repository.sincronizaTrabalhos().removeObserver(this);
            }
        };
        repository.sincronizaTrabalhos().observeForever(observer);
    }

    public boolean trabalhoEspecificoExiste(Trabalho trabalho) {
        return repository.trabalhoEspecificoExiste(trabalho);
    }

    public void recuperaTrabalhosNecessarios(Trabalho trabalho) {
        Observer<? super Resource<ArrayList<Trabalho>>> observer = new Observer<Resource<ArrayList<Trabalho>>>() {
            @Override
            public void onChanged(Resource<ArrayList<Trabalho>> trabalhosEncontrados) {
                trabalhosNecessariosResultado.setValue(trabalhosEncontrados);
                repository.recuperaTrabalhosNecessarios(trabalho).removeObserver(this);
            }
        };
        repository.recuperaTrabalhosNecessarios(trabalho).observeForever(observer);
    }

    public void recuperaTrabalhoPorId(String id) {
        Observer<? super Resource<Trabalho>> observer = new Observer<Resource<Trabalho>>() {
            @Override
            public void onChanged(Resource<Trabalho> trabalhoEncontrado) {
                trabalhoPorIdResultado.setValue(trabalhoEncontrado);
                repository.recuperaTrabalhoPorId(id).removeObserver(this);
            }
        };
        repository.recuperaTrabalhoPorId(id).observeForever(observer);
    }

    public void removeOuvinte() {
        repository.removeOuvinte();
    }
}
