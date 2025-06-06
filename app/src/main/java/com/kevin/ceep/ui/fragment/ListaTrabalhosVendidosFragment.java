package com.kevin.ceep.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_VENDAS;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS;
import static com.kevin.ceep.ui.fragment.ListaTrabalhosVendidosFragmentDirections.*;
import static com.kevin.ceep.utilitario.Utilitario.stringContemString;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentListaTrabalhosVendidosBinding;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.ui.fragment.ListaTrabalhosVendidosFragmentDirections.VaiDeTrabalhosVendidosParaDetalhesTrabalhoVendido;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhosVendidosAdapter;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhosVendidosViewModelFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListaTrabalhosVendidosFragment
        extends BaseFragment<FragmentListaTrabalhosVendidosBinding>
        implements MenuProvider {
    private ListaTrabalhosVendidosAdapter trabalhosVendidosAdapter;
    private String idPersonagem, textoFiltro;
    private ArrayList<TrabalhoVendido> trabalhosVendidos, trabalhosFiltrados;
    private RecyclerView meuRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private TrabalhosVendidosViewModel trabalhosVendidosViewModel;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
    private PersonagemViewModel personagemViewModel;

    public ListaTrabalhosVendidosFragment() {
    }
    @Override
    protected FragmentListaTrabalhosVendidosBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentListaTrabalhosVendidosBinding.inflate(
                inflater,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        configuraComponentesVisuais();
        inicializaComponentes();
        configuraRecyclerView();
        configuraSwipeRefreshLayout();
        configuraDeslizeItem();
        configuraBotaoInsereVenda();
    }

    private void configuraComponentesVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuBusca = true;
        componentesVisuais.menuNavegacaoInferior = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    private void configuraBotaoInsereVenda() {
        binding.botaoFlutuanteVendas.setOnClickListener(view -> {
            VaiDeVendasParaTrabalhos acao = ListaTrabalhosVendidosFragmentDirections.vaiDeVendasParaTrabalhos(idPersonagem);
            acao.setRequisicao(CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS);
            Navigation.findNavController(view).navigate(acao);
        });
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

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
                    mostraListaFiltrada();
                }
                return false;
            }
        });
    }

    private void mostraListaFiltrada() {
        if (trabalhosFiltrados.isEmpty()) {
            iconeListaVazia.setVisibility(VISIBLE);
            txtListaVazia.setVisibility(VISIBLE);
            trabalhosVendidosAdapter.limpaLista();
            return;
        }
        txtListaVazia.setVisibility(GONE);
        iconeListaVazia.setVisibility(GONE);
        trabalhosVendidosAdapter.atualiza(trabalhosFiltrados);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtroLista() {
        if (textoFiltro.trim().isEmpty()) return;
        trabalhosFiltrados = trabalhosVendidos.stream()
            .filter(Objects::nonNull)
            .filter(trabalho -> stringContemString(trabalho.getNome(), textoFiltro))
            .collect(Collectors.toCollection(ArrayList::new));
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
                trabalhosVendidosAdapter = (ListaTrabalhosVendidosAdapter) meuRecycler.getAdapter();
                if (trabalhosVendidosAdapter != null) {
                    TrabalhoVendido trabalhoVendidoRemovido = trabalhosFiltrados.get(itemPosicao);
                    trabalhosVendidosAdapter.remove(itemPosicao);
                    Snackbar snackbarDesfazer = Snackbar.make(binding.getRoot(), "Venda removida: ", Snackbar.LENGTH_LONG);
                    snackbarDesfazer.addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (event != DISMISS_EVENT_ACTION){
                                removeTrabalhoDoBanco(trabalhoVendidoRemovido);
                                removeTrabalhoDaLista(trabalhoVendidoRemovido);
                            }
                        }
                    });
                    snackbarDesfazer.setAction(getString(R.string.stringDesfazer), v -> trabalhosVendidosAdapter.adiciona(trabalhoVendidoRemovido, itemPosicao));
                    snackbarDesfazer.show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(meuRecycler);
    }

    private void removeTrabalhoDaLista(TrabalhoVendido trabalhoVendidoRemovido) {
        trabalhosFiltrados.remove(trabalhoVendidoRemovido);
    }

    private void removeTrabalhoDoBanco(TrabalhoVendido trabalho) {
        trabalhosVendidosViewModel.getRemocaoResultado().observe(
                getViewLifecycleOwner(),
                resultadoRemoveVenda
        -> {
            if (resultadoRemoveVenda.getErro() != null) {
                mostraMensagem("Erro ao remover venda: "+resultadoRemoveVenda.getErro());
            }
        });
        trabalhosVendidosViewModel.removeVenda(trabalho);
    }
    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            trabalhosVendidosAdapter.limpaLista();
            if (idPersonagem == null) return;
            recuperaVendas();
        });
    }

    @SuppressLint("NewApi")
    private void recuperaVendas() {
        trabalhosVendidosViewModel.getRecuperaVendasResultado().observe(
                getViewLifecycleOwner(),
                resultadoTodosTrabalhos
        -> {
            if (resultadoTodosTrabalhos.getErro() == null) {
                trabalhosVendidos = resultadoTodosTrabalhos.getDado();
                trabalhosVendidos = trabalhosVendidos.stream()
                        .filter(Objects::nonNull)
                        .filter(trabalho -> stringContemString(trabalho.getNome(), textoFiltro))
                        .sorted(Comparator.comparing(TrabalhoVendido::getDataVenda).reversed())
                        .collect(Collectors.toCollection(ArrayList::new));
                trabalhosFiltrados = (ArrayList<TrabalhoVendido>) trabalhosVendidos.clone();
                mostraListaFiltrada();
                indicadorProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
            mostraMensagem("Erro ao recuperar vendas: "+resultadoTodosTrabalhos.getErro());
        });
        trabalhosVendidosViewModel.recuperaVendas();
    }

    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter();
    }

    private void configuraAdapter() {
        trabalhosVendidosAdapter = new ListaTrabalhosVendidosAdapter(trabalhosVendidos, getContext());
        meuRecycler.setAdapter(trabalhosVendidosAdapter);
        trabalhosVendidosAdapter.setOnItemClickListener(this::vaiParaDetalhesTrabalhoVendido);
    }

    private void vaiParaDetalhesTrabalhoVendido(TrabalhoVendido trabalhoVendido) {
        VaiDeTrabalhosVendidosParaDetalhesTrabalhoVendido acao = ListaTrabalhosVendidosFragmentDirections.vaiDeTrabalhosVendidosParaDetalhesTrabalhoVendido(
                trabalhoVendido,
                idPersonagem);
        acao.setCodigoRequisicao(CODIGO_REQUISICAO_ALTERA_VENDAS);
        Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    private void inicializaComponentes() {
        trabalhosVendidos = new ArrayList<>();
        meuRecycler = binding.recyclerViewListaProdutosVendidos;
        swipeRefreshLayout = binding.swipeRefreshLayoutProdutosVendidos;
        indicadorProgresso = binding.indicadorProgressoListaProdutosVendidosFragment;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        textoFiltro = "";
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(PersonagemRepository.getInstance());
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), resultadoPegaPersonagem -> {
            if (resultadoPegaPersonagem == null) return;
            idPersonagem = resultadoPegaPersonagem.getId();
            TrabalhosVendidosViewModelFactory trabalhosVendidosViewModelFactory = new TrabalhosVendidosViewModelFactory(TrabalhoVendidoRepository.getInstance(idPersonagem));
            trabalhosVendidosViewModel = new ViewModelProvider(this, trabalhosVendidosViewModelFactory).get(idPersonagem, TrabalhosVendidosViewModel.class);
            recuperaVendas();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeOuvintePersonagem();
        removeOuvinteVenda();
        binding = null;
    }

    private void removeOuvinteVenda() {
        if (trabalhosVendidosViewModel == null) return;
        trabalhosVendidosViewModel.removeOuvinte();
    }

    private void removeOuvintePersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeOuvinte();
    }
}