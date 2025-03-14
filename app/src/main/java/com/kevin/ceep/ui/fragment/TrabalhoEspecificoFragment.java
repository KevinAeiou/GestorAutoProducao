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
import com.kevin.ceep.repository.ProfissaoRepository;
import com.kevin.ceep.repository.TrabalhoEstoqueRepository;
import com.kevin.ceep.repository.TrabalhoProducaoRepository;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoViewModel;
import com.kevin.ceep.ui.viewModel.factory.ProfissaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoEstoqueViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoProducaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoViewModelFactory;

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
    private ArrayAdapter<String> adapterEstado, trabalhoNecessarioAdapter;
    private final String[] mensagemErro={"Campo requerido!","Inválido!"};
    private String trabalhoNecessario;
    private int codigoRequisicao = CODIGO_REQUISICAO_INVALIDA;
    private boolean acrescimo = false;
    private TrabalhoViewModel trabalhoViewModel;
    private TrabalhoProducaoViewModel trabalhoProducaoViewModel;
    private TrabalhoEstoqueViewModel trabalhoEstoqueViewModel;
    private ProfissaoViewModel profissaoViewModel;
    private ArrayList<Trabalho> trabalhosNecessarios = new ArrayList<>();
    private MutableLiveData<Boolean> confirmacao = new MutableLiveData<>(true);
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
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuConfirma = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        recebeDados();
        inicializaComponentes();
        configuraAcaoImagem();
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
            trabalhoViewModel.insereTrabalho(trabalho).observe(this, resultadoInsereTrabalho -> {
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
            trabalhoViewModel.modificaTrabalho(trabalho).observe(this, resultadoModificaTrabalho -> {
                indicadorProgresso.setVisibility(GONE);
                if (resultadoModificaTrabalho.getErro() == null) {
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
        trabalhoProducaoViewModel.modificaTrabalhoProducao(trabalhoModificado).observe(this, resultado -> {
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
                    voltaParaListaTrabalhosProducao();
                    break;
                case CODIGO_TRABALHO_PRODUZINDO:
                    if (trabalhoModificado.possueTrabalhoNecessarioValido()) {
                        String[] listaIdsTrabalhosNecessarios = trabalhoModificado.getTrabalhoNecessario().split(",");
                        for (String idTrabalho : listaIdsTrabalhosNecessarios) {
                            TrabalhoEstoque trabalhoEstoqueEncontrado = trabalhoEstoqueViewModel.pegaTrabalhoEstoquePorIdTrabalho(idTrabalho);
                            if (trabalhoEstoqueEncontrado == null) {
                                break;
                            }
                            trabalhoEstoqueEncontrado.setQuantidade(trabalhoEstoqueEncontrado.getQuantidade() - 1);
                            trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEstoqueEncontrado);
                        }
                    }
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

    private void modificaProfissao(TrabalhoProducao trabalhoModificado) {
        profissaoViewModel.pegaTodasProfissoes().observe(this, resultadoProfissoes -> {
            if (resultadoProfissoes.getDado() != null) {
                indicadorProgresso.setVisibility(GONE);
                Profissao profissaoEncontrada = profissaoViewModel.retornaProfissaoModificada(resultadoProfissoes.getDado(), trabalhoModificado);
                if (profissaoEncontrada == null){
                    Snackbar.make(binding.getRoot(), "Profissão não encontrada: "+trabalhoModificado.getProfissao(), Snackbar.LENGTH_LONG).show();
                    voltaParaListaTrabalhosProducao();
                    return;
                }
                if (profissaoEncontrada.getExperiencia() < 830000) {
                    int novaExperiencia = profissaoEncontrada.getExperiencia()+ trabalhoModificado.getExperiencia();
                    profissaoEncontrada.setExperiencia(novaExperiencia);
                    profissaoViewModel.modificaExperienciaProfissao(profissaoEncontrada).observe(this, resultadoModificaExperiencia -> {
                        if (resultadoModificaExperiencia.getErro() == null) return;
                        Snackbar.make(binding.getRoot(), "Erro: "+resultadoModificaExperiencia.getErro(), Snackbar.LENGTH_LONG).show();
                        confirmacao.setValue(false);
                        if (Boolean.TRUE.equals(confirmacao.getValue())) {
                            voltaParaListaTrabalhosProducao();
                        }
                    });
                }
                return;
            }
            Snackbar.make(binding.getRoot(), "Erro: "+resultadoProfissoes.getErro(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void modificaEstoque(TrabalhoProducao trabalhoModificado) {
        TrabalhoEstoque trabalhoEstoqueEncontrado = trabalhoEstoqueViewModel.pegaTrabalhoEstoquePorIdTrabalho(trabalhoModificado.getIdTrabalho());
        if (trabalhoEstoqueEncontrado == null) {
            if (!trabalhoModificado.ehProducaoDeRecursos()) {
                TrabalhoEstoque novoTrabalhoEstoque = new TrabalhoEstoque();
                novoTrabalhoEstoque.setIdTrabalho(trabalhoModificado.getIdTrabalho());
                novoTrabalhoEstoque.setQuantidade(1);
                trabalhoEstoqueViewModel.insereTrabalhoEstoque(novoTrabalhoEstoque).observe(this, resultaSalvaTrabalhoEstoque -> {
                    if (resultaSalvaTrabalhoEstoque.getErro() == null) return;
                    Snackbar.make(binding.getRoot(), "Erro: " + resultaSalvaTrabalhoEstoque.getErro(), Snackbar.LENGTH_LONG).show();
                    confirmacao.setValue(false);
                });
            }
            return;
        }
        trabalhoEstoqueEncontrado.setQuantidade(trabalhoEstoqueEncontrado.getQuantidade()+1);
        trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEstoqueEncontrado).observe(this, resultaModificaQuantidade -> {
            if (resultaModificaQuantidade.getErro() == null) return;
            Snackbar.make(binding.getRoot(), "Erro: "+resultaModificaQuantidade.getErro(), Snackbar.LENGTH_LONG).show();
            confirmacao.setValue(false);
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
        String personagemId = TrabalhoEspecificoFragmentArgs.fromBundle(getArguments()).getIdPersonagem();
        TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(new TrabalhoProducaoRepository(requireContext(), personagemId));
        trabalhoProducaoViewModel = new ViewModelProvider(requireActivity(), trabalhoProducaoViewModelFactory).get(TrabalhoProducaoViewModel.class);
        TrabalhoEstoqueViewModelFactory trabalhoEstoqueViewModelFactory = new TrabalhoEstoqueViewModelFactory(new TrabalhoEstoqueRepository(requireContext(), personagemId));
        trabalhoEstoqueViewModel = new ViewModelProvider(requireActivity(), trabalhoEstoqueViewModelFactory).get(TrabalhoEstoqueViewModel.class);
        ProfissaoViewModelFactory profissaoViewModelFactory = new ProfissaoViewModelFactory(new ProfissaoRepository(personagemId));
        profissaoViewModel = new ViewModelProvider(requireActivity(), profissaoViewModelFactory).get(ProfissaoViewModel.class);
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
        btnExcluir.setOnClickListener(v -> {
            indicadorProgresso.setVisibility(View.VISIBLE);
            trabalhoViewModel.excluiTrabalhoEspecificoServidor(trabalhoRecebido).observe(getViewLifecycleOwner(), resultado -> {
                indicadorProgresso.setVisibility(GONE);
                if (resultado.getErro() == null) {
//                    voltaParaListaTrabalhos();
                    Snackbar.make(binding.getRoot(), "Trabalho "+trabalhoRecebido.getNome() + " removido com sucesso!", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(binding.getRoot(), "Erro: "+resultado.getErro(), Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }
    private void configuraDropdownProfissoes() {
        String[] profissoesTrabalho = getResources().getStringArray(R.array.profissoes);
        ArrayAdapter<String> profissoesAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, profissoesTrabalho);
        profissoesAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        autoCompleteProfissao.setAdapter(profissoesAdapter);
    }
    private void configuraDropdownRaridades() {
        String[] raridadesTrabalho = getResources().getStringArray(R.array.raridades);
        ArrayAdapter<String> raridadeAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, raridadesTrabalho);
        raridadeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        autoCompleteRaridade.setAdapter(raridadeAdapter);
        autoCompleteRaridade.setOnItemClickListener((adapterView, view, i, l) -> {
            String raridadeClicada = adapterView.getAdapter().getItem(i).toString();
            if (comparaString(raridadeClicada, "Melhorado") || comparaString(raridadeClicada, "Raro")) {
                linearLayoutTrabalhoNecessario2.setVisibility(View.VISIBLE);
                configuraDropdownTrabalhoNecessario();
            } else {
                linearLayoutTrabalhoNecessario2.setVisibility(GONE);
            }
        });
    }

    private void configuraDropdownTrabalhoNecessario() {
        autoCompleteTrabalhoNecessario1.setText("");
        autoCompleteTrabalhoNecessario2.setText("");
        ArrayList<String> stringTrabalhosNecessarios = new ArrayList<>();
        String raridade = Objects.requireNonNull(autoCompleteRaridade).getText().toString().trim();
        Trabalho trabalho = new Trabalho();
        if (Objects.requireNonNull(edtNivelTrabalho.getText()).toString().trim().isEmpty()) {
            trabalho.setNivel(0);
        } else {
            trabalho.setNivel(Integer.valueOf(Objects.requireNonNull(edtNivelTrabalho.getText()).toString().trim()));
        }
        trabalho.setProfissao(Objects.requireNonNull(autoCompleteProfissao).getText().toString().trim());
        if (comparaString(raridade, "melhorado")) {
            trabalho.setRaridade("Comum");
        } else if (comparaString(raridade, "raro")) {
            trabalho.setRaridade("Melhorado");
        } else {
            trabalho.setRaridade("");
        }
        trabalhoViewModel.pegaTrabalhosNecessarios(trabalho).observe(getViewLifecycleOwner(), resultadoPegaTrabalhosNecessarios -> {
            if (resultadoPegaTrabalhosNecessarios.getDado() != null) {
                trabalhosNecessarios = resultadoPegaTrabalhosNecessarios.getDado();
                if (trabalhosNecessarios.isEmpty()){
                    stringTrabalhosNecessarios.add("Nada encontrado");
                    return;
                }
                if (trabalhoRecebido != null) {
                    String[] idTrabalhosNecessarios = trabalhoRecebido.getTrabalhoNecessario().split(",");
                    String nomeTrabalhoNecessario1 = "", nomeTrabalhoNecessario2 = "";
                    for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
                        if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[0])) {
                            nomeTrabalhoNecessario1 = trabalhoEncontrado.getNome();
                            break;
                        }
                    }
                    autoCompleteTrabalhoNecessario1.setText(nomeTrabalhoNecessario1);
                    if (idTrabalhosNecessarios.length > 1) {
                        for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
                            if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[1])) {
                                nomeTrabalhoNecessario2 = trabalhoEncontrado.getNome();
                                break;
                            }
                        }
                        binding.linearLayoutTrabalhoNecessario3.setVisibility(View.VISIBLE);
                        autoCompleteTrabalhoNecessario2.setText(nomeTrabalhoNecessario2);
                    }
                }
                for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
                    stringTrabalhosNecessarios.add(trabalhoEncontrado.getNome());
                }
                trabalhoNecessarioAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, stringTrabalhosNecessarios);
                trabalhoNecessarioAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                autoCompleteTrabalhoNecessario1.setAdapter(trabalhoNecessarioAdapter);
                autoCompleteTrabalhoNecessario2.setAdapter(trabalhoNecessarioAdapter);

            }
        });
    }

    private void configuraDropdownLicencas() {
        String[] licencasTrabalho = getResources().getStringArray(R.array.licencas_completas);
        ArrayAdapter<String> adapterLicenca= new ArrayAdapter<>(requireContext(),
                R.layout.item_dropdrown, licencasTrabalho);
        adapterLicenca.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        autoCompleteLicenca.setAdapter(adapterLicenca);

        autoCompleteLicenca.setOnItemClickListener((adapterView, view, i, l) -> {
            if (comparaString(autoCompleteLicenca.getText().toString(), "licença de produção do principiante")) {
                if (!acrescimo) {
                    int novaExperiencia = (int) (1.50 * Integer.parseInt(Objects.requireNonNull(edtExperienciaTrabalho.getText()).toString()));
                    edtExperienciaTrabalho.setText(String.valueOf(novaExperiencia));
                    acrescimo = true;
                }
            } else if (acrescimo){
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
        autoCompleteProfissao.setText(trabalhoRecebido.getProfissao());
        edtExperienciaTrabalho.setText(String.valueOf(trabalhoRecebido.getExperiencia()));
        edtNivelTrabalho.setText(String.valueOf(trabalhoRecebido.getNivel()));
        autoCompleteRaridade.setText(trabalhoRecebido.getRaridade());
        configuraDropdownTrabalhoNecessario();
        if (trabalhoRecebido.getRaridade().equals("Raro") || trabalhoRecebido.getRaridade().equals("Melhorado")) {
            linearLayoutTrabalhoNecessario2.setVisibility(View.VISIBLE);
            if (trabalhoRecebido.getTrabalhoNecessario() == null || trabalhoRecebido.getTrabalhoNecessario().isEmpty()) {
                linearLayoutTrabalhoNecessario2.setVisibility(GONE);
            }
        }
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
        if (comparaString(trabalhoProducaoRecebido.getTipoLicenca(), "licença de produção do principiante")) {
            acrescimo = true;
        }
        defineValoresCamposTrabalhoProducao();
    }

    private void defineValoresCamposTrabalhoProducao() {
        edtNomeTrabalho.setText(trabalhoProducaoRecebido.getNome());
        edtNomeProducaoTrabalho.setText(trabalhoProducaoRecebido.getNomeProducao());
        autoCompleteProfissao.setText(trabalhoProducaoRecebido.getProfissao());
        edtExperienciaTrabalho.setText(String.valueOf(trabalhoProducaoRecebido.getExperiencia()));
        edtNivelTrabalho.setText(String.valueOf(trabalhoProducaoRecebido.getNivel()));
        autoCompleteRaridade.setText(trabalhoProducaoRecebido.getRaridade());
        checkBoxRecorrenciaTrabalho.setChecked(trabalhoProducaoRecebido.getRecorrencia());
        autoCompleteLicenca.setText(trabalhoProducaoRecebido.getTipoLicenca());
        autoCompleteEstado.setText(estadosTrabalho[trabalhoProducaoRecebido.getEstado()]);
        defineValoresCamposTrabalhosNecessarios();
    }

    private void defineValoresCamposTrabalhosNecessarios() {
        if (valorTrabalhoNecessarioExiste()) {
            Trabalho trabalho = new Trabalho();
            trabalho.setNivel(Integer.valueOf(Objects.requireNonNull(edtNivelTrabalho.getText()).toString()));
            trabalho.setProfissao(autoCompleteProfissao.getText().toString());
            if (comparaString(autoCompleteRaridade.getText().toString(), "melhorado")) {
                trabalho.setRaridade("Comum");
            } else if (comparaString(autoCompleteRaridade.getText().toString(), "raro")) {
                trabalho.setRaridade("Melhorado");
            } else {
                trabalho.setRaridade("");
            }
            trabalhoViewModel.pegaTrabalhosNecessarios(trabalho).observe(getViewLifecycleOwner(), resultadoPegaTrabalhosNecessarios -> {
                if (resultadoPegaTrabalhosNecessarios.getDado() != null) {
                    trabalhosNecessarios = resultadoPegaTrabalhosNecessarios.getDado();
                    if (trabalhosNecessarios.isEmpty()){
                        return;
                    }
                    String[] idTrabalhosNecessarios = trabalhoProducaoRecebido.getTrabalhoNecessario().split(",");
                    String nomeTrabalhoNecessario1 = "Não encontrado", nomeTrabalhoNecessario2 = "Não encontrado";
                    for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
                        if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[0])) {
                            nomeTrabalhoNecessario1 = trabalhoEncontrado.getNome();
                            break;
                        }
                    }
                    autoCompleteTrabalhoNecessario1.setText(nomeTrabalhoNecessario1);
                    if (idTrabalhosNecessarios.length > 1) {
                        for (Trabalho trabalhoEncontrado : trabalhosNecessarios) {
                            if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[1])) {
                                nomeTrabalhoNecessario2 = trabalhoEncontrado.getNome();
                                break;
                            }
                        }
                        binding.linearLayoutTrabalhoNecessario3.setVisibility(View.VISIBLE);
                        autoCompleteTrabalhoNecessario2.setText(nomeTrabalhoNecessario2);
                    }
                }
            });
            linearLayoutTrabalhoNecessario2.setVisibility(View.VISIBLE);
            String[] trabalhosNecessarios = trabalhoProducaoRecebido.getTrabalhoNecessario().split(",");
            if (trabalhosNecessarios.length > 1) {
                linearLayoutTrabalhoNecessario3.setVisibility(View.VISIBLE);
                autoCompleteTrabalhoNecessario1.setText(trabalhosNecessarios[0]);
                autoCompleteTrabalhoNecessario2.setText(trabalhosNecessarios[1]);
                return;
            }
            autoCompleteTrabalhoNecessario1.setText(trabalhosNecessarios[0]);
        }
    }

    private boolean valorTrabalhoNecessarioExiste() {
        return trabalhoProducaoRecebido.getTrabalhoNecessario() != null && !trabalhoProducaoRecebido.getTrabalhoNecessario().isEmpty();
    }

    private void desativaCamposTrabalhoProducao() {
        edtNomeTrabalho.setEnabled(false);
        edtNomeProducaoTrabalho.setEnabled(false);
        autoCompleteProfissao.setEnabled(false);
        edtExperienciaTrabalho.setEnabled(false);
        edtNivelTrabalho.setEnabled(false);
        autoCompleteRaridade.setEnabled(false);
        autoCompleteTrabalhoNecessario1.setEnabled(false);
        imagemTrabalhoNecessario1.setEnabled(false);
        autoCompleteTrabalhoNecessario2.setEnabled(false);
        imagemTrabalhoNecessario2.setEnabled(false);
    }

    @NonNull
    private TrabalhoProducao defineTrabalhoProducaoModificado() {
        TrabalhoProducao trabalhoModificado = new TrabalhoProducao();
        trabalhoModificado.setId(trabalhoProducaoRecebido.getId());
        trabalhoModificado.setIdTrabalho(trabalhoProducaoRecebido.getIdTrabalho());
        trabalhoModificado.setRecorrencia(checkBoxRecorrenciaTrabalho.isChecked());
        trabalhoModificado.setTipoLicenca(autoCompleteLicenca.getText().toString());
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
            inputLayout.setHelperText(mensagemErro[posicaoErro]);
            return false;
        }
        inputLayout.setHelperTextEnabled(false);
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
    public void onDestroy() {
        super.onDestroy();
        binding= null;
    }
}