package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.repository.Resource;
import com.kevin.gestorproducao.repository.TrabalhoRepository;

import java.util.ArrayList;
import java.util.List;

public class TrabalhoViewModel extends ViewModel {
    private final TrabalhoRepository repository;
    private final MutableLiveData<Boolean> triggerRecuperaTrabalhos = new MutableLiveData<>();
    private final LiveData<Resource<ArrayList<Trabalho>>> trabalhos;
    private final MediatorLiveData<Resource<Void>> insercaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> modificacaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> remocaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> sincronizacaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<ArrayList<Trabalho>>> trabalhosNecessariosResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Trabalho>> trabalhoPorIdResultado = new MediatorLiveData<>();
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

    public MediatorLiveData<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getRemocaoResultado() {
        return remocaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getSincronizacaoResultado() {
        return sincronizacaoResultado;
    }

    public MediatorLiveData<Resource<ArrayList<Trabalho>>> getTrabalhosNecessariosResultado() {
        return trabalhosNecessariosResultado;
    }

    public MediatorLiveData<Resource<Trabalho>> getTrabalhoPorIdResultado() {
        return trabalhoPorIdResultado;
    }

    public void insereTrabalho(Trabalho trabalho) {
        LiveData<Resource<Void>> source = repository.insereTrabalho(trabalho);

        insercaoResultado.addSource(source, resultado -> {
            insercaoResultado.setValue(resultado);
            insercaoResultado.removeSource(source);
        });
    }
    public void modificaTrabalho(Trabalho trabalho) {
        LiveData<Resource<Void>> source = repository.modificaTrabalho(trabalho);

        modificacaoResultado.addSource(source, resultado -> {
            modificacaoResultado.setValue(resultado);
            modificacaoResultado.removeSource(source);
        });
    }
    public void removeTrabalho(Trabalho trabalho) {
        LiveData<Resource<Void>> source = repository.removeTrabalho(trabalho);

        remocaoResultado.addSource(source, resultado -> {
            remocaoResultado.setValue(resultado);
            remocaoResultado.removeSource(source);
        });
    }
    public void recuperaTrabalhos() {
        triggerRecuperaTrabalhos.setValue(true);
    }
    public void sincronizaTrabalhos() {
        LiveData<Resource<Void>> source = repository.sincronizaTrabalhos();

        sincronizacaoResultado.addSource(source, resultado -> {
            sincronizacaoResultado.setValue(resultado);
            sincronizacaoResultado.removeSource(source);
        });
    }

    public String trabalhoEspecificoExiste(Trabalho trabalho) {
        return repository.trabalhoEspecificoExiste(trabalho);
    }

    public void recuperaTrabalhosNecessarios(Trabalho trabalho) {
        LiveData<Resource<ArrayList<Trabalho>>> source = repository.recuperaTrabalhosNecessarios(trabalho);

        trabalhosNecessariosResultado.addSource(source, resultado -> {
            trabalhosNecessariosResultado.setValue(resultado);
            trabalhosNecessariosResultado.removeSource(source);
        });
    }

    public Trabalho recuperaTrabalhoPorId(String id) {
        return repository.recuperaTrabalhoPorId(id);
    }

    public void removeObservador() {
        repository.removeOuvinte();
    }

    public Trabalho recuperaTrabalhoPorNome(String nome) {
        return repository.recuperaTrabalhoPorNome(nome);
    }

    public void limpaModificacaoResultado() {
        modificacaoResultado.setValue(null);
    }

    public void limpaRemocaoResultado() {
        remocaoResultado.setValue(null);
    }

    public void limpaInsercaoResultado() {
        insercaoResultado.setValue(null);
    }

    public void carregarSeNecessario() {
        if (trabalhos.getValue() == null) {
            sincronizaTrabalhos();
        }
    }

    public ArrayList<Trabalho> recuperaTrabalhosNecessariosPorId(List<String> listaTrabalhosNecessarios) {
        return repository.recuperaTrabalhosNecessariosPorId(listaTrabalhosNecessarios);
    }
}
