package com.kevin.ceep.ui.fragment;

import static android.view.View.GONE;
import static java.lang.Integer.parseInt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentAtributosPersonagemBinding;
import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;

public class InserePersonagemFragment
        extends BaseFragment<FragmentAtributosPersonagemBinding>
        implements MenuProvider{

    private TextInputLayout personagemNomeTxt, personagemEspacoProducaoTxt, personagemEmailTxt, personagemSenhaTxt;
    private EditText personagemNome, personagemEspacoProducao, personagemEmail, personagemSenha;
    private SwitchCompat personagemSwUso, personagemSwEstado, personagemSwAutoProducao;
    private PersonagemViewModel personagemViewModel;
    private NavController controlador;
    @Override
    protected FragmentAtributosPersonagemBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAtributosPersonagemBinding.inflate(
                inflater,
                container,
                false
        );
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        controlador = NavHostFragment.findNavController(this);
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuConfirma = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        inicializaComponentes();
    }
    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

    }
    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma) {
            if (verificaCamposValidos()){
                Personagem personagem = defineNovoPersonagem();
                inserePersonagem(personagem);
                return true;
            }
            configuraMensagemCamposValidos();
            return true;
        }
        return false;
    }

    private void inserePersonagem(Personagem personagem) {
        personagemViewModel.getInsercaoResultado().observe(
                getViewLifecycleOwner(),
                resultadoInserePersonagem -> {
            if (resultadoInserePersonagem.getErro() == null) {
                voltaParaTrabalhosProducao();
                return;
            }
            mostraMensagem("Erro: "+resultadoInserePersonagem.getErro());
        });
        personagemViewModel.inserePersonagem(personagem);
    }
    private void voltaParaTrabalhosProducao() {
        controlador.navigate(ModificaPersonagemFragmentDirections.vaiParaListaTrabalhosProducao());
    }

    private void inicializaComponentes() {
        personagemNome = binding.edtNomePersonagem;
        personagemNomeTxt = binding.txtNomePersonagem;
        personagemEspacoProducao = binding.edtEspacoProducaoPersonagem;
        personagemEspacoProducaoTxt = binding.txtEspacoProducaoPersonagem;
        personagemSwUso = binding.swUsoPersonagem;
        personagemSwEstado = binding.swEstadoPersonagem;
        personagemSwAutoProducao = binding.swAutoProducaoPersonagem;
        personagemEmail = binding.edtEmailPersonagem;
        personagemEmailTxt = binding.txtEmailPersonagem;
        personagemSenha = binding.edtSenhaPersonagem;
        personagemSenhaTxt = binding.txtSenhaPersonagem;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(PersonagemRepository.getInstance());
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
        binding.btnExcluiPersonagem.setVisibility(GONE);
    }

    private void configuraMensagemCamposValidos() {
        configuraMensagem(personagemNome, personagemNomeTxt);
        configuraMensagem(personagemEspacoProducao, personagemEspacoProducaoTxt);
        configuraMensagem(personagemEmail, personagemEmailTxt);
        configuraMensagem(personagemSenha, personagemSenhaTxt);
    }

    private void configuraMensagem(EditText personagemNome, TextInputLayout personagemNomeTxt) {
        if (personagemNome.getText().toString().isEmpty()) {
            personagemNomeTxt.setHelperText("Campo requerido!");
            return;
        }
        personagemNomeTxt.setHelperTextEnabled(false);
    }

    private Personagem defineNovoPersonagem() {
        Personagem novoPersonagem = new Personagem();
        novoPersonagem.setNome(personagemNome.getText().toString());
        novoPersonagem.setEmail(personagemEmail.getText().toString());
        novoPersonagem.setSenha(personagemSenha.getText().toString());
        novoPersonagem.setEstado(personagemSwEstado.isChecked());
        novoPersonagem.setAutoProducao(personagemSwAutoProducao.isChecked());
        novoPersonagem.setUso(personagemSwUso.isChecked());
        novoPersonagem.setEspacoProducao(parseInt(personagemEspacoProducao.getText().toString()));
        return novoPersonagem;
    }

    private boolean verificaCamposValidos() {
        return !personagemNome.getText().toString().isEmpty() &&
            !personagemEspacoProducao.getText().toString().isEmpty() &&
            !personagemEmail.getText().toString().isEmpty() &&
            !personagemSenha.getText().toString().isEmpty();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeOuvintePersonagem();
        binding = null;
    }

    private void removeOuvintePersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeOuvinte();
    }
}
