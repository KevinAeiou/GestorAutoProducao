package com.kevin.ceep.ui.activity;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TITULO_PRODUTO_VENDIDO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TRABALHO;
import static com.kevin.ceep.utilitario.Utilitario.comparaString;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.ActivityAtributosTrabalhoVendidoBinding;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.ui.viewModel.TrabalhoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhosVendidosViewModelFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AtributosTrabalhoVendidoActivity extends AppCompatActivity {
    private ActivityAtributosTrabalhoVendidoBinding binding;
    private MaterialTextView txtDescricaoTrabalhoVendido, txtDataTrabalhoVendido, txtValorTrabalhoVendido, txtQuantidadeTrabalhoVendido;
    private AutoCompleteTextView autoCompleteNomeTrabalhoVendido;
    private TrabalhoVendido trabalhoRecebido, trabalhoModificado;
    private String personagemId;
    private Trabalho trabalhoSelecionado;
    private TrabalhosVendidosViewModel trabalhosVendidosViewModel;
    private TrabalhoViewModel trabalhoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAtributosTrabalhoVendidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(CHAVE_TITULO_PRODUTO_VENDIDO);
        inicializaComponentes();
        recebeDadosIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario_nota_salva, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.itemMenuSalvaTrabalho) {
            defineTrabalhoModificado();
            if (camposTrabalhoModificado()) {
                MaterialAlertDialogBuilder dialogoDeAlerta = new MaterialAlertDialogBuilder(this);
                dialogoDeAlerta.setMessage("Deseja confirmar alterações?");
                dialogoDeAlerta.setNegativeButton("Não", ((dialogInterface, i) -> finish()));
                dialogoDeAlerta.setPositiveButton("Sim", (dialogInterface, i) -> modificaTrabalho());
                dialogoDeAlerta.show();
            } else {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void defineTrabalhoModificado() {
        trabalhoModificado = new TrabalhoVendido();
        trabalhoModificado.setId(trabalhoRecebido.getId());
        trabalhoModificado.setIdTrabalho(trabalhoSelecionado.getId());
        trabalhoModificado.setDescricao(trabalhoRecebido.getDescricao());
        trabalhoModificado.setDataVenda(trabalhoRecebido.getDataVenda());
        trabalhoModificado.setQuantidade(trabalhoRecebido.getQuantidade());
        trabalhoModificado.setValor(trabalhoRecebido.getValor());
    }

    private void modificaTrabalho() {
        trabalhosVendidosViewModel.modificaTrabalhoVendido(trabalhoModificado).observe(this, resultadoModificaTrabalho -> {
            if (resultadoModificaTrabalho.getErro() == null) finish();
            Snackbar.make(binding.getRoot(), "Erro ao modificar trabalho: " + resultadoModificaTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
        });
    }
    private boolean camposTrabalhoModificado() {
        return !comparaString(trabalhoRecebido.getIdTrabalho(), trabalhoModificado.getIdTrabalho()) ||
            !comparaString(trabalhoRecebido.getDescricao(), trabalhoModificado.getDescricao()) ||
            !comparaString(trabalhoRecebido.getDataVenda(), trabalhoModificado.getDataVenda()) ||
            !(trabalhoRecebido.getQuantidade() == trabalhoModificado.getQuantidade()) ||
            !(trabalhoRecebido.getValor() == trabalhoModificado.getValor());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pegaTodosPersonagens() {
        trabalhoViewModel.pegaTodosTrabalhos().observe(this, resultadoPegaTrabalhos -> {
            ArrayList<Trabalho> todosTrabalhos;
            if (resultadoPegaTrabalhos.getDado() != null) {
                todosTrabalhos = resultadoPegaTrabalhos.getDado();
                configuraAutoCompleteIdPersonagem(todosTrabalhos);
            }
        });
    }

    private void recebeDadosIntent() {
        Intent dadosRecebidos = getIntent();
        if ( dadosRecebidos != null && dadosRecebidos.hasExtra(CHAVE_TRABALHO) && dadosRecebidos.hasExtra(CHAVE_PERSONAGEM)){
            defineValoresCampos(dadosRecebidos);
            TrabalhosVendidosViewModelFactory trabalhosVendidosViewModelFactory = new TrabalhosVendidosViewModelFactory(new TrabalhoVendidoRepository(getApplicationContext(), personagemId));
            trabalhosVendidosViewModel = new ViewModelProvider(this, trabalhosVendidosViewModelFactory).get(TrabalhosVendidosViewModel.class);
        }
    }

    private void defineValoresCampos(Intent dadosRecebidos) {
        personagemId = (String) dadosRecebidos.getSerializableExtra(CHAVE_PERSONAGEM);
        trabalhoRecebido = (TrabalhoVendido) dadosRecebidos.getSerializableExtra(CHAVE_TRABALHO);
        assert trabalhoRecebido != null;
        txtDescricaoTrabalhoVendido.setText(trabalhoRecebido.getDescricao());
        txtDataTrabalhoVendido.setText(trabalhoRecebido.getDataVenda());
        txtValorTrabalhoVendido.setText(getString(R.string.stringOuroValor, trabalhoRecebido.getValor()));
        txtQuantidadeTrabalhoVendido.setText(getString(R.string.stringQuantidadeValor, trabalhoRecebido.getQuantidade()));
    }

    private void inicializaComponentes() {
        txtDescricaoTrabalhoVendido = binding.txtDescricaoTrabalhoVendido;
        txtDataTrabalhoVendido = binding.txtDataTrabalhoVendido;
        txtValorTrabalhoVendido = binding.txtValorTrabalhoVendido;
        txtQuantidadeTrabalhoVendido = binding.txtQuantidadeTrabalhoVendido;
        autoCompleteNomeTrabalhoVendido = binding.autoCompleteNomeTrabalhoVendido;
        TrabalhoViewModelFactory trabalhoViewModelFactory = new TrabalhoViewModelFactory(new TrabalhoRepository(getApplicationContext()));
        trabalhoViewModel = new ViewModelProvider(this, trabalhoViewModelFactory).get(TrabalhoViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraAutoCompleteIdPersonagem(ArrayList<Trabalho> todosTrabalhos) {
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
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(this, R.layout.item_dropdrown, todosNomesTrabalhos);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteNomeTrabalhoVendido.setAdapter(adapterEstado);
        autoCompleteNomeTrabalhoVendido.setOnItemClickListener((parent, view, position, id) -> trabalhoSelecionado = todosTrabalhos.get(position));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        pegaTodosPersonagens();
    }
}