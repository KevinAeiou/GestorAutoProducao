package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_TRABALHO;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INVALIDA;
import static com.kevin.gestorproducao.ui.fragment.DetalhesTrabalhoFragmentDirections.vaiParaTrabalhos;
import static com.kevin.gestorproducao.utilitario.Utilitario.comparaString;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentDetalhesTrabalhoBinding;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoVendaRepository;
import com.kevin.gestorproducao.service.TrabalhoService;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DetalhesTrabalhoFragment
    extends BaseFragment<FragmentDetalhesTrabalhoBinding>
{
    private Trabalho trabalhoRecebido;
    private LinearLayout layoutTrabalhosNecessarios;
    private TextInputEditText edtNomeTrabalho, edtNomeProducaoTrabalho, edtExperienciaTrabalho, edtNivelTrabalho;
    private TextInputLayout txtInputNome, txtInputNomeProducao, txtInputProfissao, txtInputExperiencia, txtInputNivel, txtInputRaridade;
    private AutoCompleteTextView autoCompleteProfissao, autoCompleteRaridade;
    private MaterialButton btnExcluir;
    private String[] mensagemErro;
    private int codigoRequisicao = CODIGO_REQUISICAO_INVALIDA;
    private TrabalhoViewModel trabalhoViewModel;
    private TrabalhoEstoqueViewModel estoqueViewModel;
    private PersonagemViewModel personagemViewModel;
    private TrabalhosVendidosViewModel vendasViewModel;
    private TrabalhoProducaoViewModel producaoViewModel;
    private ArrayList<Trabalho> trabalhosNecessarios;
    private NavController controlador;
    private ChipGroup chipGroupTrabalhosNecessarios;
    private MaterialButton btnConfirmar;
    private TrabalhoService trabalhoService;
    private LinearLayout loadingBotaoConfirmar;
    private LinearLayout loadingBotaoExcluir;

    @Override
    protected FragmentDetalhesTrabalhoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesTrabalhoBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DetalhesTrabalhoFragmentArgs argumentos = DetalhesTrabalhoFragmentArgs.fromBundle(
            getArguments()
        );

        codigoRequisicao = argumentos.getCodigoRequisicao();
        if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO){
            trabalhoRecebido = argumentos.getTrabalho();
        }
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {
        String titulo = trabalhoRecebido == null ?
            getString(R.string.stringNovoTrabalho) :
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        configuraCliqueCampoNivel();
        configuraBotaoExcluiTrabalho();
        configuraBotaoConfirmaTrabalho();
        observarPersonagem();
        observarTrabalho();
        observarProducao();
    }

    private void configuraBotaoConfirmaTrabalho() {
        btnConfirmar.setOnClickListener(v-> {
            iniciarLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );

            if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO) {
                verificaNovoTrabalho();
                return;
            }

            verificaModificacaoTrabalho();
        });
    }

    private void observarProducao() {
        producaoViewModel.getRemocaoReferenciaResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null)
                    mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );
    }

    private void observarTrabalho() {
        trabalhoViewModel.getInsercaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    mostraMensagemAncorada(getString(R.string.stringTrabalhoInserido));

                    trabalhoViewModel.limpaInsercaoResultado();

                    recuperaTrabalhosNecessarios();

                    limpaCampos();
                    return;
                }

                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );

        trabalhoViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    mostraMensagemAncorada(getString(R.string.stringTrabalhoModificadoComSucesso));
                    trabalhoViewModel.limpaModificacaoResultado();
                    voltaParaListaTrabalhos();
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );

        trabalhoViewModel.getRemocaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    trabalhoViewModel.limpaRemocaoResultado();

                    trabalhoService.processarPosRemocao(trabalhoRecebido.getId());
                    voltaParaListaTrabalhos();
                    return;
                }

                pararLoadingBotao(
                    btnExcluir,
                    loadingBotaoExcluir
                );

                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );

        trabalhoViewModel.getTrabalhosNecessariosResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    trabalhosNecessarios = resultado.getDado();

                    popularChipsTrabalhosNecessarios();

                    if (trabalhoRecebido != null &&
                        !trabalhoRecebido.getListaTrabalhosNecessarios().isEmpty()
                    ) {
                        selecionaTrabalhosNecessarios();
                    }
                }
            }
        );
    }

    private void popularChipsTrabalhosNecessarios() {
        chipGroupTrabalhosNecessarios.removeAllViews();

        for(Trabalho trabalho : trabalhosNecessarios) {
            Chip chip = new Chip(requireContext());
            chip.setText(trabalho.getNome());
            chip.setCheckable(true);
            chip.setTag(trabalho);

            chipGroupTrabalhosNecessarios.addView(chip);
        }
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                estoqueViewModel.setIdPersonagem(resultado.getId());
                vendasViewModel.setIdPersonagem(resultado.getId());
                producaoViewModel.setIdPersonagem(resultado.getId());
            }
        );
    }

    private void configuraCliqueCampoNivel() {
        edtNivelTrabalho.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String stringNivel = editable.toString();
                if (stringNivel.isEmpty()) return;
                recuperaTrabalhosNecessarios();
            }
        });
    }

    private void verificaNovoTrabalho() {
        Trabalho trabalho = defineNovoTrabalho();

        if (camposNovoTrabalhoEhValido(trabalho)) {
            if (trabalhoViewModel.trabalhoEspecificoExiste(trabalho) == null) {

                trabalhoViewModel.insereTrabalho(trabalho);
                return;
            }

            mostraMensagemAncorada(getString(R.string.stringJaExisteValor, trabalho.getNome()));
            pararLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );
            return;
        }

        pararLoadingBotao(
            btnConfirmar,
            loadingBotaoConfirmar
        );
    }

    private void limpaCampos() {
        edtNomeTrabalho.setText("");
        edtNomeProducaoTrabalho.setText("");
        edtNomeTrabalho.requestFocus();
        chipGroupTrabalhosNecessarios.clearCheck();
    }

    private void verificaModificacaoTrabalho() {
        Trabalho trabalho = defineTrabalhoModificado();
        if (trabalhoEhModificado(trabalho)) {
            String existente = trabalhoViewModel.trabalhoEspecificoExiste(trabalho);

            if (existente == null || existente.equals(trabalho.getId())) {
                trabalhoViewModel.modificaTrabalho(trabalho);
                return;
            }

            mostraMensagemAncorada(getString(R.string.stringJaExisteValor, trabalho.getNome()));
            pararLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );
            return;
        }

        voltaParaListaTrabalhos();
    }

    private void voltaParaListaTrabalhos() {
        controlador.navigate(vaiParaTrabalhos());
    }

    private void inicializaComponentes() {
        trabalhosNecessarios = new ArrayList<>();
        mensagemErro = new String[]{
            getString(R.string.stringCampoRequerido),
            getString(R.string.stringInvalido)
        };

        edtNomeTrabalho = binding.edtNomeDetalhesTrabalho;
        edtNomeProducaoTrabalho = binding.edtNomeProducaoDetalhesTrabalho;
        edtNivelTrabalho = binding.edtNivelDetalhesTrabalho;
        edtExperienciaTrabalho = binding.edtExperienciaDetalhesTrabalho;

        txtInputNome = binding.txtLayoutNomeDetalhesTrabalho;
        txtInputNomeProducao = binding.txtLayoutNomeProducaoDetalhesTrabalho;
        txtInputProfissao = binding.txtLayoutProfissaoDetalhesTrabalho;
        txtInputExperiencia = binding.txtLayoutExperienciaDetalhesTrabalho;
        txtInputNivel = binding.txtLayoutNivelDetalhesTrabalho;
        txtInputRaridade = binding.txtLayoutRaridadeDetalhesTrabalho;

        autoCompleteProfissao = binding.txtAutoCompleteProfissaoDetalhesTrabalho;
        autoCompleteRaridade = binding.txtAutoCompleteRaridadeDetalhesTrabalho;

        layoutTrabalhosNecessarios = binding.layoutTrabalhosNecessariosDetalhesTrabalho;
        chipGroupTrabalhosNecessarios = binding.chipGroupTrabalhosNecessarios;

        btnExcluir = binding.btnExcluiTrabalho;
        btnConfirmar = binding.btnConfirmarTrabalho;
        loadingBotaoConfirmar = binding.loadingDotsConfirmar.getRoot();
        loadingBotaoExcluir = binding.loadingDotsExcluir.getRoot();

        btnExcluir.setVisibility(GONE);
        layoutTrabalhosNecessarios.setVisibility(GONE);

        configurarMascaraMilhar(edtNivelTrabalho);
        configurarMascaraMilhar(edtExperienciaTrabalho);

        Context context = requireContext().getApplicationContext();

        trabalhoService = new TrabalhoService(
            TrabalhoVendaRepository.getInstance(context),
            TrabalhoProducaoRepository.getInstance(context),
            TrabalhoEstoqueRepository.getInstance(context)
        );

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        trabalhoViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(TrabalhoViewModel.class);

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        estoqueViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(TrabalhoEstoqueViewModel.class);

        vendasViewModel = new ViewModelProvider(
            requireActivity(),
                viewModelFactory
        ).get(TrabalhosVendidosViewModel.class);

        producaoViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(TrabalhoProducaoViewModel.class);

        controlador = Navigation.findNavController(binding.getRoot());
    }

    private void configuraBotaoExcluiTrabalho() {
        if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO) {
            btnExcluir.setVisibility(VISIBLE);

            btnExcluir.setOnClickListener(v -> {
                iniciarLoadingBotao(
                    btnExcluir,
                    loadingBotaoExcluir
                );

                ConfirmacaoDialog dialog = ConfirmacaoDialog.novaInstancia(
                    getString(R.string.stringExcluirTrabalho),
                    getString(R.string.stringConfirmaExclusaoTrabalho),
                    () -> trabalhoViewModel.removeTrabalho(trabalhoRecebido),
                    () -> pararLoadingBotao(btnConfirmar, loadingBotaoConfirmar)
                );

                dialog.show(getParentFragmentManager(), "confirmacao_exclusao");
            });
        }
    }

    private void configuraDropdownProfissoes() {
        String[] profissoesTrabalho = getResources().getStringArray(R.array.profissoes);
        ArrayAdapter<String> profissoesAdapter = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_dropdrown,
            profissoesTrabalho
        );
        profissoesAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        if (trabalhoRecebido != null) {
            autoCompleteProfissao.setText(trabalhoRecebido.getProfissao());

            chipGroupTrabalhosNecessarios.setSingleSelection(
                !trabalhoRecebido.getProfissao().equals(getString(R.string.stringProfissaoAneis)) &&
                !trabalhoRecebido.getRaridade().equals(getString(R.string.stringMelhorado))
            );
        }

        autoCompleteProfissao.setAdapter(profissoesAdapter);
        autoCompleteProfissao.setOnItemClickListener((
            adapterView,
            view,
            position,
            id
        ) -> recuperaTrabalhosNecessarios());
    }

    private void configuraDropdownRaridades() {
        String[] raridadesTrabalho = getResources().getStringArray(R.array.raridades);
        ArrayAdapter<String> raridadeAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, raridadesTrabalho);
        raridadeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        if (trabalhoRecebido != null) {
            String raridade = trabalhoRecebido.getRaridade();
            autoCompleteRaridade.setText(raridade);

            configuraCampoTrabalhoNecessario(raridade);
        }
        autoCompleteRaridade.setAdapter(raridadeAdapter);

        configuraCliqueItemRaridade();
    }

    private void configuraCliqueItemRaridade() {
        autoCompleteRaridade.setOnItemClickListener((
            adapterView,
            view,
            position,
            id
        ) -> {
            String raridade = adapterView.getAdapter().getItem(position).toString();

            chipGroupTrabalhosNecessarios.setSingleSelection(
                !raridade.equals(getString(R.string.stringEspecial))
            );

            configuraCampoTrabalhoNecessario(raridade);
        });
    }

    private void configuraCampoTrabalhoNecessario(String raridade) {
        if (ehMelhoraroOuRaro(raridade)) {
            layoutTrabalhosNecessarios.setVisibility(VISIBLE);
            recuperaTrabalhosNecessarios();
            return;
        }

        layoutTrabalhosNecessarios.setVisibility(GONE);
    }

    private boolean ehMelhoraroOuRaro(String raridadeClicada) {
        return comparaString(raridadeClicada, getString(R.string.stringMelhorado)) ||
            comparaString(raridadeClicada, getString(R.string.stringRaro));
    }

    private void recuperaTrabalhosNecessarios() {
        Trabalho trabalho = defineTrabalhoBusca();

        trabalhoViewModel.recuperaTrabalhosNecessarios(trabalho);
    }

    @NonNull
    private Trabalho defineTrabalhoBusca() {
        Trabalho trabalho = defineNivelTrabalhoBusca();
        trabalho = defineRaridadeTrabalhoBusca(trabalho);
        trabalho.setProfissao(Objects.requireNonNull(autoCompleteProfissao).getText().toString().trim());

        return trabalho;
    }

    private Trabalho defineRaridadeTrabalhoBusca(Trabalho trabalho) {
        String raridade = Objects.requireNonNull(autoCompleteRaridade).getText().toString().trim();
        if (comparaString(raridade, getString(R.string.stringMelhorado))) {
            trabalho.setRaridade(getString(R.string.stringComum));
            return trabalho;
        }
        if (comparaString(raridade, getString(R.string.stringRaro))) {
            trabalho.setRaridade(getString(R.string.stringMelhorado));
            return trabalho;
        }
        trabalho.setRaridade("");

        return trabalho;
    }

    @NonNull
    private Trabalho defineNivelTrabalhoBusca() {
        String stringNivel = edtNivelTrabalho.getText().toString().trim();
        int nivel = stringNivel.isEmpty() ? 0 : Integer.parseInt(stringNivel);

        Trabalho trabalho = new Trabalho();
        trabalho.setNivel(nivel);

        return trabalho;
    }

    private void preencheCamposTrabalho() {
        if (trabalhoRecebido == null) return;

        edtNomeTrabalho.setText(trabalhoRecebido.getNome());
        edtNomeProducaoTrabalho.setText(trabalhoRecebido.getNomeProducao());
        edtExperienciaTrabalho.setText(String.valueOf(trabalhoRecebido.getExperiencia()));
        edtNivelTrabalho.setText(String.valueOf(trabalhoRecebido.getNivel()));
    }

    private void selecionaTrabalhosNecessarios() {
        for (int i = 0; i < chipGroupTrabalhosNecessarios.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTrabalhosNecessarios.getChildAt(i);
            Trabalho trabalho = (Trabalho) chip.getTag();

            boolean selecionado = false;

            for (String id : trabalhoRecebido.getListaTrabalhosNecessarios()) {
                if (id.equals(trabalho.getId())) {
                    selecionado = true;
                    break;
                }
            }

            chip.setChecked(selecionado);
        }
    }

    @NonNull
    private Trabalho defineTrabalhoModificado() {
        Trabalho trabalho = new Trabalho();

        trabalho.setId(trabalhoRecebido.getId());
        trabalho.setNome(Objects.requireNonNull(edtNomeTrabalho.getText()).toString().trim());
        trabalho.setNomeProducao(Objects.requireNonNull(edtNomeProducaoTrabalho.getText()).toString().trim());
        trabalho.setProfissao(Objects.requireNonNull(autoCompleteProfissao).getText().toString().trim());
        trabalho.setRaridade(Objects.requireNonNull(autoCompleteRaridade).getText().toString().trim());
        trabalho.setNecessarios(montaNecessariosMap());
        trabalho.setNivel(obterValorNumerico(edtNivelTrabalho));
        trabalho.setExperiencia(obterValorNumerico(edtExperienciaTrabalho));

        return trabalho;
    }

    private Map<String, Boolean> montaNecessariosMap() {
        Map<String, Boolean> map = new HashMap<>();
        ArrayList<String> idsSelecionados = new ArrayList<>();

        for (int i = 0; i < chipGroupTrabalhosNecessarios.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTrabalhosNecessarios.getChildAt(i);

            if (chip.isChecked()) {
                Trabalho trabalho = (Trabalho) chip.getTag();
                idsSelecionados.add(trabalho.getId());
            }
        }

        for (String id : idsSelecionados) {
            map.put(id.trim(), true);
        }

        return map;
    }

    private boolean trabalhoEhModificado(Trabalho trabalho) {
        return verificaCampoModificado(trabalho.getNome(), trabalhoRecebido.getNome()) ||
            verificaCampoModificado(trabalho.getNomeProducao(), trabalhoRecebido.getNomeProducao()) ||
            verificaCampoModificado(trabalho.getProfissao(), trabalhoRecebido.getProfissao()) ||
            verificaCampoModificado(String.valueOf(trabalho.getExperiencia()), trabalhoRecebido.getExperiencia().toString()) ||
            verificaCampoTrabalhoNecessario() ||
            verificaCampoModificado(String.valueOf(trabalho.getNivel()), trabalhoRecebido.getNivel().toString()) ||
            verificaCampoModificado(trabalho.getRaridade(), trabalhoRecebido.getRaridade());
    }

    private boolean verificaCampoTrabalhoNecessario() {
        ArrayList<String> idsTrabalhos = new ArrayList<>();
        for (int i = 0; i < chipGroupTrabalhosNecessarios.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTrabalhosNecessarios.getChildAt(i);

            if (chip.isChecked()) {
                Trabalho trabalho = (Trabalho) chip.getTag();
                idsTrabalhos.add(trabalho.getId());
            }
        }
        return !idsTrabalhos.equals(trabalhoRecebido.getListaTrabalhosNecessarios());
    }

    private boolean verificaCampoModificado(String campo, String valorRecebido) {
        return !comparaString(campo, valorRecebido);
    }

    private boolean camposNovoTrabalhoEhValido(Trabalho trabalho) {
        return verificaValorCampo(trabalho.getNome(), txtInputNome, 0) &
            verificaValorCampo(trabalho.getNomeProducao(), txtInputNomeProducao, 0) &
            verificaValorCampo(trabalho.getProfissao(), txtInputProfissao, 1) &
            verificaValorCampo(String.valueOf(trabalho.getExperiencia()),txtInputExperiencia,1) &
            verificaValorCampo(String.valueOf(trabalho.getNivel()), txtInputNivel, 1) &
            verificaValorCampo(trabalho.getRaridade(), txtInputRaridade, 1);
    }

    private Boolean verificaValorCampo(String stringCampo, TextInputLayout inputLayout, int posicaoErro) {
        if (stringCampo.isEmpty() || comparaString(stringCampo, "profissões")|| comparaString(stringCampo, "raridade")){
            inputLayout.setError(mensagemErro[posicaoErro]);
            return false;
        }
        inputLayout.setErrorEnabled(false);
        return true;
    }

    private Trabalho defineNovoTrabalho() {
        Trabalho trabalho = new Trabalho();

        trabalho.setNome(edtNomeTrabalho.getText().toString().trim());
        trabalho.setNomeProducao(edtNomeProducaoTrabalho.getText().toString().trim());
        trabalho.setProfissao(autoCompleteProfissao.getText().toString().trim());
        trabalho.setRaridade(autoCompleteRaridade.getText().toString().trim());
        trabalho.setNecessarios(montaNecessariosMap());
        trabalho.setNivel(obterValorNumerico(edtNivelTrabalho));
        trabalho.setExperiencia(obterValorNumerico(edtExperienciaTrabalho));

        return trabalho;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (codigoRequisicao == CODIGO_REQUISICAO_INVALIDA) return;

        configuraDropdownProfissoes();
        configuraDropdownRaridades();

        if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO){
            preencheCamposTrabalho();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removerObservadorTrabalho();
        removerObservadorPersonagem();
        removerObservadorEstoque();
        removerObservadorVendas();
        removerObservadorProducao();
    }

    private void removerObservadorPersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }

    private void removerObservadorTrabalho() {
        if (trabalhoViewModel == null) return;
        trabalhoViewModel.removeObservador();
    }

    private void removerObservadorEstoque() {
        if (estoqueViewModel == null) return;
        estoqueViewModel.removeObservador();
    }

    private void removerObservadorVendas() {
        if (vendasViewModel == null) return;
        vendasViewModel.removeObservador();
    }

    private void removerObservadorProducao() {
        if (producaoViewModel == null) return;
        producaoViewModel.removeObservador();
    }
}
