package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.fragment.RecuperarSenhaFragmentDirections.vaiParaSlashScreen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentRecuperarSenhaBinding;
import com.kevin.gestorproducao.ui.viewModel.AutenticacaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.Objects;

public class RecuperarSenhaFragment
    extends BaseFragment<FragmentRecuperarSenhaBinding>
{
    private TextInputLayout txtRecuperaSenha;
    private TextInputEditText edtRecuperaSenha;
    private AppCompatButton botaoRecuperarSenha;
    private TextView txtLinkEntrar;
    private String email;
    private AutenticacaoViewModel autenticacaoViewModel;
    private NavController controlador;

    @Override
    protected FragmentRecuperarSenhaBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentRecuperarSenhaBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        configuraCamposTexto();
        observarAutenticacao();
        configuraBotaoRecuperacao();
        configuraLinkEntrar();
    }

    private void configuraBotaoRecuperacao() {
        botaoRecuperarSenha.setOnClickListener(v -> autenticacaoViewModel.recuperaSenha(email));
    }

    private void configuraLinkEntrar() {
        txtLinkEntrar.setOnClickListener(v -> controlador.navigate(vaiParaSlashScreen()));
    }

    private void observarAutenticacao() {
        autenticacaoViewModel.getRecuperacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    controlador.navigate(vaiParaSlashScreen());
                    mostraMensagemAncorada(getString(R.string.stringConfiraSeuEmail));
                    return;
                }

                mostraMensagemAncorada(getString(R.string.stringConfiraEmailCorretoTenteNovamente));
            }
        );
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
        botaoRecuperarSenha = binding.botaoRecuperarSenha;
        txtRecuperaSenha = binding.txtRecuperarSenha;
        edtRecuperaSenha = binding.edtRecuperarSenha;
        txtLinkEntrar = binding.txtLinkEntrar;

        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        autenticacaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(AutenticacaoViewModel.class);
    }

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
            txtRecuperaSenha.setError(getString(R.string.stringEmailInvalidoErro));
        }
        if (!configuraEditEmail(!email.isEmpty()) & email.isEmpty()){
            txtRecuperaSenha.setError(getString(R.string.stringCampoRequerido));
        }
    }

    private void habilitaBotaoRecuperaSenha() {
        txtRecuperaSenha.setErrorEnabled(false);

        botaoRecuperarSenha.setEnabled(true);
    }

    private boolean configuraEditEmail(boolean email) {
        if (email) return true;
        txtRecuperaSenha.setErrorEnabled(true);
        botaoRecuperarSenha.setEnabled(false);
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}