package com.kevin.ceep.ui.activity;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TRABALHO;
import static com.kevin.ceep.utilitario.Utilitario.stringContemString;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.ActivityListaNovaProducaoBinding;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEspecificoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEspecificoNovaProducaoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;
import com.kevin.ceep.ui.viewModel.ListaNovaProducaoViewModel;
import com.kevin.ceep.ui.viewModel.factory.ListaNovaProducaoViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaNovaProducaoActivity extends AppCompatActivity {
    private ActivityListaNovaProducaoBinding binding;
    private ProgressBar indicadorProgresso;
    private RecyclerView meuRecycler;
    private ListaTrabalhoEspecificoNovaProducaoAdapter listaTrabalhoEspecificoAdapter;
    private String personagemId, textoFiltro;
    private HorizontalScrollView linearLayoutGruposChips;
    private ChipGroup grupoChipsProfissoes;
    private ArrayList<String> listaProfissoes;
    private ArrayList<Trabalho> todosTrabalhos, listaTrabalhosFiltrada;
    private ListaNovaProducaoViewModel novaProducaoViewModel;
    private TextView txtListaVazia;
    private ImageView iconeListaVazia;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inicializaComponentes();
        recebeDadosIntent();
        configuraMeuRecycler();
        configuraChipSelecionado();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraChipSelecionado() {
        grupoChipsProfissoes.setOnCheckedStateChangeListener((grupo, listaIds) -> filtraTrabalhoPorProfissaoSelecionada(listaIds));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtraTrabalhoPorProfissaoSelecionada(List<Integer> listaIds) {
        listaTrabalhosFiltrada.clear();
        List<String> profissoesSelecionadas = defineListaDeProfissoesSelecionadas(listaIds);
        if (!profissoesSelecionadas.isEmpty()) {
            ArrayList<Trabalho> listaProfissaoEspecifica;
            for (String profissao : profissoesSelecionadas) {
                listaProfissaoEspecifica = (ArrayList<Trabalho>) todosTrabalhos.stream().filter(
                        trabalho -> stringContemString(trabalho.getProfissao(), profissao))
                        .collect(Collectors.toList());
                listaTrabalhosFiltrada.addAll(listaProfissaoEspecifica);
            }
        } else {
            listaTrabalhosFiltrada = (ArrayList<Trabalho>) todosTrabalhos.clone();
        }
        filtroLista();
    }

    @NonNull
    private List<String> defineListaDeProfissoesSelecionadas(List<Integer> listaIds) {
        List<String> profissoesSelecionadas = new ArrayList<>();
        for (int id : listaIds) {
            profissoesSelecionadas.add(listaProfissoes.get(id));
        }
        return profissoesSelecionadas;
    }

    private void configuraGrupoChipsProfissoes() {
        grupoChipsProfissoes.removeAllViews();
        if (!listaProfissoes.isEmpty()) {
            int idProfissao = 0;
            for (String profissao : listaProfissoes) {
                Chip chipProfissao = (Chip) LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_chip, null);
                chipProfissao.setText(profissao);
                chipProfissao.setId(idProfissao);
                grupoChipsProfissoes.addView(chipProfissao);
                idProfissao += 1;
            }
        }
    }

    private void configuraListaDeProfissoes() {
        listaProfissoes.clear();
        for (Trabalho trabalho : todosTrabalhos) {
            if (listaProfissoes.isEmpty()) {
                listaProfissoes.add(trabalho.getProfissao());
            } else {
                if (profissaoNaoExiste(trabalho)) {
                    listaProfissoes.add(trabalho.getProfissao());
                }
            }
        }
    }

    private boolean profissaoNaoExiste(Trabalho trabalho) {
        return !listaProfissoes.contains(trabalho.getProfissao());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_personagem, menu);
        MenuItem itemBusca = configuraItemDeBusca(menu);
        configuraCampoDeBusca(itemBusca);
        return super.onCreateOptionsMenu(menu);
    }

    @NonNull
    private MenuItem configuraItemDeBusca(Menu menu) {
        MenuItem itemBusca = menu.findItem(R.id.itemMenuBusca);
        itemBusca.setOnMenuItemClickListener(item -> {
            linearLayoutGruposChips.setVisibility(View.VISIBLE);
            return true;
        });
        return itemBusca;
    }

    private void configuraCampoDeBusca(MenuItem itemBusca) {
        androidx.appcompat.widget.SearchView busca = (androidx.appcompat.widget.SearchView) itemBusca.getActionView();
        assert busca != null;
        busca.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String texto) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textoFiltro = texto;
                    filtroLista();
                }
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtroLista() {
        if (!textoFiltro.isEmpty()) {
            ArrayList<Trabalho> listaFiltrada =
                    (ArrayList<Trabalho>) listaTrabalhosFiltrada.stream().filter(
                            trabalho -> stringContemString(trabalho.getNome(), textoFiltro))
                            .collect(Collectors.toList());
            if (listaFiltrada.isEmpty()) {
                iconeListaVazia.setVisibility(View.VISIBLE);
                txtListaVazia.setVisibility(View.VISIBLE);
            } else {
                txtListaVazia.setVisibility(View.GONE);
                iconeListaVazia.setVisibility(View.GONE);
            }
            listaTrabalhoEspecificoAdapter.atualizaLista(listaFiltrada);
        } else {
            listaTrabalhoEspecificoAdapter.atualizaLista(listaTrabalhosFiltrada);
        }
    }

    private void recebeDadosIntent() {
        Intent dadosRecebidos = getIntent();
        if (dadosRecebidos.hasExtra(CHAVE_PERSONAGEM)) {
            personagemId = (String) dadosRecebidos.getSerializableExtra(CHAVE_PERSONAGEM);
        }
    }
    private void configuraMeuRecycler() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        configuraAdapter(meuRecycler);
    }

    private void configuraAdapter(RecyclerView meuRecycler) {
        listaTrabalhoEspecificoAdapter = new ListaTrabalhoEspecificoNovaProducaoAdapter(getApplicationContext(), todosTrabalhos);
        meuRecycler.setAdapter(listaTrabalhoEspecificoAdapter);
        listaTrabalhoEspecificoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Trabalho trabalho, int adapterPosition) {
                vaiParaConfirmaTrabalhoActivity(trabalho);
            }

            @Override
            public void onItemClick(ListaTrabalhoEspecificoAdapter trabalhoEspecificoAdapter) {

            }

            @Override
            public void onItemClick(TrabalhoEstoque trabalhoEstoque, int adapterPosition, int botaoId) {

            }
        });
    }

    private void vaiParaConfirmaTrabalhoActivity(Trabalho trabalho) {
        Intent iniciaVaiParaConfirmaTrabalhoActivity = new Intent(getApplicationContext(), ConfirmaTrabalhoActivity.class);
        iniciaVaiParaConfirmaTrabalhoActivity.putExtra(CHAVE_TRABALHO, trabalho);
        iniciaVaiParaConfirmaTrabalhoActivity.putExtra(CHAVE_PERSONAGEM, personagemId);
        startActivity(iniciaVaiParaConfirmaTrabalhoActivity);
        finish();
    }

    private void inicializaComponentes() {
        binding = ActivityListaNovaProducaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        indicadorProgresso = binding.indicadorProgressoListaNovaProducao;
        meuRecycler = binding.recyclerViewListaNovaProducao;
        linearLayoutGruposChips = binding.linearLayoutGrupoChipsListaNovaProducao;
        grupoChipsProfissoes = binding.grupoProfissoesChipListaNovaProducao;
        listaProfissoes = new ArrayList<>();
        todosTrabalhos = new ArrayList<>();
        listaTrabalhosFiltrada = new ArrayList<>();
        ListaNovaProducaoViewModelFactory listaNovaProducaoViewModelFactory = new ListaNovaProducaoViewModelFactory(new TrabalhoRepository(getApplicationContext()));
        novaProducaoViewModel = new ViewModelProvider(this, listaNovaProducaoViewModelFactory).get(ListaNovaProducaoViewModel.class);
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
    }

    private void pegaTodosTrabalhos() {
        novaProducaoViewModel.pegaTodosTrabalhos().observe(this, resultadoPegaTodosTrabalhos -> {
            if (resultadoPegaTodosTrabalhos.getDado() != null) {
                todosTrabalhos = resultadoPegaTodosTrabalhos.getDado();
                listaTrabalhosFiltrada = (ArrayList<Trabalho>) todosTrabalhos.clone();
                indicadorProgresso.setVisibility(View.GONE);
                if (listaTrabalhosFiltrada.isEmpty()) {
                    iconeListaVazia.setVisibility(View.VISIBLE);
                    txtListaVazia.setVisibility(View.VISIBLE);
                } else {
                    txtListaVazia.setVisibility(View.GONE);
                    iconeListaVazia.setVisibility(View.GONE);
                }
                configuraListaDeProfissoes();
                configuraGrupoChipsProfissoes();
                listaTrabalhoEspecificoAdapter.atualizaLista(listaTrabalhosFiltrada);
            }
            if (resultadoPegaTodosTrabalhos.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoPegaTodosTrabalhos.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        pegaTodosTrabalhos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding = null;
    }
}