package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.repository.TrabalhoEstoqueRepository;
import com.kevin.ceep.ui.viewModel.singleEvent.SingleLiveEvent;

import java.util.ArrayList;

public class TrabalhoEstoqueViewModel extends ViewModel {
    private final TrabalhoEstoqueRepository repository;
    private final MutableLiveData<Boolean> triggerRecuperaEstoque = new MutableLiveData<>();
    private final MutableLiveData<String> triggerRecuperaPorId = new MutableLiveData<>();
    public final LiveData<Resource<ArrayList<TrabalhoEstoque>>> trabalhosEstoque;
    public final LiveData<Resource<TrabalhoEstoque>> trabalhoPorId;
    private final SingleLiveEvent<Resource<Void>> modificacaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> insercaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> remocaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> remocaoReferenciaResultado = new SingleLiveEvent<>();

    public TrabalhoEstoqueViewModel(TrabalhoEstoqueRepository repository) {
        this.repository = repository;
        trabalhosEstoque = Transformations.switchMap(
                triggerRecuperaEstoque,
                trigger -> repository.recuperaEstoque()
                );
        trabalhoPorId = Transformations.switchMap(
                triggerRecuperaPorId,
                repository::recuperaTrabalhoEstoquePorIdTrabalho
        );
    }
    public LiveData<Resource<ArrayList<TrabalhoEstoque>>> getTrabalhosEstoque() {
        return trabalhosEstoque;
    }

    public SingleLiveEvent<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }

    public LiveData<Resource<TrabalhoEstoque>> getTrabalhoPorId() {
        return trabalhoPorId;
    }

    public LiveData<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }

    public SingleLiveEvent<Resource<Void>> getRemocaoResultado() {
        return remocaoResultado;
    }
    public SingleLiveEvent<Resource<Void>> getRemocaoReferenciaResultado() {
        return remocaoReferenciaResultado;
    }
    public void recuperaTrabalhosEstoque() {
        triggerRecuperaEstoque.setValue(true);
    }

    public void modificaTrabalhoEstoque(TrabalhoEstoque trabalho) {
        Observer<Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                modificacaoResultado.setValue(resultado);
                repository.modificaTrabalhoEstoque(trabalho).removeObserver(this);
            }
        };
        repository.modificaTrabalhoEstoque(trabalho).observeForever(observer);
    }


    public void insereTrabalhoEstoque(TrabalhoEstoque trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                insercaoResultado.setValue(resultado);
                repository.modificaTrabalhoEstoque(trabalho).removeObserver(this);
            }
        };
        repository.insereTrabalhoEstoque(trabalho).observeForever(observer);
    }
    public void removeTrabalhoEstoque(TrabalhoEstoque trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                remocaoResultado.setValue(voidResource);
                repository.removeTrabalhoEstoque(trabalho).removeObserver(this);
            }
        };
        repository.removeTrabalhoEstoque(trabalho).observeForever(observer);
    }
    public void recuperaTrabalhoEstoquePorIdTrabalho(String idTrabalho) {
        triggerRecuperaPorId.setValue(idTrabalho);
    }
    public void removeReferenciaTrabalhoEspecifico(Trabalho trabalho) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                remocaoReferenciaResultado.setValue(voidResource);
                repository.removeReferenciaTrabalhoEspecifico(trabalho).removeObserver(this);
            }
        };
        repository.removeReferenciaTrabalhoEspecifico(trabalho).observeForever(observer);
    }
    public void removeOuvinte() {
        repository.removeOuvinte();
    }
}
