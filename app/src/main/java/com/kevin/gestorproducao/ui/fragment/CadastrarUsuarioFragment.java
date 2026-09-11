package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.fragment.CadastrarUsuarioFragmentDirections.vaiParaSlashScreen;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentCadastrarUsuarioBinding;
import com.kevin.gestorproducao.model.Usuario;
import com.kevin.gestorproducao.ui.viewModel.AutenticacaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.Objects;

public class CadastrarUsuarioFragment
    extends BaseFragment<FragmentCadastrarUsuarioBinding>
    implements View.OnClickListener
{
    private AppCompatButton botaoCadastrarUsuario;
    private TextInputLayout txtSenha;
    private TextInputEditText edtNome;
    private TextInputEditText edtSenha;
    private AutenticacaoViewModel autenticacaoViewModel;
    private NavController controlador;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        configuraEdtSenhaRobusta();
        observarAutenticacao();
        botaoCadastrarUsuario.setOnClickListener(this);
        binding.txtLinkEntrar.setOnClickListener(this);
    }

    private void observarAutenticacao() {
        autenticacaoViewModel.getInsercaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    mostraMensagemAncorada(getString(R.string.stringUsuarioCadastradoComSucesso));
                    controlador.navigate(vaiParaSlashScreen());
                    return;
                }

                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );
    }

    private void inicializaComponentes() {
        txtSenha = binding.txtSenha;
        edtSenha = binding.edtSenha;
        botaoCadastrarUsuario = binding.botaoCadastrarUsuario;

        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        autenticacaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(AutenticacaoViewModel.class);
    }

    private void configuraEdtSenhaRobusta() {
        edtSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                verificaSenhaRobusta();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void verificaSenhaRobusta() {
        String senha = Objects.requireNonNull(edtSenha.getText()).toString();
        int tamanhoSenha = senha.length();
        String upperCaseChars = getString(R.string.stringCasoChaMa);
        String lowerCaseChars = getString(R.string.stringCasoCharMi);
        String numbers = getString(R.string.stringCasoCharNum);
        String especial = getString(R.string.stringCasoCharS);
        if (configuraEditSenha(tamanhoSenha>=8)
            && configuraEditSenha(senha.matches(especial))
            && configuraEditSenha(senha.matches(numbers))
            && configuraEditSenha(senha.matches(lowerCaseChars))
            && configuraEditSenha(senha.matches(upperCaseChars))
        ){
            habilitaBotaoCadastro();
            return;
        }

        configuraMenssagemAjuda(senha, tamanhoSenha, upperCaseChars, lowerCaseChars, numbers, especial);
    }

    private void habilitaBotaoCadastro() {
        txtSenha.setErrorEnabled(false);
        botaoCadastrarUsuario.setEnabled(true);
    }

    private void configuraMenssagemAjuda(
        String senha,
        int tamanhoSenha,
        String upperCaseChars,
        String lowerCaseChars,
        String numbers,
        String especial
    ) {
        if (!configuraEditSenha(tamanhoSenha >= 8)) {
            txtSenha.setError(getString(R.string.string_senha_curta));
        }
        if (!configuraEditSenha(senha.matches(numbers))) {
            txtSenha.setError(getString(R.string.string_senha_numerica));
        }
        if (!configuraEditSenha(senha.matches(lowerCaseChars))) {
            txtSenha.setError(getString(R.string.string_senha_minuscula));
        }
        if (!configuraEditSenha(senha.matches(upperCaseChars))) {
            txtSenha.setError(getString(R.string.string_senha_maiuscula));
        }
        if (!configuraEditSenha(senha.matches(especial))) {
            txtSenha.setError(getString(R.string.string_senha_especial));
        }
    }

    private boolean configuraEditSenha(boolean senha) {
        if (senha) return true;

        txtSenha.setErrorEnabled(true);
        botaoCadastrarUsuario.setEnabled(false);
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.txtLinkEntrar:
                controlador.navigate(vaiParaSlashScreen());
                break;
            case R.id.botaoCadastrarUsuario:
                cadastrarUsuario();
        }
    }

    private void cadastrarUsuario() {
        edtNome = binding.edtNome;
        TextInputEditText edtEmail = binding.edtEmail;
        Usuario usuario = new Usuario();
        usuario.setNome(Objects.requireNonNull(edtNome.getText()).toString());
        usuario.setEmail(Objects.requireNonNull(edtEmail.getText()).toString());
        usuario.setSenha(Objects.requireNonNull(edtSenha.getText()).toString());

        botaoCadastrarUsuario.setEnabled(false);
        if (verificaCampos(usuario)){
            autenticacaoViewModel.criaUsuario(usuario).observe(
                getViewLifecycleOwner(),
                resultado -> {
                    if (resultado.getErro() == null) {
                        salvarDadosUsuario();
                        return;
                    }

                    Snackbar snackbar = Snackbar.make(binding.getRoot(), resultado.getErro(), Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            );
            return;
        }
        mostraMensagemAncorada(getString(R.string.stringPreencherTodosCampos));
    }

    private void salvarDadosUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        usuario.setNome(Objects.requireNonNull(edtNome.getText()).toString());
        autenticacaoViewModel.insereUsuario(usuario);
    }

    private boolean verificaCampos(Usuario usuario) {
        return !(usuario.getNome().isEmpty() || usuario.getEmail().isEmpty() || usuario.getSenha().isEmpty());
    }

    @Override
    protected FragmentCadastrarUsuarioBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentCadastrarUsuarioBinding.inflate(inflater, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}