package com.kevin.ceep.ui.fragment;

import static android.view.View.GONE;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_TRABALHO_FEITO;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_TRABALHO_PARA_PRODUZIR;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_TRABALHO_PRODUZINDO;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_TRABALHO;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INVALIDA;
import static com.kevin.ceep.ui.fragment.TrabalhoEspecificoFragmentDirections.vaiDeTrabalhoEspecficoParaTrabalhos;
import static com.kevin.ceep.ui.fragment.TrabalhoEspecificoFragmentDirections.vaiParaListaTrabalhosProducao;
import static com.kevin.ceep.utilitario.Utilitario.comparaString;

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
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentTrabalhoEspecificoBinding;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.TrabalhoEstoqueRepository;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.ceep.ui.viewModel.factory.ProfissaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoEstoqueViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoProducaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhosVendidosViewModelFactory;

import java.util.ArrayList;
import java.util.Objects;

public class TrabalhoEspecificoFragment extends Fragment implements MenuProvider{
    private FragmentTrabalhoEspecificoBinding binding;
    private TrabalhoProducao trabalhoProducaoRecebido;
    private Trabalho trabalhoRecebido;
    private LinearLayoutCompat linearLayoutTrabalhoNecessario2, linearLayoutTrabalhoNecessario3;
    private TextInputEditText edtNomeTrabalho, edtNomeProducaoTrabalho, edtExperienciaTrabalho, edtNivelTrabalho;
    private TextInputLayout txtInputNome, txtInputNomeProducao, txtInputProfissao, txtInputExperiencia, txtInputNivel, txtInputRaridade, txtInputLicenca, txtInputEstado;
    private CheckBox checkBoxRecorrenciaTrabalho;
    private AutoCompleteTextView autoCompleteProfissao, autoCompleteRaridade, autoCompleteTrabalhoNecessario1, autoCompleteTrabalhoNecessario2, autoCompleteLicenca, autoCompleteEstado;
    private ShapeableImageView imagemTrabalhoNecessario1, imagemTrabalhoNecessario2;
    private LinearProgressIndicator indicadorProgresso;
    private AppCompatButton btnExcluir;
    private String[] estadosTrabalho;
    private ArrayAdapter<String> adapterEstado;
    private final String[] mensagemErro={"Campo requerido!","Inválido!"};
    private String trabalhoNecessario;
    private int codigoRequisicao = CODIGO_REQUISICAO_INVALIDA;
    private boolean acrescimo = false;
    private TrabalhoViewModel trabalhoViewModel;
    private TrabalhoProducaoViewModel trabalhoProducaoViewModel;
    private TrabalhoEstoqueViewModel trabalhoEstoqueViewModel;
    private ProfissaoViewModel profissaoViewModel;
    private ArrayList<Trabalho> trabalhosNecessarios = new ArrayList<>();
    private MutableLiveData<Boolean> confirmacao;
    private NavController controlador;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrabalhoEspecificoBinding.inflate(inflater, container, false);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configuraComponentesVisuais();
        recebeDados();
        inicializaComponentes();
        configuraAcaoImagem();
        configuraCliqueCampoNivel();
    }

    private void configuraCliqueCampoNivel() {
        edtNivelTrabalho.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("trabalho", "Editable: " + editable.toString());
                String stringNivel = editable.toString();
                if (stringNivel.isEmpty()) return;
                String raridadeSelecionada = autoCompleteRaridade.getText().toString();
                configuraCampoTrabalhoNecessario(raridadeSelecionada);
            }
        });
    }

    private void configuraComponentesVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuConfirma = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma) {
            indicadorProgresso.setVisibility(View.VISIBLE);
            defineTrabalhoNecessario();
            if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO) {
                verificaModificacaoTrabalhoProducao();
                return true;
            }
            if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO) {
                verificaModificacaoTrabalho();
                return true;
            }
            if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO) {
                verificaNovoTrabalho();
            }
        }
        return false;
    }

    private void verificaNovoTrabalho() {
        Trabalho trabalho = defineNovoTrabalho();
        if (camposNovoTrabalhoEhValido(trabalho)) {
            if (trabalhoViewModel.trabalhoEspecificoExiste(trabalho)) {
                Snackbar.make(binding.getRoot(), trabalho.getNome()+" já existe!", Snackbar.LENGTH_LONG).show();
                indicadorProgresso.setVisibility(GONE);
                return;
            }
            trabalhoViewModel.insereTrabalho(trabalho).observe(getViewLifecycleOwner(), resultadoInsereTrabalho -> {
                indicadorProgresso.setVisibility(GONE);
                if (resultadoInsereTrabalho.getErro() == null) {
                    Snackbar.make(binding.getRoot(), trabalho.getNome()+" inserido!", Snackbar.LENGTH_LONG).show();
                    limpaCampos();
                    return;
                }
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoInsereTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
            });
            return;
        }
        indicadorProgresso.setVisibility(GONE);
    }

    private void limpaCampos() {
        edtNomeTrabalho.setText("");
        edtNomeProducaoTrabalho.setText("");
        edtNomeTrabalho.requestFocus();
    }

    private void verificaModificacaoTrabalho() {
        Trabalho trabalho = defineTrabalhoModificado();
        if (trabalhoEhModificado(trabalho)) {
            trabalhoViewModel.modificaTrabalho(trabalho).observe(getViewLifecycleOwner(), resultadoModificaTrabalho -> {
                indicadorProgresso.setVisibility(GONE);
                if (resultadoModificaTrabalho.getErro() == null) {
                    Snackbar.make(binding.getRoot(), "Trabalho modificado com sucesso!", Snackbar.LENGTH_LONG).show();
                    voltaParaListaTrabalhos();
                    return;
                }
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoModificaTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
            });
            return;
        }
        voltaParaListaTrabalhos();
    }

    private void voltaParaListaTrabalhos() {
        controlador.navigate(vaiDeTrabalhoEspecficoParaTrabalhos());
    }

    private void verificaModificacaoTrabalhoProducao() {
        TrabalhoProducao trabalhoModificado = defineTrabalhoProducaoModificado();
        if (verificaTrabalhoProducaoModificado(trabalhoModificado)) {
            processaTrabalhoProducaoModificado(trabalhoModificado);
            return;
        }
        voltaParaListaTrabalhosProducao();
    }

    private void processaTrabalhoProducaoModificado(TrabalhoProducao trabalhoModificado) {
        trabalhoProducaoViewModel.modificaTrabalhoProducao(trabalhoModificado).observe(getViewLifecycleOwner(), resultado -> {
            if (resultado.getErro() == null) {
                verficaEstadoTrabalhoProducaoModificado(trabalhoModificado);
                return;
            }
            Snackbar.make(binding.getRoot(), "Erro: "+resultado.getErro(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void verficaEstadoTrabalhoProducaoModificado(TrabalhoProducao trabalhoModificado) {
        if (estadoTrabalhoProducaoEhModificado(trabalhoModificado)) {
            switch (trabalhoModificado.getEstado()) {
                case CODIGO_TRABALHO_PARA_PRODUZIR:
                    Snackbar.make(binding.getRoot(), trabalhoProducaoRecebido.getNome() + " foi modificado com sucesso!", Snackbar.LENGTH_LONG).show();
                    voltaParaListaTrabalhosProducao();
                    break;
                case CODIGO_TRABALHO_PRODUZINDO:
                    if (trabalhoModificado.possueTrabalhoNecessarioValido()) {
                        String[] listaIdsTrabalhosNecessarios = trabalhoModificado.getTrabalhoNecessario().split(",");
                        for (String idTrabalho : listaIdsTrabalhosNecessarios) {
                            trabalhoEstoqueViewModel.recuperaTrabalhoEstoquePorIdTrabalho(idTrabalho).observe(getViewLifecycleOwner(), resultadoTrabalhoEncontrado -> {
                                if (resultadoTrabalhoEncontrado.getErro() == null) {
                                    TrabalhoEstoque trabalhoEncontrado = resultadoTrabalhoEncontrado.getDado();
                                    if (trabalhoEncontrado == null) return;
                                    trabalhoEncontrado.setQuantidade(trabalhoEncontrado.getQuantidade() - 1);
                                    trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEncontrado).observe(getViewLifecycleOwner(), resultadoModificaTrabalho -> {
                                        if (resultadoModificaTrabalho.getErro() == null) {
                                            Snackbar.make(binding.getRoot(), trabalhoProducaoRecebido.getNome() + " foi modificado com sucesso!", Snackbar.LENGTH_LONG).show();
                                            voltaParaListaTrabalhosProducao();
                                        }
                                    });
                                    return;
                                }
                                Snackbar.make(binding.getRoot(), "Erro: "+ resultadoTrabalhoEncontrado.getErro(), Snackbar.LENGTH_LONG).show();
                            });
                        }
                        break;
                    }
                    Snackbar.make(binding.getRoot(), trabalhoProducaoRecebido.getNome() + " foi modificado com sucesso!", Snackbar.LENGTH_LONG).show();
                    voltaParaListaTrabalhosProducao();
                    break;
                case CODIGO_TRABALHO_FEITO:
                    modificaEstoque(trabalhoModificado);
                    modificaProfissao(trabalhoModificado);
                    break;
            }
            return;
        }
        voltaParaListaTrabalhosProducao();
    }

    private void modificaProfissao(TrabalhoProducao trabalho) {
        profissaoViewModel.recuperaProfissoes().observe(getViewLifecycleOwner(), resultadoProfissoes -> {
            if (resultadoProfissoes.getDado() != null) {
                Profissao profissaoEncontrada = profissaoViewModel.retornaProfissaoModificada(resultadoProfissoes.getDado(), trabalho);
                if (profissaoEncontrada == null){
                    indicadorProgresso.setVisibility(GONE);
                    Snackbar.make(binding.getRoot(), "Profissão não encontrada: "+trabalho.getProfissao(), Snackbar.LENGTH_LONG).show();
                    voltaParaListaTrabalhosProducao();
                    return;
                }
                if (profissaoEncontrada.getExperiencia() < 996000) {
                    int novaExperiencia = profissaoEncontrada.getExperiencia()+ trabalho.getExperiencia();
                    profissaoEncontrada.setExperiencia(novaExperiencia);
                    profissaoViewModel.modificaExperienciaProfissao(profissaoEncontrada).observe(getViewLifecycleOwner(), resultadoModificaExperiencia -> {
                        indicadorProgresso.setVisibility(GONE);
                        if (resultadoModificaExperiencia.getErro() == null){
                            if (Boolean.TRUE.equals(confirmacao.getValue())) {
                                Snackbar.make(binding.getRoot(), trabalhoProducaoRecebido.getNome() + " foi modificado com sucesso!", Snackbar.LENGTH_LONG).show();
                                voltaParaListaTrabalhosProducao();
                            }
                            return;
                        }
                        confirmacao.setValue(false);
                        Snackbar.make(binding.getRoot(), "Erro: "+resultadoModificaExperiencia.getErro(), Snackbar.LENGTH_LONG).show();
                    });
                }
                return;
            }
            Snackbar.make(binding.getRoot(), "Erro: "+resultadoProfissoes.getErro(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void modificaEstoque(TrabalhoProducao trabalhoModificado) {
        trabalhoEstoqueViewModel.recuperaTrabalhoEstoquePorIdTrabalho(trabalhoModificado.getIdTrabalho()).observe(getViewLifecycleOwner(), resultadoTrabalhoEncontrado -> {
            if (resultadoTrabalhoEncontrado.getErro() == null) {
                TrabalhoEstoque trabalhoEncontrado = resultadoTrabalhoEncontrado.getDado();
                if (trabalhoEncontrado == null) {
                    if (!trabalhoModificado.ehProducaoDeRecursos()) {
                        TrabalhoEstoque novoTrabalhoEstoque = new TrabalhoEstoque();
                        novoTrabalhoEstoque.setIdTrabalho(trabalhoModificado.getIdTrabalho());
                        novoTrabalhoEstoque.setQuantidade(1);
                        trabalhoEstoqueViewModel.insereTrabalhoEstoque(novoTrabalhoEstoque).observe(getViewLifecycleOwner(), resultaSalvaTrabalhoEstoque -> {
                            if (resultaSalvaTrabalhoEstoque.getErro() == null) return;
                            Snackbar.make(binding.getRoot(), "Erro: " + resultaSalvaTrabalhoEstoque.getErro(), Snackbar.LENGTH_LONG).show();
                            confirmacao.setValue(false);
                        });
                    }
                    return;
                }
                trabalhoEncontrado.setQuantidade(trabalhoEncontrado.getQuantidade()+1);
                trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEncontrado).observe(getViewLifecycleOwner(), resultaModificaQuantidade -> {
                    if (resultaModificaQuantidade.getErro() == null) return;
                    Snackbar.make(binding.getRoot(), "Erro: "+resultaModificaQuantidade.getErro(), Snackbar.LENGTH_LONG).show();
                    confirmacao.setValue(false);
                });
            }
        });
    }

    private void voltaParaListaTrabalhosProducao() {
        controlador.navigate(vaiParaListaTrabalhosProducao());
    }

    private void inicializaComponentes() {
        linearLayoutTrabalhoNecessario2 = binding.linearLayoutTrabalhoNecessario2;
        linearLayoutTrabalhoNecessario3 = binding.linearLayoutTrabalhoNecessario3;
        edtNomeTrabalho = binding.edtNomeTrabalho;
        edtNomeProducaoTrabalho = binding.edtNomeProducaoTrabalho;
        edtNivelTrabalho = binding.edtNivelTrabalho;
        edtExperienciaTrabalho = binding.edtExperienciaTrabalho;

        txtInputEstado = binding.txtLayoutEstadoTrabalho;
        txtInputLicenca = binding.txtLayoutLicencaTrabalho;
        txtInputNome = binding.txtLayoutNomeTrabalho;
        txtInputNomeProducao = binding.txtLayoutNomeProducaoTrabalho;
        txtInputProfissao = binding.txtLayoutProfissaoTrabalho;
        txtInputExperiencia = binding.txtLayoutExperienciaTrabalho;
        txtInputNivel = binding.txtLayoutNivelTrabalho;
        txtInputRaridade = binding.txtLayoutRaridadeTrabalho;

        autoCompleteProfissao = binding.txtAutoCompleteProfissaoTrabalho;
        autoCompleteRaridade = binding.txtAutoCompleteRaridadeTrabalho;
        autoCompleteTrabalhoNecessario1 = binding.txtAutoCompleteTrabalhoNecessario;
        autoCompleteTrabalhoNecessario2 = binding.txtAutoCompleteTrabalhoNecessario2;
        autoCompleteLicenca = binding.txtAutoCompleteLicencaTrabalho;
        autoCompleteEstado = binding.txtAutoCompleteEstadoTrabalho;
        imagemTrabalhoNecessario1 = binding.imagemTrabalhoNecessario1;
        imagemTrabalhoNecessario2 = binding.imagemTrabalhoNecessario2;

        checkBoxRecorrenciaTrabalho = binding.checkBoxRecorrenciaTrabalho;
        indicadorProgresso = binding.indicadorProgressoTrabalhoEspecifico;
        btnExcluir = binding.btnExcluiTrabalhoEspecifico;

        estadosTrabalho = getResources().getStringArray(R.array.estados);

        TrabalhoViewModelFactory trabalhoViewModelFactory = new TrabalhoViewModelFactory(new TrabalhoRepository(getContext()));
        trabalhoViewModel = new ViewModelProvider(requireActivity(), trabalhoViewModelFactory).get(TrabalhoViewModel.class);
        confirmacao = new MutableLiveData<>(true);
        controlador = Navigation.findNavController(binding.getRoot());
    }
    private void recebeDados() {
        codigoRequisicao = TrabalhoEspecificoFragmentArgs.fromBundle(getArguments()).getCodigoRequisicao();
        if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO){
            trabalhoRecebido = TrabalhoEspecificoFragmentArgs.fromBundle(getArguments()).getTrabalho();
            return;
        }
        if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO) {
            trabalhoProducaoRecebido = TrabalhoEspecificoFragmentArgs.fromBundle(getArguments()).getTrabalhoProducao();
        }
    }

    private void configuraLayoutModificaTrabalhoProducao() {
        if (trabalhoProducaoRecebido == null) return;
        String idPersonagem = TrabalhoEspecificoFragmentArgs.fromBundle(getArguments()).getIdPersonagem();
        if (idPersonagem == null) return;
        TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(idPersonagem);
        trabalhoProducaoViewModel = new ViewModelProvider(requireActivity(), trabalhoProducaoViewModelFactory).get(idPersonagem, TrabalhoProducaoViewModel.class);
        TrabalhoEstoqueViewModelFactory trabalhoEstoqueViewModelFactory = new TrabalhoEstoqueViewModelFactory(new TrabalhoEstoqueRepository(idPersonagem));
        trabalhoEstoqueViewModel = new ViewModelProvider(requireActivity(), trabalhoEstoqueViewModelFactory).get(idPersonagem, TrabalhoEstoqueViewModel.class);
        ProfissaoViewModelFactory profissaoViewModelFactory = new ProfissaoViewModelFactory(idPersonagem);
        profissaoViewModel = new ViewModelProvider(requireActivity(), profissaoViewModelFactory).get(idPersonagem, ProfissaoViewModel.class);
        configuraComponentesAlteraTrabalhoProducao();
    }

    private void configuraLayoutModificaTrabalho() {
        if (trabalhoRecebido == null) return;
        configuraComponentesAlteraTrabalho();
        configuraBotaoExcluiTrabalhoEspecifico();
    }

    private void configuraAcaoImagem() {
        imagemTrabalhoNecessario1.setOnClickListener(view -> linearLayoutTrabalhoNecessario3.setVisibility(View.VISIBLE));
        imagemTrabalhoNecessario2.setOnClickListener(view -> {
            autoCompleteTrabalhoNecessario2.setText("");
            linearLayoutTrabalhoNecessario3.setVisibility(GONE);
        });
    }
    private void defineTrabalhoNecessario() {
        String trabalhoNecessario1 = Objects.requireNonNull(autoCompleteTrabalhoNecessario1).getText().toString().trim();
        String trabalhoNecessario2 = Objects.requireNonNull(autoCompleteTrabalhoNecessario2).getText().toString().trim();
        trabalhoNecessario = "";
        String idTrabalhoNecessario1 = "", idTrabalhoNecessario2 = "";
        if (!trabalhoNecessario1.isEmpty()){
            for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
                if (trabalhoEncontrado.getNome().contains(trabalhoNecessario1)) {
                    idTrabalhoNecessario1 = trabalhoEncontrado.getId();
                    break;
                }
            }
        }
        if (!trabalhoNecessario2.isEmpty()){
            for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
                if (trabalhoEncontrado.getNome().contains(trabalhoNecessario2)) {
                    idTrabalhoNecessario2 = trabalhoEncontrado.getId();
                    break;
                }
            }
        }
        if (!idTrabalhoNecessario1.isEmpty() && !idTrabalhoNecessario2.isEmpty()) {
            trabalhoNecessario = idTrabalhoNecessario1 + "," + idTrabalhoNecessario2;
            return;
        }
        if (!idTrabalhoNecessario1.isEmpty()) {
            trabalhoNecessario = idTrabalhoNecessario1;
            return;
        }
        if (!idTrabalhoNecessario2.isEmpty()) {
            trabalhoNecessario = idTrabalhoNecessario2;
        }
    }

    private void configuraBotaoExcluiTrabalhoEspecifico() {
        TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory("");
        trabalhoProducaoViewModel = new ViewModelProvider(requireActivity(), trabalhoProducaoViewModelFactory).get(TrabalhoProducaoViewModel.class);
        TrabalhoEstoqueViewModelFactory trabalhoEstoqueViewModelFactory = new TrabalhoEstoqueViewModelFactory(new TrabalhoEstoqueRepository());
        trabalhoEstoqueViewModel = new ViewModelProvider(requireActivity(), trabalhoEstoqueViewModelFactory).get(TrabalhoEstoqueViewModel.class);
        TrabalhosVendidosViewModelFactory trabalhosVendidosViewModelFactory= new TrabalhosVendidosViewModelFactory(new TrabalhoVendidoRepository());
        TrabalhosVendidosViewModel trabalhosVendidosViewModel= new ViewModelProvider(requireActivity(), trabalhosVendidosViewModelFactory).get(TrabalhosVendidosViewModel.class);
        btnExcluir.setOnClickListener(v -> {
            indicadorProgresso.setVisibility(View.VISIBLE);
            trabalhoViewModel.removeTrabalhoEspecificoServidor(trabalhoRecebido).observe(getViewLifecycleOwner(), resultado -> {
                indicadorProgresso.setVisibility(GONE);
                if (resultado.getErro() == null) {
                    trabalhoProducaoViewModel.removeReferenciaTrabalhoEspecifico(trabalhoRecebido).observe(getViewLifecycleOwner(), resultadoRemoveProducao -> {
                        if (resultadoRemoveProducao.getErro() != null) Snackbar.make(binding.getRoot(), "Erro: "+ resultadoRemoveProducao.getErro(), Snackbar.LENGTH_LONG).show();
                    });
                    trabalhoEstoqueViewModel.removeReferenciaTrabalhoEspecifico(trabalhoRecebido).observe(getViewLifecycleOwner(), resultadoRemoveEstoque -> {
                        if (resultadoRemoveEstoque.getErro() != null) Snackbar.make(binding.getRoot(), "Erro: " + resultadoRemoveEstoque.getErro(), Snackbar.LENGTH_LONG).show();
                    });
                    trabalhosVendidosViewModel.removeReferenciaTrabalhoEspecfico(trabalhoRecebido);
                    voltaParaListaTrabalhos();
                    return;
                }
                Snackbar.make(binding.getRoot(), "Erro: "+resultado.getErro(), Snackbar.LENGTH_LONG).show();
            });
        });
    }
    private void configuraDropdownProfissoes() {
        String[] profissoesTrabalho = getResources().getStringArray(R.array.profissoes);
        ArrayAdapter<String> profissoesAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, profissoesTrabalho);
        profissoesAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        if (trabalhoRecebido != null) autoCompleteProfissao.setText(trabalhoRecebido.getProfissao());
        autoCompleteProfissao.setAdapter(profissoesAdapter);
        autoCompleteProfissao.setOnItemClickListener((adapterView, view, i, l) -> {
            String raridadeSelecionada = autoCompleteRaridade.getText().toString();
            Log.d("trabalho", "raridade selecionada: " + raridadeSelecionada);
            configuraCampoTrabalhoNecessario(raridadeSelecionada);
        });
    }
    private void configuraDropdownRaridades() {
        String[] raridadesTrabalho = getResources().getStringArray(R.array.raridades);
        ArrayAdapter<String> raridadeAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, raridadesTrabalho);
        raridadeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        if (trabalhoRecebido != null) autoCompleteRaridade.setText(trabalhoRecebido.getRaridade());
        autoCompleteRaridade.setAdapter(raridadeAdapter);
        configuraCliqueItemRaridade();
    }

    private void configuraCliqueItemRaridade() {
        autoCompleteRaridade.setOnItemClickListener((adapterView, view, i, l) -> {
            String raridadeClicada = adapterView.getAdapter().getItem(i).toString();
            configuraCampoTrabalhoNecessario(raridadeClicada);
        });
    }

    private void configuraCampoTrabalhoNecessario(String raridadeClicada) {
        if (raridadeEhMelhoraroOuRaro(raridadeClicada)) {
            linearLayoutTrabalhoNecessario2.setVisibility(View.VISIBLE);
            configuraDropdownTrabalhoNecessario();
            return;
        }
        linearLayoutTrabalhoNecessario2.setVisibility(GONE);
        linearLayoutTrabalhoNecessario3.setVisibility(GONE);
    }

    private boolean raridadeEhMelhoraroOuRaro(String raridadeClicada) {
        return comparaString(raridadeClicada, getString(R.string.stringMelhorado)) || comparaString(raridadeClicada, getString(R.string.stringRaro));
    }

    private void configuraDropdownTrabalhoNecessario() {
        ArrayList<String> stringTrabalhosNecessarios = new ArrayList<>();
        stringTrabalhosNecessarios.add(getString(R.string.stringNadaEncontrado));
        recuperaTrabalhosNecessarios(stringTrabalhosNecessarios);
    }

    private void configuraAdapterTrabalhoNecessario(ArrayList<String> stringTrabalhosNecessarios) {
        ArrayAdapter<String> trabalhoNecessarioAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, stringTrabalhosNecessarios);
        trabalhoNecessarioAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        autoCompleteTrabalhoNecessario1.setAdapter(trabalhoNecessarioAdapter);
        autoCompleteTrabalhoNecessario2.setAdapter(trabalhoNecessarioAdapter);
    }

    private void recuperaTrabalhosNecessarios(ArrayList<String> stringTrabalhosNecessarios) {
        Trabalho trabalho = defineTrabalhoBusca();
        trabalhoViewModel.pegaTrabalhosNecessarios(trabalho).observe(getViewLifecycleOwner(), resultadoPegaTrabalhosNecessarios -> {
            if (resultadoPegaTrabalhosNecessarios.getErro() == null) {
                trabalhosNecessarios = resultadoPegaTrabalhosNecessarios.getDado();
                if (trabalhosNecessarios.isEmpty()) return;
                stringTrabalhosNecessarios.clear();
                stringTrabalhosNecessarios.add(getString(R.string.stringSelecioneTrabalho));
                for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
                    stringTrabalhosNecessarios.add(trabalhoEncontrado.getNome());
                }
                preencheCamposTrabalhosNecessarios();
                configuraAdapterTrabalhoNecessario(stringTrabalhosNecessarios);
            }
        });
    }

    private void preencheCamposTrabalhosNecessarios() {
        if (trabalhoRecebido == null || trabalhoRecebido.getTrabalhoNecessario() == null) return;
        String[] idTrabalhosNecessarios = trabalhoRecebido.getTrabalhoNecessario().split(",");
        for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
            if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[0])) {
                autoCompleteTrabalhoNecessario1.setText(trabalhoEncontrado.getNome());
                break;
            }
        }
        if (idTrabalhosNecessarios.length > 1) {
            for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
                if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[1])) {
                    autoCompleteTrabalhoNecessario2.setText(trabalhoEncontrado.getNome());
                    break;
                }
            }
            binding.linearLayoutTrabalhoNecessario3.setVisibility(View.VISIBLE);
        }
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
        Trabalho trabalho = new Trabalho();
        String stringNivel = edtNivelTrabalho.getText().toString().trim();
        int nivel = stringNivel.isEmpty() ? 0 : Integer.parseInt(stringNivel);
        trabalho.setNivel(nivel);
        return trabalho;
    }

    private void configuraDropdownLicencas() {
        String[] licencasTrabalho = getResources().getStringArray(R.array.licencas_completas);
        ArrayAdapter<String> adapterLicenca= new ArrayAdapter<>(requireContext(),
                R.layout.item_dropdrown, licencasTrabalho);
        adapterLicenca.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        autoCompleteLicenca.setAdapter(adapterLicenca);
        autoCompleteLicenca.setOnItemClickListener((adapterView, view, i, l) -> {
            if (comparaString(autoCompleteLicenca.getText().toString(), getString(R.string.licencaIniciante))) {
                if (acrescimo) return;
                int novaExperiencia = (int) (1.50 * Integer.parseInt(Objects.requireNonNull(edtExperienciaTrabalho.getText()).toString()));
                edtExperienciaTrabalho.setText(String.valueOf(novaExperiencia));
                acrescimo = true;
                return;
            }
            if (acrescimo){
                int novaExperiencia = (int) ((Integer.parseInt(Objects.requireNonNull(edtExperienciaTrabalho.getText()).toString())) / 1.5);
                edtExperienciaTrabalho.setText(String.valueOf(novaExperiencia));
                acrescimo = false;
            }
        });
    }
    private void configuraDropdownEstados() {
        adapterEstado= new ArrayAdapter<>(requireContext(),
                R.layout.item_dropdrown, estadosTrabalho);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteEstado.setAdapter(adapterEstado);
    }
    private void configuraComponentesAlteraTrabalho() {
        checkBoxRecorrenciaTrabalho.setVisibility(GONE);
        txtInputLicenca.setVisibility(GONE);
        txtInputEstado.setVisibility(GONE);
        btnExcluir.setVisibility(View.VISIBLE);
        edtNomeTrabalho.setText(trabalhoRecebido.getNome());
        edtNomeProducaoTrabalho.setText(trabalhoRecebido.getNomeProducao());
        edtExperienciaTrabalho.setText(String.valueOf(trabalhoRecebido.getExperiencia()));
        edtNivelTrabalho.setText(String.valueOf(trabalhoRecebido.getNivel()));
        configuraCampoTrabalhoNecessario(trabalhoRecebido.getRaridade());
    }

    private void configuraLayoutNovoTrabalho() {
        linearLayoutTrabalhoNecessario2.setVisibility(GONE);
        txtInputLicenca.setVisibility(GONE);
        txtInputEstado.setVisibility(GONE);
        checkBoxRecorrenciaTrabalho.setVisibility(GONE);
        configuraDropdownTrabalhoNecessario();
    }

    private void configuraComponentesAlteraTrabalhoProducao() {
        desativaCamposTrabalhoProducao();
        if (comparaString(trabalhoProducaoRecebido.getTipoLicenca(), getString(R.string.licencaIniciante))) {
            acrescimo = true;
        }
        defineValoresCamposTrabalhoProducao();
    }

    private void defineValoresCamposTrabalhoProducao() {
        edtNomeTrabalho.setText(trabalhoProducaoRecebido.getNome());
        edtNomeProducaoTrabalho.setText(trabalhoProducaoRecebido.getNomeProducao());
        autoCompleteProfissao.setText(trabalhoProducaoRecebido.getProfissao());
        int experiencia= trabalhoProducaoRecebido.getExperiencia();
        if (trabalhoProducaoRecebido.getTipoLicenca().equals(getString(R.string.licencaIniciante))) experiencia= (int) (1.5* experiencia);
        edtExperienciaTrabalho.setText(String.valueOf(experiencia));
        edtNivelTrabalho.setText(String.valueOf(trabalhoProducaoRecebido.getNivel()));
        autoCompleteRaridade.setText(trabalhoProducaoRecebido.getRaridade());
        checkBoxRecorrenciaTrabalho.setChecked(trabalhoProducaoRecebido.getRecorrencia());
        autoCompleteLicenca.setText(trabalhoProducaoRecebido.getTipoLicenca());
        autoCompleteEstado.setText(estadosTrabalho[trabalhoProducaoRecebido.getEstado()]);
        defineValoresCamposTrabalhosNecessarios();
    }

    private void defineValoresCamposTrabalhosNecessarios() {
        if (valorTrabalhoNecessarioExiste()) {
            String[] idTrabalhosNecessarios = trabalhoProducaoRecebido.getTrabalhoNecessario().split(",");
            int tamanhoLista= idTrabalhosNecessarios.length;
            linearLayoutTrabalhoNecessario2.setVisibility(View.VISIBLE);
            trabalhoViewModel.pegaTrabalhoPorId(idTrabalhosNecessarios[0]).observe(getViewLifecycleOwner(), resultadoBusca -> {
                if (resultadoBusca.getDado() == null){
                    autoCompleteTrabalhoNecessario1.setText(getString(R.string.string_nao_encontrado));
                    Snackbar.make(binding.getRoot(), "Erro: " + resultadoBusca.getErro(), Snackbar.LENGTH_LONG).show();
                    return;
                }
                autoCompleteTrabalhoNecessario1.setText(resultadoBusca.getDado().getNome());
            });
            if (tamanhoLista > 1){
                linearLayoutTrabalhoNecessario3.setVisibility(View.VISIBLE);
                trabalhoViewModel.pegaTrabalhoPorId(idTrabalhosNecessarios[1]).observe(getViewLifecycleOwner(), resultadoBusca -> {
                    if (resultadoBusca.getDado() == null){
                        autoCompleteTrabalhoNecessario2.setText(getString(R.string.string_nao_encontrado));
                        Snackbar.make(binding.getRoot(), "Erro: " + resultadoBusca.getErro(), Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    autoCompleteTrabalhoNecessario2.setText(resultadoBusca.getDado().getNome());
                });
            }
        }
    }

    private boolean valorTrabalhoNecessarioExiste() {
        return trabalhoProducaoRecebido.getTrabalhoNecessario() != null && !trabalhoProducaoRecebido.getTrabalhoNecessario().isEmpty();
    }

    private void desativaCamposTrabalhoProducao() {
        edtNomeTrabalho.setEnabled(false);
        edtNomeProducaoTrabalho.setEnabled(false);
        autoCompleteProfissao.setEnabled(false);
        binding.txtLayoutProfissaoTrabalho.setEndIconVisible(false);
        edtExperienciaTrabalho.setEnabled(false);
        edtNivelTrabalho.setEnabled(false);
        autoCompleteRaridade.setEnabled(false);
        binding.txtLayoutRaridadeTrabalho.setEndIconVisible(false);
        autoCompleteTrabalhoNecessario1.setEnabled(false);
        binding.txtLayoutTrabalhoNecessario.setEndIconVisible(false);
        imagemTrabalhoNecessario1.setVisibility(GONE);
        autoCompleteTrabalhoNecessario2.setEnabled(false);
        binding.txtLayoutTrabalhoNecessario2.setEndIconVisible(false);
        imagemTrabalhoNecessario2.setVisibility(GONE);
    }

    @NonNull
    private TrabalhoProducao defineTrabalhoProducaoModificado() {
        TrabalhoProducao trabalhoModificado = new TrabalhoProducao();
        trabalhoModificado.setId(trabalhoProducaoRecebido.getId());
        trabalhoModificado.setProfissao(trabalhoProducaoRecebido.getProfissao());
        trabalhoModificado.setIdTrabalho(trabalhoProducaoRecebido.getIdTrabalho());
        trabalhoModificado.setRecorrencia(checkBoxRecorrenciaTrabalho.isChecked());
        trabalhoModificado.setTipoLicenca(autoCompleteLicenca.getText().toString());
        trabalhoModificado.setExperiencia(trabalhoProducaoRecebido.getExperiencia());
        trabalhoModificado.setTrabalhoNecessario(trabalhoProducaoRecebido.getTrabalhoNecessario());
        trabalhoModificado.setEstado(adapterEstado.getPosition(autoCompleteEstado.getText().toString()));
        return trabalhoModificado;
    }

    @NonNull
    private Trabalho defineTrabalhoModificado() {
        Trabalho trabalho = new Trabalho();
        trabalho.setId(trabalhoRecebido.getId());
        trabalho.setNome(Objects.requireNonNull(edtNomeTrabalho.getText()).toString().trim());
        trabalho.setNomeProducao(Objects.requireNonNull(edtNomeProducaoTrabalho.getText()).toString().trim());
        trabalho.setProfissao(Objects.requireNonNull(autoCompleteProfissao).getText().toString().trim());
        trabalho.setRaridade(Objects.requireNonNull(autoCompleteRaridade).getText().toString().trim());
        trabalho.setTrabalhoNecessario(trabalhoNecessario);
        trabalho.setNivel(Integer.valueOf(Objects.requireNonNull(edtNivelTrabalho.getText()).toString().trim()));
        trabalho.setExperiencia(Integer.valueOf(Objects.requireNonNull(edtExperienciaTrabalho.getText()).toString().trim()));
        return trabalho;
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
        return !trabalhoNecessario.equals(trabalhoRecebido.getTrabalhoNecessario());
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

    private boolean verificaTrabalhoProducaoModificado(TrabalhoProducao trabalhoModificado) {
        return licencaTrabalhoProducaoEhModificado(trabalhoModificado) ||
            estadoTrabalhoProducaoEhModificado(trabalhoModificado) ||
            verificaCheckModificado(trabalhoModificado);
    }

    private boolean licencaTrabalhoProducaoEhModificado(TrabalhoProducao trabalhoModificado) {
        return !comparaString(trabalhoProducaoRecebido.getTipoLicenca(), trabalhoModificado.getTipoLicenca());
    }

    private boolean estadoTrabalhoProducaoEhModificado(TrabalhoProducao trabalhoModificado) {
        return !trabalhoModificado.getEstado().equals(trabalhoProducaoRecebido.getEstado());
    }

    private boolean verificaCheckModificado(TrabalhoProducao trabalhoModificado) {

        return trabalhoModificado.getRecorrencia() != trabalhoProducaoRecebido.getRecorrencia();
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
        trabalho.setNome(Objects.requireNonNull(edtNomeTrabalho.getText()).toString().trim());
        trabalho.setNomeProducao(Objects.requireNonNull(edtNomeProducaoTrabalho.getText()).toString().trim());
        trabalho.setProfissao(Objects.requireNonNull(autoCompleteProfissao).getText().toString().trim());
        trabalho.setRaridade(Objects.requireNonNull(autoCompleteRaridade).getText().toString().trim());
        trabalho.setTrabalhoNecessario(trabalhoNecessario);
        trabalho.setNivel(Integer.valueOf(Objects.requireNonNull(edtNivelTrabalho.getText()).toString().trim()));
        trabalho.setExperiencia(Integer.valueOf(Objects.requireNonNull(edtExperienciaTrabalho.getText()).toString().trim()));
        return trabalho;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (codigoRequisicao == CODIGO_REQUISICAO_INVALIDA) return;
        if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO || codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO) {
            configuraDropdownProfissoes();
            configuraDropdownRaridades();
            if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO){
                configuraDropdownTrabalhoNecessario();
                configuraLayoutNovoTrabalho();
                return;
            }
            if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO){
                configuraLayoutModificaTrabalho();
            }
            return;
        }
        if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO) {
            configuraLayoutModificaTrabalhoProducao();
            configuraDropdownLicencas();
            configuraDropdownEstados();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}