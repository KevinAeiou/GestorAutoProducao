package com.kevin.ceep.repository;

import static com.kevin.ceep.repository.TrabalhoProducaoRepository.destroyInstance;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_USUARIOS2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kevin.ceep.model.Usuario;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FirebaseAuthRepository {
    private static FirebaseAuthRepository instancia;
    private final FirebaseAuth minhaInstancia;
    private final DatabaseReference minhaReferencia;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);

    public FirebaseAuthRepository() {
        this.minhaInstancia = FirebaseAuth.getInstance();
        this.minhaReferencia = FirebaseDatabase.getInstance().getReference(CHAVE_USUARIOS2);
    }

    public static FirebaseAuthRepository getInstance() {
        if (instancia == null) {
            destroyInstance();
            instancia = new FirebaseAuthRepository();
        }
        return instancia;
    }

    public LiveData<Resource<Void>> autenticarUsuario(Usuario usuario) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaInstancia.signInWithEmailAndPassword(usuario.getEmail(),usuario.getSenha())
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    liveData.postValue(new Resource<>(null, null));
                    return;
                }
                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao autenticar usuário");
                liveData.postValue(new Resource<>(null, erro));            });
        return liveData;
    }

    public LiveData<Resource<Void>> criaUsuario(Usuario usuario) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaInstancia.createUserWithEmailAndPassword(usuario.getEmail(),usuario.getSenha())
                .addOnCompleteListener(backgroundExecutor, task -> {
                    if (task.isSuccessful()){
                        liveData.postValue(new Resource<>(null, null));
                        return;
                    }
                    String erro;
                    try{
                        throw Objects.requireNonNull(task.getException());
                    }catch (FirebaseAuthWeakPasswordException e){
                        erro = "A senha deve conter no mínimo 8 caracteres!";
                    } catch (FirebaseAuthUserCollisionException e){
                        erro = "Conta já cadastrada!";
                    }catch (FirebaseAuthInvalidCredentialsException e) {
                        erro = "Email inválido!";
                    }catch (Exception e) {
                        erro = "Erro ao cadastrar usuário";
                    }
                    liveData.postValue(new Resource<>(null, erro));
                });
        return liveData;
    }

    public LiveData<Resource<Void>> insereUsuario(Usuario usuario) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferencia.child(usuario.getId()).setValue(usuario).addOnCompleteListener(backgroundExecutor, task -> {
            if (task.isSuccessful()) {
                liveData.postValue(new Resource<>(null, null));
                return;
            }
            Exception exception = task.getException();
            String erro = recuperaErro(exception, "Erro desconhecido ao inserir usuário");
            liveData.postValue(new Resource<>(null, erro));
        });
        return liveData;
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }
}
