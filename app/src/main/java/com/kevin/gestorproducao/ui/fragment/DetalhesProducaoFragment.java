package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.fragment.DetalhesProducaoFragmentDirections.vaiParaListaTrabalhosProducao;
import static com.kevin.gestorproducao.utilitario.Utilitario.comparaString;
import static com.kevin.gestorproducao.utilitario.Utilitario.formatarTimestamp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
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
    private CheckBox recorrenciaTrabalho;
    private TextView txtNomeTrabalho, txtNomeProducaoTrabalho, txtProfissaoTrabalho,
        txtExperienciaTrabalho, txtNivelTrabalho, txtRaridadeTrabalho,
        txtCriadoEm, txtModificadoEm, txtFinalizadoEm, txtIniciadoEm;
    private AutoCompleteTextView autoCompleteLicenca, autoCompleteEstado;
    private MaterialButton btnExcluir, btnConfirmar;
    private String[] estadosTrabalho, licencasTrabalho;
    private ArrayAdapter<String> adapterEstado;
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
        txtNivelTrabalho.setText(String.valueOf(trabalho.getNivel()));
        txtRaridadeTrabalho.setText(trabalho.getRaridade());
        recorrenciaTrabalho.setChecked(trabalho.getRecorrencia());
        autoCompleteLicenca.setText(trabalho.getTipoLicenca());
        autoCompleteEstado.setText(estadosTrabalho[trabalho.getEstado()]);
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
        recorrenciaTrabalho = binding.checkBoxRecorrenciaDetalhesTrabalho;
        autoCompleteLicenca = binding.txtAutoCompleteLicencaTrabalho;
        autoCompleteEstado = binding.txtAutoCompleteEstadoTrabalho;

        txtNomeTrabalho = binding.txtNomeTrabalho;
        txtNomeProducaoTrabalho = binding.txtNomeProducaoTrabalho;
        txtRaridadeTrabalho = binding.txtRaridadeTrabalho;
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

        experienciaBase = trabalho.getExperiencia();

        estadosTrabalho = getResources().getStringArray(R.array.estados);
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

    private void configurarEstados() {
        adapterEstado= new ArrayAdapter<>(
            context,
            R.layout.item_dropdrown,
            estadosTrabalho
        );

        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        autoCompleteEstado.setAdapter(adapterEstado);
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
    }

    @Override
    protected FragmentDetalhesProducaoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesProducaoBinding.inflate(inflater, container, false);
    }

    private void verificaModificacaoTrabalho() {
        int estado = adapterEstado.getPosition(autoCompleteEstado.getText().toString());

        trabalhoModificado = trabalho;
        trabalhoModificado.setRecorrencia(recorrenciaTrabalho.isChecked());
        trabalhoModificado.setTipoLicenca(autoCompleteLicenca.getText().toString());
        trabalhoModificado.atualizarEstado(estado);
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
        configurarEstados();
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