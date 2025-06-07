package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.ceep.model.Usuario;
import com.kevin.ceep.repository.FirebaseAuthRepository;
import com.kevin.ceep.repository.Resource;

public class AutenticacaoViewModel extends ViewModel {
    private final FirebaseAuthRepository repository;

    public AutenticacaoViewModel(FirebaseAuthRepository repository) {
        this.repository = repository;
    }
    public LiveData<Resource<Void>> autenticarUsuario(Usuario usuario) {
        return repository.autenticarUsuario(usuario);
    }

    public LiveData<Resource<Void>> criaUsuario(Usuario usuario) {
        return repository.criaUsuario(usuario);
    }

    public LiveData<Resource<Void>> insereUsuario(Usuario usuario) {
        return repository.insereUsuario(usuario);
    }
}
