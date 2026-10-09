package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentDetalhesProfissaoPersonagemBinding;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;
import com.kevin.gestorproducao.service.PlanejamentoProducaoService;
import com.kevin.gestorproducao.service.ProducaoServicosFactory;
import com.kevin.gestorproducao.service.ResumoPlanejamento;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoPersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;
import com.kevin.gestorproducao.utilitario.Formatador;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class DetalhesProfissaoPersonagemFragment
    extends BaseFragment<FragmentDetalhesProfissaoPersonagemBinding>
    implements MenuProvider
{
    private ProfissaoPersonagem profissaoRecebida;
    private TextInputEditText edtExperiencia;
    private MaterialSwitch swtPrioridade;
    private ProfissaoPersonagemViewModel profissaoPersonagemViewModel;
    private ArrayList<TrabalhoProducao> producao;
    private TrabalhoProducaoViewModel producaoViewModel;
    private PersonagemViewModel personagemViewModel;
    private String idPersonagemSelecionado;
    private CircularProgressIndicator indicadorAtual, indicadorMaximo, indicadorProduzindo, indicadorProduzir;
    private TextView txtExperienciaRingValor, txtExpProduzir, txtExpProduzindo;
    private TextView txtNivel, txtPercentual, txtXpFaltante, txtExpAtual, seloPrioridade;
    private TextInputLayout txtExperiencia;
    private NavController controlador;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DetalhesProfissaoPersonagemFragmentArgs argumentos = DetalhesProfissaoPersonagemFragmentArgs.fromBundle(
            getArguments()
        );

        profissaoRecebida = argumentos.getProfissao();

    }

    @Override
    protected FragmentDetalhesProfissaoPersonagemBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesProfissaoPersonagemBinding.inflate(inflater, container, false);
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
        preencheCampos();
        configuraResultadoModificacao();
        observaPersonagem();
        observaProducao();
    }

    private void observaProducao() {
        producaoViewModel.getProducoes().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getDado() != null) {
                    producao = resultado.getDado().stream().filter(
                        trabalho ->
                            trabalho.getProfissao().equals(profissaoRecebida.getNome())
                    ).collect(Collectors.toCollection(ArrayList::new));

                    configuraBarraProgressoCircular();
                }

                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(resultado.getErro());
                }
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
            profissaoRecebida.getNome(),
            false
        );
    }

    private void observaPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                idPersonagemSelecionado = personagem.getId();
                producaoViewModel.setIdPersonagem(personagem.getId());
                profissaoPersonagemViewModel.setIdPersonagem(personagem.getId());

                producaoViewModel.recuperaProducaoPorProfissaoPersonagem();
            }
        );
    }

    private void configuraResultadoModificacao() {
        profissaoPersonagemViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado.getErro() == null) {
                    if (prioridadeFoiLigada()) {
                        atualizaProducaoPriorizada();
                    }
                    voltaParaListaProfissoes();
                }

                txtExperiencia.setError(resultado.getErro());
            }
        );
    }

    private boolean prioridadeFoiLigada() {
        return swtPrioridade.isChecked() && !profissaoRecebida.isPrioridade();
    }

    // Gatilho automático: ao ligar a prioridade, já completa a fila da profissão.
    private void atualizaProducaoPriorizada() {
        if (idPersonagemSelecionado == null) return;

        Context contexto = requireContext().getApplicationContext();
        PlanejamentoProducaoService planejamento = ProducaoServicosFactory.cria(
            TrabalhoRepository.getInstancia(contexto),
            TrabalhoEstoqueRepository.getInstance(contexto),
            TrabalhoProducaoRepository.getInstance(contexto),
            ProfissaoPersonagemRepository.getInstance(contexto),
            idPersonagemSelecionado,
            contexto
        ).getPlanejamentoProducaoService();

        ResumoPlanejamento resumo = planejamento.atualizaAgora();
        if (resumo.temNovidades()) {
            mostraMensagemAncorada(resumo.paraMensagem());
        }
    }

    private void voltaParaListaProfissoes() {
        controlador.navigateUp();
    }

    private void preencheCampos() {
        edtExperiencia.setText(String.valueOf(profissaoRecebida.getExperiencia()));
        swtPrioridade.setChecked(profissaoRecebida.isPrioridade());
        seloPrioridade.setVisibility(swtPrioridade.isChecked() ? VISIBLE : GONE);
        txtNivel.setText(getString(R.string.stringNivelTitulo, profissaoRecebida.getNivel()));
        swtPrioridade.setOnCheckedChangeListener(
            (botao, marcado) -> seloPrioridade.setVisibility(marcado ? VISIBLE : GONE)
        );
        binding.btnSalvarProfissaoPersonagem.setOnClickListener(v -> confirmarModificacao());
    }

    private void inicializaComponentes() {
        txtExperiencia = binding.txtExperienciaProfissaoFragment;
        edtExperiencia = binding.edtExperienciaProfissaoFragment;
        swtPrioridade = binding.swtPrioridadeProfissaoFragment;

        indicadorMaximo = binding.indicadorExperienciaMaxima;
        indicadorAtual = binding.indicadorExperienciaAtual;
        indicadorProduzindo = binding.indicadorExperienciaProduzindo;
        indicadorProduzir = binding.indicadorExperienciaProduzir;

        txtExperienciaRingValor = binding.txtExperienciaRingValor;
        txtExpProduzir = binding.txtExperienciaProduzirProfissaoFragment;
        txtExpProduzindo = binding.txtExperienciaProduzindoProfissaoFragment;
        txtNivel = binding.txtNivelProfissaoFragment;
        txtPercentual = binding.txtPercentualProgresso;
        txtXpFaltante = binding.txtXpFaltante;
        txtExpAtual = binding.txtExperienciaAtualProfissaoFragment;
        seloPrioridade = binding.txtSeloPrioridade;

        producao = new ArrayList<>();

        configurarMascaraMilhar(edtExperiencia);

        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());
        profissaoPersonagemViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(ProfissaoPersonagemViewModel.class);

        producaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoProducaoViewModel.class);

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);
    }

    private void confirmarModificacao() {
        Integer experiencia = defineValorExperiencia();
        if (experiencia == null) return;

        ProfissaoPersonagem profissao = new ProfissaoPersonagem();
        profissao.setId(profissaoRecebida.getId());
        profissao.setExperiencia(experiencia);
        profissao.setPrioridade(swtPrioridade.isChecked());

        profissaoPersonagemViewModel.modificaExperienciaProfissao(profissao);
    }

    @Nullable
    private Integer defineValorExperiencia() {
        String experiencia = edtExperiencia.getText().toString().trim();

        if (experiencia.isEmpty()) {
            txtExperiencia.setError(getString(R.string.stringCampoObrigatorio));
            return null;
        }

        int novaExperiencia;
        try {
            novaExperiencia = obterValorNumerico(edtExperiencia);

            if (novaExperiencia < 0) {
                throw new NumberFormatException(getString(R.string.stringExperienciaNaoPodeSerNegativa));
            }
        } catch (NumberFormatException e) {
            txtExperiencia.setError(e.getMessage());
            return null;
        }

        if (Objects.equals(profissaoRecebida.getExperiencia(), novaExperiencia) &&
            swtPrioridade.isChecked() == profissaoRecebida.isPrioridade()
        ) {
            voltaParaListaProfissoes();
            return null;
        }

        return novaExperiencia;
    }

    private void configuraBarraProgressoCircular() {
        int xpNecessario = profissaoRecebida.getXpNecessario();

        indicadorMaximo.setMax(xpNecessario);
        indicadorAtual.setMax(xpNecessario);
        indicadorProduzindo.setMax(xpNecessario);
        indicadorProduzir.setMax(xpNecessario);

        int experienciaAtual = profissaoRecebida.getExperienciaRelativa();
        int experienciaProduzindo = 0;
        int experienciaProduzir = 0;
        for(TrabalhoProducao trabalho : producao) {
            if (trabalho.getProfissao().equals(profissaoRecebida.getNome())){
                if (trabalho.ehProduzindo()) {
                    experienciaProduzindo += trabalho.getExperiencia();
                }
                if (trabalho.ehProduzir()) {
                    experienciaProduzir += trabalho.getExperiencia();
                }
            }
        }
        txtExperienciaRingValor.setText(getString(
            R.string.stringExperienciaFracao,
            Formatador.formatarMilhar(experienciaAtual),
            Formatador.formatarMilhar(xpNecessario)
        ));
        txtExpProduzir.setText(Formatador.formatarMilhar(experienciaProduzir));
        txtExpProduzindo.setText(Formatador.formatarMilhar(experienciaProduzindo));

        int percentual = xpNecessario == 0 ? 0 : experienciaAtual * 100 / xpNecessario;
        txtPercentual.setText(getString(R.string.stringPercentualProgresso, percentual));
        txtExpAtual.setText(Formatador.formatarMilhar(experienciaAtual));
        txtXpFaltante.setText(getString(
            R.string.stringFaltamParaProximoNivel,
            Formatador.formatarMilhar(Math.max(xpNecessario - experienciaAtual, 0))
        ));

        configuraVisibilidadeView(xpNecessario, txtExperienciaRingValor);

        experienciaProduzindo += experienciaAtual;
        experienciaProduzir += experienciaProduzindo;

        animateProgress(indicadorAtual, experienciaAtual);
        animateProgress(indicadorProduzir, experienciaProduzir);
        animateProgress(indicadorProduzindo, experienciaProduzindo);
    }

    private void configuraVisibilidadeView(int experiencia, View view) {
        int visibilidade = experiencia == 0 ? GONE : VISIBLE;
        view.setVisibility(visibilidade);
    }

    private void animateProgress(CircularProgressIndicator indicador, int experiencia) {
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
            indicador,
            "progress",
            0,
            experiencia
        );
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma) {
            confirmarModificacao();
            return true;
        }

        return false;
    }
}