package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static com.kevin.gestorproducao.ui.fragment.ModificaPersonagemFragmentDirections.vaiParaListaTrabalhosProducao;
import static com.kevin.gestorproducao.utilitario.Utilitario.comparaString;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentDetalhesPersonagemBinding;
import com.kevin.gestorproducao.model.Personagem;
import com.kevin.gestorproducao.repository.PersonagemRepository;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoVendaRepository;
import com.kevin.gestorproducao.service.PersonagemFluxoService;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

public class ModificaPersonagemFragment
    extends BaseFragment<FragmentDetalhesPersonagemBinding>
    implements MenuProvider
{
    private Personagem personagemRecebido;
    private TextInputLayout personagemNomeTxt, personagemEspacoProducaoTxt, personagemEmailTxt, personagemSenhaTxt;
    private TextInputEditText personagemNome, personagemEspacoProducao, personagemEmail, personagemSenha;
    private MaterialSwitch personagemSwUso, personagemSwEstado, personagemSwAutoProducao;
    private PersonagemViewModel personagemViewModel;
    private NavController controlador;
    private PersonagemFluxoService personagemFluxoService;

    @Override
    protected FragmentDetalhesPersonagemBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesPersonagemBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controlador = NavHostFragment.findNavController(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(
            this,
            getViewLifecycleOwner(),
            androidx.lifecycle.Lifecycle.State.RESUMED
        );

        inicializaComponentes();
        configuraBotaoExcluir();
        observarPersonagem();
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;
                personagemRecebido = resultado;

                preencheCampos();
            }
        );

        personagemViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    personagemViewModel.limpaModificacaoResultado();

                    voltaParaTrabalhosProducao();
                    return;
                }
                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );

        personagemViewModel.getRemocaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    personagemViewModel.limpaRemocaoResultado();

                    personagemFluxoService.processarPosRemocao(personagemRecebido.getId());

                    mostraMensagemAncorada(getString(R.string.stringPersonagemRemovidoValor, personagemRecebido.getId()));
                    personagemViewModel.definePersonagemSelecionado(null);
                    voltaParaTrabalhosProducao();
                    return;
                }
                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            false,
            false,
            true,
            false,
            false,
            getString(R.string.stringDadosDoPersonagem),
            false
        );
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma) {
            if (!verificaCamposValidos()) {
                configuraMensagemCamposValidos();
                return true;
            }

            Personagem personagem = definePersonagemModificado();
            if (personagemEhModificado(personagem)) {
                personagemViewModel.modificaPersonagem(personagem);
                return true;
            }

            voltaParaTrabalhosProducao();
            return true;
        }
        return false;
    }

    private boolean verificaCamposValidos() {
        return !personagemNome.getText().toString().isEmpty() &&
            !personagemEspacoProducao.getText().toString().isEmpty() &&
            !personagemEmail.getText().toString().isEmpty() &&
            !personagemSenha.getText().toString().isEmpty();
    }

    private void configuraMensagemCamposValidos() {
        configuraMensagem(personagemNome, personagemNomeTxt);
        configuraMensagem(personagemEspacoProducao, personagemEspacoProducaoTxt);
        configuraMensagem(personagemEmail, personagemEmailTxt);
        configuraMensagem(personagemSenha, personagemSenhaTxt);
    }

    private void configuraMensagem(TextInputEditText campo, TextInputLayout campoTxt) {
        if (campo.getText().toString().isEmpty()) {
            campoTxt.setError(getString(R.string.stringCampoRequerido));
            return;
        }

        campoTxt.setErrorEnabled(false);
    }

    private void configuraBotaoExcluir() {
        binding.btnExcluiPersonagem.setOnClickListener(view -> {
            ConfirmacaoDialog dialog = ConfirmacaoDialog.novaInstancia(
                getString(R.string.stringPersonagemSeraRemovido),
                getString(R.string.stringDesejaContinuar),
                () -> personagemViewModel.removePersonagem(personagemRecebido.getId()),
                () -> {}
            );

            dialog.show(getParentFragmentManager(), "confirmacao_exclusao");
        });
    }

    private void voltaParaTrabalhosProducao() {
        controlador.navigate(vaiParaListaTrabalhosProducao());
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

        Context context = requireContext().getApplicationContext();

        ProfissaoPersonagemRepository profissaoPersonagemRepository = ProfissaoPersonagemRepository.getInstance(context);
        PersonagemRepository personagemRepository = PersonagemRepository.getInstance(context);
        TrabalhoProducaoRepository producaoRepository = TrabalhoProducaoRepository.getInstance(context);

        TrabalhoEstoqueRepository estoqueRepository = TrabalhoEstoqueRepository.getInstance(context);
        TrabalhoVendaRepository vendaRepository = TrabalhoVendaRepository.getInstance(context);

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        personagemFluxoService = new PersonagemFluxoService(
            profissaoPersonagemRepository,
            personagemRepository,
            producaoRepository,
            estoqueRepository,
            vendaRepository
        );

        binding.btnConfirmarPersonagem.setVisibility(GONE);
    }

    private Personagem definePersonagemModificado() {
        Personagem personagem = new Personagem();

        personagem.setId(personagemRecebido.getId());
        personagem.setNome(personagemNome.getText().toString());
        personagem.setEmail(personagemEmail.getText().toString());
        personagem.setSenha(personagemSenha.getText().toString());
        personagem.setEstado(personagemSwEstado.isChecked());
        personagem.setAutoProducao(personagemSwAutoProducao.isChecked());
        personagem.setUso(personagemSwUso.isChecked());
        personagem.setEspacoProducao(obterValorNumerico(personagemEspacoProducao));

        return personagem;
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
        personagemViewModel.removeObservador();
    }
}