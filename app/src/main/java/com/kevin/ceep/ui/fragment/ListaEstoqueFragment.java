package com.kevin.ceep.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE;
import static com.kevin.ceep.utilitario.Utilitario.stringContemString;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentListaTrabalhosEstoqueBinding;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.repository.TrabalhoEstoqueRepository;
import com.kevin.ceep.ui.fragment.ListaEstoqueFragmentDirections.VaiDeEstoqueParaTrabalhos;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEstoqueAdapter;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.ProfissaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoEstoqueViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaEstoqueFragment extends Fragment implements MenuProvider {
    private FragmentListaTrabalhosEstoqueBinding binding;
    private ListaTrabalhoEstoqueAdapter trabalhoEstoqueAdapter;
    private RecyclerView recyclerView;
    private ArrayList<TrabalhoEstoque> todosTrabalhosEstoque, trabalhosEstoqueFiltrada;
    private ArrayList<String> profissoes;
    private String idPersonagem, textoFiltro;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorDeProgresso;
    private ChipGroup grupoChipsProfissoes;
    private TrabalhoEstoqueViewModel trabalhoEstoqueViewModel;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
    private PersonagemViewModel personagemViewModel;
    public ListaEstoqueFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaTrabalhosEstoqueBinding.inflate(inflater, container, false);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuBusca = true;
        componentesVisuais.menuNavegacaoLateral = true;
        componentesVisuais.menuNavegacaoInferior = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        inicializaComponentes();
        configuraPersonagemSelecionado();
        configuraRecyclerView();
        configuraDeslizeItem();
        configuraSwipeRefreshLayout();
        configuraChipSelecionado();
        configuraBotaoInsereTrabalho();
    }

    private void configuraIndicadorListaVazia(ArrayList<TrabalhoEstoque> listaFiltrada) {
        if (listaFiltrada.isEmpty()) {
            iconeListaVazia.setVisibility(VISIBLE);
            txtListaVazia.setVisibility(VISIBLE);
            return;
        }
        txtListaVazia.setVisibility(GONE);
        iconeListaVazia.setVisibility(GONE);
    }

    private void configuraPersonagemSelecionado() {
        personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), personagemSelecionado -> {
            if (personagemSelecionado == null) return;
            idPersonagem = personagemSelecionado.getId();
            TrabalhoEstoqueViewModelFactory trabalhoEstoqueViewModelFactory = new TrabalhoEstoqueViewModelFactory(TrabalhoEstoqueRepository.getInstance(idPersonagem));
            trabalhoEstoqueViewModel = new ViewModelProvider(this, trabalhoEstoqueViewModelFactory).get(idPersonagem, TrabalhoEstoqueViewModel.class);
        });
    }

    private void configuraBotaoInsereTrabalho() {
        binding.floatingButtonFragmentTrabalhosEstoque.setOnClickListener(view -> {
            VaiDeEstoqueParaTrabalhos acao = ListaEstoqueFragmentDirections.vaiDeEstoqueParaTrabalhos(idPersonagem);
            acao.setRequisicao(CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE);
            Navigation.findNavController(view).navigate(acao);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraChipSelecionado() {
        grupoChipsProfissoes.setOnCheckedStateChangeListener((grupo, listaIDS) -> filtraTrabalhoPorProfissaoSelecionada(listaIDS));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtraTrabalhoPorProfissaoSelecionada(List<Integer> listaIDS) {
        trabalhosEstoqueFiltrada.clear();
        List<String> profissoesSelecionadas = defineListaDeProfissoesSelecionadas(listaIDS);
        if (profissoesSelecionadas.isEmpty()) {
            trabalhosEstoqueFiltrada = (ArrayList<TrabalhoEstoque>) todosTrabalhosEstoque.clone();
            configuraIndicadorListaVazia(trabalhosEstoqueFiltrada);
            trabalhoEstoqueAdapter.atualiza(trabalhosEstoqueFiltrada);
            return;
        }
        ArrayList<TrabalhoEstoque> listaProfissaoEspecifica;
        for (String profissao : profissoesSelecionadas) {
            listaProfissaoEspecifica = (ArrayList<TrabalhoEstoque>) todosTrabalhosEstoque.stream().filter(
                            trabalho -> stringContemString(trabalho.getProfissao(), profissao))
                    .collect(Collectors.toList());
            trabalhosEstoqueFiltrada.addAll(listaProfissaoEspecifica);
        }
        configuraIndicadorListaVazia(trabalhosEstoqueFiltrada);
        trabalhoEstoqueAdapter.atualiza(trabalhosEstoqueFiltrada);
        filtroLista();
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
            if (idPersonagem == null) return;
            recuperaTrabalhosEstoque();
        });
    }

    private void recuperaTrabalhosEstoque() {
        Log.d("estoque", "Função recuperaTrabalhosEstoque iniciada");
        trabalhoEstoqueViewModel.recuperaTrabalhosEstoque().observe(getViewLifecycleOwner(), resultadorecuperaTrabalhos -> {
            Log.d("estoque", "Resultado devlvido");
            if (resultadorecuperaTrabalhos.getErro() == null) {
                Log.d("estoque", "Estoque recuperado: ");
                todosTrabalhosEstoque = resultadorecuperaTrabalhos.getDado();
                trabalhosEstoqueFiltrada = (ArrayList<TrabalhoEstoque>) todosTrabalhosEstoque.clone();
                indicadorDeProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (trabalhosEstoqueFiltrada.isEmpty()) {
                    iconeListaVazia.setVisibility(View.VISIBLE);
                    txtListaVazia.setVisibility(View.VISIBLE);
                    txtListaVazia.setVisibility(View.VISIBLE);
                    return;
                }
                iconeListaVazia.setVisibility(View.GONE);
                txtListaVazia.setVisibility(View.GONE);
                trabalhoEstoqueAdapter.atualiza(trabalhosEstoqueFiltrada);
                configuraListaDeProfissoes();
                return;
            }
            Log.d("estoque", "Resultado erro");
            Snackbar.make(binding.getRoot(), "Erro: "+resultadorecuperaTrabalhos.getErro(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void configuraGrupoChipsProfissoes() {
        grupoChipsProfissoes.removeAllViews();
        for (String profissao : profissoes) {
            adicionaChip(profissao);
        }
    }

    private void adicionaChip(String profissao) {
        Chip novoChip= new Chip(new ContextThemeWrapper(requireContext(), R.style.estiloChip), null, 0);
        novoChip.setText(profissao);
        novoChip.setId(profissoes.indexOf(profissao));
        novoChip.setCheckable(true);
        grupoChipsProfissoes.addView(novoChip);
    }

    private void configuraListaDeProfissoes() {
        profissoes.clear();
        ProfissaoViewModelFactory profissaoViewModelFactory = new ProfissaoViewModelFactory(idPersonagem);
        ProfissaoViewModel profissaoViewModel = new ViewModelProvider(this, profissaoViewModelFactory).get(idPersonagem, ProfissaoViewModel.class);
        profissaoViewModel.recuperaProfissoes().observe(getViewLifecycleOwner(), resultadoProfissoes -> {
            if (resultadoProfissoes.getErro() == null) {
                for (Profissao profissao : resultadoProfissoes.getDado()) {
                    profissoes.add(profissao.getNome());
                }
                configuraGrupoChipsProfissoes();
                return;
            }
            Snackbar.make(binding.getRoot(), "Erro ao buscar profissões: "+ resultadoProfissoes.getErro(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void inicializaComponentes() {
        idPersonagem= "";
        textoFiltro= "";
        todosTrabalhosEstoque = new ArrayList<>();
        trabalhosEstoqueFiltrada = new ArrayList<>();
        profissoes= new ArrayList<>();
        recyclerView = binding.listaTrabalhoEstoqueRecyclerView;
        grupoChipsProfissoes = binding.grupoProfissoesChipListaEstoque;
        swipeRefreshLayout = binding.swipeRefreshLayoutTrabalhosEstoque;
        indicadorDeProgresso = binding.indicadorProgressoListaEstoqueFragment;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(PersonagemRepository.getInstance());
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
    }
    private void configuraRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter(recyclerView);
    }
    private void configuraAdapter(RecyclerView listaTrabalhos) {
        trabalhoEstoqueAdapter = new ListaTrabalhoEstoqueAdapter(trabalhosEstoqueFiltrada,getContext());
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
        trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEstoqueModificado).observe(getViewLifecycleOwner(), resultadoModificaQuantidade -> {
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
                    TrabalhoEstoque trabalhoremovido = trabalhosEstoqueFiltrada.get(itemPosicao);
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
        trabalhoEstoqueViewModel.removeTrabalhoEstoque(trabalhoremovido).observe(getViewLifecycleOwner(), resultadoRemoveTrabalho -> {
            if (resultadoRemoveTrabalho.getErro() != null) Snackbar.make(binding.getRoot(), "Erro: "+resultadoRemoveTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        recuperaTrabalhosEstoque();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        MenuItem itemBusca= menu.findItem(R.id.itemMenuBusca);
        SearchView visualizacaoBusca= (SearchView) itemBusca.getActionView();
        assert visualizacaoBusca != null;
        visualizacaoBusca.setOnQueryTextFocusChangeListener((view, b) -> {
            if (b) {
                grupoChipsProfissoes.setVisibility(VISIBLE);
                return;
            }
            grupoChipsProfissoes.setVisibility(GONE);
        });
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            configuraComportamentoBuscaPorTexto(menuItem);
            return true;
        }
        return false;
    }

    private void configuraComportamentoBuscaPorTexto(@NonNull MenuItem menuItem) {
        SearchView busca = (SearchView) menuItem.getActionView();
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
        if (textoFiltro.isEmpty()) {
            trabalhoEstoqueAdapter.atualiza(trabalhosEstoqueFiltrada);
            return;
        }
        ArrayList<TrabalhoEstoque> listaFiltrada =
                (ArrayList<TrabalhoEstoque>) trabalhosEstoqueFiltrada.stream().filter(
                                trabalho -> stringContemString(trabalho.getNome(), textoFiltro))
                        .collect(Collectors.toList());
        if (listaFiltrada.isEmpty()) {
            iconeListaVazia.setVisibility(VISIBLE);
            txtListaVazia.setVisibility(VISIBLE);
            trabalhoEstoqueAdapter.atualiza(listaFiltrada);
            return;
        }
        txtListaVazia.setVisibility(GONE);
        iconeListaVazia.setVisibility(GONE);
        trabalhoEstoqueAdapter.atualiza(listaFiltrada);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeOuvintePersonagem();
        removeOuvinteEstoque();
        binding = null;
    }

    private void removeOuvinteEstoque() {
        if (trabalhoEstoqueViewModel == null) return;
        trabalhoEstoqueViewModel.removeOuvinte();
    }

    private void removeOuvintePersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeOuvinte();
    }
}