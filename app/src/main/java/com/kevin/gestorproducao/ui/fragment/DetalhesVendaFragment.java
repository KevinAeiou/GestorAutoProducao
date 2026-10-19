package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_VENDAS;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS;
import static com.kevin.gestorproducao.ui.fragment.DetalhesVendaFragmentDirections.vaiDeDetalhesTrabalhoVendidoParaTrabalhosVendidos;
import static com.kevin.gestorproducao.utilitario.Utilitario.formatarTimestamp;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentDetalhesVendaBinding;
import com.kevin.gestorproducao.model.RecursoComumAvancado;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.service.PricingService;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.RecursosProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;
import java.util.Comparator;

public class DetalhesVendaFragment
    extends BaseFragment<FragmentDetalhesVendaBinding>
{
    public static final int TAXA = 70;
    public int mediaValorRecursoUnitarioComumMercado = 0;
    public int mediaValorRecursoUnitarioCompostoMercado = 0;
    private int mediaValorRecursoUnitarioEnergiaMercado = 0;
    private int mediaValorRecursoUnitarioEtereoMercado = 0;
    private TextInputEditText edtDescricaoTrabalhoVendido,
        edtValorTrabalhoVendido, edtQuantidadeTrabalhoVendido, edtTaxaLucroTrabalhoVendido, 
        edtValorProducaoTrabalhoVendido, edtValorLucroTrabalhoVendido;
    private View layoutDatas;
    private AutoCompleteTextView autoCompleteNomeTrabalhoVendido;
    private TrabalhoVendido trabalhoRecebido;
    private Trabalho trabalhoSelecionado;
    private TrabalhosVendidosViewModel vendaViewModel;
    private RecursosProducaoViewModel recursosProducaoViewModel;
    private TrabalhoViewModel trabalhoViewModel;
    private PersonagemViewModel personagemViewModel;
    private MaterialButton btnExcluir, btnConfirmar;
    private TextView txtCriadoEm, txtModificadoEm;
    private int novaTaxa, valorProducaoComum, novoValorLucro;
    private int valorProducaoMelhorado;
    private int valorProducaoRaro;
    private int valorProducaoRecurso;
    private ArrayList<RecursoComumAvancado> recursosAvancados;
    private TextInputEditText edtOfertasTrabalhoVendido;
    private int codigoRequisicao;
    private LinearLayout loadingBotaoConfirmar, loadingBotaoExcluir;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DetalhesVendaFragmentArgs argumentos = DetalhesVendaFragmentArgs.fromBundle(getArguments());

        trabalhoRecebido = argumentos.getTrabalhoVendido();
        codigoRequisicao = argumentos.getCodigoRequisicao();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        preencheCampos();
        configuraListenerCampoTaxaLucro();
        configuraListenerCampoOfertas();
        configuraListenerCampoQuantidade();
        confguraListenerCampoValorLucro();
        configuraBotaoExcluir();
        configuraBotaoConfirmar();

        observarPersonagem();
        observarVenda();
        observarRecursoProducao();
        observarTrabalho();
    }

    private void configuraBotaoConfirmar() {
        btnConfirmar.setOnClickListener(v -> {
            iniciarLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );

            if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS) {
                if (camposValidos()) {
                    TrabalhoVendido venda = defineNovaVenda();

                    vendaViewModel.insereVenda(venda);
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );
                return;
            }

            if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_VENDAS) {
                if (camposValidos()) {
                    TrabalhoVendido venda = defineTrabalhoModificado();
                    if (camposTrabalhoModificado(venda)) {
                        vendaViewModel.modificaVenda(venda);
                        return;
                    }

                    voltaParaTrabalhosVendidos();
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );
                return;
            }

            voltaParaTrabalhosVendidos();
        });
    }

    @NonNull
    private TrabalhoVendido defineNovaVenda() {
        TrabalhoVendido novaVenda = new TrabalhoVendido();

        novaVenda.setIdTrabalho(trabalhoSelecionado.getId());
        novaVenda.setDescricao(edtDescricaoTrabalhoVendido.getText().toString().trim());
        novaVenda.marcarCriacao();
        novaVenda.setValor(obterValorNumerico(edtValorTrabalhoVendido));
        novaVenda.setQuantidade(obterValorNumerico(edtQuantidadeTrabalhoVendido));
        return novaVenda;
    }

    private void configuraBotaoExcluir() {
        if (CODIGO_REQUISICAO_ALTERA_VENDAS == codigoRequisicao) {
            btnExcluir.setVisibility(VISIBLE);
            btnExcluir.setOnClickListener(v -> {
                ConfirmacaoDialog dialog = ConfirmacaoDialog.novaInstancia(
                    getString(R.string.stringExcluirVenda),
                    getString(R.string.stringConfirmaExclusaoVenda),
                    () -> {
                        iniciarLoadingBotao(btnExcluir, loadingBotaoExcluir);
                        vendaViewModel.removeVenda(trabalhoRecebido);
                    },
                    () -> {}
                );

                dialog.show(getParentFragmentManager(), "confirmacao_exclusao");
            });
        }
    }

    private void observarTrabalho() {
        trabalhoViewModel.getTrabalhos().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getDado() != null) {
                    configuraAutoCompleteTrabalhos(resultado.getDado());

                    cofiguraCampoValorProducao();
                }
            }
        );

        trabalhoViewModel.recuperaTrabalhos();
    }

    private void observarRecursoProducao() {
        recursosProducaoViewModel.getInsercaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );

        recursosProducaoViewModel.getRecursos().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    if (resultado.getDado().isEmpty()) {
                        recursosProducaoViewModel.insereListaRecursos();
                        return;
                    }
                    recursosAvancados = resultado.getDado();
                    recalculaValoresProducao();
                }
            }
        );
    }

    private void recalculaValoresProducao() {
        if (trabalhoSelecionado == null || recursosAvancados == null) return;

        PricingService.ValoresMercadoRecursos valoresMercado = PricingService.mapeiaValoresMercado(
            trabalhoSelecionado,
            recursosAvancados,
            getContext()
        );
        mediaValorRecursoUnitarioComumMercado = valoresMercado.mediaComum;
        mediaValorRecursoUnitarioCompostoMercado = valoresMercado.mediaComposto;
        mediaValorRecursoUnitarioEnergiaMercado = valoresMercado.mediaEnergia;
        mediaValorRecursoUnitarioEtereoMercado = valoresMercado.mediaEtereo;

        if (trabalhoSelecionado.ehProducaoDeRecursos()) {
            valorProducaoRecurso = PricingService.calculaValorProducaoRecurso(
                PricingService.quantidadeProduzidaPorTrabalho(trabalhoSelecionado),
                trabalhoSelecionado.ehLicencaProducaoAprendiz(),
                mediaValorRecursoUnitarioComumMercado,
                PricingService.calculaCustoMateriaisRecurso(
                    trabalhoSelecionado,
                    mediaValorRecursoUnitarioComumMercado,
                    mediaValorRecursoUnitarioCompostoMercado
                ),
                quantidadeVendida()
            );
        } else if (trabalhoSelecionado.ehComum()) {
            calculaValorProducaoComum();
        } else if (trabalhoSelecionado.ehMelhorado()) {
            calculaValorProducaoComum();
            calculcaValorProducaoMelhorado();
        } else if (trabalhoSelecionado.ehRaro()) {
            calculaValorProducaoComum();
            calculcaValorProducaoMelhorado();
            calculcaValorProducaoRaro();
        }

        int valorProducao = valorProducaoAtual();
        edtValorProducaoTrabalhoVendido.setText(String.valueOf(valorProducao));
        atualizaValorLucro(valorProducao);
    }

    private int quantidadeVendida() {
        try {
            String texto = edtQuantidadeTrabalhoVendido.getText().toString().replaceAll("[^0-9]", "");
            return Math.max(Integer.parseInt(texto), 1);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void configuraCampoQuantidade(boolean redefinirValor) {
        boolean ehRecurso = trabalhoSelecionado != null && trabalhoSelecionado.ehProducaoDeRecursos();
        edtQuantidadeTrabalhoVendido.setEnabled(ehRecurso);
        if (!ehRecurso && redefinirValor) edtQuantidadeTrabalhoVendido.setText("1");
    }

    private void configuraListenerCampoQuantidade() {
        edtQuantidadeTrabalhoVendido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!edtQuantidadeTrabalhoVendido.isFocused()) return;
                if (trabalhoSelecionado == null || !trabalhoSelecionado.ehProducaoDeRecursos()) return;
                recalculaValoresProducao();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void observarVenda() {
        vendaViewModel.getInsercaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {

                    mostraMensagemAncorada(getString(R.string.stringVendaInseridaComSucesso));
                    voltaParaTrabalhosVendidos();
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                mostraMensagemAncorada(getString(R.string.stringErroAoInserirVendaValor, resultado.getErro()));
            }
        );

        vendaViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    mostraMensagemAncorada(getString(R.string.stringVendaModificadaComSucesso));
                    voltaParaTrabalhosVendidos();
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                mostraMensagemAncorada(getString(R.string.stringErroAoModificarTrabalhoValor, resultado.getErro()));
            }
        );

        vendaViewModel.getRemocaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    voltaParaTrabalhosVendidos();
                    mostraMensagemAncorada(getString(R.string.stringVendaRemovidaComSucesso));
                    return;
                }

                pararLoadingBotao(btnExcluir, loadingBotaoExcluir);

                mostraMensagemAncorada(getString(R.string.stringErroAoExcluirVendaValor, resultado.getErro()));
            }
        );
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                vendaViewModel.setIdPersonagem(resultado.getId());
                recursosProducaoViewModel.setIdPersonagem(resultado.getId());
            }
        );
    }

    private boolean camposValidos() {
        String quantidade = edtQuantidadeTrabalhoVendido.getText().toString().trim();
        String valor = edtValorTrabalhoVendido.getText().toString().trim();

        if (quantidade.trim().isEmpty()) {
            edtQuantidadeTrabalhoVendido.setText("1");
        }

        if (valor.trim().isEmpty()) {
            edtValorTrabalhoVendido.setText("0");
        }

        return verificaCampoInteiro(edtValorTrabalhoVendido, binding.txtInputValorTrabalhoVendido) &
            verificaCampoInteiro(edtQuantidadeTrabalhoVendido, binding.txtInputQuantidadeTrabalhoVendido);
    }

    private boolean verificaCampoInteiro(TextInputEditText campo, TextInputLayout label) {
        label.setErrorEnabled(false);

        try {
            int valorInteiro = obterValorNumerico(campo);
            if (valorInteiro < 0) throw new NumberFormatException();
            return true;

        } catch (NumberFormatException e) {
            label.setError(getString(R.string.strngValorInvalido));
            return false;
        }
    }


    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        String titulo = codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS ?
            getString(R.string.stringNovaVenda) :
            trabalhoRecebido.getNome();

        return new ComponentesVisuais(
            true,
            false,
            false,
            false,
            false,
            false,
            titulo,
            false
        );
    }

    private void confguraListenerCampoValorLucro() {
        edtValorLucroTrabalhoVendido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (edtValorLucroTrabalhoVendido.isFocused()) {
                    if (charSequence == null) return;
                    String stringValorLucro = charSequence.toString();
                    stringValorLucro = stringValorLucro.replaceAll("[^0-9-]", "");
                    if (stringValorLucro.isEmpty()) return;
                    novoValorLucro = Integer.parseInt(stringValorLucro);
                    atualizaTaxaLucro();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) edtValorLucroTrabalhoVendido.setText("0");
            }
        });
    }

    private void atualizaTaxaLucro() {
        int valorProducao = valorProducaoAtual();
        if (valorProducao == 0) return;
        calculaTaxa(valorProducao);
    }

    private int quantidadeOfertas() {
        try {
            String texto = edtOfertasTrabalhoVendido.getText().toString().replaceAll("[^0-9]", "");
            return Math.max(Integer.parseInt(texto), 1);
        } catch (NumberFormatException e) {
            return PricingService.OFERTAS_PADRAO;
        }
    }

    private int valorProducaoAtual() {
        if (trabalhoSelecionado == null) return 0;
        if (trabalhoSelecionado.ehProducaoDeRecursos()) return valorProducaoRecurso;
        if (trabalhoSelecionado.ehComum()) return valorProducaoComum;
        if (trabalhoSelecionado.ehMelhorado()) return valorProducaoMelhorado;
        if (trabalhoSelecionado.ehRaro()) return valorProducaoRaro;
        return 0;
    }

    private void configuraListenerCampoOfertas() {
        edtOfertasTrabalhoVendido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!edtOfertasTrabalhoVendido.isFocused()) return;
                int valorProducao = valorProducaoAtual();
                if (valorProducao == 0) return;
                atualizaValorLucro(valorProducao);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void calculaTaxa(int valorProducao) {
        int porcentual = PricingService.calculaTaxa(novoValorLucro, valorProducao, quantidadeOfertas());
        edtTaxaLucroTrabalhoVendido.setText(String.valueOf(porcentual));
    }

    private void configuraListenerCampoTaxaLucro() {
        edtTaxaLucroTrabalhoVendido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (edtTaxaLucroTrabalhoVendido.isFocused()) {
                    if (charSequence == null) return;
                    String strValorTaxa = charSequence.toString();
                    strValorTaxa = strValorTaxa.replaceAll("[^0-9-]", "");
                    if (strValorTaxa.isEmpty() || strValorTaxa.equals("-")) return;
                    novaTaxa = Integer.parseInt(strValorTaxa);
                    if (trabalhoSelecionado == null) return;
                    int valorProducao = valorProducaoAtual();
                    atualizaValorLucro(valorProducao);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String stringTaxa = editable.toString();
                if (stringTaxa.isEmpty()) edtTaxaLucroTrabalhoVendido.setText("0");
            }
        });
    }

    private void cofiguraCampoValorProducao() {
        configuraCampoQuantidade(false);
    }

    private void calculcaValorProducaoRaro() {
        valorProducaoRaro = PricingService.calculaValorProducaoRaro(
            trabalhoSelecionado,
            getContext(),
            valorProducaoMelhorado,
            mediaValorRecursoUnitarioEtereoMercado
        );
    }

    private void calculcaValorProducaoMelhorado() {
        valorProducaoMelhorado = PricingService.calculaValorProducaoMelhorado(
            trabalhoSelecionado,
            getContext(),
            valorProducaoComum,
            mediaValorRecursoUnitarioEnergiaMercado
        );
    }

    private void calculaValorProducaoComum() {
        valorProducaoComum = PricingService.calculaValorProducaoComum(
            trabalhoSelecionado,
            getContext(),
            mediaValorRecursoUnitarioComumMercado,
            mediaValorRecursoUnitarioCompostoMercado
        );
    }

    private void atualizaValorLucro(int valorProducao) {
        int valorTotalLucro = PricingService.calculaValorLucro(novaTaxa, valorProducao, quantidadeOfertas());
        edtValorLucroTrabalhoVendido.setText(String.valueOf(valorTotalLucro));
    }

    private TrabalhoVendido defineTrabalhoModificado() {
        TrabalhoVendido trabalho = new TrabalhoVendido();

        trabalho.setId(trabalhoRecebido.getId());
        trabalho.setIdTrabalho(trabalhoSelecionado.getId());
        trabalho.setDescricao(edtDescricaoTrabalhoVendido.getText().toString().trim());
        trabalho.setCriadoEm(trabalhoRecebido.getCriadoEm());
        trabalho.marcarModificacao();
        trabalho.setQuantidade(obterValorNumerico(edtQuantidadeTrabalhoVendido));
        trabalho.setValor(obterValorNumerico(edtValorTrabalhoVendido));

        return trabalho;
    }

    private void voltaParaTrabalhosVendidos() {
        NavController controlador = Navigation.findNavController(binding.getRoot());
        controlador.navigate(vaiDeDetalhesTrabalhoVendidoParaTrabalhosVendidos());
    }

    private boolean camposTrabalhoModificado(TrabalhoVendido trabalhoVendido) {
        return !trabalhoRecebido.equals(trabalhoVendido);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void preencheCampos() {
        edtDescricaoTrabalhoVendido.setText(trabalhoRecebido.getDescricao());
        edtValorTrabalhoVendido.setText(String.valueOf(trabalhoRecebido.getValor()));
        edtQuantidadeTrabalhoVendido.setText(String.valueOf(trabalhoRecebido.getQuantidade()));
        edtTaxaLucroTrabalhoVendido.setText(String.valueOf(novaTaxa));
        edtValorProducaoTrabalhoVendido.setEnabled(false);
        edtValorProducaoTrabalhoVendido.setText(getString(R.string.stringValorProducaoValor, 0));

        if (trabalhoRecebido.getCriadoEm() == null) {
            layoutDatas.setVisibility(GONE);
            return;
        }

        txtCriadoEm.setText(formatarTimestamp(trabalhoRecebido.getCriadoEm()));
        txtModificadoEm.setText(formatarTimestamp(trabalhoRecebido.getModificadoEm()));
    }

    private void inicializaComponentes() {
        novaTaxa = TAXA;
        valorProducaoComum = 0;
        novoValorLucro = 0;
        valorProducaoMelhorado = 0;
        valorProducaoRaro = 0;
        edtDescricaoTrabalhoVendido = binding.edtInputDescricaoTrabalhoVendido;
        edtValorTrabalhoVendido = binding.edtInputValorTrabalhoVendido;
        edtQuantidadeTrabalhoVendido = binding.edtInputQuantidadeTrabalhoVendido;
        autoCompleteNomeTrabalhoVendido = binding.autoCompleteNomeTrabalhoVendido;
        edtTaxaLucroTrabalhoVendido = binding.edtInputTaxaLucroTrabalhoVendido;
        edtValorProducaoTrabalhoVendido = binding.edtInputValorProducaoTrabalhoVendido;
        edtValorLucroTrabalhoVendido = binding.edtInputValorLucroTrabalhoVendido;
        edtOfertasTrabalhoVendido = binding.edtInputOfertasTrabalhoVendido;
        edtOfertasTrabalhoVendido.setText(String.valueOf(PricingService.OFERTAS_PADRAO));
        btnExcluir = binding.btnExcluiVenda;
        btnConfirmar = binding.btnConfirmarVenda;
        loadingBotaoConfirmar = binding.loadingDotsConfirmar.getRoot();
        loadingBotaoExcluir = binding.loadingDotsExcluir.getRoot();
        txtCriadoEm = binding.txtCriadoEmTrabalho;
        txtModificadoEm = binding.txtModificadoEmTrabalho;
        layoutDatas = binding.layoutDatasVenda;

        btnExcluir.setVisibility(GONE);

        configurarMascaraMilhar(edtValorTrabalhoVendido);
        configurarMascaraMilhar(edtQuantidadeTrabalhoVendido);
        configurarMascaraMilhar(edtTaxaLucroTrabalhoVendido);
        configurarMascaraMilhar(edtValorProducaoTrabalhoVendido);
        configurarMascaraMilhar(edtValorLucroTrabalhoVendido);

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        trabalhoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoViewModel.class);

        vendaViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhosVendidosViewModel.class);

        recursosProducaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(RecursosProducaoViewModel.class);

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);
    }

    private void configuraAutoCompleteTrabalhos(ArrayList<Trabalho> trabalhos) {
        trabalhos.sort(
            Comparator.comparing(
                Trabalho::getNome,
                String.CASE_INSENSITIVE_ORDER
            )
        );
        ArrayAdapter<Trabalho> adapterEstado = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_dropdrown,
            trabalhos
        );

        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        autoCompleteNomeTrabalhoVendido.setAdapter(adapterEstado);
        autoCompleteNomeTrabalhoVendido.setOnItemClickListener((
            parent,
            view,
            position,
            id
        ) -> {
            trabalhoSelecionado = adapterEstado.getItem(position);
            configuraCampoQuantidade(true);
            recalculaValoresProducao();
        });

        selecionarTrabalhoRecebido(trabalhos);
    }

    private void selecionarTrabalhoRecebido(ArrayList<Trabalho> trabalhos) {
        if (trabalhoRecebido == null) return;

        for (Trabalho trabalho : trabalhos) {

            if (trabalho.getId().equals(
                trabalhoRecebido.getIdTrabalho()
            )) {
                trabalhoSelecionado = trabalho;

                autoCompleteNomeTrabalhoVendido.setText(
                    trabalho.getNome(),
                    false
                );

                break;
            }
        }
    }

    @Override
    protected FragmentDetalhesVendaBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesVendaBinding.inflate(inflater, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        trabalhoRecebido = null;

        removeObservadorRecurso();
        removeObservadorVenda();
        removeObservadorTrabalho();
    }

    private void removeObservadorTrabalho() {
        if (trabalhoViewModel == null) return;
        trabalhoViewModel.removeObservador();
    }

    private void removeObservadorVenda() {
        if (vendaViewModel == null) return;
        vendaViewModel.removeObservador();
    }

    private void removeObservadorRecurso() {
        if (recursosProducaoViewModel == null) return;
        recursosProducaoViewModel.removeObservador();
    }
}