package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.fragment.EntrarUsuarioFragmentDirections.vaiDeEntrarParaCadastrar;
import static com.kevin.gestorproducao.ui.fragment.EntrarUsuarioFragmentDirections.vaiDeEntrarParaProducao;
import static com.kevin.gestorproducao.ui.fragment.EntrarUsuarioFragmentDirections.vaiDeEntrarParaRecuperarSenha;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentEntrarUsuarioBinding;
import com.kevin.gestorproducao.model.Usuario;
import com.kevin.gestorproducao.ui.viewModel.AutenticacaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.Objects;

public class EntrarUsuarioFragment
    extends BaseFragment<FragmentEntrarUsuarioBinding>
    implements View.OnClickListener
{
    private TextInputEditText edtEmail, edtSenha;
    private TextInputLayout txtEmail, txtSenha;
    private TextView txtCadastrar, txtRecuperarSenha;
    private MaterialButton botao_entrar;
    private AutenticacaoViewModel autenticacaoViewModel;
    private PersonagemViewModel personagemViewModel;
    private NavController controlador;

    @Override
    protected FragmentEntrarUsuarioBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentEntrarUsuarioBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        autenticacaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(AutenticacaoViewModel.class);

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.botaoEntrar:
                iniciarLoadingBotao(botao_entrar, binding.loadingDotsBotao.getRoot());
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
        controlador.navigate(vaiDeEntrarParaRecuperarSenha());
    }

    private void vaiParaCadastroUsuarioActivity() {
        controlador.navigate(vaiDeEntrarParaCadastrar());
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
        pararLoadingBotao(botao_entrar, binding.loadingDotsBotao.getRoot());
        configuraErroCampoEmailVazio(usuario);
        configuraErroCampoSenhaVazia(usuario);
    }

    private void configuraErroCampoEmailVazio(Usuario usuario) {
        if (usuario.getEmail().isEmpty()) {
            txtEmail.setError(getString(R.string.stringCampoRequerido));
            return;
        }
        txtEmail.setErrorEnabled(false);
    }

    private void configuraErroCampoSenhaVazia(Usuario usuario) {
        if (usuario.getSenha().isEmpty()) {
            txtSenha.setError(getString(R.string.stringCampoRequerido));
            return;
        }
        txtSenha.setErrorEnabled(false);
    }

    private static boolean camposVazios(Usuario personagem) {
        return personagem.getEmail().isEmpty() || personagem.getSenha().isEmpty();
    }

    private void autenticarUsuario(Usuario usuario) {
        autenticacaoViewModel.autenticarUsuario(usuario).observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    personagemViewModel.sincronizaPersonagens(user.getUid());

                    vaiParaMenuNavegacao();
                    return;
                }
                configuraErroExecoesCampos(resultado.getErro());
            }
        );
    }

    private void configuraErroExecoesCampos(String mensagem) {
        pararLoadingBotao(botao_entrar, binding.loadingDotsBotao.getRoot());
        if (mensagem.equals("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")) {
            mostraMensagemAncorada(getString(R.string.stringSemConexaoComInternet));
            return;
        }
        txtEmail.setHelperText(getString(R.string.stringEmailInvalidoAjuda));
        txtSenha.setHelperText(getString(R.string.stringSenhaInvalidaAjuda));
    }

    private void vaiParaMenuNavegacao() {
        controlador.navigate(vaiDeEntrarParaProducao());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}