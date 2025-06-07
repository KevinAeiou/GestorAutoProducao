package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.repository.Resource;
import com.kevin.ceep.ui.viewModel.singleEvent.SingleLiveEvent;

import java.util.ArrayList;

public class PersonagemViewModel extends ViewModel {
    private final PersonagemRepository repository;
    private final MutableLiveData<Personagem> personagemSelecionado;
    private final LiveData<Resource<ArrayList<String>>> idsPersonagens;
    private final LiveData<Resource<ArrayList<Personagem>>> personagensEncontrados;
    private final MutableLiveData<Boolean> triggerRecuperaIdsPersonagens = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> triggerRecuperaPersonagens = new MutableLiveData<>();
    private final SingleLiveEvent<Resource<Void>> insercaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> modificacaoResultado = new SingleLiveEvent<>();
    private final SingleLiveEvent<Resource<Void>> remocaoResultado = new SingleLiveEvent<>();
    public PersonagemViewModel(PersonagemRepository personagemRepository) {
        this.repository = personagemRepository;
        personagemSelecionado = new MutableLiveData<>();
        idsPersonagens = Transformations.switchMap(
                triggerRecuperaIdsPersonagens,
                trigger -> repository.pegaIdsPersonagens()
        );
        personagensEncontrados = Transformations.switchMap(
                triggerRecuperaPersonagens,
                repository::recuperaPersonagensServidor
        );
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
    public LiveData<Personagem> pegaPersonagemSelecionado() {
        return personagemSelecionado;
    }
    public void definePersonagemSelecionado(Personagem personagem) {
        personagemSelecionado.setValue(personagem);
    }
    public void modificaPersonagem(Personagem personagem) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                modificacaoResultado.setValue(resultado);
                repository.modificaPersonagem(personagem).removeObserver(this);
            }
        };
        repository.modificaPersonagem(personagem).observeForever(observer);
    }
    public void inserePersonagem(Personagem personagem) {
        Observer<Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                insercaoResultado.setValue(resultado);
                repository.inserePersonagem(personagem).removeObserver(this);
            }
        };
        repository.inserePersonagem(personagem).observeForever(observer);
    }
    public void removePersonagem(Personagem personagem) {
        Observer<? super Resource<Void>> observer = new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resultado) {
                remocaoResultado.setValue(resultado);
                repository.removePersonagem(personagem).removeObserver(this);
            }
        };
        repository.removePersonagem(personagem).observeForever(observer);
    }
    public void pegaIdsPersonagens() {
        triggerRecuperaIdsPersonagens.setValue(true);
    }
    public void recuperaPersonagens(ArrayList<String> idsPersonagens) {
        triggerRecuperaPersonagens.setValue(idsPersonagens);
    }
    public LiveData<Resource<ArrayList<String>>> getIdsPersonagens() {
        return idsPersonagens;
    }
    public LiveData<Resource<ArrayList<Personagem>>> getPersonagensEncontrados() {
        return personagensEncontrados;
    }
    public void removeOuvinte() {
        repository.removeOuvinte();
    }

}
