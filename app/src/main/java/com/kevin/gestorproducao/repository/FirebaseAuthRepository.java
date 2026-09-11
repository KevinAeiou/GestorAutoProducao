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
import com.kevin.gestorproducao.repository.helper.FirebaseTimeoutHelper;

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
        return FirebaseTimeoutHelper.execute(callback -> minhaInstancia
            .signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    callback.sucesso();
                    return;
                }
                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao autenticar usuário");
                callback.erro(erro);
            })
        );
    }

    public LiveData<Resource<Void>> criaUsuario(Usuario usuario) {
        return FirebaseTimeoutHelper.execute(callback -> minhaInstancia
            .createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    callback.sucesso();
                    return;
                }

                String erro;
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthWeakPasswordException e) {
                    erro = "A senha deve conter no mínimo 8 caracteres!";
                } catch (FirebaseAuthUserCollisionException e) {
                    erro = "Conta já cadastrada!";
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    erro = "Email inválido!";
                } catch (Exception e) {
                    erro = "Erro ao cadastrar usuário";
                }
                callback.erro(erro);
            })
        );
    }

    public LiveData<Resource<Void>> insereUsuario(Usuario usuario) {
        return FirebaseTimeoutHelper.execute(callback -> minhaReferencia
            .child(usuario.getId())
            .setValue(usuario)
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    callback.sucesso();
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao inserir usuário");
                callback.erro(erro);
            })
        );
    }

    private String recuperaErro(Exception exception, String erro) {
        return exception == null ? erro : exception.getMessage();
    }

    public LiveData<Resource<Void>> recuperaSenha(String email) {
        return FirebaseTimeoutHelper.execute(callback -> minhaInstancia
            .sendPasswordResetEmail(email)
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    callback.sucesso();
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido recuperar a senha");
                callback.erro(erro);
            })
        );
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
