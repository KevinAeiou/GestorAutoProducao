package com.kevin.ceep.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentEntrarUsuarioBinding;
import com.kevin.ceep.model.Usuario;
import com.kevin.ceep.repository.FirebaseAuthRepository;
import com.kevin.ceep.ui.viewModel.AutenticacaoViewModel;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.factory.AutenticacaoViewModelFactor;

import java.util.Objects;

public class EntrarUsuarioFragment extends Fragment implements View.OnClickListener {
    private FragmentEntrarUsuarioBinding binding;
    private TextInputEditText edtEmail, edtSenha;
    private TextInputLayout txtEmail, txtSenha;
    private TextView txtCadastrar, txtRecuperarSenha;
    private AppCompatButton botao_entrar;
    private AutenticacaoViewModel autenticacaoViewModel;
    private EstadoAppViewModel estadoAppViewModel;
    String [] menssagens = {"Campo requerido!", "Login efetuado com sucesso!"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEntrarUsuarioBinding.inflate(inflater, container, false);
        Log.d("fluxo", "onCreateView");

        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inicializaComponentes();
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        estadoAppViewModel.componentes.setValue(componentesVisuais);
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
        AutenticacaoViewModelFactor autenticacaoViewModelFactor = new AutenticacaoViewModelFactor(FirebaseAuthRepository.getInstance());
        autenticacaoViewModel = new ViewModelProvider(this, autenticacaoViewModelFactor).get(AutenticacaoViewModel.class);
        estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
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
        NavDirections acao = EntrarUsuarioFragmentDirections.vaiDeEntrarParaRecuperarSenha();
        Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    private void vaiParaCadastroUsuarioActivity() {
        NavDirections acao = EntrarUsuarioFragmentDirections.vaiDeEntrarParaCadastrar();
        Navigation.findNavController(binding.getRoot()).navigate(acao);
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
        botao_entrar.setEnabled(true);
        configuraErroCampoEmailVazio(usuario);
        configuraErroCampoSenhaVazia(usuario);
    }

    private void configuraErroCampoEmailVazio(Usuario usuario) {
        if (usuario.getEmail().isEmpty()) {
            txtEmail.setHelperText(menssagens[0]);
            return;
        }
        txtEmail.setHelperTextEnabled(false);
    }

    private void configuraErroCampoSenhaVazia(Usuario usuario) {
        if (usuario.getSenha().isEmpty()) {
            txtSenha.setHelperText(menssagens[0]);
            return;
        }
        txtSenha.setHelperTextEnabled(false);
    }

    private static boolean camposVazios(Usuario personagem) {
        return personagem.getEmail().isEmpty() || personagem.getSenha().isEmpty();
    }

    private void autenticarUsuario(Usuario usuario) {
        autenticacaoViewModel.autenticarUsuario(usuario).observe(getViewLifecycleOwner(), resultadoAutenticacao -> {
            if (resultadoAutenticacao.getErro() == null) {
                Log.d("fluxo", "USUARIO AUTENTICADO");
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
        NavDirections acao = EntrarUsuarioFragmentDirections.vaiParaListaTrabalhosProducao();
        Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("fluxo", "USUARIO ATUAL: " + usuarioAtual);
        if (usuarioAtual == null) return;
        vaiParaMenuNavegacao();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}