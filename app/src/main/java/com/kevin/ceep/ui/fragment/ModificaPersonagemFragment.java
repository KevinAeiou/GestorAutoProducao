package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.utilitario.Utilitario.comparaString;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentAtributosPersonagemBinding;
import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;

public class ModificaPersonagemFragment
        extends BaseFragment<FragmentAtributosPersonagemBinding>
        implements MenuProvider{

    private Personagem personagemRecebido;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controlador = NavHostFragment.findNavController(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        configuraComponentesVisuais();
        inicializaComponentes();
        pegaPersonagemSelecionado();
        configuraBotaoExcluir();
    }

    private void configuraComponentesVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuConfirma = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma) {
            Personagem personagem = definePersonagemModificado();
            if (personagemEhModificado(personagem)) {
                MaterialAlertDialogBuilder dialogoDeAlerta = new MaterialAlertDialogBuilder(requireContext());
                dialogoDeAlerta.setMessage("Deseja confirmar alterações?");
                dialogoDeAlerta.setNegativeButton("Não", ((dialogInterface, i) -> onDestroy()));
                dialogoDeAlerta.setPositiveButton("Sim", (dialogInterface, i) -> modificaPersonagem(personagem));
                dialogoDeAlerta.show();
                return true;
            }
            voltaParaTrabalhosProducao();
            return true;
        }
        return false;
    }

    private void configuraBotaoExcluir() {
        binding.btnExcluiPersonagem.setOnClickListener(view -> {
            MaterialAlertDialogBuilder dialogoDeAlerta = new MaterialAlertDialogBuilder(requireContext());
            dialogoDeAlerta.setTitle("Personagem será removido permanentemente");
            dialogoDeAlerta.setMessage("Deseja continuar?");
            dialogoDeAlerta.setNegativeButton("Não", ((dialogInterface, i) -> voltaParaTrabalhosProducao()));
            dialogoDeAlerta.setPositiveButton("Sim", (dialogInterface, i) -> removePersonagem());
            dialogoDeAlerta.show();
        });
    }

    private void removePersonagem() {
        personagemViewModel.getRemocaoResultado().observe(
            getViewLifecycleOwner(),
            resultadoPersonagem -> {
            if (resultadoPersonagem.getErro() == null) {
                mostraMensagem("Personagem: " + personagemRecebido.getId() + " foi removido!");
                voltaParaTrabalhosProducao();
                return;
            }
            mostraMensagem("Erro: "+resultadoPersonagem.getErro());
        });
        personagemViewModel.removePersonagem(personagemRecebido);
    }

    private void pegaPersonagemSelecionado() {
        personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), resultadoPegaPersonagem -> {
            if (resultadoPegaPersonagem == null) return;
            personagemRecebido = resultadoPegaPersonagem;
            preencheCampos();
        });
    }

    private void voltaParaTrabalhosProducao() {
        controlador.navigate(ModificaPersonagemFragmentDirections.vaiParaListaTrabalhosProducao());
    }

    private void preencheCampos() {
        personagemNome.setText(personagemRecebido.getNome());
        personagemEspacoProducao.setText(String.valueOf(personagemRecebido.getEspacoProducao()));
        personagemEmail.setText(personagemRecebido.getEmail());
        personagemSenha.setText(personagemRecebido.getSenha());
        personagemSwUso.setChecked(personagemRecebido.getUso());
        personagemSwEstado.setChecked(personagemRecebido.getEstado());
        personagemSwAutoProducao.setChecked(personagemRecebido.isAutoProducao());
    }

    private void inicializaComponentes() {
        personagemNome = binding.edtNomePersonagem;
        personagemEspacoProducao = binding.edtEspacoProducaoPersonagem;
        personagemSwUso = binding.swUsoPersonagem;
        personagemSwEstado = binding.swEstadoPersonagem;
        personagemSwAutoProducao = binding.swAutoProducaoPersonagem;
        personagemEmail = binding.edtEmailPersonagem;
        personagemSenha = binding.edtSenhaPersonagem;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(PersonagemRepository.getInstance());
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
    }

    private Personagem definePersonagemModificado() {
        Personagem personagemModificado = new Personagem();
        personagemModificado.setId(personagemRecebido.getId());
        personagemModificado.setNome(personagemNome.getText().toString());
        personagemModificado.setEmail(personagemEmail.getText().toString());
        personagemModificado.setSenha(personagemSenha.getText().toString());
        personagemModificado.setEstado(personagemSwEstado.isChecked());
        personagemModificado.setAutoProducao(personagemSwAutoProducao.isChecked());
        personagemModificado.setUso(personagemSwUso.isChecked());
        personagemModificado.setEspacoProducao(parseInt(personagemEspacoProducao.getText().toString()));
        return personagemModificado;
    }

    private void modificaPersonagem(Personagem personagemModificado) {
        personagemViewModel.getModificacaoResultado().observe(getViewLifecycleOwner(), resultadoModificaPersonagem -> {
            if (resultadoModificaPersonagem.getErro() == null) {
                voltaParaTrabalhosProducao();
                return;
            }
            mostraMensagem("Erro: "+resultadoModificaPersonagem.getErro());
        });
        personagemViewModel.modificaPersonagem(personagemModificado);
    }

    private boolean personagemEhModificado(Personagem personagem) {
        return !(comparaString(personagem.getNome(),personagemRecebido.getNome()) &
               personagem.getUso() == personagemRecebido.getUso() &&
               personagem.getEstado() == personagemRecebido.getEstado() &&
               personagem.isAutoProducao() == personagemRecebido.isAutoProducao() &&
               personagem.getEspacoProducao() == personagemRecebido.getEspacoProducao() &&
               comparaString(personagem.getEmail(), personagemRecebido.getEmail()) &&
               comparaString(personagem.getSenha(), personagemRecebido.getSenha()));
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