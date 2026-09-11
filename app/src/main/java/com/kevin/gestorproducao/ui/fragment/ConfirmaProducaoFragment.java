package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_FEITO;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PARA_PRODUZIR;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PRODUZINDO;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
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
    private AutoCompleteTextView autoCompleteLicenca, autoCompleteQuantidade, autoCompleteEstado;
    private Trabalho trabalhoRecebido;
    private TrabalhoProducao producao;
    private int contador = 0;
    private PersonagemViewModel personagemViewModel;
    private TrabalhoProducaoViewModel producaoViewModel;
    private CheckBox checkRecorrencia;
    private int quantidadeSelecionada = 0;
    private String[] licencas, quantidade, estados;
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
                txtQuantidade.setError("Informe uma quantidade");
                return;
            }

            int quantidade = Integer.parseInt(valorQuantidade);

            if (quantidade <= 0) {
                txtQuantidade.setError("A quantidade deve ser maior que zero");
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
                            trabalhoRecebido.getNome() + " foi inserido com sucesso!"
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
        autoCompleteEstado = binding.txtAutoCompleteEstadoConfirmaTrabalho;
        checkRecorrencia = binding.checkBoxProducaoRecorrenteConfirmaTrabalho;
        btnConfirmar = binding.btnConfirmarProducao;
        txtQuantidade = binding.txtInputLayoutQuantidadeConfirmaTrabalho;
        loadingBotaoConfirmar = binding.loadingDotsConfirmar.getRoot();

        licencas = getResources().getStringArray(R.array.licencas_completas);
        quantidade = getResources().getStringArray(R.array.quantidade);
        estados = getResources().getStringArray(R.array.estados);

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

        binding.txtProfissaoConfirmaTrabalho.setText(trabalhoRecebido.getProfissao());
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
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_dropdrown,
            estados
        );

        autoCompleteLicenca.setAdapter(adapterLicenca);
        autoCompleteQuantidade.setAdapter(adapterQuantidade);
        autoCompleteEstado.setAdapter(adapterEstado);

        if (autoCompleteLicenca.getText().toString().isEmpty()) {
            autoCompleteLicenca.setText(licencas[3], false);
        }

        if (autoCompleteQuantidade.getText().toString().isEmpty()) {
            autoCompleteQuantidade.setText(quantidade[0], false);
        }

        if (autoCompleteEstado.getText().toString().isEmpty()) {
            autoCompleteEstado.setText(estados[0], false);
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
                trabalhoRecebido.getNome() + " foi inserido com sucesso!"
            );

        controlador.popBackStack(
            R.id.listaTrabalhosProducao,
            false
        );
    }

    private TrabalhoProducao defineNovoTrabalhoProducao() {
        TrabalhoProducao novaProducao = new TrabalhoProducao();

        int estado = recuperaPosicaoEstadoSelecionado();

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

    private int recuperaPosicaoEstadoSelecionado() {
        String estadoSelecionado = autoCompleteEstado.getText().toString();

        for (int i = 0; i < estados.length; i++) {
            if (estados[i].equals(estadoSelecionado)) {
                return i;
            }
        }

        return 0;
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