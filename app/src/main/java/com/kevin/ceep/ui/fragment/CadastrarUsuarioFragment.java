package com.kevin.ceep.ui.fragment;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentCadastrarUsuarioBinding;
import com.kevin.ceep.model.Usuario;
import com.kevin.ceep.repository.FirebaseAuthRepository;
import com.kevin.ceep.ui.viewModel.AutenticacaoViewModel;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.factory.AutenticacaoViewModelFactor;

import java.util.Objects;

public class CadastrarUsuarioFragment extends Fragment implements View.OnClickListener {
    private FragmentCadastrarUsuarioBinding binding;
    private AppCompatButton botaoCadastrarUsuario;
    private TextInputLayout txtSenha;
    private TextInputEditText edtNome;
    private TextInputEditText edtSenha;
    String[] menssagens = {"Preencha todos os campos", "Usuário cadastrado com sucesso!"};
    private AutenticacaoViewModel autenticacaoViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCadastrarUsuarioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        txtSenha = binding.txtSenha;
        edtSenha = binding.edtSenha;
        AutenticacaoViewModelFactor autenticacaoViewModelFactor = new AutenticacaoViewModelFactor(new FirebaseAuthRepository());
        autenticacaoViewModel = new ViewModelProvider(this, autenticacaoViewModelFactor).get(AutenticacaoViewModel.class);
        configuraEdtSenhaRobusta();
        botaoCadastrarUsuario = binding.botaoCadastrarUsuario;
        botaoCadastrarUsuario.setOnClickListener(this);
        binding.txtLinkEntrar.setOnClickListener(this);
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
                & configuraEditSenha(senha.matches(especial))
                & configuraEditSenha(senha.matches(numbers))
                & configuraEditSenha(senha.matches(lowerCaseChars))
                & configuraEditSenha(senha.matches(upperCaseChars))){
            habilitaBotaoCadastro();
            return;
        }
        configuraMenssagemAjuda(senha, tamanhoSenha, upperCaseChars, lowerCaseChars, numbers, especial);
    }

    private void habilitaBotaoCadastro() {
        txtSenha.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#007FFF")));
        txtSenha.setBoxStrokeColor(Color.parseColor("#007FFF"));
        txtSenha.setHelperTextEnabled(false);
        botaoCadastrarUsuario.setEnabled(true);
    }

    private void configuraMenssagemAjuda(String senha, int tamanhoSenha, String upperCaseChars, String lowerCaseChars, String numbers, String especial) {
        if (!configuraEditSenha(tamanhoSenha >= 8)) {
            txtSenha.setHelperText(getString(R.string.string_senha_curta));
        }
        if (!configuraEditSenha(senha.matches(numbers))) {
            txtSenha.setHelperText(getString(R.string.string_senha_numerica));
        }
        if (!configuraEditSenha(senha.matches(lowerCaseChars))) {
            txtSenha.setHelperText(getString(R.string.string_senha_minuscula));
        }
        if (!configuraEditSenha(senha.matches(upperCaseChars))) {
            txtSenha.setHelperText(getString(R.string.string_senha_maiuscula));
        }
        if (!configuraEditSenha(senha.matches(especial))) {
            txtSenha.setHelperText(getString(R.string.string_senha_especial));
        }
    }

    @SuppressLint("NewApi")
    private boolean configuraEditSenha(boolean senha) {
        if (!senha){
//            txtSenha.setHintTextColor(ColorStateList.valueOf(getColor(R.color.cor_background_bordo)));
            txtSenha.setBoxStrokeColor(Color.parseColor("#A71500"));
//            txtSenha.setHelperTextColor(ColorStateList.valueOf(getColor(R.color.cor_background_bordo)));
            txtSenha.setHelperTextEnabled(true);
            botaoCadastrarUsuario.setEnabled(false);
            return false;
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.txtLinkEntrar:
                NavDirections acao = CadastrarUsuarioFragmentDirections.vaiParaSlashScreen();
                Navigation.findNavController(binding.getRoot()).navigate(acao);
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

        if (verificaCampos(usuario)){
            autenticacaoViewModel.criaUsuario(usuario).observe(this, resultadoCriaUsuario -> {
                if (resultadoCriaUsuario.getErro() == null) {
                    salvarDadosUsuario();
                    return;
                }
                Snackbar snackbar = Snackbar.make(binding.getRoot(), resultadoCriaUsuario.getErro(), Snackbar.LENGTH_SHORT);
                snackbar.setBackgroundTint(Color.WHITE);
                snackbar.setTextColor(Color.BLACK);
                snackbar.show();
            });
            return;
        }
        Snackbar snackbar = Snackbar.make(binding.getRoot(), menssagens[0], Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(Color.WHITE);
        snackbar.setTextColor(Color.BLACK);
        snackbar.show();
    }

    private void salvarDadosUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        usuario.setNome(Objects.requireNonNull(edtNome.getText()).toString());
        autenticacaoViewModel.insereUsuario(usuario).observe(this, resultadoInsereUsuario -> {
            if (resultadoInsereUsuario.getErro() == null) {
                NavDirections acao = CadastrarUsuarioFragmentDirections.vaiParaSlashScreen();
                Navigation.findNavController(binding.getRoot()).navigate(acao);
                return;
            }
            Snackbar.make(binding.getRoot(), resultadoInsereUsuario.getErro(), Snackbar.LENGTH_SHORT).show();
        });
    }

    private boolean verificaCampos(Usuario personagem) {
        return personagem.getNome().isEmpty() || personagem.getEmail().isEmpty() || personagem.getSenha().isEmpty();
    }
}