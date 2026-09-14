package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_FEITO;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PARA_PRODUZIR;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PRODUZINDO;
import static com.kevin.gestorproducao.ui.fragment.DetalhesProducaoFragmentDirections.vaiParaListaTrabalhosProducao;
import static com.kevin.gestorproducao.utilitario.Utilitario.comparaString;
import static com.kevin.gestorproducao.utilitario.Utilitario.formatarTimestamp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentDetalhesProducaoBinding;
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
import com.kevin.gestorproducao.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;
import com.kevin.gestorproducao.utilitario.Formatador;

public class DetalhesProducaoFragment
    extends BaseFragment<FragmentDetalhesProducaoBinding>
{
    private TrabalhoProducao trabalho, trabalhoModificado;
    private SwitchMaterial recorrenciaTrabalho;
    private TextView txtNomeTrabalho, txtNomeProducaoTrabalho, txtProfissaoTrabalho,
        txtExperienciaTrabalho, txtNivelTrabalho, txtLicencaHero,
        txtCriadoEm, txtModificadoEm, txtFinalizadoEm, txtIniciadoEm;
    private View heroRaridadeTarja;
    private MaterialCardView heroEstadoSelo;
    private ImageView heroEstadoIcone;
    private TextView heroEstadoTexto;
    private MaterialCardView chipEstadoParaProduzir, chipEstadoProduzindo, chipEstadoFeito;
    private ImageView iconeChipParaProduzir, iconeChipProduzindo, iconeChipFeito;
    private TextView txtChipParaProduzir, txtChipProduzindo, txtChipFeito;
    private int estadoSelecionado;
    private AutoCompleteTextView autoCompleteLicenca;
    private MaterialButton btnExcluir, btnConfirmar;
    private String[] licencasTrabalho;
    private TrabalhoProducaoViewModel producaoViewModel;
    private PersonagemViewModel personagemViewModel;
    private TrabalhoEstoqueViewModel estoqueViewModel;
    private TrabalhoViewModel trabalhoViewModel;
    private NavController controlador;
    private Context context;
    private TrabalhoRepository trabalhoRepo;
    private TrabalhoEstoqueRepository estoqueRepo;
    private TrabalhoProducaoRepository producaoRepo;
    private ProfissaoPersonagemRepository profissaoPersonagemRepo;
    private ProducaoFluxoService producaoFluxoService;
    private PlanejamentoProducaoService planejamentoProducaoService;
    private int experienciaBase = 0, estadoAnterior = -1;
    private LinearLayout loadingBotaoConfirmar, layoutCamposDatas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DetalhesProducaoFragmentArgs argumentos = DetalhesProducaoFragmentArgs.fromBundle(getArguments());

        trabalho = argumentos.getTrabalho();
        estadoAnterior = trabalho.getEstado();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializarComponentes();
        configurarBotaoExcluir();
        configurarBotaoConfirmar();
        preencherCampos();
        observarPersonagem();
        observarProducao();
    }

    private void configurarBotaoConfirmar() {
        btnConfirmar.setOnClickListener(v -> {
            iniciarLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );

            if (!validarConexao()) {
                return;
            }

            verificaModificacaoTrabalho();
        });
    }

    private void configurarBotaoExcluir() {
        btnExcluir.setOnClickListener(v -> {
            iniciarLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );

            ConfirmacaoDialog dialog = ConfirmacaoDialog.novaInstancia(
                getString(R.string.stringExcluirProducao),
                getString(R.string.stringConfirmaExclusaoProducao),
                () -> {
                    btnExcluir.setEnabled(false);
                    producaoViewModel.removeTrabalhoProducao(trabalho);
                },
                () -> pararLoadingBotao(btnConfirmar, loadingBotaoConfirmar)
            );

            dialog.show(getParentFragmentManager(), "confirmacao_exclusao");
        });
    }

    private void observarProducao() {
        producaoViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    producaoViewModel.limpaModificacaoResultado();

                    producaoFluxoService.processarPosModificacao(
                        trabalhoModificado,
                        estadoAnterior
                    );

                    vaiParaListaProducao();
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );

        producaoViewModel.getRemocaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                btnExcluir.setEnabled(true);

                if (resultado.getErro() == null) {
                    vaiParaListaProducao();
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

    private void vaiParaListaProducao() {
        if (controlador.getCurrentDestination().getId() == R.id.trabalhoProducaoFragment) {
            controlador.navigate(vaiParaListaTrabalhosProducao());
        }
    }

    private void preencherCampos() {
        txtNomeTrabalho.setText(trabalho.getNome());
        txtNomeProducaoTrabalho.setText(trabalho.getNomeProducao());
        txtProfissaoTrabalho.setText(trabalho.getProfissao());
        txtExperienciaTrabalho.setText(Formatador.formatarMilhar(trabalho.getExperiencia()));
        txtNivelTrabalho.setText(context.getString(R.string.stringNivelBadge, trabalho.getNivel()));
        configuraRaridadeHero(trabalho);
        recorrenciaTrabalho.setChecked(trabalho.getRecorrencia());
        autoCompleteLicenca.setText(trabalho.getTipoLicenca());
        configuraCorLicenca(trabalho.getTipoLicenca());
        estadoSelecionado = trabalho.getEstado();
        configuraEstadoVisual(estadoSelecionado);
        txtCriadoEm.setText(formatarTimestamp(trabalho.getCriadoEm()));
        txtModificadoEm.setText(formatarTimestamp(trabalho.getModificadoEm()));

        if (trabalho.getIniciadoEm() == null && trabalho.getFinalizadoEm() == null) return;
        layoutCamposDatas.setVisibility(VISIBLE);

        String dataInicio = trabalho.getIniciadoEm() == null
            ? getString(R.string.stringNaoIniciado)
            : formatarTimestamp(trabalho.getIniciadoEm());

        String dataFim = trabalho.getFinalizadoEm() == null
            ? getString(R.string.stringNaoFinalizado)
            : formatarTimestamp(trabalho.getFinalizadoEm());

        txtIniciadoEm.setText(dataInicio);
        txtFinalizadoEm.setText(dataFim);
    }

    private void configuraRaridadeHero(TrabalhoProducao trabalho) {
        String raridade = trabalho.getRaridade();
        int cor;
        if ("Melhorado".equals(raridade)) {
            cor = ContextCompat.getColor(context, R.color.cor_producao_raridade_melhorado);
        } else if ("Raro".equals(raridade)) {
            cor = ContextCompat.getColor(context, R.color.cor_producao_raridade_raro);
        } else if ("Especial".equals(raridade)) {
            cor = ContextCompat.getColor(context, R.color.cor_producao_raridade_especial);
        } else {
            cor = MaterialColors.getColor(txtNomeTrabalho, com.google.android.material.R.attr.colorOnSurface);
        }
        txtNomeTrabalho.setTextColor(cor);
    }

    private void configuraCorLicenca(String licenca) {
        if (licenca == null) return;

        int cor;
        if (licenca.equals(getString(R.string.licencaNovato))) {
            cor = R.color.cor_producao_licenca_novato;
        } else if (licenca.equals(getString(R.string.licencaAprendiz))) {
            cor = R.color.cor_producao_licenca_aprendiz;
        } else if (licenca.equals(getString(R.string.licencaIniciante))) {
            cor = R.color.cor_producao_licenca_iniciante;
        } else {
            cor = R.color.cor_producao_licenca_mestre;
        }

        int corValor = ContextCompat.getColor(context, cor);
        autoCompleteLicenca.setTextColor(corValor);
        txtLicencaHero.setText(licenca);
        txtLicencaHero.setTextColor(corValor);
    }

    private void configuraEstadoVisual(int estado) {
        int corContainer;
        int corOnContainer;
        int corTarja;
        int icone;
        int textoLabel;
        if (estado == 0) {
            corContainer = R.color.cor_estado_para_produzir_container;
            corOnContainer = R.color.cor_estado_para_produzir_on_container;
            corTarja = R.color.cor_estado_para_produzir_tarja;
            icone = R.drawable.ic_estado_para_produzir;
            textoLabel = R.string.stringFiltroParaProduzir;
        } else if (estado == 1) {
            corContainer = R.color.cor_estado_produzindo_container;
            corOnContainer = R.color.cor_estado_produzindo_on_container;
            corTarja = R.color.cor_estado_produzindo_tarja;
            icone = R.drawable.ic_estado_produzindo;
            textoLabel = R.string.stringFiltroProduzindo;
        } else {
            corContainer = R.color.cor_estado_feito_container;
            corOnContainer = R.color.cor_estado_feito_on_container;
            corTarja = R.color.cor_estado_feito_tarja;
            icone = R.drawable.ic_estado_feito;
            textoLabel = R.string.stringFiltroFeito;
        }

        int corOnContainerValor = ContextCompat.getColor(context, corOnContainer);
        heroRaridadeTarja.setBackgroundColor(ContextCompat.getColor(context, corTarja));
        heroEstadoSelo.setCardBackgroundColor(ContextCompat.getColor(context, corContainer));
        heroEstadoIcone.setImageResource(icone);
        ImageViewCompat.setImageTintList(heroEstadoIcone, ColorStateList.valueOf(corOnContainerValor));
        heroEstadoTexto.setText(textoLabel);
        heroEstadoTexto.setTextColor(corOnContainerValor);

        configuraChipEstado(chipEstadoParaProduzir, iconeChipParaProduzir, txtChipParaProduzir, estado == 0);
        configuraChipEstado(chipEstadoProduzindo, iconeChipProduzindo, txtChipProduzindo, estado == 1);
        configuraChipEstado(chipEstadoFeito, iconeChipFeito, txtChipFeito, estado == 2);
    }

    private void configuraChipEstado(MaterialCardView chip, ImageView icone, TextView texto, boolean selecionado) {
        if (!selecionado) {
            chip.setCardBackgroundColor(MaterialColors.getColor(chip, com.google.android.material.R.attr.colorSurface));
            chip.setStrokeColor(MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOutlineVariant));
            int corPadrao = MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOnSurfaceVariant);
            texto.setTextColor(corPadrao);
            ImageViewCompat.setImageTintList(icone, ColorStateList.valueOf(corPadrao));
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
        ImageViewCompat.setImageTintList(icone, ColorStateList.valueOf(corOnContainerValor));
    }

    private void selecionaEstado(int estado) {
        estadoSelecionado = estado;
        configuraEstadoVisual(estadoSelecionado);
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                producaoViewModel.setIdPersonagem(personagem.getId());
                estoqueViewModel.setIdPersonagem(personagem.getId());

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

    private void inicializarComponentes() {
        trabalhoModificado = new TrabalhoProducao();
        btnExcluir = binding.btnExcluiTrabalho;
        btnConfirmar = binding.btnConfirmarProducao;
        recorrenciaTrabalho = binding.switchRecorrenciaDetalhesTrabalho;
        autoCompleteLicenca = binding.txtAutoCompleteLicencaTrabalho;

        txtNomeTrabalho = binding.txtNomeTrabalho;
        txtNomeProducaoTrabalho = binding.txtNomeProducaoTrabalho;
        txtLicencaHero = binding.txtLicencaHero;
        txtProfissaoTrabalho = binding.txtProfissaoTrabalho;
        txtExperienciaTrabalho = binding.txtExperienciaTrabalho;
        txtNivelTrabalho = binding.txtNivelTrabalho;
        txtCriadoEm = binding.txtCriadoEmTrabalho;
        txtModificadoEm = binding.txtModificadoEmTrabalho;
        txtIniciadoEm = binding.txtIniciadoEmTrabalho;
        txtFinalizadoEm = binding.txtFinalizadoEmTrabalho;
        loadingBotaoConfirmar = binding.loadingDotsConfirmar.getRoot();
        layoutCamposDatas = binding.layoutDatas2Producao;
        layoutCamposDatas.setVisibility(GONE);

        heroRaridadeTarja = binding.heroRaridadeTarja;
        heroEstadoSelo = binding.heroEstadoSelo;
        heroEstadoIcone = binding.heroEstadoIcone;
        heroEstadoTexto = binding.heroEstadoTexto;

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

        experienciaBase = trabalho.getExperiencia();

        licencasTrabalho = getResources().getStringArray(R.array.licencas_completas);

        context = requireContext().getApplicationContext();
        controlador = Navigation.findNavController(binding.getRoot());

        trabalhoRepo = TrabalhoRepository.getInstancia(context);
        estoqueRepo = TrabalhoEstoqueRepository.getInstance(context);
        profissaoPersonagemRepo = ProfissaoPersonagemRepository.getInstance(context);
        producaoRepo = TrabalhoProducaoRepository.getInstance(context);

        ViewModelFactory viewModelFactory = new ViewModelFactory(
            context
        );

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        producaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoProducaoViewModel.class);

        estoqueViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoEstoqueViewModel.class);

        trabalhoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoViewModel.class);
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
            trabalho.getNome(),
            false
        );
    }

    private void configurarLicencas() {
        ArrayAdapter<String> adapterLicenca= new ArrayAdapter<>(
            context,
            R.layout.item_dropdrown,
            licencasTrabalho
        );

        adapterLicenca.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        autoCompleteLicenca.setAdapter(adapterLicenca);

        autoCompleteLicenca.setOnItemClickListener((parent, view, posicao, id) -> {
            String licencaSelecionada = parent.getItemAtPosition(posicao).toString();

            aplicarLicenca(licencaSelecionada);
        });
    }

    private void aplicarLicenca(String licenca) {
        String licencaIniciante = getString(R.string.licencaIniciante);

        boolean eraIniciante = comparaString(trabalho.getTipoLicenca(), licencaIniciante);
        boolean agoraIniciante = comparaString(licenca, licencaIniciante);

        int novaExperiencia = experienciaBase;

        if (!eraIniciante && agoraIniciante) {
            novaExperiencia = (int) (experienciaBase * 1.5);
        } else if (eraIniciante && !agoraIniciante) {
            novaExperiencia = (int) (experienciaBase / 1.5);
        }

        txtExperienciaTrabalho.setText(String.valueOf(novaExperiencia));
        configuraCorLicenca(licenca);
    }

    @Override
    protected FragmentDetalhesProducaoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesProducaoBinding.inflate(inflater, container, false);
    }

    private void verificaModificacaoTrabalho() {
        trabalhoModificado = trabalho;
        trabalhoModificado.setRecorrencia(recorrenciaTrabalho.isChecked());
        trabalhoModificado.setTipoLicenca(autoCompleteLicenca.getText().toString());
        trabalhoModificado.atualizarEstado(estadoSelecionado);
        trabalhoModificado.marcarModificacao();

        TrabalhoProducao producao = getTrabalhoProducao();

        producaoViewModel.modificaTrabalhoProducao(producao);
    }

    @NonNull
    private TrabalhoProducao getTrabalhoProducao() {
        TrabalhoProducao producao = new TrabalhoProducao();

        producao.setId(trabalhoModificado.getId());
        producao.setIdTrabalho(trabalhoModificado.getIdTrabalho());
        producao.setExperiencia(trabalhoModificado.getExperiencia());
        producao.setEstado(trabalhoModificado.getEstado());
        producao.setTipoLicenca(trabalhoModificado.getTipoLicenca());
        producao.setRecorrencia(trabalhoModificado.getRecorrencia());
        producao.setCriadoEm(trabalhoModificado.getCriadoEm());
        producao.setIniciadoEm(trabalhoModificado.getIniciadoEm());
        producao.setFinalizadoEm(trabalhoModificado.getFinalizadoEm());

        return producao;
    }

    @Override
    public void onResume() {
        super.onResume();

        configurarLicencas();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeObservadorTrabalho();
        removerObservadorProducao();
    }

    private void removerObservadorProducao() {
        if (producaoViewModel == null) return;
        producaoViewModel.removeObservador();
    }

    private void removeObservadorTrabalho() {
        if (trabalhoViewModel == null) return;
        trabalhoViewModel.removeObservador();
    }

}