package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.repository.Resource;

import java.util.ArrayList;

public class PersonagemViewModel extends ViewModel {
    private final PersonagemRepository personagemRepository;

    public PersonagemViewModel(PersonagemRepository personagemRepository) {
        this.personagemRepository = personagemRepository;
    }

    public LiveData<Resource<ArrayList<Personagem>>> pegaTodosPersonagens() {
        return personagemRepository.pegaTodosPersonagens();
    }

    public LiveData<Resource<Void>> sincronizaPersonagens() {
        return personagemRepository.sincronizaPersonagens();
    }
    public LiveData<Resource<Void>> modificaPersonagem(Personagem personagemModificado) {
        return personagemRepository.modificaPersonagem(personagemModificado);
    }

    public LiveData<Resource<Void>> inserePersonagem(Personagem novoPersonagem) {
        return personagemRepository.inserePersonagem(novoPersonagem);
    }
}
