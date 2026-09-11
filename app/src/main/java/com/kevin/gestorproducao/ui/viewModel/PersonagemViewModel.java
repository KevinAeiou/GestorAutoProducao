package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.kevin.gestorproducao.model.Personagem;
import com.kevin.gestorproducao.repository.PersonagemRepository;
import com.kevin.gestorproducao.repository.Resource;

import java.util.ArrayList;

public class PersonagemViewModel extends ViewModel {
    private final PersonagemRepository repository;
    private final MutableLiveData<Personagem> personagemSelecionado;
    private final LiveData<Resource<ArrayList<Personagem>>> personagens;
    private final MutableLiveData<Boolean> triggerRecuperaPersonagens = new MutableLiveData<>();
    private final MediatorLiveData<Resource<Void>> insercaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> modificacaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> remocaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> sincronizacaoResultado = new MediatorLiveData<>();

    public PersonagemViewModel(PersonagemRepository repository) {
        this.repository = repository;
        personagemSelecionado = new MutableLiveData<>();

        personagens = Transformations.switchMap(
            triggerRecuperaPersonagens,
            trigger -> repository.recuperaPersonagens()
        );
    }
    public LiveData<Resource<ArrayList<Personagem>>> getPersonagens() {
        return personagens;
    }

    public MutableLiveData<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }

    public MutableLiveData<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }

    public MutableLiveData<Resource<Void>> getRemocaoResultado() {
        return remocaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getSincronizacaoResultado() {
        return sincronizacaoResultado;
    }

    public LiveData<Personagem> pegaPersonagemSelecionado() {
        return personagemSelecionado;
    }

    public void definePersonagemSelecionado(Personagem personagem) {
        personagemSelecionado.setValue(personagem);
    }
    public void modificaPersonagem(Personagem personagem) {
        LiveData<Resource<Void>> source = repository.modificaPersonagem(personagem);

        modificacaoResultado.addSource(source, resultado -> {
            modificacaoResultado.setValue(resultado);
            modificacaoResultado.removeSource(source);
        });
    }

    public void inserePersonagemUsuario(Personagem personagem) {
        LiveData<Resource<Void>> source = repository.inserePersonagemUsuario(personagem);

        insercaoResultado.addSource(source, resultado -> {
            insercaoResultado.setValue(resultado);
            insercaoResultado.removeSource(source);
        });
    }

    public void removePersonagem(String idPersonagem) {
        LiveData<Resource<Void>> source = repository.removePersonagem(idPersonagem);

        remocaoResultado.addSource(source, resultado -> {
            remocaoResultado.setValue(resultado);
            remocaoResultado.removeSource(source);
        });
    }

    public void recuperaPersonagens() {
        triggerRecuperaPersonagens.setValue(true);
    }

    public void sincronizaPersonagens(String id) {
        if (id == null) return;

        LiveData<Resource<Void>> source = repository.sincronizaPersonagens(id);

        sincronizacaoResultado.addSource(source, resultado -> {
            sincronizacaoResultado.setValue(resultado);
            sincronizacaoResultado.removeSource(source);
        });
    }

    public void removeObservador() {
        repository.removeOuvinte();
    }

    public void limpaInsercaoResultado() {
        insercaoResultado.setValue(null);
    }

    public void limpaRemocaoResultado() {
        remocaoResultado.setValue(null);
    }

    public void limpaModificacaoResultado() {
        modificacaoResultado.setValue(null);
    }
}
