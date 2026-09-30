package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.Resource;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;

import java.util.ArrayList;
import java.util.List;

public class TrabalhoProducaoViewModel extends ViewModel {
    private final TrabalhoProducaoRepository repository;
    private final LiveData<Resource<ArrayList<TrabalhoProducao>>> producoes;
    private final MediatorLiveData<Resource<Void>> modificacaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> insercaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> remocaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> remocaoReferenciaResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> triggerRecuperaProducaoPorProfissaoPersonagem = new MediatorLiveData<>();
    private final MutableLiveData<String> idPersonagem = new MutableLiveData<>();
    private final MediatorLiveData<Resource<Void>> sincronizacaoResultado = new MediatorLiveData<>();

    public TrabalhoProducaoViewModel(
        TrabalhoProducaoRepository repository
    ) {
        this.repository = repository;
        this.producoes = Transformations.switchMap(
            idPersonagem,
            id -> {
                if (id == null) return new MutableLiveData<>();

                return repository.recuperaProducoes(id);
            }
        );
    }
    public LiveData<Resource<ArrayList<TrabalhoProducao>>> getProducoes() {
        return producoes;
    }

    public MediatorLiveData<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }
    public MediatorLiveData<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }
    public MediatorLiveData<Resource<Void>> getRemocaoResultado() {
        return remocaoResultado;
    }
    public MediatorLiveData<Resource<Void>> getRemocaoReferenciaResultado() {
        return remocaoReferenciaResultado;
    }

    public MediatorLiveData<Resource<Void>> getSincronizacaoResultado() {
        return sincronizacaoResultado;
    }

    public void modificaTrabalhoProducao(
        TrabalhoProducao trabalho
    ) {
        LiveData<Resource<Void>> source = repository.modificaTrabalhoProducao(
            trabalho,
            idPersonagem.getValue()
        );

        modificacaoResultado.addSource(source, resultado -> {
            modificacaoResultado.setValue(resultado);
            modificacaoResultado.removeSource(source);
        });
    }

    public void insereTrabalhoProducao(TrabalhoProducao trabalho) {
        LiveData<Resource<Void>> source = repository.insereTrabalhoProducao(
            trabalho,
            idPersonagem.getValue()
        );

        insercaoResultado.addSource(source, resultado -> {
            insercaoResultado.setValue(resultado);
            insercaoResultado.removeSource(source);
        });
    }
    public void removeTrabalhoProducao(TrabalhoProducao trabalho) {
        LiveData<Resource<Void>> source = repository.removeTrabalhoProducao(
            trabalho,
            idPersonagem.getValue()
        );

        remocaoResultado.addSource(source, resultado -> {
            remocaoResultado.setValue(resultado);
            remocaoResultado.removeSource(source);
        });
    }

    public void removeTrabalhosProducao(List<TrabalhoProducao> trabalhos) {
        LiveData<Resource<Void>> source = repository.removeTrabalhosProducao(
            trabalhos,
            idPersonagem.getValue()
        );

        remocaoResultado.addSource(source, resultado -> {
            remocaoResultado.setValue(resultado);
            remocaoResultado.removeSource(source);
        });
    }

    public void limpaRemocaoResultado() {
        remocaoResultado.setValue(null);
    }

    public void removeObservador() {
        repository.removeObservador();
    }

    public void recuperaProducaoPorProfissaoPersonagem() {
        triggerRecuperaProducaoPorProfissaoPersonagem.setValue(true);
    }

    public void setIdPersonagem(String id) {
        if (id == null) return;

        if (id.equals(idPersonagem.getValue())) return;

        idPersonagem.setValue(id);
    }

    public void atualizaProducao() {
        String id = idPersonagem.getValue();
        if (id == null) return;

        idPersonagem.setValue(id);
    }

    public void sincronizaProducao() {
        LiveData<Resource<Void>> source = repository.sincronizaProducao(idPersonagem.getValue());

        sincronizacaoResultado.addSource(source, resultado -> {
            sincronizacaoResultado.setValue(resultado);
            sincronizacaoResultado.removeSource(source);
        });
    }

    public void limpaModificacaoResultado() {
        modificacaoResultado.setValue(null);
    }

    public void carregarSeNecessario() {
        if (producoes.getValue() == null) {
            sincronizaProducao();
        }
    }
}
