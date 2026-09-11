package com.kevin.gestorproducao.repository;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_USUARIOS2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kevin.gestorproducao.model.Usuario;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FirebaseAuthRepository {
    private static volatile FirebaseAuthRepository instancia;
    private final FirebaseAuth minhaInstancia;
    private final DatabaseReference minhaReferencia;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);

    public FirebaseAuthRepository() {
        this.minhaInstancia = FirebaseAuth.getInstance();
        this.minhaReferencia = FirebaseDatabase.getInstance().getReference(CHAVE_USUARIOS2);
    }

    public static synchronized FirebaseAuthRepository getInstance() {
        if (instancia == null) {
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

    public LiveData<Resource<Void>> recuperaSenha(String email) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        minhaInstancia.sendPasswordResetEmail(email).addOnCompleteListener(
            backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    liveData.postValue(new Resource<>(null, null));
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido recuperar a senha");
                liveData.postValue(new Resource<>(null, erro));
            }
        );

        return liveData;
    }

    public LiveData<Resource<Usuario>> recuperaUsuarioAtual() {
        MutableLiveData<Resource<Usuario>> liveData = new MutableLiveData<>();

        if (minhaInstancia.getCurrentUser() == null) {
            liveData.postValue(
                new Resource<>(null, "Usuário não autenticado")
            );

            return liveData;
        }

        String uid = minhaInstancia.getCurrentUser().getUid();

        minhaReferencia.child(uid).get().addOnCompleteListener(
            backgroundExecutor, task -> {
                if (task.isSuccessful()) {

                    Usuario usuario = task.getResult().getValue(Usuario.class);

                    liveData.postValue(new Resource<>(usuario, null));

                    return;
                }

                Exception exception = task.getException();

                String erro = recuperaErro(
                    exception,
                    "Erro ao recuperar usuário"
                );

                liveData.postValue(new Resource<>(null, erro));
            });

        return liveData;
    }
}
