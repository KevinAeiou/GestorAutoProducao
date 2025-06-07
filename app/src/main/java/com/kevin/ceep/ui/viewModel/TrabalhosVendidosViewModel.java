package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.ui.viewModel.singleEvent.SingleLiveEvent;

import java.util.ArrayList;

public class TrabalhosVendidosViewModel extends ViewModel {
    private final TrabalhoVendidoRepository repository;
    private final SingleLiveEvent<Resource<ArrayList<TrabalhoVendido>>> recuperaVendasResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> insercaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> modificacaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> remocaoResultado = new SingleLiveEvent<>();
    public TrabalhosVendidosViewModel(TrabalhoVendidoRepository repository) {
        this.repository = repository;
    }
    public SingleLiveEvent<Resource<ArrayList<TrabalhoVendido>>> getRecuperaVendasResultado() {
        return recuperaVendasResultado;
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
    public void recuperaVendas() {
        Observer<? super Resource<ArrayList<TrabalhoVendido>>> observer = new Observer<Resource<ArrayList<TrabalhoVendido>>>() {
            @Override
            public void onChanged(Resource<ArrayList<TrabalhoVendido>> vendasEncontradas) {
                recuperaVendasResultado.setValue(vendasEncontradas);
                repository.recuperaVendas().removeObserver(this);
            }
        };
        repository.recuperaVendas().observeForever(observer);
    }
    public void insereVenda(TrabalhoVendido trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                insercaoResultado.setValue(resultado);
                repository.insereVenda(trabalho).removeObserver(this);
            }
        };
        repository.insereVenda(trabalho).observeForever(observer);
    }
    public void removeVenda(TrabalhoVendido trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                remocaoResultado.setValue(resultado);
                repository.removeTrabalho(trabalho).removeObserver(this);
            }
        };
        repository.removeTrabalho(trabalho).observeForever(observer);
    }

    public void modificaVenda(TrabalhoVendido trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                modificacaoResultado.setValue(resultado);
                repository.modificaVenda(trabalho).removeObserver(this);
            }
        };
        repository.modificaVenda(trabalho).observeForever(observer);
    }

    public void removeReferenciaTrabalhoEspecfico(Trabalho trabalho) {
        repository.removeReferenciaTrabalhoEspecfico(trabalho);
    }


    public void removeOuvinte() {
        repository.removeOuvinte();
    }
}
