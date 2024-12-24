package com.kevin.ceep.ui.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.ActivityEntrarUsuarioBinding;
import com.kevin.ceep.model.Usuario;
import com.kevin.ceep.repository.FirebaseAuthRepository;
import com.kevin.ceep.ui.viewModel.AutenticacaoViewModel;
import com.kevin.ceep.ui.viewModel.factory.AutenticacaoViewModelFactor;

import java.util.Objects;

public class EntrarUsuarioActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityEntrarUsuarioBinding binding;
    private TextInputEditText edtEmail, edtSenha;
    private TextInputLayout txtEmail, txtSenha;
    private TextView txtCadastrar, txtRecuperarSenha;
    private AppCompatButton botao_entrar;
    private AutenticacaoViewModel autenticacaoViewModel;
    String [] menssagens = {"Campo requerido!", "Login efetuado com sucesso!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntrarUsuarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        inicializaComponentes();
        botao_entrar.setOnClickListener(this);
        txtCadastrar.setOnClickListener(this);
        txtRecuperarSenha.setOnClickListener(this);
    }

    private void inicializaComponentes() {
        botao_entrar = binding.botaoEntrar;
        txtCadastrar = binding.txtLinkCadastro;
        txtRecuperarSenha = binding.txtEsqueceuSenha;
        txtEmail = binding.txtEmail;
        txtSenha = binding.txtSenha;
        edtEmail = binding.edtEmail;
        edtSenha = binding.edtSenha;
        AutenticacaoViewModelFactor autenticacaoViewModelFactor = new AutenticacaoViewModelFactor(new FirebaseAuthRepository());
        autenticacaoViewModel = new ViewModelProvider(this, autenticacaoViewModelFactor).get(AutenticacaoViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.botaoEntrar:
                botao_entrar.setEnabled(false);
                entrarUsuario();
                break;
            case R.id.txtLinkCadastro:
                vaiParaCadastroUsuarioActivity();
                break;
            case R.id.txtEsqueceuSenha:
                vaiParaRecuperarSenhaActivity();
                break;
        }
    }

    private void vaiParaRecuperarSenhaActivity() {
        Intent iniciaVaiParaRecuperarSenhaActivity = new Intent(this, RecuperarSenhaActivity.class);
        startActivity(iniciaVaiParaRecuperarSenhaActivity,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void vaiParaCadastroUsuarioActivity() {
        Intent iniciaVaiParaCadastroUsuarioActivity = new Intent(this, CadastrarUsuarioActivity.class);
        startActivity(iniciaVaiParaCadastroUsuarioActivity,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void entrarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail(Objects.requireNonNull(edtEmail.getText()).toString());
        usuario.setSenha(Objects.requireNonNull(edtSenha.getText()).toString());
        if (camposVazios(usuario)){
            configuraErrosCampos(usuario);
            return;
        }
        txtEmail.setHelperTextEnabled(false);
        txtSenha.setHelperTextEnabled(false);
        autenticarUsuario(usuario);
    }

    private void configuraErrosCampos(Usuario usuario) {
        if (configuraErroCampoEmailVazio(usuario)) return;
        if (configuraErroCampoSenhaVazia(usuario)) return;
        botao_entrar.setEnabled(true);
    }

    private boolean configuraErroCampoEmailVazio(Usuario usuario) {
        if (usuario.getEmail().isEmpty()) {
            txtEmail.setHelperText(menssagens[0]);
            return true;
        }
        txtEmail.setHelperTextEnabled(false);
        return false;
    }

    private boolean configuraErroCampoSenhaVazia(Usuario usuario) {
        if (usuario.getSenha().isEmpty()) {
            txtSenha.setHelperText(menssagens[0]);
            return true;
        }
        txtSenha.setHelperTextEnabled(false);
        return false;
    }

    private static boolean camposVazios(Usuario personagem) {
        return personagem.getEmail().isEmpty() || personagem.getSenha().isEmpty();
    }

    private void autenticarUsuario(Usuario usuario) {
        autenticacaoViewModel.autenticarUsuario(usuario).observe(this, resultadoAutenticacao -> {
            if (resultadoAutenticacao.getErro() == null) {
                vaiParaMenuNavegacao();
                return;
            }
            configuraErroExecoesCampos(resultadoAutenticacao.getErro());
        });
    }

    private void configuraErroExecoesCampos(String mensagem) {
        botao_entrar.setEnabled(true);
        if (mensagem.equals("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")) {
            Snackbar.make(binding.getRoot(), "Sem conexão com a internet!", Snackbar.LENGTH_LONG).show();
            return;
        }
        txtEmail.setHelperText("Email inválido!");
        txtSenha.setHelperText("Senha inválida!");
    }

    private void vaiParaMenuNavegacao() {
        Intent vaiParaMenuNavegacao =  new Intent(getApplicationContext(), MainActivity.class);
        startActivity(vaiParaMenuNavegacao,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioAtual != null){
            vaiParaMenuNavegacao();
        }
    }
}