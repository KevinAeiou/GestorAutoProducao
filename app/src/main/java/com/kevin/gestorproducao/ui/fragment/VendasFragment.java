package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS;
import static com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.vaiDeVendasParaFiltro;
import static com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.vaiDeVendasParaTrabalhos;
import static com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.vaiDeVendasParaVendasPorTrabalho;
import static com.kevin.gestorproducao.utilitario.Utilitario.filtrarTrabalhos;

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
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentVendasBinding;
import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.VaiDeVendasParaFiltro;
import com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.VaiDeVendasParaTrabalhos;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaTrabalhosVendidosAdapter;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.EstadoAppViewModel;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendasFragment
    extends BaseFragment<FragmentVendasBinding>
    implements MenuProvider
{
    private ListaTrabalhosVendidosAdapter vendasAdapter;
    private ArrayList<TrabalhoVendido> vendas, vendasFiltradas;
    private RecyclerView meuRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private TrabalhosVendidosViewModel vendasViewModel;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
    private PersonagemViewModel personagemViewModel;
    private FiltroViewModel filtroViewModel;
    private FiltroTrabalho filtroAtual;
    private NavController controlador;
    private EstadoAppViewModel estadoAppViewModel;
    private PieChart pieChart;
    private FloatingActionButton floatingActionButton;

    public VendasFragment() {}

    @Override
    protected FragmentVendasBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentVendasBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(
            this,
            getViewLifecycleOwner(),
            androidx.lifecycle.Lifecycle.State.RESUMED
        );

        inicializaComponentes();
        configuraRecyclerView();
        configuraSwipeRefreshLayout();
        configuraDeslizeItem();
        configuraBotaoInsereVenda();
        observarVendas();
        observarPersonagem();
        observarFiltros();

        vendasViewModel.carregarSeNecessario();
    }

    private void observarFiltros() {
        filtroViewModel.getFiltro().observe(
            getViewLifecycleOwner(),
            filtro -> {
                filtroAtual = filtro;
                aplicarFiltros();
            }
        );
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                vendasViewModel.carregarMaisVendidos(resultado.getId());
            }
        );
    }

    private void observarVendas() {
        vendasViewModel.getMaisVendidos().observe(
            getViewLifecycleOwner(),
            resultado -> {
                indicadorProgresso.setVisibility(GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (resultado.getDado() != null) {
                    vendas = resultado.getDado();

                    aplicarFiltros();
                }

                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
                }
            }
        );

        vendasViewModel.getSincronizacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
                    return;
                }

                vendasViewModel.atualizaMaisVendidos();
            }
        );
    }

    private void configuraGrafico() {

        if (vendasFiltradas == null || vendasFiltradas.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        List<Map.Entry<String, Integer>> listaOrdenada = getEntries();

        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : listaOrdenada) {

            entries.add(
                    new PieEntry(
                            entry.getValue(),
                            entry.getKey()
                    )
            );
        }

        PieDataSet dataSet = new PieDataSet(entries, null);

        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        dataSet.setDrawValues(true);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);

        pieChart.getDescription().setEnabled(false);

        pieChart.setDrawEntryLabels(false);

        pieChart.getLegend().setEnabled(true);

        pieChart.setUsePercentValues(false);

        pieChart.setDrawHoleEnabled(true);

        pieChart.setHoleRadius(40f);

        pieChart.setTransparentCircleRadius(45f);

        pieChart.invalidate();
    }

    @NonNull
    private List<Map.Entry<String, Integer>> getEntries() {
        Map<String, Integer> mapa = new HashMap<>();

        for (TrabalhoVendido trabalho : vendasFiltradas) {

            String nomeTrabalho = trabalho.getNome();

            if (nomeTrabalho == null || nomeTrabalho.isEmpty()) {
                nomeTrabalho = getString(R.string.stringDesconhecido);
            }

            int quantidadeAtual = mapa.getOrDefault(nomeTrabalho, 0);

            mapa.put(
                    nomeTrabalho,
                    quantidadeAtual + trabalho.getQuantidade()
            );
        }

        List<Map.Entry<String, Integer>> listaOrdenada =
                new ArrayList<>(mapa.entrySet());

        listaOrdenada.sort(
                (a, b) -> b.getValue().compareTo(a.getValue())
        );
        return listaOrdenada;
    }

    private void aplicarFiltros() {
        configuraGrafico();

        if (filtroAtual == null) {
            vendasFiltradas = (ArrayList<TrabalhoVendido>) vendas.clone();

            vendasAdapter.atualiza(vendasFiltradas);
            atualizaVisibilidadeListaVazia(vendasFiltradas.isEmpty());
            meuRecycler.smoothScrollToPosition(0);
            return;
        }

        vendasFiltradas = filtrarTrabalhos(vendas, filtroAtual);

        vendasAdapter.atualiza(vendasFiltradas);
        atualizaVisibilidadeListaVazia(vendasFiltradas.isEmpty());
        meuRecycler.smoothScrollToPosition(0);
    }

    private void atualizaVisibilidadeListaVazia(boolean listaVazia) {
        if (listaVazia) {
            iconeListaVazia.setVisibility(VISIBLE);
            txtListaVazia.setVisibility(VISIBLE);
            pieChart.setVisibility(GONE);
            return;
        }
        iconeListaVazia.setVisibility(GONE);
        txtListaVazia.setVisibility(GONE);
        pieChart.setVisibility(VISIBLE);
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            false,
            true,
            false,
            true,
            true,
            null,
            false
        );
    }

    private void configuraBotaoInsereVenda() {
        floatingActionButton.setOnClickListener(view -> {
            VaiDeVendasParaTrabalhos acao = vaiDeVendasParaTrabalhos();
            acao.setRequisicao(CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS);
            controlador.navigate(acao);
        });
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            if (controlador.getCurrentDestination().getId() == R.id.listaTrabalhosVendidos) {
                VaiDeVendasParaFiltro acao = vaiDeVendasParaFiltro();
                controlador.navigate(acao);
            }
            return true;
        }
        return false;
    }

    private void configuraDeslizeItem() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                @NonNull RecyclerView.ViewHolder target
            ) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int itemPosicao = viewHolder.getBindingAdapterPosition();
                vendasAdapter = (ListaTrabalhosVendidosAdapter) meuRecycler.getAdapter();
                if (vendasAdapter != null) {
                    TrabalhoVendido trabalhoVendidoRemovido = vendasFiltradas.get(itemPosicao);
                    vendasAdapter.remove(itemPosicao);
                    Snackbar snackbarDesfazer = Snackbar.make(
                        binding.getRoot(),
                        getString(R.string.stringVendaRemovida), Snackbar.LENGTH_LONG
                    );
                    snackbarDesfazer.addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (event != DISMISS_EVENT_ACTION){
                            vendasViewModel.removeVenda(trabalhoVendidoRemovido);
                            removeTrabalhoDaLista(trabalhoVendidoRemovido);
                        }
                        }
                    });
                    snackbarDesfazer.setAction(
                        getString(R.string.stringDesfazer),
                        v -> vendasAdapter.adiciona(trabalhoVendidoRemovido, itemPosicao)
                    );
                    snackbarDesfazer.show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(meuRecycler);
    }

    private void removeTrabalhoDaLista(TrabalhoVendido trabalhoVendidoRemovido) {
        vendasFiltradas.remove(trabalhoVendidoRemovido);
    }

    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            vendasAdapter.limpaLista();

            vendasViewModel.sincronizaVendas();
        });
    }

    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        configurarHideOnScroll(meuRecycler, estadoAppViewModel);

        configuraAdapter();
    }

    private void configuraAdapter() {
        vendasAdapter = new ListaTrabalhosVendidosAdapter(requireContext());
        meuRecycler.setAdapter(vendasAdapter);
        vendasAdapter.setOnItemClickListener(this::vaiParaVendasPorTrabalho);
    }

    private void vaiParaVendasPorTrabalho(TrabalhoVendido trabalho) {
        controlador.navigate(vaiDeVendasParaVendasPorTrabalho(trabalho));
    }

    private void inicializaComponentes() {
        vendas = new ArrayList<>();
        vendasFiltradas = new ArrayList<>();
        meuRecycler = binding.recyclerViewListaProdutosVendidos;
        swipeRefreshLayout = binding.swipeRefreshLayoutProdutosVendidos;
        indicadorProgresso = binding.indicadorProgressoListaProdutosVendidosFragment;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        pieChart = binding.chartProdutosVendidos;
        floatingActionButton = binding.botaoFlutuanteVendas;

        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext());

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        vendasViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(TrabalhosVendidosViewModel.class);

        filtroViewModel = new ViewModelProvider(
            requireActivity()
        ).get(FiltroViewModel.class);

        estadoAppViewModel = new ViewModelProvider(
            requireActivity()
        ).get(EstadoAppViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();

        vendasViewModel.atualizaMaisVendidos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeObservadorPersonagem();
        removeObservadorVenda();
        binding = null;
    }

    private void removeObservadorVenda() {
        if (vendasViewModel == null) return;
        vendasViewModel.removeObservador();
    }

    private void removeObservadorPersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }
}