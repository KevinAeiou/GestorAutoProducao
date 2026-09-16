package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS;
import static com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.vaiDeVendasParaFiltro;
import static com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.vaiDeVendasParaTrabalhos;
import static com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.vaiDeVendasParaVendasPorTrabalho;
import static com.kevin.gestorproducao.utilitario.Utilitario.calcularIntervaloUltimosMeses;
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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentVendasBinding;
import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.PeriodoFiltro;
import com.kevin.gestorproducao.model.TrabalhoChanceVenda;
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.VaiDeVendasParaFiltro;
import com.kevin.gestorproducao.ui.fragment.VendasFragmentDirections.VaiDeVendasParaTrabalhos;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaChanceVendaAdapter;
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
    private ChipGroup chipGroupPeriodo;
    private TextView txtValorPeriodo;
    private ControleFiltroPeriodo controleFiltroPeriodo;
    private TabLayout tabLayoutVendas;
    private ListaChanceVendaAdapter chanceVendaAdapter;
    private ArrayList<TrabalhoChanceVenda> chanceVenda = new ArrayList<>();
    private ArrayList<TrabalhoChanceVenda> chanceVendaFiltrada = new ArrayList<>();
    private RecyclerView meuRecyclerChanceVenda;
    private SwipeRefreshLayout swipeRefreshLayoutChanceVenda;
    private ChipGroup chipGroupPeriodoChanceVenda;
    private TextView txtValorPeriodoChanceVenda;
    private ControleFiltroPeriodo controleFiltroPeriodoChanceVenda;
    private boolean abaChanceVendaSelecionada;

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
        configuraRecyclerViewChanceVenda();
        configuraSwipeRefreshLayout();
        configuraSwipeRefreshLayoutChanceVenda();
        configuraDeslizeItem();
        configuraBotaoInsereVenda();
        configuraFiltroPeriodo();
        configuraFiltroPeriodoChanceVenda();
        configuraTabs();
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
                aplicarFiltrosChanceVenda();
            }
        );
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                vendasViewModel.carregarMaisVendidos(resultado.getId());
                vendasViewModel.carregarChanceVenda(resultado.getId());
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

        vendasViewModel.getChanceVenda().observe(
            getViewLifecycleOwner(),
            resultado -> {
                swipeRefreshLayoutChanceVenda.setRefreshing(false);

                if (resultado.getDado() != null) {
                    chanceVenda = resultado.getDado();

                    aplicarFiltrosChanceVenda();
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
                vendasViewModel.atualizaChanceVenda();
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

    private void configuraTabs() {
        tabLayoutVendas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                abaChanceVendaSelecionada = tab.getPosition() == 1;
                atualizaVisibilidadeAbas();
                atualizaEstadoVazio();
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {}
        });
    }

    private void atualizaVisibilidadeAbas() {
        binding.cardCamposProdutosVendidos.setVisibility(abaChanceVendaSelecionada ? GONE : VISIBLE);
        binding.swipeRefreshLayoutProdutosVendidos.setVisibility(abaChanceVendaSelecionada ? GONE : VISIBLE);
        binding.cardCamposChanceVenda.setVisibility(abaChanceVendaSelecionada ? VISIBLE : GONE);
        swipeRefreshLayoutChanceVenda.setVisibility(abaChanceVendaSelecionada ? VISIBLE : GONE);
    }

    private void configuraRecyclerViewChanceVenda() {
        meuRecyclerChanceVenda.setHasFixedSize(true);
        meuRecyclerChanceVenda.setLayoutManager(new LinearLayoutManager(requireContext()));

        chanceVendaAdapter = new ListaChanceVendaAdapter(requireContext());
        meuRecyclerChanceVenda.setAdapter(chanceVendaAdapter);
    }

    private void configuraSwipeRefreshLayoutChanceVenda() {
        swipeRefreshLayoutChanceVenda.setOnRefreshListener(() -> {
            chanceVendaAdapter.limpaLista();

            vendasViewModel.sincronizaVendas();
        });
    }

    private void configuraFiltroPeriodo() {
        controleFiltroPeriodo = new ControleFiltroPeriodo(
            this,
            chipGroupPeriodo,
            txtValorPeriodo,
            (dataInicio, dataFim, tipo) -> vendasViewModel.atualizaPeriodoVendas(dataInicio, dataFim)
        );

        controleFiltroPeriodo.configurar();
    }

    // Mesmo controle de período da aba "Mais Vendidos" (Dia/Semana/Mês/Ano/Personalizado), mas
    // já abre no chip "Personalizado" com o intervalo pré-preenchido para os últimos 6 meses —
    // a análise de chance de venda não faz sentido sem uma janela de tempo, e isso evita obrigar
    // o usuário a escolher o intervalo manualmente antes de ver algo.
    private void configuraFiltroPeriodoChanceVenda() {
        long[] ultimosSeisMeses = calcularIntervaloUltimosMeses(6);

        controleFiltroPeriodoChanceVenda = new ControleFiltroPeriodo(
            this,
            chipGroupPeriodoChanceVenda,
            txtValorPeriodoChanceVenda,
            (dataInicio, dataFim, tipo) -> vendasViewModel.atualizaPeriodoChanceVenda(dataInicio, dataFim),
            PeriodoFiltro.PERSONALIZADO,
            ultimosSeisMeses[0],
            ultimosSeisMeses[1]
        );

        controleFiltroPeriodoChanceVenda.configurar();
    }

    private void aplicarFiltros() {
        configuraGrafico();

        if (filtroAtual == null) {
            vendasFiltradas = (ArrayList<TrabalhoVendido>) vendas.clone();

            vendasAdapter.atualiza(vendasFiltradas);
            atualizaEstadoVazio();
            meuRecycler.smoothScrollToPosition(0);
            return;
        }

        vendasFiltradas = filtrarTrabalhos(vendas, filtroAtual);

        vendasAdapter.atualiza(vendasFiltradas);
        atualizaEstadoVazio();
        meuRecycler.smoothScrollToPosition(0);
    }

    // Mesmo filtro de descrição/profissão/raridade/nível da aba "Mais Vendidos" (FiltroTrabalho
    // é aplicável a qualquer T extends Trabalho) — sem isso a aba "Chance de Venda" ignorava o
    // filtro escolhido no ícone de busca da toolbar.
    private void aplicarFiltrosChanceVenda() {
        chanceVendaFiltrada = filtrarTrabalhos(chanceVenda, filtroAtual);

        chanceVendaAdapter.atualiza(chanceVendaFiltrada);
        atualizaEstadoVazio();
    }

    // Estado vazio (ícone + texto) é compartilhado pelas duas abas, mas reflete só a lista da
    // aba ativa no momento — o PieChart só existe na aba "Mais Vendidos".
    private void atualizaEstadoVazio() {
        boolean listaVazia = abaChanceVendaSelecionada
            ? chanceVendaFiltrada == null || chanceVendaFiltrada.isEmpty()
            : vendasFiltradas == null || vendasFiltradas.isEmpty();

        iconeListaVazia.setVisibility(listaVazia ? VISIBLE : GONE);
        txtListaVazia.setVisibility(listaVazia ? VISIBLE : GONE);

        if (!abaChanceVendaSelecionada) {
            pieChart.setVisibility(listaVazia ? GONE : VISIBLE);
        }
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            true,
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
        chanceVenda = new ArrayList<>();
        chanceVendaFiltrada = new ArrayList<>();
        meuRecycler = binding.recyclerViewListaProdutosVendidos;
        swipeRefreshLayout = binding.swipeRefreshLayoutProdutosVendidos;
        indicadorProgresso = binding.indicadorProgressoListaProdutosVendidosFragment;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        pieChart = binding.chartProdutosVendidos;
        floatingActionButton = binding.botaoFlutuanteVendas;
        chipGroupPeriodo = binding.chipGroupPeriodoVendas;
        txtValorPeriodo = binding.txtValorPeriodoVendas;
        tabLayoutVendas = binding.tabLayoutVendas;
        meuRecyclerChanceVenda = binding.recyclerViewListaChanceVenda;
        swipeRefreshLayoutChanceVenda = binding.swipeRefreshLayoutChanceVenda;
        chipGroupPeriodoChanceVenda = binding.chipGroupPeriodoChanceVenda;
        txtValorPeriodoChanceVenda = binding.txtValorPeriodoChanceVenda;

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
        vendasViewModel.atualizaChanceVenda();
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