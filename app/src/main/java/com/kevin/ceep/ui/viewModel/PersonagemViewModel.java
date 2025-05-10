package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.repository.Resource;
import java.util.ArrayList;

public class PersonagemViewModel extends ViewModel {
    private final PersonagemRepository personagemRepository;
    private final MutableLiveData<Personagem> personagemSelecionado;

    public PersonagemViewModel(PersonagemRepository personagemRepository) {
        this.personagemRepository = personagemRepository;
        personagemSelecionado = new MutableLiveData<>();
    }

    public LiveData<Personagem> pegaPersonagemSelecionado() {
        return personagemSelecionado;
    }

    public void definePersonagemSelecionado(Personagem personagem) {
        personagemSelecionado.setValue(personagem);
    }

    public LiveData<Resource<Void>> modificaPersonagem(Personagem personagemModificado) {
        return personagemRepository.modificaPersonagem(personagemModificado);
    }

    public LiveData<Resource<Void>> inserePersonagem(Personagem personagem) {
        return personagemRepository.inserePersonagem(personagem);
    }

    public LiveData<Resource<Void>> removePersonagem(Personagem personagem) {
        return personagemRepository.removePersonagem(personagem);
    }

    public LiveData<Resource<ArrayList<String>>> pegaIdsPersonagens() {
        return personagemRepository.pegaIdsPersonagens();
    }

    public LiveData<Resource<ArrayList<Personagem>>> recuperaPersonagensServidor(ArrayList<String> idsPersonagens) {
        return personagemRepository.recuperaPersonagensServidor(idsPersonagens);
    }

    public void removeOuvinte() {
        personagemRepository.removeOuvinte();
    }
}
