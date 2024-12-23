package com.kevin.ceep.ui.activity;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_NOME_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TITULO_NOVO_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_ALTERA_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_INSERE_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_INVALIDA;
import static com.kevin.ceep.utilitario.Utilitario.comparaString;
import static com.kevin.ceep.utilitario.Utilitario.stringContemString;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.ActivityTrabalhoEspecificoBinding;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.ProfissaoRepository;
import com.kevin.ceep.repository.TrabalhoEstoqueRepository;
import com.kevin.ceep.repository.TrabalhoProducaoRepository;
import com.kevin.ceep.repository.TrabalhoRepository;
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

public class TrabalhoEspecificoActivity extends AppCompatActivity {
    private ActivityTrabalhoEspecificoBinding binding;
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
    private String licencaModificada;
    private String nome;
    private String nomeProducao;
    private String profissao;
    private String experiencia;
    private String nivel;
    private String raridade;
    private String trabalhoNecessario1, trabalhoNecessario2, trabalhoNecessario;
    private int codigoRequisicao = CODIGO_REQUISICAO_INVALIDA, posicaoEstadoModificado;
    private boolean acrescimo = false, recorrenciaModificada;
    private TrabalhoViewModel trabalhoViewModel;
    private TrabalhoProducaoViewModel trabalhoProducaoViewModel;
    private TrabalhoEstoqueViewModel trabalhoEstoqueViewModel;
    private ProfissaoViewModel profissaoViewModel;
    private ArrayList<Trabalho> todosTrabalhos;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrabalhoEspecificoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inicializaComponentes();
        recebeDadosIntent();
        configuraAcaoImagem();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.itemMenuSalvaTrabalho) {
            indicadorProgresso.setVisibility(View.VISIBLE);
            recuperaValoresCampos();
            if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO) {
                verificaModificacaoTrabalhoProducao();
            }
            else if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO) {
                verificaModificacaoTrabalho();
            }
            else if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO) {
                verificaNovoTrabalho();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void verificaNovoTrabalho() {
        if (verificaCamposNovoTrabalho()) {
            Trabalho trabalho = defineNovoTrabalho();
            if (trabalhoViewModel.trabalhoEspecificoExiste(trabalho)) {
                Snackbar.make(binding.getRoot(), trabalho.getNome()+" já existe!", Snackbar.LENGTH_LONG).show();
                indicadorProgresso.setVisibility(View.GONE);
                return;
            }
            trabalhoViewModel.adicionaTrabalho(trabalho).observe(this, resultadoAdicionaTrabalho -> {
                indicadorProgresso.setVisibility(View.GONE);
                if (resultadoAdicionaTrabalho.getErro() == null) {
                    Snackbar.make(binding.getRoot(), trabalho.getNome()+" adicionado!", Snackbar.LENGTH_LONG).show();
                    limpaCampos();
                    return;
                }
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoAdicionaTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
            });
            return;
        }
        indicadorProgresso.setVisibility(View.GONE);
    }

    private void limpaCampos() {
        edtNomeTrabalho.setText("");
        edtNomeProducaoTrabalho.setText("");
        edtNomeTrabalho.requestFocus();
    }

    private void verificaModificacaoTrabalho() {
        if (verificaTrabalhoModificado()) {
            Log.d("verificaTrabalhoModificado", "Trabalho modificado");
            Trabalho trabalho = defineTrabalhoModificado();
            trabalhoViewModel.modificaTrabalho(trabalho).observe(this, resultadoModificaTrabalho -> {
                indicadorProgresso.setVisibility(View.GONE);
                if (resultadoModificaTrabalho.getErro() == null) {
                    finish();
                } else {
                    Snackbar.make(binding.getRoot(), "Erro: "+resultadoModificaTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            Log.d("verificaTrabalhoModificado", "Trabalho não modificado");
            finish();
        }
    }

    private void verificaModificacaoTrabalhoProducao() {
        if (verificaTrabalhoProducaoModificado()) {
            TrabalhoProducao trabalhoModificado = defineTrabalhoProducaoModificado();
            trabalhoProducaoViewModel.modificaTrabalhoProducao(trabalhoModificado).observe(this, resultado -> {
                if (resultado.getErro() == null) {
                    if (verificaEstadoModificado()) {
                        Integer estado = trabalhoModificado.getEstado();
                        switch (estado) {
                            case 0:
                                finish();
                                break;
                            case 1:
                                if (trabalhoModificado.possueTrabalhoNecessarioValido()) {
                                    String[] listaTrabalhosNecessarios = trabalhoModificado.getTrabalhoNecessario().split(",");
                                    for (String trabalhoNecessario2 : listaTrabalhosNecessarios) {
                                        TrabalhoEstoque trabalhoEstoqueEncontrado = trabalhoEstoqueViewModel.pegaTrabalhoEspecificoEstoque(trabalhoNecessario2);
                                        if (trabalhoEstoqueEncontrado != null) {
                                            int novaQuantidade = trabalhoEstoqueEncontrado.getQuantidade() - 1;
                                            if (novaQuantidade < 0) {
                                                novaQuantidade = 0;
                                            }
                                            trabalhoEstoqueEncontrado.setQuantidade(novaQuantidade);
                                            trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEstoqueEncontrado);
                                        }
                                    }
                                }
                                finish();
                                break;
                            case 2:
                                MutableLiveData<Boolean> confirmacao = new MutableLiveData<>(true);
                                TrabalhoEstoque trabalhoEstoqueEncontrado = trabalhoEstoqueViewModel.pegaTrabalhoEspecificoEstoque(trabalhoModificado.getIdTrabalho());
                                if (trabalhoEstoqueEncontrado == null) {
                                    if (!trabalhoModificado.ehProducaoDeRecursos()) {
                                        TrabalhoEstoque novoTrabalhoEstoque = new TrabalhoEstoque();
                                        novoTrabalhoEstoque.setTrabalhoId(trabalhoModificado.getIdTrabalho());
                                        novoTrabalhoEstoque.setQuantidade(1);
                                        trabalhoEstoqueViewModel.insereTrabalhoEstoque(novoTrabalhoEstoque).observe(this, resultaSalvaTrabalhoEstoque -> {
                                            if (resultaSalvaTrabalhoEstoque.getErro() != null){
                                                Snackbar.make(binding.getRoot(), "Erro: "+resultaSalvaTrabalhoEstoque.getErro(), Snackbar.LENGTH_LONG).show();
                                                confirmacao.setValue(false);
                                            }
                                        });
                                    }
                                    return;
                                }
                                trabalhoEstoqueEncontrado.setQuantidade(trabalhoEstoqueEncontrado.getQuantidade()+1);
                                trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEstoqueEncontrado).observe(this, resultaModificaQuantidade -> {
                                    if (resultaModificaQuantidade.getErro() != null){
                                        Snackbar.make(binding.getRoot(), "Erro: "+resultaModificaQuantidade.getErro(), Snackbar.LENGTH_LONG).show();
                                        confirmacao.setValue(false);
                                    }
                                });
                                profissaoViewModel.pegaTodasProfissoes().observe(this, resultadoProfissoes -> {
                                    if (resultadoProfissoes.getDado() != null) {
                                        Profissao profissaoEncontrada = profissaoViewModel.retornaProfissaoModificada(resultadoProfissoes.getDado(), trabalhoModificado);
                                        if (profissaoEncontrada != null && profissaoEncontrada.getExperiencia() < 830000) {
                                            int novaExperiencia = profissaoEncontrada.getExperiencia()+trabalhoModificado.getExperiencia();
                                            if (novaExperiencia > 830000) {
                                                novaExperiencia = 830000;
                                            }
                                            profissaoEncontrada.setExperiencia(novaExperiencia);
                                            profissaoViewModel.modificaExperienciaProfissao(profissaoEncontrada).observe(this, resultadoModificaExperiencia -> {
                                                if (resultadoModificaExperiencia.getErro() != null){
                                                    Snackbar.make(binding.getRoot(), "Erro: "+resultadoModificaExperiencia.getErro(), Snackbar.LENGTH_LONG).show();
                                                    confirmacao.setValue(false);
                                                }
                                                if (confirmacao.getValue()) {
                                                    finish();
                                                }
                                            });
                                            return;
                                        }
                                        finish();
                                        return;
                                    }
                                    finish();
                                });
                                break;
                        }
                        return;
                    }
                    finish();
                    return;
                }
                Snackbar.make(binding.getRoot(), "Erro: "+resultado.getErro(), Snackbar.LENGTH_LONG).show();
            });
            return;
        }
        finish();
    }

    private void inicializaComponentes() {
        todosTrabalhos = new ArrayList<>();
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

        TrabalhoViewModelFactory trabalhoViewModelFactory = new TrabalhoViewModelFactory(new TrabalhoRepository(getApplicationContext()));
        trabalhoViewModel = new ViewModelProvider(this, trabalhoViewModelFactory).get(TrabalhoViewModel.class);
    }
    private void recebeDadosIntent() {
        Intent dadosRecebidos = getIntent();
        if (dadosRecebidos != null && dadosRecebidos.hasExtra(CHAVE_TRABALHO)){
            codigoRequisicao = (int) dadosRecebidos.getSerializableExtra(CHAVE_TRABALHO);
            if (codigoRequisicao != CODIGO_REQUISICAO_INVALIDA){
                trabalhoViewModel.pegaTodosTrabalhos().observe(this, resultadoTodosTrabalhos -> {
                    if (resultadoTodosTrabalhos.getDado() != null) {
                        todosTrabalhos = resultadoTodosTrabalhos.getDado();
                    } else if (resultadoTodosTrabalhos.getErro() != null) {
                        Snackbar.make(binding.getRoot(), "Erro: "+resultadoTodosTrabalhos.getErro(), Snackbar.LENGTH_LONG).show();
                    }
                });
                if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO){
                    configuraLayoutNovoTrabalho();
                } else if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO){
                    trabalhoRecebido = (Trabalho) dadosRecebidos.getSerializableExtra(CHAVE_NOME_TRABALHO);
                    configuraLayoutModificaTrabalho(trabalhoRecebido);
                } else if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO) {
                    configuraLayoutModificaTrabalhoProducao(dadosRecebidos);
                }
            }
        }
    }

    private void configuraLayoutModificaTrabalhoProducao(Intent dadosRecebidos) {
        trabalhoProducaoRecebido = (TrabalhoProducao) dadosRecebidos
                .getSerializableExtra(CHAVE_NOME_TRABALHO);
        String personagemId = (String) dadosRecebidos.getSerializableExtra(CHAVE_PERSONAGEM);
        if (trabalhoProducaoRecebido != null) {
            TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(new TrabalhoProducaoRepository(getApplicationContext(), personagemId));
            trabalhoProducaoViewModel = new ViewModelProvider(this, trabalhoProducaoViewModelFactory).get(TrabalhoProducaoViewModel.class);
            TrabalhoEstoqueViewModelFactory trabalhoEstoqueViewModelFactory = new TrabalhoEstoqueViewModelFactory(new TrabalhoEstoqueRepository(getApplicationContext(), personagemId));
            trabalhoEstoqueViewModel = new ViewModelProvider(this, trabalhoEstoqueViewModelFactory).get(TrabalhoEstoqueViewModel.class);
            ProfissaoViewModelFactory profissaoViewModelFactory = new ProfissaoViewModelFactory(new ProfissaoRepository(personagemId));
            profissaoViewModel = new ViewModelProvider(this, profissaoViewModelFactory).get(ProfissaoViewModel.class);
            configuraComponentesAlteraTrabalhoProducao();
        }
    }

    private void configuraLayoutModificaTrabalho(Trabalho trabalhoRecebido) {
        if (trabalhoRecebido != null){
            configuraComponentesAlteraTrabalho();
            configuraBotaoExcluiTrabalhoEspecifico();
        }
    }

    private void configuraAcaoImagem() {
        imagemTrabalhoNecessario1.setOnClickListener(view -> linearLayoutTrabalhoNecessario3.setVisibility(View.VISIBLE));
        imagemTrabalhoNecessario2.setOnClickListener(view -> {
            autoCompleteTrabalhoNecessario2.setText("");
            linearLayoutTrabalhoNecessario3.setVisibility(View.GONE);
        });
    }
    private void recuperaValoresCampos() {
        nome = Objects.requireNonNull(edtNomeTrabalho.getText()).toString().trim();
        nomeProducao = Objects.requireNonNull(edtNomeProducaoTrabalho.getText()).toString().trim();
        profissao = Objects.requireNonNull(autoCompleteProfissao).getText().toString().trim();
        experiencia = Objects.requireNonNull(edtExperienciaTrabalho.getText()).toString().trim();
        nivel = Objects.requireNonNull(edtNivelTrabalho.getText()).toString().trim();
        raridade = Objects.requireNonNull(autoCompleteRaridade).getText().toString().trim();
        trabalhoNecessario1 = Objects.requireNonNull(autoCompleteTrabalhoNecessario1).getText().toString().trim();
        trabalhoNecessario2 = Objects.requireNonNull(autoCompleteTrabalhoNecessario2).getText().toString().trim();
        trabalhoNecessario = "";
        if (comparaString(trabalhoNecessario1, "Trabalho necessário")){
            trabalhoNecessario1 = "";
        }
        if (comparaString(trabalhoNecessario2, "Trabalho necessário")){
            trabalhoNecessario2 = "";
        }
        if (!trabalhoNecessario1.isEmpty() && !trabalhoNecessario2.isEmpty()){
            trabalhoNecessario = trabalhoNecessario1+ ","+ trabalhoNecessario2;
        } else if (!trabalhoNecessario1.isEmpty()){
            trabalhoNecessario = trabalhoNecessario1;
        } else if (!trabalhoNecessario2.isEmpty()){
            trabalhoNecessario = trabalhoNecessario2;
        }
    }

    private void configuraBotaoExcluiTrabalhoEspecifico() {
        btnExcluir.setOnClickListener(v -> {
            indicadorProgresso.setVisibility(View.VISIBLE);
            trabalhoViewModel.excluiTrabalhoEspecificoServidor(trabalhoRecebido).observe(this, resultado -> {
                indicadorProgresso.setVisibility(View.GONE);
                if (resultado.getErro() == null) {
                    finish();
                } else {
                    Snackbar.make(binding.getRoot(), "Erro: "+resultado.getErro(), Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }
    private void configuraDropdownProfissoes() {
        String[] profissoesTrabalho = getResources().getStringArray(R.array.profissoes);
        ArrayAdapter<String> profissoesAdapter = new ArrayAdapter<>(getApplication(), R.layout.item_dropdrown, profissoesTrabalho);
        profissoesAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        autoCompleteProfissao.setAdapter(profissoesAdapter);
    }
    private void configuraDropdownRaridades() {
        String[] raridadesTrabalho = getResources().getStringArray(R.array.raridades);
        ArrayAdapter<String> raridadeAdapter = new ArrayAdapter<>(getApplication(), R.layout.item_dropdrown, raridadesTrabalho);
        raridadeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        autoCompleteRaridade.setAdapter(raridadeAdapter);
        autoCompleteRaridade.setOnItemClickListener((adapterView, view, i, l) -> {
            String raridadeClicada = adapterView.getAdapter().getItem(i).toString();
            if (comparaString(raridadeClicada, "Melhorado") || comparaString(raridadeClicada, "Raro")) {
                linearLayoutTrabalhoNecessario2.setVisibility(View.VISIBLE);
                configuraDropdownTrabalhoNecessario();
            } else {
                linearLayoutTrabalhoNecessario2.setVisibility(View.GONE);
            }
        });
    }

    private void configuraDropdownTrabalhoNecessario() {
        profissao = autoCompleteProfissao.getText().toString();
        nivel = Objects.requireNonNull(edtNivelTrabalho.getText()).toString();
        raridade = Objects.requireNonNull(autoCompleteRaridade).getText().toString().trim();
        Log.d("dropDown", "Profissao: "+profissao+" Nivel: "+nivel+" Raridade: "+raridade);
        ArrayList<String> trabalhosNecessarios = new ArrayList<>();
        ArrayAdapter<String> trabalhoNecessarioAdapter;
        for (Trabalho trabalho : todosTrabalhos) {
            Log.d("dropDown", "Trabalho: "+trabalho.getNome());
            if (comparaString(raridade, "melhorado")) {
                Log.d("dropDown", "Trabalho melhorado");
                if (comparaString(trabalho.getProfissao(), profissao)
                    && comparaString(trabalho.getNivel().toString(), nivel)
                    && comparaString(trabalho.getRaridade(), "Comum")){
                    trabalhosNecessarios.add(trabalho.getNome());
                }
            } else if (comparaString(raridade, "raro")) {
                Log.d("dropDown", "Trabalho raro");
                if (comparaString(trabalho.getProfissao(), profissao)
                    && comparaString(trabalho.getNivel().toString(), nivel)
                    && comparaString(trabalho.getRaridade(), "Melhorado")){
                    trabalhosNecessarios.add(trabalho.getNome());
                }
            }
        }
        if (trabalhosNecessarios.isEmpty()){
            trabalhosNecessarios.add("Nada encontrado");
        }
        trabalhoNecessarioAdapter = new ArrayAdapter<>(getApplication(), R.layout.item_dropdrown, trabalhosNecessarios);
        trabalhoNecessarioAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        autoCompleteTrabalhoNecessario1.setAdapter(trabalhoNecessarioAdapter);
        autoCompleteTrabalhoNecessario2.setAdapter(trabalhoNecessarioAdapter);
    }

    private void configuraDropdownLicencas() {
        String[] licencasTrabalho = getResources().getStringArray(R.array.licencas_completas);
        ArrayAdapter<String> adapterLicenca= new ArrayAdapter<>(this,
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
        adapterEstado= new ArrayAdapter<>(this,
                R.layout.item_dropdrown, estadosTrabalho);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteEstado.setAdapter(adapterEstado);
    }
    private void configuraComponentesAlteraTrabalho() {
        checkBoxRecorrenciaTrabalho.setVisibility(View.GONE);
        txtInputLicenca.setVisibility(View.GONE);
        txtInputEstado.setVisibility(View.GONE);
        btnExcluir.setVisibility(View.VISIBLE);
        setTitle(trabalhoRecebido.getNome());

        edtNomeTrabalho.setText(trabalhoRecebido.getNome());
        edtNomeProducaoTrabalho.setText(trabalhoRecebido.getNomeProducao());
        autoCompleteProfissao.setText(trabalhoRecebido.getProfissao());
        edtExperienciaTrabalho.setText(String.valueOf(trabalhoRecebido.getExperiencia()));
        edtNivelTrabalho.setText(String.valueOf(trabalhoRecebido.getNivel()));
        autoCompleteRaridade.setText(trabalhoRecebido.getRaridade());
        if (trabalhoRecebido.getRaridade().equals("Raro") || trabalhoRecebido.getRaridade().equals("Melhorado")) {
            linearLayoutTrabalhoNecessario2.setVisibility(View.VISIBLE);
            configuraDropdownTrabalhoNecessario();
            String[] trabalhosNecessarios = null;
            if (trabalhoRecebido.getTrabalhoNecessario() != null) {
                trabalhosNecessarios = trabalhoRecebido.getTrabalhoNecessario().split(",");
            }
            if (trabalhosNecessarios != null) {
                if (trabalhosNecessarios.length > 1) {
                    binding.linearLayoutTrabalhoNecessario3.setVisibility(View.VISIBLE);
                    autoCompleteTrabalhoNecessario1.setText(trabalhosNecessarios[0]);
                    autoCompleteTrabalhoNecessario2.setText(trabalhosNecessarios[1]);
                } else {
                    autoCompleteTrabalhoNecessario1.setText(trabalhosNecessarios[0]);
                }
            }
        }
    }

    private void configuraLayoutNovoTrabalho() {
        setTitle(CHAVE_TITULO_NOVO_TRABALHO);
        linearLayoutTrabalhoNecessario2.setVisibility(View.GONE);
        txtInputLicenca.setVisibility(View.GONE);
        txtInputEstado.setVisibility(View.GONE);
        checkBoxRecorrenciaTrabalho.setVisibility(View.GONE);
        configuraDropdownTrabalhoNecessario();
    }

    private void configuraComponentesAlteraTrabalhoProducao() {
        setTitle(trabalhoProducaoRecebido.getNome());
        desativaCamposTrabalhoProducao();
        recorrenciaModificada = trabalhoProducaoRecebido.getRecorrencia();
        licencaModificada = trabalhoProducaoRecebido.getTipo_licenca();
        posicaoEstadoModificado = trabalhoProducaoRecebido.getEstado();
        if (comparaString(licencaModificada, "licença de produção do principiante")) {
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
        checkBoxRecorrenciaTrabalho.setChecked(recorrenciaModificada);
        autoCompleteLicenca.setText(licencaModificada);
        autoCompleteEstado.setText(estadosTrabalho[posicaoEstadoModificado]);
        defineValoresCamposTrabalhosNecessarios();
    }

    private void defineValoresCamposTrabalhosNecessarios() {
        if (valorTrabalhoNecessarioExiste()) {
            linearLayoutTrabalhoNecessario2.setVisibility(View.VISIBLE);
            String[] trabalhosNecessarios = trabalhoProducaoRecebido.getTrabalhoNecessario().split(",");
            if (trabalhosNecessarios.length > 1) {
                autoCompleteTrabalhoNecessario1.setText(trabalhosNecessarios[0]);
                autoCompleteTrabalhoNecessario2.setText(trabalhosNecessarios[1]);
            } else {
                autoCompleteTrabalhoNecessario1.setText(trabalhosNecessarios[0]);
            }
        }
    }

    private boolean valorTrabalhoNecessarioExiste() {
        return trabalhoProducaoRecebido.getTrabalhoNecessario() != null;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario_nota_salva,
                menu);
        return super.onCreateOptionsMenu(menu);
    }
    @NonNull
    private TrabalhoProducao defineTrabalhoProducaoModificado() {
        TrabalhoProducao trabalhoModificado = new TrabalhoProducao();
        trabalhoModificado.setId(trabalhoProducaoRecebido.getId());
        trabalhoModificado.setIdTrabalho(trabalhoProducaoRecebido.getIdTrabalho());
        trabalhoModificado.setRecorrencia(checkBoxRecorrenciaTrabalho.isChecked());
        trabalhoModificado.setTipo_licenca(autoCompleteLicenca.getText().toString());
        trabalhoModificado.setEstado(adapterEstado.getPosition(autoCompleteEstado.getText().toString()));
        return trabalhoModificado;
    }

    @NonNull
    private Trabalho defineTrabalhoModificado() {
        Trabalho trabalho = new Trabalho(
                nome,
                nomeProducao,
                profissao,
                raridade,
                trabalhoNecessario,
                Integer.parseInt(nivel),
                Integer.parseInt(experiencia));
        trabalho.setId(trabalhoRecebido.getId());
        return trabalho;
    }
    private boolean verificaTrabalhoModificado() {
        return verificaCampoModificado(nome, trabalhoRecebido.getNome()) ||
                verificaCampoModificado(nomeProducao, trabalhoRecebido.getNomeProducao()) ||
                verificaCampoModificado(profissao, trabalhoRecebido.getProfissao()) ||
                verificaCampoModificado(experiencia, trabalhoRecebido.getExperiencia().toString()) ||
                verificaCampoTrabalhoNecessario(trabalhoNecessario1, trabalhoRecebido.getTrabalhoNecessario()) ||
                verificaCampoTrabalhoNecessario(trabalhoNecessario2, trabalhoRecebido.getTrabalhoNecessario()) ||
                verificaCampoModificado(nivel, trabalhoRecebido.getNivel().toString()) ||
                verificaCampoModificado(raridade, trabalhoRecebido.getRaridade());
    }

    private boolean verificaCampoTrabalhoNecessario(String trabalhoNecessario, String trabalhoNecessarioRecebido) {
        if (trabalhoNecessario.isEmpty()) {
            if (trabalhoNecessarioRecebido == null) {
                return false;
            }
            return false;
        } else if (trabalhoNecessarioRecebido == null) {
            return true;
        }
        return !stringContemString(trabalhoNecessarioRecebido, trabalhoNecessario);
    }

    private boolean verificaCampoModificado(String campo, String valorRecebido) {
        return !comparaString(campo, valorRecebido);
    }

    private boolean verificaCamposNovoTrabalho() {
        return verificaValorCampo(nome, txtInputNome, 0) &
            verificaValorCampo(nomeProducao, txtInputNomeProducao, 0) &
            verificaValorCampo(profissao, txtInputProfissao, 1) &
            verificaValorCampo(experiencia,txtInputExperiencia,1) &
            verificaValorCampo(nivel, txtInputNivel, 1) &
            verificaValorCampo(raridade, txtInputRaridade, 1);
    }

    private boolean verificaTrabalhoProducaoModificado() {
        return verificaLicencaModificada() ||
                verificaEstadoModificado() ||
                verificaCheckModificado();
    }

    private boolean verificaLicencaModificada() {
        return !comparaString(licencaModificada, autoCompleteLicenca.getText().toString());
    }

    private boolean verificaEstadoModificado() {
        return adapterEstado.getPosition(autoCompleteEstado.getText().toString()) != posicaoEstadoModificado;
    }

    private boolean verificaCheckModificado() {
        return checkBoxRecorrenciaTrabalho.isChecked() != recorrenciaModificada;
    }
    private Boolean verificaValorCampo(String stringCampo, TextInputLayout inputLayout, int posicaoErro) {
        if (stringCampo.isEmpty() || comparaString(stringCampo, "profissões")|| comparaString(stringCampo, "raridade")){
            inputLayout.setHelperText(mensagemErro[posicaoErro]);
            inputLayout.setHelperTextColor(AppCompatResources.getColorStateList(this,R.color.cor_background_bordo));
            return false;

        }
        inputLayout.setHelperTextEnabled(false);
        return true;
    }

    private Trabalho defineNovoTrabalho() {
        return new Trabalho(
                nome,
                nomeProducao,
                profissao,
                raridade,
                trabalhoNecessario,
                Integer.parseInt(nivel),
                Integer.parseInt(experiencia));
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (codigoRequisicao != CODIGO_REQUISICAO_INVALIDA) {
            if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO || codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO) {
                configuraDropdownProfissoes();
                configuraDropdownRaridades();
            } else if (codigoRequisicao == CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO) {
                configuraDropdownLicencas();
                configuraDropdownEstados();
            }
        }
    }
}