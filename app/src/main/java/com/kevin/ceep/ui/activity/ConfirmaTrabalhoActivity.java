package com.kevin.ceep.ui.activity;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TITULO_CONFIRMA;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TRABALHO;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.ActivityConfirmaTrabalhoBinding;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.TrabalhoProducaoRepository;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoViewModel;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoProducaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoViewModelFactory;

import java.util.Arrays;
import java.util.List;

public class ConfirmaTrabalhoActivity extends AppCompatActivity {
    private ActivityConfirmaTrabalhoBinding binding;
    private AutoCompleteTextView autoCompleteLicenca,autoCompleteQuantidade;
    private String idPersonagem;
    private Trabalho trabalhoRecebido;
    private int contador;
    private String nomesTrabalhosNecessarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmaTrabalhoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(CHAVE_TITULO_CONFIRMA);
        recebeDadosIntent();
        preencheCampos();
        configuraBotaoCadastraTrabalho();
    }

    private void preencheCampos() {
        binding.txtProfissaoConfirmaTrabalho.setText(trabalhoRecebido.getProfissao());
        TrabalhoViewModelFactory trabalhoViewModelFactory = new TrabalhoViewModelFactory(new TrabalhoRepository(getApplicationContext()));
        TrabalhoViewModel trabalhoViewModel = new ViewModelProvider(this, trabalhoViewModelFactory).get(TrabalhoViewModel.class);
        List<String> idsTrabalhosNecessarios = Arrays.asList(trabalhoRecebido.getTrabalhoNecessario().split(","));
        nomesTrabalhosNecessarios = "";
        for (String id : idsTrabalhosNecessarios) {
            trabalhoViewModel.pegaTrabalhoPorId(id).observe(this, resultadoPegaTrabalho -> {
                if (resultadoPegaTrabalho.getErro() == null) {
                    nomesTrabalhosNecessarios += resultadoPegaTrabalho.getDado().getNome();
                } else {
                    Snackbar.make(binding.getRoot(), id + " " + resultadoPegaTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
                }
                if (!nomesTrabalhosNecessarios.isEmpty())
                    binding.txtTrabalhoNecessarioConfirmaTrabalho.setText(nomesTrabalhosNecessarios);
            });
        }
    }

    private void recebeDadosIntent() {
        Intent dadosRecebidos = getIntent();
        if (dadosRecebidos.hasExtra(CHAVE_TRABALHO)) {
            trabalhoRecebido = (Trabalho) dadosRecebidos
                    .getSerializableExtra(CHAVE_TRABALHO);
            if (trabalhoRecebido != null) {
                setTitle(trabalhoRecebido.getNome());
            }
            idPersonagem = (String) dadosRecebidos.
                    getSerializableExtra(CHAVE_PERSONAGEM);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        configuraDropDrow();
    }
    private void configuraDropDrow() {
        autoCompleteLicenca = findViewById(R.id.txtAutoCompleteLicencaConfirmaTrabalho);
        autoCompleteQuantidade = findViewById(R.id.txtAutoCompleteQuantidadeConfirmaTrabalho);

        String[] licencas = getResources().getStringArray(R.array.licencas_completas);
        String[] quantidade = getResources().getStringArray(R.array.quantidade);

        ArrayAdapter<String> adapterLicenca = new ArrayAdapter<>(this,
                R.layout.item_dropdrown, licencas);
        ArrayAdapter<String> adapterQuantidade = new ArrayAdapter<>(this,
                R.layout.item_dropdrown, quantidade);

        adapterLicenca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterQuantidade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteLicenca.setText(licencas[3]);
        autoCompleteLicenca.setAdapter(adapterLicenca);
        autoCompleteQuantidade.setAdapter(adapterQuantidade);
    }

    private void configuraBotaoCadastraTrabalho() {
        AppCompatButton botaoCadastraTrabalho = findViewById(R.id.botaoCadastraConfirmaTrabalho);
        botaoCadastraTrabalho.setOnClickListener(view -> {
            botaoCadastraTrabalho.setEnabled(false);
            adicionaTrabalhoProducao();
        });
    }

    private void adicionaTrabalhoProducao() {
        TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(new TrabalhoProducaoRepository(getApplicationContext(), idPersonagem));
        TrabalhoProducaoViewModel trabalhoProducaoViewModel = new ViewModelProvider(this, trabalhoProducaoViewModelFactory).get(TrabalhoProducaoViewModel.class);
        int quantidadeSelecionada = Integer.parseInt(autoCompleteQuantidade.getText().toString());
        contador = 1;
        for (int x = 0; x < quantidadeSelecionada; x ++){
            TrabalhoProducao novoTrabalho = defineNovoModeloTrabalhoProducao();
            trabalhoProducaoViewModel.insereTrabalhoProducao(novoTrabalho).observe(this, resposta -> {
                if (resposta.getErro() == null) {
                    if (contador == quantidadeSelecionada) {
                        finish();
                    }
                    contador += 1;
                }
            });
        }
    }

    private TrabalhoProducao defineNovoModeloTrabalhoProducao() {
        CheckBox checkRecorrencia = findViewById(R.id.checkBoxProducaoRecorrenteConfirmaTrabalho);
        TrabalhoProducao trabalhoProducao = new TrabalhoProducao();
        trabalhoProducao.setIdTrabalho(trabalhoRecebido.getId());
        trabalhoProducao.setTipo_licenca(autoCompleteLicenca.getText().toString());
        trabalhoProducao.setRecorrencia(checkRecorrencia.isChecked());
        trabalhoProducao.setEstado(0);
        return trabalhoProducao;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}