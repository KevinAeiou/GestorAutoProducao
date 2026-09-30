package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_FEITO;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PARA_PRODUZIR;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PRODUZINDO;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentConfirmaProducaoBinding;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;
import com.kevin.gestorproducao.service.PlanejamentoProducaoService;
import com.kevin.gestorproducao.service.ProducaoFluxoService;
import com.kevin.gestorproducao.service.ProducaoServicosFactory;
import com.kevin.gestorproducao.service.ServicosProducaoPersonagem;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

public class ConfirmaProducaoFragment
    extends BaseFragment<FragmentConfirmaProducaoBinding>
{
    private AutoCompleteTextView autoCompleteLicenca, autoCompleteQuantidade;
    private Trabalho trabalhoRecebido;
    private TrabalhoProducao producao;
    private int contador = 0;
    private PersonagemViewModel personagemViewModel;
    private TrabalhoProducaoViewModel producaoViewModel;
    private MaterialSwitch checkRecorrencia;
    private int quantidadeSelecionada = 0;
    private int estadoSelecionado = CODIGO_TRABALHO_PARA_PRODUZIR;
    private String[] licencas, quantidade;
    private View heroRaridadeTarja;
    private TextView txtNomeConfirmaTrabalho, txtNivelConfirmaTrabalho;
    private MaterialCardView chipEstadoParaProduzir, chipEstadoProduzindo, chipEstadoFeito;
    private ImageView iconeChipParaProduzir, iconeChipProduzindo, iconeChipFeito;
    private TextView txtChipParaProduzir, txtChipProduzindo, txtChipFeito;
    private ProducaoFluxoService producaoFluxoService;
    private PlanejamentoProducaoService planejamentoProducaoService;
    private Context context;
    private TrabalhoRepository trabalhoRepo;
    private TrabalhoEstoqueRepository estoqueRepo;
    private ProfissaoPersonagemRepository profissaoPersonagemRepo;
    private TrabalhoProducaoRepository producaoRepo;
    private int estadoAnterior = -1;
    private MaterialButton btnConfirmar;
    private LinearLayout loadingBotaoConfirmar;
    private TextInputLayout txtQuantidade;

    @Override
    protected FragmentConfirmaProducaoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentConfirmaProducaoBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ConfirmaProducaoFragmentArgs argumentos = ConfirmaProducaoFragmentArgs.fromBundle(getArguments());
        trabalhoRecebido = argumentos.getTrabalho();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        preencheCampos();
        configuraBotaoConfirmar();
        observarPersonagem();
        observarProducao();
    }

    private void configuraBotaoConfirmar() {
        btnConfirmar.setOnClickListener(v -> {
            String valorQuantidade = autoCompleteQuantidade
                .getText()
                .toString()
                .trim();

            if (valorQuantidade.isEmpty()) {
                txtQuantidade.setError(getString(R.string.stringInformeQuantidade));
                return;
            }

            int quantidade = Integer.parseInt(valorQuantidade);

            if (quantidade <= 0) {
                txtQuantidade.setError(getString(R.string.stringQuantidadeMaiorQueZero));
                return;
            }

            txtQuantidade.setError(null);

            iniciarLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );

            quantidadeSelecionada = quantidade;
            contador = 0;

            insereTrabalhoProducaoXVezes();
        });
    }

    private void observarProducao() {
        producaoViewModel.getInsercaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    contador ++;

                    if (contador >= quantidadeSelecionada) {

                        producao.setIdTrabalho(trabalhoRecebido.getId());
                        producao.setExperiencia(trabalhoRecebido.getExperiencia());
                        producao.setRaridade(trabalhoRecebido.getRaridade());
                        producao.setNecessarios(trabalhoRecebido.getNecessarios());
                        producao.setProfissao(trabalhoRecebido.getProfissao());
                        producao.setNivel(trabalhoRecebido.getNivel());

                        producaoFluxoService.processarPosModificacao(
                            producao,
                            estadoAnterior
                        );

                        mostraMensagemAncorada(
                            getString(R.string.stringInseridoComSucessoValor, trabalhoRecebido.getNome())
                        );
                        voltaParaListaProducao();
                    }
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );
                mostraMensagemAncorada(resultado.getErro());
            }
        );
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                producaoViewModel.setIdPersonagem(personagem.getId());

                ServicosProducaoPersonagem servicos = ProducaoServicosFactory.cria(
                    trabalhoRepo,
                    estoqueRepo,
                    producaoRepo,
                    profissaoPersonagemRepo,
                    personagem.getId(),
                    context
                );

                planejamentoProducaoService = servicos.getPlanejamentoProducaoService();
                producaoFluxoService = servicos.getProducaoFluxoService();
            }
        );
    }

    private void inicializaComponentes() {
        autoCompleteLicenca = binding.txtAutoCompleteLicencaConfirmaTrabalho;
        autoCompleteQuantidade = binding.txtAutoCompleteQuantidadeConfirmaTrabalho;
        checkRecorrencia = binding.switchProducaoRecorrenteConfirmaTrabalho;
        btnConfirmar = binding.btnConfirmarProducao;
        txtQuantidade = binding.txtInputLayoutQuantidadeConfirmaTrabalho;
        loadingBotaoConfirmar = binding.loadingDotsConfirmar.getRoot();

        heroRaridadeTarja = binding.heroRaridadeTarja;
        txtNomeConfirmaTrabalho = binding.txtNomeConfirmaTrabalho;
        txtNivelConfirmaTrabalho = binding.txtNivelConfirmaTrabalho;

        chipEstadoParaProduzir = binding.chipEstadoParaProduzir;
        chipEstadoProduzindo = binding.chipEstadoProduzindo;
        chipEstadoFeito = binding.chipEstadoFeito;
        iconeChipParaProduzir = binding.iconeChipParaProduzir;
        iconeChipProduzindo = binding.iconeChipProduzindo;
        iconeChipFeito = binding.iconeChipFeito;
        txtChipParaProduzir = binding.txtChipParaProduzir;
        txtChipProduzindo = binding.txtChipProduzindo;
        txtChipFeito = binding.txtChipFeito;

        chipEstadoParaProduzir.setOnClickListener(v -> selecionaEstado(CODIGO_TRABALHO_PARA_PRODUZIR));
        chipEstadoProduzindo.setOnClickListener(v -> selecionaEstado(CODIGO_TRABALHO_PRODUZINDO));
        chipEstadoFeito.setOnClickListener(v -> selecionaEstado(CODIGO_TRABALHO_FEITO));

        licencas = getResources().getStringArray(R.array.licencas_completas);
        quantidade = getResources().getStringArray(R.array.quantidade);

        context = requireContext().getApplicationContext();
        trabalhoRepo = TrabalhoRepository.getInstancia(context);
        estoqueRepo = TrabalhoEstoqueRepository.getInstance(context);
        profissaoPersonagemRepo = ProfissaoPersonagemRepository.getInstance(context);
        producaoRepo = TrabalhoProducaoRepository.getInstance(context);

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        producaoViewModel = new ViewModelProvider(
            getViewModelStore(),
            viewModelFactory
        ).get(TrabalhoProducaoViewModel.class);
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
            trabalhoRecebido.getNome(),
            false
        );
    }

    private void preencheCampos() {
        if (trabalhoRecebido == null) return;

        txtNomeConfirmaTrabalho.setText(trabalhoRecebido.getNome());
        txtNivelConfirmaTrabalho.setText(context.getString(R.string.stringNivelBadge, trabalhoRecebido.getNivel()));
        binding.txtProfissaoConfirmaTrabalho.setText(trabalhoRecebido.getProfissao());
        configuraRaridadeHero(trabalhoRecebido);
        configuraChipsEstado();
    }

    private void configuraRaridadeHero(Trabalho trabalho) {
        String raridade = trabalho.getRaridade();
        int corRaridade;
        int corTarja;
        if ("Melhorado".equals(raridade)) {
            corRaridade = ContextCompat.getColor(context, R.color.cor_producao_raridade_melhorado);
            corTarja = corRaridade;
        } else if ("Raro".equals(raridade)) {
            corRaridade = ContextCompat.getColor(context, R.color.cor_producao_raridade_raro);
            corTarja = corRaridade;
        } else if ("Especial".equals(raridade)) {
            corRaridade = ContextCompat.getColor(context, R.color.cor_producao_raridade_especial);
            corTarja = corRaridade;
        } else {
            corRaridade = MaterialColors.getColor(txtNomeConfirmaTrabalho, com.google.android.material.R.attr.colorOnSurface);
            corTarja = MaterialColors.getColor(heroRaridadeTarja, com.google.android.material.R.attr.colorOutlineVariant);
        }
        txtNomeConfirmaTrabalho.setTextColor(corRaridade);
        heroRaridadeTarja.setBackgroundColor(corTarja);
    }

    private void configuraChipsEstado() {
        configuraChipEstado(chipEstadoParaProduzir, iconeChipParaProduzir, txtChipParaProduzir, estadoSelecionado == CODIGO_TRABALHO_PARA_PRODUZIR);
        configuraChipEstado(chipEstadoProduzindo, iconeChipProduzindo, txtChipProduzindo, estadoSelecionado == CODIGO_TRABALHO_PRODUZINDO);
        configuraChipEstado(chipEstadoFeito, iconeChipFeito, txtChipFeito, estadoSelecionado == CODIGO_TRABALHO_FEITO);
    }

    private void configuraChipEstado(MaterialCardView chip, ImageView icone, TextView texto, boolean selecionado) {
        if (!selecionado) {
            chip.setCardBackgroundColor(MaterialColors.getColor(chip, com.google.android.material.R.attr.colorSurface));
            chip.setStrokeColor(MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOutlineVariant));
            int corPadrao = MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOnSurfaceVariant);
            texto.setTextColor(corPadrao);
            icone.setVisibility(View.GONE);
            return;
        }

        int corContainer;
        int corOnContainer;
        if (chip == chipEstadoParaProduzir) {
            corContainer = R.color.cor_estado_para_produzir_container;
            corOnContainer = R.color.cor_estado_para_produzir_on_container;
        } else if (chip == chipEstadoProduzindo) {
            corContainer = R.color.cor_estado_produzindo_container;
            corOnContainer = R.color.cor_estado_produzindo_on_container;
        } else {
            corContainer = R.color.cor_estado_feito_container;
            corOnContainer = R.color.cor_estado_feito_on_container;
        }

        int corOnContainerValor = ContextCompat.getColor(context, corOnContainer);
        chip.setCardBackgroundColor(ContextCompat.getColor(context, corContainer));
        chip.setStrokeColor(corOnContainerValor);
        texto.setTextColor(corOnContainerValor);
        icone.setVisibility(View.VISIBLE);
        icone.setImageResource(R.drawable.ic_estado_feito);
        ImageViewCompat.setImageTintList(icone, ColorStateList.valueOf(corOnContainerValor));
    }

    private void selecionaEstado(int estado) {
        estadoSelecionado = estado;
        configuraChipsEstado();
    }

    private void configuraDropDrown() {
        ArrayAdapter<String> adapterLicenca = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_dropdrown,
            licencas
        );
        ArrayAdapter<String> adapterQuantidade = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_dropdrown,
            quantidade
        );

        autoCompleteLicenca.setAdapter(adapterLicenca);
        autoCompleteQuantidade.setAdapter(adapterQuantidade);

        if (autoCompleteLicenca.getText().toString().isEmpty()) {
            autoCompleteLicenca.setText(licencas[3], false);
        }

        if (autoCompleteQuantidade.getText().toString().isEmpty()) {
            autoCompleteQuantidade.setText(quantidade[0], false);
        }
    }

    private void insereTrabalhoProducaoXVezes() {
        for (int x = 0; x < quantidadeSelecionada; x ++){
            insereTrabalhoProducao();
        }
    }

    private void insereTrabalhoProducao() {
        TrabalhoProducao trabalho = defineNovoTrabalhoProducao();

        producaoViewModel.insereTrabalhoProducao(trabalho);
    }

    private void voltaParaListaProducao() {
        NavController controlador = Navigation.findNavController(binding.getRoot());

        controlador.getBackStackEntry(R.id.listaTrabalhosProducao)
            .getSavedStateHandle()
            .set(
                "mensagem_sucesso",
                getString(R.string.stringInseridoComSucessoValor, trabalhoRecebido.getNome())
            );

        controlador.popBackStack(
            R.id.listaTrabalhosProducao,
            false
        );
    }

    private TrabalhoProducao defineNovoTrabalhoProducao() {
        TrabalhoProducao novaProducao = new TrabalhoProducao();

        int estado = estadoSelecionado;

        if (estado == CODIGO_TRABALHO_PRODUZINDO) {
            novaProducao.marcarIniciado();
            estadoAnterior = CODIGO_TRABALHO_PARA_PRODUZIR;
        } else if (estado == CODIGO_TRABALHO_FEITO) {
            novaProducao.marcarIniciado();
            novaProducao.marcarFinalizado();
            estadoAnterior = CODIGO_TRABALHO_PRODUZINDO;
        }

        novaProducao.setIdTrabalho(trabalhoRecebido.getId());
        novaProducao.setExperiencia(trabalhoRecebido.getExperiencia());
        novaProducao.setTipoLicenca(autoCompleteLicenca.getText().toString());
        novaProducao.setRecorrencia(checkRecorrencia.isChecked());
        novaProducao.setEstado(estado);

        producao = new TrabalhoProducao();
        producao.setEstado(novaProducao.getEstado());
        producao.setTipoLicenca(novaProducao.getTipoLicenca());

        return novaProducao;
    }

    @Override
    public void onResume() {
        super.onResume();

        configuraDropDrown();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        autoCompleteLicenca = null;
        autoCompleteQuantidade = null;
        removeOuvinteProducao();
        removeOuvintePersonagem();
    }

    private void removeOuvintePersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }

    private void removeOuvinteProducao() {
        if (producaoViewModel == null) return;
        producaoViewModel.removeObservador();
    }
}