package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_VENDAS;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS;
import static com.kevin.ceep.ui.fragment.DetalhesTrabalhoVendidoFragmentDirections.*;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentDetalhesTrabalhoVendidoBinding;
import com.kevin.ceep.model.RecursoAvancado;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.ui.viewModel.RecursosProducaoViewModel;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.ceep.ui.viewModel.factory.RecursosProducaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhosVendidosViewModelFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class DetalhesTrabalhoVendidoFragment
        extends BaseFragment<FragmentDetalhesTrabalhoVendidoBinding>
        implements MenuProvider{
    public static final int TAXA = 70;
    public int mediaValorRecursoUnitarioComumMercado = 0;
    public int mediaValorRecursoUnitarioCompostoMercado = 0;
    private int mediaValorRecursoUnitarioEnergiaMercado = 0;
    private int mediaValorRecursoUnitarioEtereoMercado = 0;
    private static final Double FATOR_PERCENTUAL = 0.01;
    public static final double FATOR_PERCENTUAL_MERCADO = 1.1;
    private static final int MEDIA_VALOR_LICENCA_INICIANTE = 1000;
    private TextInputEditText edtDescricaoTrabalhoVendido, edtDataTrabalhoVendido, edtValorTrabalhoVendido, edtQuantidadeTrabalhoVendido, edtTaxaLucroTrabalhoVendido, edtValorProducaoTrabalhoVendido, edtValorLucroTrabalhoVendido;
    private AutoCompleteTextView autoCompleteNomeTrabalhoVendido;
    private TrabalhoVendido trabalhoRecebido;
    private String idPersonagem;
    private Trabalho trabalhoSelecionado;
    private TrabalhosVendidosViewModel trabalhosVendidosViewModel;
    private RecursosProducaoViewModel recursosProducaoViewModel;
    private TrabalhoViewModel trabalhoViewModel;
    private int novaTaxa, valorProducaoComum, novoValorLucro;
    private int valorProducaoMelhorado;
    private int valorProducaoRaro;
    private int codigoRequisicao;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        configuraComponentesVisuais();
        recebeDados();
        inicializaComponentes();
        preencheCampos();
        configuraListenerCampoTaxaLucro();
        confguraListenerCampoValorLucro();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma) {
            if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS) {
                if (camposValidos()) {
                    Log.d("vendas", "Campos estão válidos!");
                    TrabalhoVendido novaVenda = new TrabalhoVendido();
                    novaVenda.setIdPersonagem(idPersonagem);
                    novaVenda.setIdTrabalho(trabalhoSelecionado.getId());
                    novaVenda.setDescricao(edtDescricaoTrabalhoVendido.getText().toString().trim());
                    novaVenda.setDataVenda(edtDataTrabalhoVendido.getText().toString().trim());
                    novaVenda.setValor(Integer.parseInt(edtValorTrabalhoVendido.getText().toString().trim()));
                    novaVenda.setQuantidade(Integer.parseInt(edtQuantidadeTrabalhoVendido.getText().toString().trim()));
                    Log.d("vendas", "Nova venda: "+ novaVenda);
                    insereVenda(novaVenda);
                }
                return true;
            }
            if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_VENDAS) {
                if (camposValidos()) {
                    TrabalhoVendido trabalhoVendido = defineTrabalhoModificado();
                    if (camposTrabalhoModificado(trabalhoVendido)) {
                        MaterialAlertDialogBuilder dialogoDeAlerta = new MaterialAlertDialogBuilder(requireContext());
                        dialogoDeAlerta.setMessage("Deseja confirmar alterações?");
                        dialogoDeAlerta.setNegativeButton("Não", ((dialogInterface, i) -> voltaParaTrabalhosVendidos()));
                        dialogoDeAlerta.setPositiveButton("Sim", (dialogInterface, i) -> modificaTrabalho(trabalhoVendido));
                        dialogoDeAlerta.show();
                        return true;
                    }
                    voltaParaTrabalhosVendidos();
                }
                return true;
            }
            voltaParaTrabalhosVendidos();
        }
        return false;
    }

    private void insereVenda(TrabalhoVendido novaVenda) {
        trabalhosVendidosViewModel.getInsercaoResultado().observe(
                getViewLifecycleOwner(),
                resultadoInsereVenda
        ->{
            if (resultadoInsereVenda.getErro() == null) {
                voltaParaTrabalhosVendidos();
                mostraMensagem("Venda inserida com sucesso!");
                return;
            }
            mostraMensagem("Erro ao inserir venda: " + resultadoInsereVenda.getErro());
            voltaParaTrabalhosVendidos();
        });
        trabalhosVendidosViewModel.insereVenda(novaVenda);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean camposValidos() {
        String descricao = edtDescricaoTrabalhoVendido.getText().toString().trim();
        String data = edtDataTrabalhoVendido.getText().toString().trim();
        String quantidade = edtQuantidadeTrabalhoVendido.getText().toString().trim();
        String valor = edtValorTrabalhoVendido.getText().toString().trim();
        Log.d("vendas", "Campo descrição: " + descricao);
        Log.d("vendas", "Campo data: " + data);
        Log.d("vendas", "Campo quantidade: " + quantidade);
        Log.d("vendas", "Campo valor: " + valor);
        if (quantidade.trim().isEmpty()) {
            edtQuantidadeTrabalhoVendido.setText("1");
        }
        if (valor.trim().isEmpty()) {
            edtValorTrabalhoVendido.setText("0");
        }
        if (data.trim().isEmpty()) {
            edtDataTrabalhoVendido.setText(recuperaDataAtual());
        }
        return verificaCampoDescricao(descricao) & verificaCampoInteiro(valor, binding.txtInputValorTrabalhoVendido) & verificaCampoInteiro(quantidade, binding.txtInputQuantidadeTrabalhoVendido);
    }

    private boolean verificaCampoInteiro(String valor, TextInputLayout txtInputTrabalhoVendido) {
        txtInputTrabalhoVendido.setErrorEnabled(false);
        try {
            int valorInteiro = Integer.parseInt(valor);
            if (valorInteiro < 0) throw new NumberFormatException();
            return true;
        } catch (NumberFormatException e) {
            txtInputTrabalhoVendido.setError(getString(R.string.strngValorInvalido));
            txtInputTrabalhoVendido.setErrorTextColor(ColorStateList.valueOf(getContext().getColor(R.color.cor_background_bordo)));
            return false;
        }
    }


    private boolean verificaCampoDescricao(String descricao) {
        TextInputLayout txtDescricaoTrabalhoVendido = binding.txtInputDescricaoTrabalhoVendido;
        txtDescricaoTrabalhoVendido.setErrorEnabled(false);
        if (descricao.trim().isEmpty()) {
            txtDescricaoTrabalhoVendido.setError(getString(R.string.stringCampoRequerido));
            txtDescricaoTrabalhoVendido.setErrorTextColor(ColorStateList.valueOf(getContext().getColor(R.color.cor_background_bordo)));
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String recuperaDataAtual() {
        LocalDate dataAtual = LocalDate.now();
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return dataAtual.format(formatador);
    }

    private void configuraComponentesVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuConfirma = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    private void confguraListenerCampoValorLucro() {
        edtValorLucroTrabalhoVendido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

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
        Log.d("trabalhoVendido", "Valor lucro: " + novoValorLucro);
        int valorLucroSemTaxaMercado = (int) Math.round(novoValorLucro / FATOR_PERCENTUAL_MERCADO);
        Log.d("trabalhoVendido", "Valor lucro sem taxa mercado: " + valorLucroSemTaxaMercado + " valor de produção: " + valorProducao);
        double taxa = (double) valorLucroSemTaxaMercado / valorProducao;
        Log.d("trabalhoVendido", "Valor taxa: " + taxa);
        taxa = taxa >= 1 ? (taxa - 1) * 100 : (1 - taxa) * -100;
        Log.d("trabalhoVendido", "Valor taxa: " + taxa);
        int porcentual = (int) Math.round(taxa);
        Log.d("trabalhoVendido", "Valor taxa porcentual: " + porcentual);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void cofiguraCampoValorProducao() {
        if (trabalhoSelecionado == null) {
            Log.d("trabalhoVendido", "Trabalho selecionado é nulo");
            return;
        }
        Log.d("trabalhoVendido", "Trabalho selecionado: "+ trabalhoSelecionado);
        if (trabalhoSelecionado.ehProducaoDeRecursos()) {
            Log.d("trabalhoVendido", "Trabalho selecionado é produção de recuros");
            edtTaxaLucroTrabalhoVendido.setEnabled(false);
            edtValorLucroTrabalhoVendido.setEnabled(false);
            edtValorLucroTrabalhoVendido.setText(R.string.stringIndefinido);
            edtValorProducaoTrabalhoVendido.setText(R.string.stringIndefinido);
            return;
        }
        recursosProducaoViewModel.getRecuperaRecursosResultado().observe(
                getViewLifecycleOwner(),
                resultadoRecuperacao
        -> {
            if (resultadoRecuperacao.getErro() == null) {
                if (resultadoRecuperacao.getDado().isEmpty()) {
                    recursosProducaoViewModel.getInsercaoResultado().observe(
                            getViewLifecycleOwner(),
                            resultadoInsereRecursos
                    -> {
                        if (resultadoInsereRecursos.getErro() != null) mostraMensagem("Erro: " + resultadoInsereRecursos.getErro());
                    });
                    recursosProducaoViewModel.insereListaRecursos();
                    return;
                }
                ArrayList<RecursoAvancado> recursosAvancados = resultadoRecuperacao.getDado();
                Log.d("trabalhoVendido", "Recursos avançados: " + recursosAvancados);
                if (trabalhoSelecionado.ehAmuletos(getContext()) || trabalhoSelecionado.ehAneis(getContext()) || trabalhoSelecionado.ehCapotes(getContext()) || trabalhoSelecionado.ehBraceletes(getContext())) {
                    Log.d("trabalhoVendido", "Trabalho selecionado uso ESSÊNCIAS");
                    for (RecursoAvancado recursoAvancado : recursosAvancados) {
                        switch (recursoAvancado.getId()) {
                            case "e580e375-abc1-44f8-b332-774b7f1a490c":
                                mediaValorRecursoUnitarioComumMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor essência comum: " + mediaValorRecursoUnitarioComumMercado);
                                continue;
                            case "94b66657-c7c6-41c0-b6f0-922614182549":
                                mediaValorRecursoUnitarioCompostoMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor essência composta: " + mediaValorRecursoUnitarioCompostoMercado);
                                continue;
                            case "c9751ecc-f528-4a80-88c3-d2a8af2804fa":
                                mediaValorRecursoUnitarioEnergiaMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor essência de energia: " + mediaValorRecursoUnitarioEnergiaMercado);
                                continue;
                            case "7c27a18c-fc60-484c-9545-99030a623129":
                                mediaValorRecursoUnitarioEtereoMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor essência etérea: " + mediaValorRecursoUnitarioEtereoMercado);
                                break;
                        }
                    }
                }
                if (trabalhoSelecionado.ehLongoAlcance(getContext()) || trabalhoSelecionado.ehCorpoCorpo(getContext())) {
                    Log.d("trabalhoVendido", "Trabalho selecionado uso CATALIZADORES");
                    for (RecursoAvancado recursoAvancado : recursosAvancados) {
                        switch (recursoAvancado.getId()) {
                            case "b7f69638-c9b7-4c69-865e-cbacef5c45b1":
                                mediaValorRecursoUnitarioComumMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor catalizador Comum: " + mediaValorRecursoUnitarioComumMercado);
                                continue;
                            case "3a085587-5093-471d-9187-27b2370e4b38":
                                mediaValorRecursoUnitarioCompostoMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor catalizador composto: " + mediaValorRecursoUnitarioCompostoMercado);
                                continue;
                            case "259d5a95-72fd-4b36-b17f-c7b6a2a6897f":
                                mediaValorRecursoUnitarioEnergiaMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor catalizador energia: " + mediaValorRecursoUnitarioEnergiaMercado);
                                continue;
                            case "2d8c434a-50eb-4269-bc70-725ded6bc7e9":
                                mediaValorRecursoUnitarioEtereoMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor catalizador etereo: " + mediaValorRecursoUnitarioEtereoMercado);
                                break;
                        }
                    }
                }
                if (trabalhoSelecionado.ehArmaduraPesada(getContext()) || trabalhoSelecionado.ehArmaduraLeve(getContext()) || trabalhoSelecionado.ehArmaduraTecido(getContext())) {
                    Log.d("trabalhoVendido", "Trabalho selecionado uso SUBSTÂNCIAS");
                    for (RecursoAvancado recursoAvancado : recursosAvancados) {
                        switch (recursoAvancado.getId()) {
                            case "6ac21d44-1e8d-4bf8-bd62-53248e568417":
                                mediaValorRecursoUnitarioComumMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor substância comum: " + mediaValorRecursoUnitarioComumMercado);
                                continue;
                            case "6250e394-4a82-4ccb-b697-c788b9094c41":
                                mediaValorRecursoUnitarioCompostoMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor substância composta: " + mediaValorRecursoUnitarioCompostoMercado);
                                continue;
                            case "b2f158f9-5b52-444a-a27b-7ac1284063c6":
                                mediaValorRecursoUnitarioEnergiaMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor substância de energia: " + mediaValorRecursoUnitarioEnergiaMercado);
                                continue;
                            case "e12c1346-9343-414e-a0b5-631e494423b2":
                                mediaValorRecursoUnitarioEtereoMercado = recursoAvancado.getValor();
                                Log.d("trabalhoVendido", "Valor substância etérea: " + mediaValorRecursoUnitarioEtereoMercado);
                                break;
                        }
                    }
                }
                if (trabalhoSelecionado.ehComum()) {
                    Log.d("trabalhoVendido", "Trabalho selecionado é COMUM");
                    calculaValorProducaoComum();
                    binding.edtInputValorProducaoTrabalhoVendido.setText(getString(R.string.stringValorProducaoValor, valorProducaoComum));
                    atualizaValorLucro(valorProducaoComum);
                    return;
                }
                if (trabalhoSelecionado.ehMelhorado()) {
                    Log.d("trabalhoVendido", "Trabalho selecionado é MELHORADO");
                    calculaValorProducaoComum();
                    calculcaValorProducaoMelhorado();
                    edtValorProducaoTrabalhoVendido.setText(getString(R.string.stringValorProducaoValor, valorProducaoMelhorado));
                    atualizaValorLucro(valorProducaoMelhorado);
                    return;
                }
                if (trabalhoSelecionado.ehRaro()) {
                    Log.d("trabalhoVendido", "Trabalho selecionado é RARO");
                    calculaValorProducaoComum();
                    calculcaValorProducaoMelhorado();
                    calculcaValorProducaoRaro();
                    edtValorProducaoTrabalhoVendido.setText(getString(R.string.stringValorProducaoValor, valorProducaoRaro));
                    atualizaValorLucro(valorProducaoRaro);
                }
                return;
            }
            Log.d("trabalhoVendido", "Erro ao recuperar recursos: " + resultadoRecuperacao.getErro());
        });
        recursosProducaoViewModel.recuperaRecursos();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void calculcaValorProducaoRaro() {
        int quantidadeRecursoEtereo = trabalhoSelecionado.recuperaQuantidadeMaximaRecursosEtereo(getContext());
        Log.d("trabalhoVendido", "Quantidade de recursos etéreos necessarios: " + quantidadeRecursoEtereo);
        valorProducaoRaro = valorProducaoMelhorado + (mediaValorRecursoUnitarioEtereoMercado * quantidadeRecursoEtereo) + MEDIA_VALOR_LICENCA_INICIANTE;
        Log.d("trabalhoVendido", "Valor producao raro: " + valorProducaoRaro);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void calculcaValorProducaoMelhorado() {
        Log.d("trabalhoVendido", "Trabalhos necessarios: " + trabalhoSelecionado.getTrabalhoNecessario());
        String[] listaTrabalhosNecessarios = trabalhoSelecionado.recuperaListaTrabalhosNecessarios();
        Log.d("trabalhoVendido", "Lista trabalhos necessarios: " + Arrays.toString(listaTrabalhosNecessarios));
        int quantidadeTrabalhosComunsNecessarios = listaTrabalhosNecessarios.length;
        Log.d("trabalhoVendido", "Quantidade de trabalhos comuns necessarios: " + quantidadeTrabalhosComunsNecessarios);
        int quantidadeRecursoEnerga = trabalhoSelecionado.recuperaQuantidadeMaximaRecursosEnergia(getContext());
        Log.d("trabalhoVendido", "Quantidade de recursos de energia necessarios: " + quantidadeRecursoEnerga);
        valorProducaoMelhorado = (valorProducaoComum * quantidadeTrabalhosComunsNecessarios) + (mediaValorRecursoUnitarioEnergiaMercado * quantidadeRecursoEnerga) + MEDIA_VALOR_LICENCA_INICIANTE;
        Log.d("trabalhoVendido", "Valor de produção melhorado: " + valorProducaoMelhorado);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void calculaValorProducaoComum() {
        int quantidadeMaximaRecursos = trabalhoSelecionado.recuperaQuantidadeMaximaRecursos(getContext());
        Log.d("trabalhoVendido", "Trabalho selecionado possui quantidade máxima de recursos: " + quantidadeMaximaRecursos);
        int quantidadeTotalRecursos = quantidadeMaximaRecursos * 3 + 3;
        Log.d("trabalhoVendido", "Trabalho selecionado possui quantidade final de recursos: " + quantidadeTotalRecursos);
        int quantidadeMaximaRecursosProduzido = trabalhoSelecionado.getNivel() > 14 ? 24 : 18;
        Log.d("trabalhoVendido", "Trabalho selecionado possui quantidade máxima de recursos produzidos: " + quantidadeMaximaRecursosProduzido);
        int quantidadeRecursosNecessarios = trabalhoSelecionado.getNivel() > 14 ? 8 : 4;
        Log.d("trabalhoVendido", "Trabalho selecionado possui quantidade máxima de recursos (Comum/Composto): " + quantidadeRecursosNecessarios);
        int valorRecursoUnitario = (mediaValorRecursoUnitarioCompostoMercado * quantidadeRecursosNecessarios) / quantidadeMaximaRecursosProduzido;
        Log.d("trabalhoVendido", "Trabalho selecionado possui recurso necessário com valor unitário: " + valorRecursoUnitario);
        int valorLicencaComum = 80;
        int valorLicencaAprendiz = mediaValorRecursoUnitarioComumMercado * 4 / 2 + 80;
        Log.d("trabalhoVendido", "Trabalho selecionado possui valor da licença do aprendiz: " + valorLicencaAprendiz);
        double resultado = (double) quantidadeTotalRecursos / quantidadeMaximaRecursosProduzido;
        Log.d("trabalhoVendido", "Trabalho selecionado possui quantidade de licenças do aprendiz utilizadas: " + resultado);
        int quantidadeLicencaAprendizUtilizada = (int) Math.max(Math.round(resultado), 1);
        Log.d("trabalhoVendido", "Trabalho selecionado possui quantidade de licenças do aprendiz utilizadas (inteiro): " + quantidadeLicencaAprendizUtilizada);
        int valorLicencas = valorLicencaComum + (valorLicencaAprendiz * quantidadeLicencaAprendizUtilizada);
        Log.d("trabalhoVendido", "Trabalho selecionado possui valor total de licenças utilizadas: " + valorLicencas);
        int valorRecursoTotal = quantidadeTotalRecursos * valorRecursoUnitario;
        Log.d("trabalhoVendido", "Trabalho selecionado possui valor total de recursos: " + valorRecursoTotal);
        valorProducaoComum = valorRecursoTotal + valorLicencas;
        Log.d("trabalhoVendido", "Trabalho selecionado possui valor de produção: " + valorProducaoComum);
    }

    private void atualizaValorLucro(int valorProducao) {
        Log.d("trabalhoVendido", "Trabalho selecionado possui novaTaxa: " + novaTaxa);
        double v = novaTaxa * FATOR_PERCENTUAL;
        Log.d("trabalhoVendido", "Trabalho selecionado possui v: " + v);
        double porcentagem = v >= 0 ? v + 1 : v + 1.0;
        Log.d("trabalhoVendido", "Trabalho selecionado possui porcentagem: " + porcentagem);
        int valorProducaoTaxa = (int) (valorProducao * porcentagem);
        Log.d("trabalhoVendido", "Trabalho selecionado possui valor com lucro (" + novaTaxa + "%): " + valorProducaoTaxa);
        int valorTotalLucro = (int) (valorProducaoTaxa * FATOR_PERCENTUAL_MERCADO);
        valorTotalLucro = Math.max(valorTotalLucro, 0);
        Log.d("trabalhoVendido", "Trabalho selecionado possui valor com lucro (" + novaTaxa + "%) + taxa do mercado (10%): " + valorTotalLucro);
        edtValorLucroTrabalhoVendido.setText(String.valueOf(valorTotalLucro));
    }

    private TrabalhoVendido defineTrabalhoModificado() {
        TrabalhoVendido trabalhoModificado = new TrabalhoVendido();
        trabalhoModificado.setId(trabalhoRecebido.getId());
        trabalhoModificado.setIdTrabalho(trabalhoSelecionado.getId());
        trabalhoModificado.setDescricao(edtDescricaoTrabalhoVendido.getText().toString().trim());
        trabalhoModificado.setDataVenda(edtDataTrabalhoVendido.getText().toString().trim());
        trabalhoModificado.setQuantidade(Integer.parseInt(edtQuantidadeTrabalhoVendido.getText().toString().trim()));
        trabalhoModificado.setValor(Integer.parseInt(edtValorTrabalhoVendido.getText().toString().trim()));
        return trabalhoModificado;
    }

    private void modificaTrabalho(TrabalhoVendido trabalhoModificado) {
        trabalhosVendidosViewModel.getModificacaoResultado().observe(
                getViewLifecycleOwner(),
                resultadoModificaTrabalho
        -> {
            if (resultadoModificaTrabalho.getErro() == null) {
                mostraMensagem("Venda modificada com sucesso!");
                voltaParaTrabalhosVendidos();
                return;
            }
            mostraMensagem("Erro ao modificar trabalho: " + resultadoModificaTrabalho.getErro());
        });
        trabalhosVendidosViewModel.modificaVenda(trabalhoModificado);
    }

    private void voltaParaTrabalhosVendidos() {
        Navigation.findNavController(binding.getRoot()).navigate(vaiDeDetalhesTrabalhoVendidoParaTrabalhosVendidos());
    }

    private boolean camposTrabalhoModificado(TrabalhoVendido trabalhoVendido) {
        return !trabalhoRecebido.equals(trabalhoVendido);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void recuperaTrabalhos() {
        trabalhoViewModel.getTrabalhos().observe(getViewLifecycleOwner(), resultadoRecuperaTrabalhos -> {
            if (resultadoRecuperaTrabalhos.getDado() != null) {
                ArrayList<Trabalho> todosTrabalhos;
                todosTrabalhos = resultadoRecuperaTrabalhos.getDado();
                configuraAutoCompleteTrabalhos(todosTrabalhos);
                cofiguraCampoValorProducao();
            }
        });
        trabalhoViewModel.recuperaTrabalhos();
    }

    private void recebeDados() {
        idPersonagem = DetalhesTrabalhoVendidoFragmentArgs.fromBundle(getArguments()).getIdPersonagem();
        trabalhoRecebido = DetalhesTrabalhoVendidoFragmentArgs.fromBundle(getArguments()).getTrabalhoVendido();
        codigoRequisicao = DetalhesTrabalhoVendidoFragmentArgs.fromBundle(getArguments()).getCodigoRequisicao();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void preencheCampos() {
        String data = trabalhoRecebido.getDataVenda() == null? recuperaDataAtual() : trabalhoRecebido.getDataVenda();
        edtDescricaoTrabalhoVendido.setText(trabalhoRecebido.getDescricao());
        edtDataTrabalhoVendido.setText(data);
        edtValorTrabalhoVendido.setText(String.valueOf(trabalhoRecebido.getValor()));
        edtQuantidadeTrabalhoVendido.setText(String.valueOf(trabalhoRecebido.getQuantidade()));
        edtTaxaLucroTrabalhoVendido.setText(String.valueOf(novaTaxa));
        edtValorProducaoTrabalhoVendido.setEnabled(false);
        edtValorProducaoTrabalhoVendido.setText(getString(R.string.stringValorProducaoValor, 0));
    }

    private void inicializaComponentes() {
        novaTaxa = TAXA;
        valorProducaoComum = 0;
        novoValorLucro = 0;
        valorProducaoMelhorado = 0;
        valorProducaoRaro = 0;
        edtDescricaoTrabalhoVendido = binding.edtInputDescricaoTrabalhoVendido;
        edtDataTrabalhoVendido = binding.edtInputDataTrabalhoVendido;
        edtValorTrabalhoVendido = binding.edtInputValorTrabalhoVendido;
        edtQuantidadeTrabalhoVendido = binding.edtInputQuantidadeTrabalhoVendido;
        autoCompleteNomeTrabalhoVendido = binding.autoCompleteNomeTrabalhoVendido;
        edtTaxaLucroTrabalhoVendido = binding.edtInputTaxaLucroTrabalhoVendido;
        edtValorProducaoTrabalhoVendido = binding.edtInputValorProducaoTrabalhoVendido;
        edtValorLucroTrabalhoVendido = binding.edtInputValorLucroTrabalhoVendido;
        TrabalhoViewModelFactory trabalhoViewModelFactory = new TrabalhoViewModelFactory(TrabalhoRepository.getInstancia(getContext()));
        trabalhoViewModel = new ViewModelProvider(this, trabalhoViewModelFactory).get(TrabalhoViewModel.class);
        TrabalhosVendidosViewModelFactory trabalhosVendidosViewModelFactory = new TrabalhosVendidosViewModelFactory(TrabalhoVendidoRepository.getInstance(idPersonagem));
        trabalhosVendidosViewModel = new ViewModelProvider(this, trabalhosVendidosViewModelFactory).get(TrabalhosVendidosViewModel.class);
        RecursosProducaoViewModelFactory recursosProducaoViewModelFactory = new RecursosProducaoViewModelFactory(idPersonagem, getContext());
        recursosProducaoViewModel = new ViewModelProvider(this, recursosProducaoViewModelFactory).get(RecursosProducaoViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraAutoCompleteTrabalhos(ArrayList<Trabalho> todosTrabalhos) {
        ArrayList<String> todosNomesTrabalhos = new ArrayList<>();
        for (int posicao = 0; posicao < todosTrabalhos.size(); posicao += 1) {
            todosNomesTrabalhos.add(todosTrabalhos.get(posicao).getNome());
            if (todosTrabalhos.get(posicao).getId().equals(trabalhoRecebido.getIdTrabalho())){
                autoCompleteNomeTrabalhoVendido.setText(todosTrabalhos.get(posicao).getNome());
                trabalhoSelecionado = todosTrabalhos.get(posicao);
            }
        }
        Collections.sort(todosNomesTrabalhos);
        todosTrabalhos.sort(Comparator.comparing(Trabalho::getNome));
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, todosNomesTrabalhos);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteNomeTrabalhoVendido.setAdapter(adapterEstado);
        autoCompleteNomeTrabalhoVendido.setOnItemClickListener((parent, view, position, id) -> trabalhoSelecionado = todosTrabalhos.get(position));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        recuperaTrabalhos();
    }

    @Override
    protected FragmentDetalhesTrabalhoVendidoBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDetalhesTrabalhoVendidoBinding.inflate(
                inflater,
                container,
                false
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeOuvinteRecurso();
        removeOuvinteVenda();
        removeOuvinteTrabalho();
        binding = null;
    }

    private void removeOuvinteTrabalho() {
        if (trabalhoViewModel == null) return;
        trabalhoViewModel.removeOuvinte();
    }

    private void removeOuvinteVenda() {
        if (trabalhosVendidosViewModel == null) return;
        trabalhosVendidosViewModel.removeOuvinte();
    }

    private void removeOuvinteRecurso() {
        if (recursosProducaoViewModel == null) return;
        recursosProducaoViewModel.removeOuvinte();
    }
}