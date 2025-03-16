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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentAtributosPersonagemBinding;
import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;

public class InserePersonagemFragment extends Fragment implements MenuProvider{

    private TextInputLayout personagemNomeTxt, personagemEspacoProducaoTxt, personagemEmailTxt, personagemSenhaTxt;
    private EditText personagemNome, personagemEspacoProducao, personagemEmail, personagemSenha;
    private SwitchCompat personagemSwUso, personagemSwEstado, personagemSwAutoProducao;
    private FragmentAtributosPersonagemBinding binding;
    private PersonagemViewModel personagemViewModel;
    private NavController controlador;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAtributosPersonagemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controlador = NavHostFragment.findNavController(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                Personagem novoPersonagem = definePersonagemModificado();
                inserePersonagemServidor(novoPersonagem);
                return true;
            }
            configuraMensagemCamposValidos();
            return true;
        }
        return false;
    }

    private void inserePersonagemServidor(Personagem novoPersonagem) {
        personagemViewModel.inserePersonagem(novoPersonagem).observe(this, resultadoInserePersonagem -> {
            if (resultadoInserePersonagem.getErro() == null) {
                voltaParaTrabalhosProducao();
                return;
            }
            Snackbar.make(binding.getRoot(),"Erro: "+resultadoInserePersonagem.getErro(), Snackbar.LENGTH_LONG).show();
        });
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
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(new PersonagemRepository(getContext()));
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
        binding.btnExcluiPersonagem.setVisibility(GONE);
    }

    private void configuraMensagemCamposValidos() {
        if (configuraMensagem(personagemNome, personagemNomeTxt)) return;
        if (configuraMensagem(personagemEspacoProducao, personagemEspacoProducaoTxt)) return;
        if (configuraMensagem(personagemEmail, personagemEmailTxt)) return;
        configuraMensagem(personagemSenha, personagemSenhaTxt);
    }

    private boolean configuraMensagem(EditText personagemNome, TextInputLayout personagemNomeTxt) {
        if (personagemNome.getText().toString().isEmpty()) {
            personagemNomeTxt.setHelperText("Campo requerido!");
            return true;
        }
        personagemNomeTxt.setHelperTextEnabled(false);
        return false;
    }

    private Personagem definePersonagemModificado() {
        Personagem personagemModificado = new Personagem();
        personagemModificado.setNome(personagemNome.getText().toString());
        personagemModificado.setEmail(personagemEmail.getText().toString());
        personagemModificado.setSenha(personagemSenha.getText().toString());
        personagemModificado.setEstado(personagemSwEstado.isChecked());
        personagemModificado.setAutoProducao(personagemSwAutoProducao.isChecked());
        personagemModificado.setUso(personagemSwUso.isChecked());
        personagemModificado.setEspacoProducao(parseInt(personagemEspacoProducao.getText().toString()));
        return personagemModificado;
    }

    private boolean verificaCamposValidos() {
        return !personagemNome.getText().toString().isEmpty() &&
            !personagemEspacoProducao.getText().toString().isEmpty() &&
            !personagemEmail.getText().toString().isEmpty() &&
            !personagemSenha.getText().toString().isEmpty();
    }
}
