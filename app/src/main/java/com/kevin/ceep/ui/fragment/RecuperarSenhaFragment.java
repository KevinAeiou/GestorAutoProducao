package com.kevin.ceep.ui.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.kevin.ceep.databinding.FragmentRecuperarSenhaBinding;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;

import java.util.Objects;

public class RecuperarSenhaFragment extends Fragment {
    private FragmentRecuperarSenhaBinding binding;
    private TextInputLayout txtRecuperaSenha;
    private TextInputEditText edtRecuperaSenha;
    private AppCompatButton botaoRecuperarSenha;
    private FirebaseAuth autenticacao;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecuperarSenhaBinding.inflate(inflater, container, false);
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
        autenticacao = FirebaseAuth.getInstance();
        botaoRecuperarSenha = binding.botaoRecuperarSenha;
        txtRecuperaSenha = binding.txtRecuperarSenha;
        edtRecuperaSenha = binding.edtRecuperarSenha;
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

        botaoRecuperarSenha.setOnClickListener(v -> recuperaSenha());
    }

    @SuppressLint("NewApi")
    private void verificaEmailValido(String email) {
        if(configuraEditEmail(!email.isEmpty()) & configuraEditEmail(Patterns.EMAIL_ADDRESS.matcher(email).matches())){
            habilitaBotaoRecuperaSenha();
        }else {
            configuraMenssagemAjuda(email);
        }
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
        if (!email){
//            txtRecuperaSenha.setHintTextColor(ColorStateList.valueOf(getColor(R.color.cor_background_bordo)));
            txtRecuperaSenha.setBoxStrokeColor(Color.parseColor("#A71500"));
//            txtRecuperaSenha.setHelperTextColor(ColorStateList.valueOf(getColor(R.color.cor_background_bordo)));
            txtRecuperaSenha.setHelperTextEnabled(true);
            botaoRecuperarSenha.setEnabled(false);
            return false;
        }
        return true;
    }

    private void recuperaSenha() {
        autenticacao.sendPasswordResetEmail(email).
                addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Snackbar.make(binding.getRoot(),
                                "Confira seu email.",
                                Snackbar.LENGTH_LONG).show();
                        NavDirections acao = CadastrarUsuarioFragmentDirections.vaiParaSlashScreen();
                        Navigation.findNavController(binding.getRoot()).navigate(acao);
                        return;
                    }
                    Snackbar.make(binding.getRoot(),
                            "Confira se seu email está correto e tente novamente.",
                            Snackbar.LENGTH_LONG).show();
                });
    }
}