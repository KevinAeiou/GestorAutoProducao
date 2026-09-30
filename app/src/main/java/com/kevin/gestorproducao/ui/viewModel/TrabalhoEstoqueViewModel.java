package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.repository.Resource;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;

import java.util.ArrayList;

public class TrabalhoEstoqueViewModel extends ViewModel {
    private final TrabalhoEstoqueRepository repository;
    private final MutableLiveData<String> idPersonagem = new MutableLiveData<>();
    public final LiveData<Resource<ArrayList<TrabalhoEstoque>>> estoque;
    private final MediatorLiveData<Resource<Void>> modificacaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> insercaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> remocaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<TrabalhoEstoque>> trabalhoEstoqueResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> sincronizacaoResultado = new MediatorLiveData<>();

    public TrabalhoEstoqueViewModel(TrabalhoEstoqueRepository repository) {
        this.repository = repository;
        estoque = Transformations.switchMap(
            idPersonagem,
            id -> {
                if (id == null) return new MutableLiveData<>();

                return repository.recuperaEstoque(id);
            }
        );
    }

    public LiveData<Resource<ArrayList<TrabalhoEstoque>>> getEstoque() {
        return estoque;
    }

    public MediatorLiveData<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }

    public void getTrabalhoPorId(String idTrabalho) {
        LiveData<Resource<TrabalhoEstoque>> source = repository.recuperaTrabalhoEstoquePorIdTrabalho(
            idTrabalho,
            idPersonagem.getValue()
        );

        trabalhoEstoqueResultado.addSource(source, resultado -> {
            trabalhoEstoqueResultado.setValue(resultado);
            trabalhoEstoqueResultado.removeSource(source);
        });
    }

    public LiveData<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getRemocaoResultado() {
        return remocaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getSincronizacaoResultado() {
        return sincronizacaoResultado;
    }

    public void modificaEstoque(TrabalhoEstoque trabalho) {
        LiveData<Resource<Void>> source = repository.modificaEstoque(
            trabalho,
            idPersonagem.getValue()
        );

        modificacaoResultado.addSource(source, resultado -> {
            modificacaoResultado.setValue(resultado);
            modificacaoResultado.removeSource(source);
        });
    }

    public void insereEstoque(TrabalhoEstoque trabalho) {
        LiveData<Resource<Void>> source = repository.insereEstoque(
            trabalho,
            idPersonagem.getValue()
        );

        insercaoResultado.addSource(source, resultado -> {
            insercaoResultado.setValue(resultado);
            insercaoResultado.removeSource(source);
        });
    }
    public void removeTrabalhoEstoque(TrabalhoEstoque trabalho) {
        LiveData<Resource<Void>> source = repository.removeTrabalhoEstoque(
            trabalho,
            idPersonagem.getValue()
        );

        remocaoResultado.addSource(source, resultado -> {
            remocaoResultado.setValue(resultado);
            remocaoResultado.removeSource(source);
        });
    }

    public void removeObservador() {
        repository.removeOuvinte();
    }

    public void sincronizaEstoque() {
        LiveData<Resource<Void>> source = repository.sincronizaEstoque(idPersonagem.getValue());

        sincronizacaoResultado.addSource(source, resultado -> {
            sincronizacaoResultado.setValue(resultado);
            sincronizacaoResultado.removeSource(source);
        });
    }

    public void setIdPersonagem(String id) {
        if (id == null) return;

        if (id.equals(idPersonagem.getValue())) return;

        idPersonagem.setValue(id);
    }

    public void atualizaEstoque() {
        String id = idPersonagem.getValue();
        if (id == null) return;

        idPersonagem.setValue(id);
    }

    public MediatorLiveData<Resource<TrabalhoEstoque>> getTrabalhoEstoqueResultado() {
        return trabalhoEstoqueResultado;
    }

    public TrabalhoEstoque recuperaTrabalhoPorId(String idTrabalho) {
        return repository.recuperaTrabalhoPorId(idPersonagem.getValue(), idTrabalho);
    }

    public void carregarSeNecessario() {
        if (estoque.getValue() == null) {
            sincronizaEstoque();
        }
    }
}
