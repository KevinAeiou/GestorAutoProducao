package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE;
import static com.kevin.ceep.utilitario.Utilitario.stringContemString;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentListaTrabalhosEstoqueBinding;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.repository.TrabalhoEstoqueRepository;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEstoqueAdapter;
import com.kevin.ceep.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoEstoqueViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaEstoqueFragment extends Fragment {
    private FragmentListaTrabalhosEstoqueBinding binding;
    private ListaTrabalhoEstoqueAdapter trabalhoEstoqueAdapter;
    private RecyclerView recyclerView;
    private ArrayList<TrabalhoEstoque> todosTrabalhosEstoque, listaTrabalhosEstoqueFiltrada;
    private ArrayList<String> profissoes;
    private String personagemId;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorDeProgresso;
    private ChipGroup grupoChipsProfissoes;
    private TrabalhoEstoqueViewModel trabalhoEstoqueViewModel;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
    public ListaEstoqueFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recebeDadosIntent();
    }

    private void recebeDadosIntent() {
        Bundle argumento = getArguments();
        if (argumento != null) {
            if (argumento.containsKey(CHAVE_PERSONAGEM)){
                personagemId = argumento.getString(CHAVE_PERSONAGEM);
                if (personagemId != null) {
                    TrabalhoEstoqueViewModelFactory trabalhoEstoqueViewModelFactory = new TrabalhoEstoqueViewModelFactory(new TrabalhoEstoqueRepository(getContext(), personagemId));
                    trabalhoEstoqueViewModel = new ViewModelProvider(this, trabalhoEstoqueViewModelFactory).get(TrabalhoEstoqueViewModel.class);
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaTrabalhosEstoqueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inicializaComponentes();
        configuraRecyclerView();
        configuraDeslizeItem();
        configuraSwipeRefreshLayout();
        configuraChipSelecionado();
        configuraBotaoInsereTrabalho();
    }

    private void configuraBotaoInsereTrabalho() {
        binding.floatingButtonFragmentTrabalhosEstoque.setOnClickListener(view -> {
            Bundle argumento = new Bundle();
            argumento.putString(CHAVE_PERSONAGEM, personagemId);
            argumento.putInt(CHAVE_TRABALHO, CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE);
            ListaTrabalhosInsereNovoTrabalhoFragment listaNovoTrabalhoEstoqueFragment = new ListaTrabalhosInsereNovoTrabalhoFragment();
            listaNovoTrabalhoEstoqueFragment.setArguments(argumento);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment_content_main, listaNovoTrabalhoEstoqueFragment);
            fragmentTransaction.commit();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraChipSelecionado() {
        grupoChipsProfissoes.setOnCheckedStateChangeListener((grupo, listaIDS) -> filtraTrabalhoPorProfissaoSelecionada(listaIDS));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtraTrabalhoPorProfissaoSelecionada(List<Integer> listaIDS) {
        listaTrabalhosEstoqueFiltrada.clear();
        List<String> profissoesSelecionadas = defineListaDeProfissoesSelecionadas(listaIDS);
        if (profissoesSelecionadas.isEmpty()) {
            listaTrabalhosEstoqueFiltrada = (ArrayList<TrabalhoEstoque>) todosTrabalhosEstoque.clone();
            trabalhoEstoqueAdapter.atualiza(listaTrabalhosEstoqueFiltrada);
            return;
        }
        ArrayList<TrabalhoEstoque> listaProfissaoEspecifica;
        for (String profissao : profissoesSelecionadas) {
            listaProfissaoEspecifica = (ArrayList<TrabalhoEstoque>) todosTrabalhosEstoque.stream().filter(
                            trabalho -> stringContemString(trabalho.getProfissao(), profissao))
                    .collect(Collectors.toList());
            listaTrabalhosEstoqueFiltrada.addAll(listaProfissaoEspecifica);
        }
        trabalhoEstoqueAdapter.atualiza(listaTrabalhosEstoqueFiltrada);
    }

    private List<String> defineListaDeProfissoesSelecionadas(List<Integer> listaIDS) {
        List<String> profissoesSelecionadas = new ArrayList<>();
        for (int id : listaIDS) {
            profissoesSelecionadas.add(profissoes.get(id));
        }
        return profissoesSelecionadas;
    }

    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (personagemId != null) sincronizaTrabalhosEstoque();
        });
    }

    private void pegaTodosTrabalhosEstoque() {
        trabalhoEstoqueViewModel.pegaTodosTrabalhosEstoque().observe(getViewLifecycleOwner(), resultadoPegaTodosTrabalhos -> {
            if (resultadoPegaTodosTrabalhos.getDado() != null) {
                todosTrabalhosEstoque = resultadoPegaTodosTrabalhos.getDado();
                listaTrabalhosEstoqueFiltrada = (ArrayList<TrabalhoEstoque>) todosTrabalhosEstoque.clone();
                indicadorDeProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (listaTrabalhosEstoqueFiltrada.isEmpty()) {
                    iconeListaVazia.setVisibility(View.VISIBLE);
                    txtListaVazia.setVisibility(View.VISIBLE);
                    txtListaVazia.setVisibility(View.VISIBLE);
                    return;
                }
                iconeListaVazia.setVisibility(View.GONE);
                txtListaVazia.setVisibility(View.GONE);
                trabalhoEstoqueAdapter.atualiza(listaTrabalhosEstoqueFiltrada);
                configuraListaDeProfissoes();
                configuraGrupoChipsProfissoes();
            }
            if (resultadoPegaTodosTrabalhos.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoPegaTodosTrabalhos.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void sincronizaTrabalhosEstoque() {
        trabalhoEstoqueViewModel.sincronizaEstoque().observe(getViewLifecycleOwner(), resultadoSincronizaEstoque -> {
            if (resultadoSincronizaEstoque.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoSincronizaEstoque.getErro(), Snackbar.LENGTH_LONG).show();
            }
            pegaTodosTrabalhosEstoque();
        });
    }

    @SuppressLint("ResourceType")
    private void configuraGrupoChipsProfissoes() {
        grupoChipsProfissoes.removeAllViews();
        if (profissoes.isEmpty() || profissoes.size() < 2) return;
        for (String profissao : profissoes) {
            Chip chipProfissao = (Chip) LayoutInflater.from(getContext()).inflate(R.layout.item_chip, grupoChipsProfissoes, false);
            chipProfissao.setText(profissao);
            chipProfissao.setId(profissoes.indexOf(profissao));
            grupoChipsProfissoes.addView(chipProfissao);
        }
    }

    private void configuraListaDeProfissoes() {
        profissoes = new ArrayList<>();
        for (TrabalhoEstoque trabalhoEstoque : todosTrabalhosEstoque) {
            if (profissoes.isEmpty() && !trabalhoEstoque.getProfissao().isEmpty()) {
                profissoes.add(trabalhoEstoque.getProfissao());
                continue;
            }
            if (profissaoExiste(trabalhoEstoque) || trabalhoEstoque.getProfissao().isEmpty()) continue;
            profissoes.add(trabalhoEstoque.getProfissao());
        }
    }

    private boolean profissaoExiste(TrabalhoEstoque trabalhoEstoque) {
        return profissoes.contains(trabalhoEstoque.getProfissao());
    }

    private void inicializaComponentes() {
        todosTrabalhosEstoque = new ArrayList<>();
        listaTrabalhosEstoqueFiltrada = new ArrayList<>();
        recyclerView = binding.listaTrabalhoEstoqueRecyclerView;
        grupoChipsProfissoes = binding.grupoProfissoesChipListaEstoque;
        swipeRefreshLayout = binding.swipeRefreshLayoutTrabalhosEstoque;
        indicadorDeProgresso = binding.indicadorProgressoListaEstoqueFragment;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
    }
    private void configuraRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter(recyclerView);
    }
    private void configuraAdapter(RecyclerView listaTrabalhos) {
        trabalhoEstoqueAdapter = new ListaTrabalhoEstoqueAdapter(listaTrabalhosEstoqueFiltrada,getContext());
        listaTrabalhos.setAdapter(trabalhoEstoqueAdapter);
        trabalhoEstoqueAdapter.setOnItemClickListener(this::alteraQuantidade);
    }

    @SuppressLint("NonConstantResourceId")
    private void alteraQuantidade(TrabalhoEstoque trabalhoEstoqueModificado, int adapterPosition, int botaoId) {
        int novaQuantidade = trabalhoEstoqueModificado.getQuantidade();
        switch (botaoId) {
            case R.id.itemBotaoMenosUm:
                novaQuantidade -= 1;
                break;
            case R.id.itemBotaoMaisUm:
                novaQuantidade += 1;
                break;
            case R.id.itemBotaoMenosCinquenta:
                novaQuantidade -= 50;
                break;
            case R.id.itemBotaoMaisCinquenta:
                novaQuantidade += 50;
                break;
        }
        trabalhoEstoqueModificado.setQuantidade(novaQuantidade);
        trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEstoqueModificado).observe(this, resultadoModificaQuantidade -> {
            if (resultadoModificaQuantidade.getErro() != null) {
                Snackbar.make(binding.getRoot(), resultadoModificaQuantidade.getErro(), Snackbar.LENGTH_SHORT).show();
                return;
            }
            trabalhoEstoqueAdapter.altera(adapterPosition, trabalhoEstoqueModificado);
        });
    }
    private void configuraDeslizeItem() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int itemPosicao = viewHolder.getAdapterPosition();
                ListaTrabalhoEstoqueAdapter trabalhoAdapter = (ListaTrabalhoEstoqueAdapter) recyclerView.getAdapter();
                if (trabalhoAdapter != null) {
                    TrabalhoEstoque trabalhoremovido = listaTrabalhosEstoqueFiltrada.get(itemPosicao);
                    trabalhoAdapter.remove(itemPosicao);
                    Snackbar snackbarDesfazer = Snackbar.make(binding.getRoot(), trabalhoremovido.getNome()+ " excluido", Snackbar.LENGTH_LONG);
                    snackbarDesfazer.addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (event != DISMISS_EVENT_ACTION){
                                removeTrabalhoDoBanco(trabalhoremovido);
                                removeTrabalhoDaLista(trabalhoremovido);
                            }
                        }
                    });
                    snackbarDesfazer.setAnchorView(binding.floatingButtonFragmentTrabalhosEstoque);
                    snackbarDesfazer.setAction(getString(R.string.stringDesfazer), v -> trabalhoEstoqueAdapter.adiciona(trabalhoremovido, itemPosicao));
                    snackbarDesfazer.show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void removeTrabalhoDaLista(TrabalhoEstoque trabalhoRemovido) {
        todosTrabalhosEstoque.remove(trabalhoRemovido);
    }

    private void removeTrabalhoDoBanco(TrabalhoEstoque trabalhoremovido) {
        trabalhoEstoqueViewModel.removeTrabalhoEstoque(trabalhoremovido).observe(this, resultadoRemoveTrabalho -> {
            if (resultadoRemoveTrabalho.getErro() != null) Snackbar.make(binding.getRoot(), "Erro: "+resultadoRemoveTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (personagemId != null) pegaTodosTrabalhosEstoque();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}