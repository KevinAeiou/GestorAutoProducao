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
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.RecursosProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DetalhesVendaFragment
    extends BaseFragment<FragmentDetalhesVendaBinding>
{
    public static final int TAXA = 70;
    public int mediaValorRecursoUnitarioComumMercado = 0;
    public int mediaValorRecursoUnitarioCompostoMercado = 0;
    private int mediaValorRecursoUnitarioEnergiaMercado = 0;
    private int mediaValorRecursoUnitarioEtereoMercado = 0;
    private static final Double FATOR_PERCENTUAL = 0.01;
    public static final double FATOR_PERCENTUAL_MERCADO = 1.1;
    private static final int MEDIA_VALOR_LICENCA_INICIANTE = 1000;
    private TextInputEditText edtDescricaoTrabalhoVendido,
        edtValorTrabalhoVendido, edtQuantidadeTrabalhoVendido, edtTaxaLucroTrabalhoVendido, 
        edtValorProducaoTrabalhoVendido, edtValorLucroTrabalhoVendido;
    private LinearLayout layoutDatas;
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
    private int codigoRequisicao;
    private LinearLayout loadingBotaoConfirmar;

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
                iniciarLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                ConfirmacaoDialog dialog = ConfirmacaoDialog.novaInstancia(
                    "Excluir venda",
                    "Tem certeza que deseja excluir esta venda?",
                    () -> vendaViewModel.removeVenda(trabalhoRecebido),
                    () -> pararLoadingBotao(btnConfirmar, loadingBotaoConfirmar)
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
                if (resultado.getErro() != null) mostraMensagemAncorada("Erro: " + resultado.getErro());
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
                    ArrayList<RecursoComumAvancado> recursosAvancados = resultado.getDado();
                    if (trabalhoSelecionado == null) return;
                    if (trabalhoSelecionado.ehAmuletos(getContext()) || trabalhoSelecionado.ehAneis(getContext()) || trabalhoSelecionado.ehCapotes(getContext()) || trabalhoSelecionado.ehBraceletes(getContext())) {
                        for (RecursoComumAvancado recursoAvancado : recursosAvancados) {
                            switch (recursoAvancado.getId()) {
                                case "e580e375-abc1-44f8-b332-774b7f1a490c":
                                    mediaValorRecursoUnitarioComumMercado = recursoAvancado.getValor();
                                    continue;
                                case "94b66657-c7c6-41c0-b6f0-922614182549":
                                    mediaValorRecursoUnitarioCompostoMercado = recursoAvancado.getValor();
                                    continue;
                                case "c9751ecc-f528-4a80-88c3-d2a8af2804fa":
                                    mediaValorRecursoUnitarioEnergiaMercado = recursoAvancado.getValor();
                                    continue;
                                case "7c27a18c-fc60-484c-9545-99030a623129":
                                    mediaValorRecursoUnitarioEtereoMercado = recursoAvancado.getValor();
                                    break;
                            }
                        }
                    }
                    if (trabalhoSelecionado.ehLongoAlcance(getContext()) || trabalhoSelecionado.ehCorpoCorpo(getContext())) {
                        for (RecursoComumAvancado recursoAvancado : recursosAvancados) {
                            switch (recursoAvancado.getId()) {
                                case "b7f69638-c9b7-4c69-865e-cbacef5c45b1":
                                    mediaValorRecursoUnitarioComumMercado = recursoAvancado.getValor();
                                    continue;
                                case "3a085587-5093-471d-9187-27b2370e4b38":
                                    mediaValorRecursoUnitarioCompostoMercado = recursoAvancado.getValor();
                                    continue;
                                case "259d5a95-72fd-4b36-b17f-c7b6a2a6897f":
                                    mediaValorRecursoUnitarioEnergiaMercado = recursoAvancado.getValor();
                                    continue;
                                case "2d8c434a-50eb-4269-bc70-725ded6bc7e9":
                                    mediaValorRecursoUnitarioEtereoMercado = recursoAvancado.getValor();
                                    break;
                            }
                        }
                    }
                    if (trabalhoSelecionado.ehArmaduraPesada(getContext()) || trabalhoSelecionado.ehArmaduraLeve(getContext()) || trabalhoSelecionado.ehArmaduraTecido(getContext())) {
                        for (RecursoComumAvancado recursoAvancado : recursosAvancados) {
                            switch (recursoAvancado.getId()) {
                                case "6ac21d44-1e8d-4bf8-bd62-53248e568417":
                                    mediaValorRecursoUnitarioComumMercado = recursoAvancado.getValor();
                                    continue;
                                case "6250e394-4a82-4ccb-b697-c788b9094c41":
                                    mediaValorRecursoUnitarioCompostoMercado = recursoAvancado.getValor();
                                    continue;
                                case "b2f158f9-5b52-444a-a27b-7ac1284063c6":
                                    mediaValorRecursoUnitarioEnergiaMercado = recursoAvancado.getValor();
                                    continue;
                                case "e12c1346-9343-414e-a0b5-631e494423b2":
                                    mediaValorRecursoUnitarioEtereoMercado = recursoAvancado.getValor();
                                    break;
                            }
                        }
                    }
                    if (trabalhoSelecionado.ehComum()) {
                        calculaValorProducaoComum();
                        edtValorProducaoTrabalhoVendido.setText(String.valueOf(valorProducaoComum));
                        atualizaValorLucro(valorProducaoComum);
                        return;
                    }
                    if (trabalhoSelecionado.ehMelhorado()) {
                        calculaValorProducaoComum();
                        calculcaValorProducaoMelhorado();
                        edtValorProducaoTrabalhoVendido.setText(String.valueOf(valorProducaoMelhorado));
                        atualizaValorLucro(valorProducaoMelhorado);
                        return;
                    }
                    if (trabalhoSelecionado.ehRaro()) {
                        calculaValorProducaoComum();
                        calculcaValorProducaoMelhorado();
                        calculcaValorProducaoRaro();
                        edtValorProducaoTrabalhoVendido.setText(String.valueOf(valorProducaoRaro));
                        atualizaValorLucro(valorProducaoRaro);
                    }
                }
            }
        );
    }

    private void observarVenda() {
        vendaViewModel.getInsercaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {

                    mostraMensagemAncorada("Venda inserida com sucesso!");
                    voltaParaTrabalhosVendidos();
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                mostraMensagemAncorada("Erro ao inserir venda: " + resultado.getErro());
            }
        );

        vendaViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    mostraMensagemAncorada("Venda modificada com sucesso!");
                    voltaParaTrabalhosVendidos();
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                mostraMensagemAncorada("Erro ao modificar trabalho: " + resultado.getErro());
            }
        );

        vendaViewModel.getRemocaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    voltaParaTrabalhosVendidos();
                    mostraMensagemAncorada("Venda removida com sucesso!");
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                mostraMensagemAncorada("Erro ao excluir venda: " + resultado.getErro());
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
        String descricao = edtDescricaoTrabalhoVendido.getText().toString().trim();
        String quantidade = edtQuantidadeTrabalhoVendido.getText().toString().trim();
        String valor = edtValorTrabalhoVendido.getText().toString().trim();

        if (quantidade.trim().isEmpty()) {
            edtQuantidadeTrabalhoVendido.setText("1");
        }

        if (valor.trim().isEmpty()) {
            edtValorTrabalhoVendido.setText("0");
        }

        return verificaCampoDescricao(descricao) &
            verificaCampoInteiro(edtValorTrabalhoVendido, binding.txtInputValorTrabalhoVendido) &
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


    private boolean verificaCampoDescricao(String descricao) {
        obterValorNumerico(edtValorTrabalhoVendido);
        TextInputLayout txtDescricaoTrabalhoVendido = binding.txtInputDescricaoTrabalhoVendido;
        txtDescricaoTrabalhoVendido.setErrorEnabled(false);
        if (descricao.trim().isEmpty()) {
            txtDescricaoTrabalhoVendido.setError(getString(R.string.stringCampoRequerido));
            return false;
        }
        return true;
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
        if (trabalhoSelecionado == null) return;
        if (trabalhoSelecionado.ehComum()) {
            if (valorProducaoComum == 0) return;
            calculaTaxa(valorProducaoComum);
        }
        if (trabalhoSelecionado.ehMelhorado()) {
            if (valorProducaoMelhorado == 0) return;
            calculaTaxa(valorProducaoMelhorado);
        }
        if (trabalhoSelecionado.ehRaro()) {
            if (valorProducaoRaro == 0) return;
            calculaTaxa(valorProducaoRaro);
        }
    }

    private void calculaTaxa(int valorProducao) {
        int valorLucroSemTaxaMercado = (int) Math.round(novoValorLucro / FATOR_PERCENTUAL_MERCADO);
        double taxa = (double) valorLucroSemTaxaMercado / valorProducao;
        taxa = taxa >= 1 ? (taxa - 1) * 100 : (1 - taxa) * -100;
        int porcentual = (int) Math.round(taxa);
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
                    int valorProducao = 0;
                    if (trabalhoSelecionado == null) return;
                    if (trabalhoSelecionado.ehComum()) {
                        valorProducao = valorProducaoComum;
                    }
                    else if (trabalhoSelecionado.ehMelhorado()) {
                        valorProducao = valorProducaoMelhorado;
                    }
                    else if (trabalhoSelecionado.ehRaro()) {
                        valorProducao = valorProducaoRaro;
                    }
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
        if (trabalhoSelecionado == null) {
            return;
        }
        if (trabalhoSelecionado.ehProducaoDeRecursos()) {
            edtTaxaLucroTrabalhoVendido.setEnabled(false);
            edtValorLucroTrabalhoVendido.setEnabled(false);
            edtValorLucroTrabalhoVendido.setText(R.string.stringIndefinido);
            edtValorProducaoTrabalhoVendido.setText(R.string.stringIndefinido);
        }
    }

    private void calculcaValorProducaoRaro() {
        int quantidadeRecursoEtereo = trabalhoSelecionado.recuperaQuantidadeMaximaRecursosEtereo(getContext());
        valorProducaoRaro = valorProducaoMelhorado + (mediaValorRecursoUnitarioEtereoMercado * quantidadeRecursoEtereo) + MEDIA_VALOR_LICENCA_INICIANTE;
    }

    private void calculcaValorProducaoMelhorado() {
        List<String> listaTrabalhosNecessarios = trabalhoSelecionado.getListaTrabalhosNecessarios();
        int quantidadeTrabalhosComunsNecessarios = listaTrabalhosNecessarios.size();
        int quantidadeRecursoEnerga = trabalhoSelecionado.recuperaQuantidadeMaximaRecursosEnergia(getContext());
        valorProducaoMelhorado = (valorProducaoComum * quantidadeTrabalhosComunsNecessarios) + (mediaValorRecursoUnitarioEnergiaMercado * quantidadeRecursoEnerga) + MEDIA_VALOR_LICENCA_INICIANTE;
    }

    private void calculaValorProducaoComum() {
        int quantidadeMaximaRecursos = trabalhoSelecionado.recuperaQuantidadeMaximaRecursos(getContext());
        int quantidadeTotalRecursos = quantidadeMaximaRecursos * 3 + 3;
        int quantidadeMaximaRecursosProduzido = trabalhoSelecionado.getNivel() > 14 ? 24 : 18;
        int quantidadeRecursosNecessarios = trabalhoSelecionado.getNivel() > 14 ? 8 : 4;
        int valorRecursoUnitario = (mediaValorRecursoUnitarioCompostoMercado * quantidadeRecursosNecessarios) / quantidadeMaximaRecursosProduzido;
        int valorLicencaComum = 80;
        int valorLicencaAprendiz = mediaValorRecursoUnitarioComumMercado * 4 / 2 + 80;
        double resultado = (double) quantidadeTotalRecursos / quantidadeMaximaRecursosProduzido;
        int quantidadeLicencaAprendizUtilizada = (int) Math.max(Math.round(resultado), 1);
        int valorLicencas = valorLicencaComum + (valorLicencaAprendiz * quantidadeLicencaAprendizUtilizada);
        int valorRecursoTotal = quantidadeTotalRecursos * valorRecursoUnitario;
        valorProducaoComum = valorRecursoTotal + valorLicencas;
    }

    private void atualizaValorLucro(int valorProducao) {
        double v = novaTaxa * FATOR_PERCENTUAL;
        double porcentagem = v >= 0 ? v + 1 : v + 1.0;
        int valorProducaoTaxa = (int) (valorProducao * porcentagem);
        int valorTotalLucro = (int) (valorProducaoTaxa * FATOR_PERCENTUAL_MERCADO);
        valorTotalLucro = Math.max(valorTotalLucro, 0);
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
        btnExcluir = binding.btnExcluiVenda;
        btnConfirmar = binding.btnConfirmarVenda;
        loadingBotaoConfirmar = binding.loadingDotsConfirmar.getRoot();
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
        recursosProducaoViewModel.removeOuvinte();
    }
}