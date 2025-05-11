package com.kevin.ceep.ui.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.kevin.ceep.databinding.FragmentRecuperarSenhaBinding;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;

import java.util.Objects;

public class RecuperarSenhaFragment
        extends BaseFragment<FragmentRecuperarSenhaBinding> {
    private TextInputLayout txtRecuperaSenha;
    private TextInputEditText edtRecuperaSenha;
    private AppCompatButton botaoRecuperarSenha;
    private FirebaseAuth autenticacao;
    private String email;

    @Override
    protected FragmentRecuperarSenhaBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentRecuperarSenhaBinding.inflate(
                inflater,
                container,
                false
        );
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configuraComponentesVisuais();
        inicializaComponentes();
        configuraCamposTexto();

        botaoRecuperarSenha.setOnClickListener(v -> recuperaSenha());
    }

    private void configuraCamposTexto() {
        edtRecuperaSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email = Objects.requireNonNull(edtRecuperaSenha.getText()).toString().trim();
                verificaEmailValido(email);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void inicializaComponentes() {
        autenticacao = FirebaseAuth.getInstance();
        botaoRecuperarSenha = binding.botaoRecuperarSenha;
        txtRecuperaSenha = binding.txtRecuperarSenha;
        edtRecuperaSenha = binding.edtRecuperarSenha;
    }

    private void configuraComponentesVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    @SuppressLint("NewApi")
    private void verificaEmailValido(String email) {
        if(configuraEditEmail(!email.isEmpty()) &
            configuraEditEmail(Patterns.EMAIL_ADDRESS.matcher(email).matches())
        ) {
            habilitaBotaoRecuperaSenha();
            return;
        }
        configuraMenssagemAjuda(email);
    }

    private void configuraMenssagemAjuda(String email) {
        if (!configuraEditEmail(Patterns.EMAIL_ADDRESS.matcher(email).matches())){
            txtRecuperaSenha.setHelperText("Por favor, informe um email válido!");
        }
        if (!configuraEditEmail(!email.isEmpty()) & email.isEmpty()){
            txtRecuperaSenha.setHelperText("Campo requerido!");
        }
    }

    private void habilitaBotaoRecuperaSenha() {
        txtRecuperaSenha.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#007FFF")));
        txtRecuperaSenha.setBoxStrokeColor(Color.parseColor("#007FFF"));
        txtRecuperaSenha.setHelperTextEnabled(false);
        botaoRecuperarSenha.setEnabled(true);
    }

    @SuppressLint("NewApi")
    private boolean configuraEditEmail(boolean email) {
        if (email) return true;
        txtRecuperaSenha.setBoxStrokeColor(Color.parseColor("#A71500"));
        txtRecuperaSenha.setHelperTextEnabled(true);
        botaoRecuperarSenha.setEnabled(false);
        return false;
    }

    private void recuperaSenha() {
        autenticacao.sendPasswordResetEmail(email).
                addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        mostraMensagem("Confira seu email");
                        NavDirections acao = CadastrarUsuarioFragmentDirections.vaiParaSlashScreen();
                        Navigation.findNavController(binding.getRoot()).navigate(acao);
                        return;
                    }
                    mostraMensagem("Confira se seu email está correto e tente novamente");
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}