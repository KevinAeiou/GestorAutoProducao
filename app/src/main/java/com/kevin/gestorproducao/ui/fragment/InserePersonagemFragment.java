package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static com.kevin.gestorproducao.ui.fragment.ModificaPersonagemFragmentDirections.vaiParaListaTrabalhosProducao;
import static java.lang.Integer.parseInt;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
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

public class InserePersonagemFragment
    extends BaseFragment<FragmentDetalhesPersonagemBinding>
{
    private TextInputLayout personagemNomeTxt, personagemEspacoProducaoTxt, personagemEmailTxt, personagemSenhaTxt;
    private EditText personagemNome, personagemEspacoProducao, personagemEmail, personagemSenha;
    private SwitchCompat personagemSwUso, personagemSwEstado, personagemSwAutoProducao;
    private MaterialButton btnConfirmar;
    private PersonagemViewModel personagemViewModel;
    private Personagem personagem;
    private NavController controlador;
    private PersonagemFluxoService personagemFluxoService;
    private LinearLayout loadingBotaoConfirmar;

    @Override
    protected FragmentDetalhesPersonagemBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesPersonagemBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        configuraBotaoConfirmar();
        observarPersonagem();
    }

    private void configuraBotaoConfirmar() {
        btnConfirmar.setOnClickListener(v -> {
            iniciarLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );

            if (verificaCamposValidos()){
                defineNovoPersonagem();

                personagemViewModel.inserePersonagemUsuario(personagem);
                return;
            }

            pararLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );

            configuraMensagemCamposValidos();
        });
    }

    private void observarPersonagem() {
        personagemViewModel.getInsercaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    personagemViewModel.limpaInsercaoResultado();

                    personagemFluxoService.processarPosInsercao(personagem);
                    mostraMensagemAncorada(getString(R.string.stringPersonagemInseridoComSucessoValor, personagem.getNome()));

                    voltaParaTrabalhosProducao();
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

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
            false,
            false,
            false,
            getString(R.string.stringNovoPersonagem),
            false
        );
    }

    private void voltaParaTrabalhosProducao() {
        controlador.navigate(vaiParaListaTrabalhosProducao());
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
        btnConfirmar = binding.btnConfirmarPersonagem;
        loadingBotaoConfirmar = binding.loadingDotsConfirmar.getRoot();

        controlador = NavHostFragment.findNavController(this);

        Context context = requireContext().getApplicationContext();

        ProfissaoPersonagemRepository profissaoRepository = ProfissaoPersonagemRepository.getInstance(context);
        PersonagemRepository personagemRepository = PersonagemRepository.getInstance(context);
        TrabalhoProducaoRepository producaoRepository = TrabalhoProducaoRepository.getInstance(context);
        TrabalhoEstoqueRepository estoqueRepository = TrabalhoEstoqueRepository.getInstance(context);
        TrabalhoVendaRepository vendaRepository = TrabalhoVendaRepository.getInstance(context);

        personagemFluxoService = new PersonagemFluxoService(
            profissaoRepository,
            personagemRepository,
            producaoRepository,
            estoqueRepository,
            vendaRepository
        );

        ViewModelFactory viewModelFactory = new ViewModelFactory(context);

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

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
            personagemNomeTxt.setError(getString(R.string.stringCampoRequerido));
            return;
        }

        personagemNomeTxt.setErrorEnabled(false);
    }

    private void defineNovoPersonagem() {
        personagem = new Personagem();

        personagem.setNome(personagemNome.getText().toString());
        personagem.setEmail(personagemEmail.getText().toString());
        personagem.setSenha(personagemSenha.getText().toString());
        personagem.setEstado(personagemSwEstado.isChecked());
        personagem.setAutoProducao(personagemSwAutoProducao.isChecked());
        personagem.setUso(personagemSwUso.isChecked());
        personagem.setEspacoProducao(parseInt(personagemEspacoProducao.getText().toString()));
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
        personagemViewModel.removeObservador();
    }
}
