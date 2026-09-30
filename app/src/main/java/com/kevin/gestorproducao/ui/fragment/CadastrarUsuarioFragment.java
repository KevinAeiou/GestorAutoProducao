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
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
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
import java.util.regex.Pattern;

public class CadastrarUsuarioFragment
    extends BaseFragment<FragmentCadastrarUsuarioBinding>
    implements View.OnClickListener
{
    private static final int TAMANHO_MINIMO_SENHA = 8;
    private static final Pattern MAIUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern NUMERO = Pattern.compile("\\d");
    private static final Pattern ESPECIAL = Pattern.compile("[^A-Za-z0-9\\s]");

    private MaterialButton botaoCadastrarUsuario;
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

                pararLoadingBotao(botaoCadastrarUsuario, binding.loadingDotsBotao.getRoot());
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

    private void verificaSenhaRobusta() {
        String senha = Objects.requireNonNull(edtSenha.getText()).toString();
        Integer erro = primeiroRequisitoNaoAtendido(senha);
        if (erro == null) {
            txtSenha.setErrorEnabled(false);
            botaoCadastrarUsuario.setEnabled(true);
            return;
        }

        txtSenha.setError(getString(erro));
        botaoCadastrarUsuario.setEnabled(false);
    }

    // Os regex ficam no código: em strings.xml o aapt trata "\" como escape e "\d" vira "d".
    @Nullable
    @StringRes
    private static Integer primeiroRequisitoNaoAtendido(String senha) {
        if (senha.length() < TAMANHO_MINIMO_SENHA) return R.string.string_senha_curta;
        if (!MAIUSCULA.matcher(senha).find()) return R.string.string_senha_maiuscula;
        if (!MINUSCULA.matcher(senha).find()) return R.string.string_senha_minuscula;
        if (!NUMERO.matcher(senha).find()) return R.string.string_senha_numerica;
        if (!ESPECIAL.matcher(senha).find()) return R.string.string_senha_especial;
        return null;
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

        iniciarLoadingBotao(botaoCadastrarUsuario, binding.loadingDotsBotao.getRoot());
        if (verificaCampos(usuario)){
            autenticacaoViewModel.criaUsuario(usuario).observe(
                getViewLifecycleOwner(),
                resultado -> {
                    if (resultado.getErro() == null) {
                        salvarDadosUsuario();
                        return;
                    }

                    pararLoadingBotao(botaoCadastrarUsuario, binding.loadingDotsBotao.getRoot());
                    Snackbar snackbar = Snackbar.make(binding.getRoot(), resultado.getErro(), Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            );
            return;
        }
        pararLoadingBotao(botaoCadastrarUsuario, binding.loadingDotsBotao.getRoot());
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