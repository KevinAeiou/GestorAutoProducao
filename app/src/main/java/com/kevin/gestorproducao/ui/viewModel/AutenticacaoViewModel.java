package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.gestorproducao.model.Usuario;
import com.kevin.gestorproducao.repository.FirebaseAuthRepository;
import com.kevin.gestorproducao.repository.Resource;

public class AutenticacaoViewModel extends ViewModel {
    private final FirebaseAuthRepository repository;
    private final MediatorLiveData<Resource<Void>> recuperacaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> insercaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Usuario>> usuarioAtual = new MediatorLiveData<>();
    public AutenticacaoViewModel(FirebaseAuthRepository repository) {
        this.repository = repository;
    }

    public MediatorLiveData<Resource<Void>> getRecuperacaoResultado() {
        return recuperacaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }

    public LiveData<Resource<Usuario>> getUsuarioAtual() {
        return usuarioAtual;
    }
    public LiveData<Resource<Void>> autenticarUsuario(Usuario usuario) {
        return repository.autenticarUsuario(usuario);
    }

    public LiveData<Resource<Void>> criaUsuario(Usuario usuario) {
        return repository.criaUsuario(usuario);
    }

    public void insereUsuario(Usuario usuario) {
        LiveData<Resource<Void>> source = repository.insereUsuario(usuario);

        insercaoResultado.addSource(source, resultado -> {
            insercaoResultado.setValue(resultado);
            insercaoResultado.removeSource(source);
        });
    }

    public void recuperaSenha(String email) {
        LiveData<Resource<Void>> source = repository.recuperaSenha(email);

        recuperacaoResultado.addSource(source, resultado -> {
            recuperacaoResultado.setValue(resultado);
            recuperacaoResultado.removeSource(source);
        });
    }

    public void recuperaUsuarioAtual() {
        LiveData<Resource<Usuario>> source = repository.recuperaUsuarioAtual();

        usuarioAtual.addSource(source, resultado -> {

            usuarioAtual.setValue(resultado);

            usuarioAtual.removeSource(source);
        });
    }
}
