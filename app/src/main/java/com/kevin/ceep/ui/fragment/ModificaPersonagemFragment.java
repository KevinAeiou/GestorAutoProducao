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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentAtributosPersonagemBinding;
import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;

public class ModificaPersonagemFragment extends Fragment implements MenuProvider{

    private Personagem personagemRecebido;
    private EditText personagemNome, personagemEspacoProducao, personagemEmail, personagemSenha;
    private SwitchCompat personagemSwUso, personagemSwEstado, personagemSwAutoProducao;
    private FragmentAtributosPersonagemBinding binding;
    private PersonagemViewModel personagemViewModel;
    private NavController controlador;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAtributosPersonagemBinding.inflate(inflater, container, false);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
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
        pegaPersonagemSelecionado();
        configuraBotaoExcluir();
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
                dialogoDeAlerta.setPositiveButton("Sim", (dialogInterface, i) -> modificaPersonagemServidor(personagem));
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
        personagemViewModel.removePersonagem(personagemRecebido).observe(getViewLifecycleOwner(), resultadoPersonagem -> {
            if (resultadoPersonagem.getErro() == null) {
                Snackbar.make(binding.getRoot(), "Personagem: " + personagemRecebido.getId() + " foi removido!", Snackbar.LENGTH_LONG).show();
                voltaParaTrabalhosProducao();
                return;
            }
            Snackbar.make(binding.getRoot(), "Erro: "+resultadoPersonagem.getErro(), Snackbar.LENGTH_LONG).show();
        });
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

    private void modificaPersonagemServidor(Personagem personagemModificado) {
        personagemViewModel.modificaPersonagem(personagemModificado).observe(getViewLifecycleOwner(), resultadoModificaPersonagem -> {
            if (resultadoModificaPersonagem.getErro() == null) {
                voltaParaTrabalhosProducao();
                return;
            }
            Snackbar.make(binding.getRoot(), "Erro: "+resultadoModificaPersonagem.getErro(), Snackbar.LENGTH_LONG).show();
        });
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