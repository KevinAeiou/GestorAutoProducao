package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.fragment.ListaTrabalhosProducaoFragmentDirections.vaiDeProducaoParaFiltro;
import static com.kevin.gestorproducao.ui.fragment.ListaTrabalhosProducaoFragmentDirections.vaiParaDetalhesProducao;
import static com.kevin.gestorproducao.ui.fragment.ListaTrabalhosProducaoFragmentDirections.vaiParaNovaProducao;
import static com.kevin.gestorproducao.utilitario.Utilitario.filtrarTrabalhos;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentListaTrabalhosProducaoBinding;
import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;
import com.kevin.gestorproducao.rules.exception.ProducaoException;
import com.kevin.gestorproducao.service.PlanejamentoProducaoService;
import com.kevin.gestorproducao.service.ProducaoFluxoService;
import com.kevin.gestorproducao.service.ProducaoServicosFactory;
import com.kevin.gestorproducao.service.ResumoPlanejamento;
import com.kevin.gestorproducao.service.ServicosProducaoPersonagem;
import com.kevin.gestorproducao.ui.componente.EstadoVazioView;
import com.kevin.gestorproducao.ui.fragment.ListaTrabalhosProducaoFragmentDirections.VaiDeProducaoParaFiltro;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaTrabalhoProducaoAdapter;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemLongClickListenerTrabalhoProducao;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.EstadoAppViewModel;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListaTrabalhosProducaoFragment
    extends BaseFragment<FragmentListaTrabalhosProducaoBinding>
    implements MenuProvider
{
    // Tempo em que o item deslizado permanece na posição atual, já com o novo estado, antes de
    // ser reposicionado (ou removido, se não passar mais no filtro).
    private static final long ATRASO_REORGANIZAR_LISTA_MS = 800;

    // Mesma ordem de ProducaoDao.recuperaProducoes: estado, profissão, raridade, nível e nome.
    private static final Comparator<TrabalhoProducao> ORDEM_PRODUCAO = Comparator
        .comparing(TrabalhoProducao::getEstado, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(TrabalhoProducao::getProfissao, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(TrabalhoProducao::getRaridade, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(TrabalhoProducao::getNivel, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(TrabalhoProducao::getNome, Comparator.nullsFirst(Comparator.naturalOrder()));

    private final Runnable reorganizaLista = this::reorganizaListaAposDeslize;
    private ListaTrabalhoProducaoAdapter trabalhoAdapter;
    private ActionMode modoSelecao;
    private RecyclerView meuRecycler;
    private ArrayList<TrabalhoProducao> trabalhos, trabalhosFiltrados;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private EstadoVazioView estadoVazio;
    private FloatingActionButton floatingActionButton;
    private PersonagemViewModel personagemViewModel;
    private TrabalhoProducaoViewModel producaoViewModel;
    private TrabalhoViewModel trabalhoViewModel;
    private EstadoAppViewModel estadoAppViewModel;
    private FiltroViewModel filtroViewModel;
    private NavController controlador;
    private FiltroTrabalho filtroAtual;
    private TrabalhoProducao trabalhoSelecionado;
    private int estadoAnterior = -1;
    private float ultimoDX = 0;
    private ProducaoFluxoService producaoFluxoService;
    private Context context;
    private TrabalhoRepository trabalhoRepo;
    private TrabalhoEstoqueRepository estoqueRepo;
    private TrabalhoProducaoRepository producaoRepo;
    private ProfissaoPersonagemRepository profissaoPersonagemRepo;
    private PlanejamentoProducaoService planejamentoProducaoService;
    private ChipGroup chipGroupPeriodo;
    private TextView txtValorPeriodo;
    private ControleFiltroPeriodo controleFiltroPeriodo;
    private long[] intervaloPeriodoAtual;

    @Override
    protected FragmentListaTrabalhosProducaoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentListaTrabalhosProducaoBinding.inflate(inflater, container, false);
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

        controlador.getCurrentBackStackEntry()
            .getSavedStateHandle()
            .getLiveData("mensagem_sucesso")
            .observe(
                    getViewLifecycleOwner(),
                    mensagem -> {

                        if (mensagem == null) return;

                        mostraMensagemAncorada(mensagem.toString());

                        controlador.getCurrentBackStackEntry()
                                .getSavedStateHandle()
                                .remove("mensagem_sucesso");
                    }
            );

        configuraRecycler();
        configuraSwipeRefreshLayout();
        configuraBotaoInsereTrabalho();
        configuraDeslizeItem();
        configuraPersonagemSelecionado();
        configuraFiltroPeriodo();
        observarFiltros();
        observarProducao();

        producaoViewModel.carregarSeNecessario();
        producaoViewModel.atualizaProducao();
    }

    private void observarProducao() {
        producaoViewModel.getSincronizacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
                    return;
                }

                producaoViewModel.atualizaProducao();
            }
        );

        producaoViewModel.getProducoes().observe(
            getViewLifecycleOwner(),
            resultado -> {
                indicadorProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (resultado.getDado() != null) {
                    trabalhos = resultado.getDado();

                    aplicarFiltros();
                }

                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(resultado.getErro());
                }
            }
        );

        producaoViewModel.getRemocaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                producaoViewModel.limpaRemocaoResultado();

                if (resultado.getErro() == null) {
                    finalizaModoSelecao();
                    producaoViewModel.atualizaProducao();
                    return;
                }

                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );

        producaoViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    producaoViewModel.limpaModificacaoResultado();

                    producaoFluxoService.processarPosModificacao(
                        trabalhoSelecionado,
                        estadoAnterior
                    );

                    return;
                }

                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );
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

    private void configuraPersonagemSelecionado() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                producaoViewModel.setIdPersonagem(personagem.getId());

                ServicosProducaoPersonagem servicos = ProducaoServicosFactory.cria(
                    trabalhoRepo,
                    estoqueRepo,
                    producaoRepo,
                    profissaoPersonagemRepo,
                    personagem.getId(),
                    context
                );

                planejamentoProducaoService = servicos.getPlanejamentoProducaoService();
                planejamentoProducaoService.setOuvinteFalhaGravacao(erro ->
                    mostraMensagemAncorada("Falha ao gravar produção: " + erro)
                );
                planejamentoProducaoService.setOuvinteResumoAutomatico(resumo ->
                    mostraMensagemAncorada(resumo.paraMensagem())
                );
                producaoFluxoService = servicos.getProducaoFluxoService();
            }
        );
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

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            if (controlador.getCurrentDestination().getId() == R.id.listaTrabalhosProducao) {
                VaiDeProducaoParaFiltro acao = vaiDeProducaoParaFiltro();
                acao.setEhProducao(true);
                controlador.navigate(acao);
            }
            return true;
        }
        return false;
    }

    private void configuraDeslizeItem() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT
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
            public int getSwipeDirs(
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder
            ) {
                if (trabalhoAdapter.isModoSelecao()) return 0;

                int posicao = viewHolder.getBindingAdapterPosition();
                if (posicao == RecyclerView.NO_POSITION) return 0;

                TrabalhoProducao trabalho = trabalhosFiltrados.get(posicao);

                if (trabalho.ehProduzir()) {
                    return ItemTouchHelper.LEFT;
                }

                if (trabalho.ehFeito()) {
                    return ItemTouchHelper.RIGHT;
                }

                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 1f / 3f;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int posicao = viewHolder.getBindingAdapterPosition();
                if (posicao == RecyclerView.NO_POSITION) return;

                trabalhoSelecionado = trabalhosFiltrados.get(posicao);

                estadoAnterior = trabalhoSelecionado.getEstado();
                trabalhoSelecionado.atualizarEstado(defineNovoEstado(trabalhoSelecionado, direction));
                trabalhoSelecionado.marcarModificacao();
                meuRecycler.getAdapter().notifyItemChanged(posicao);
                agendaReorganizacaoLista();

                TrabalhoProducao producao = getTrabalhoProducao();

                producaoViewModel.modificaTrabalhoProducao(producao);
            }

            @Override
            public void onChildDraw(
                @NonNull Canvas c,
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                float dX,
                float dY,
                int actionState,
                boolean isCurrentlyActive
            ) {
                View itemView = viewHolder.itemView;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    if (isCurrentlyActive) {
                        ultimoDX = dX;
                        itemView.setTranslationX(dX);
                        return;
                    }

                    if (ultimoDX < 0) {
                        itemView.setTranslationX(itemView.getWidth());
                    } else {
                        itemView.setTranslationX(-itemView.getWidth());
                    }

                    itemView.animate()
                        .translationX(0)
                        .setDuration(250)
                        .start();
                    return;
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(meuRecycler);
    }

    private void agendaReorganizacaoLista() {
        meuRecycler.removeCallbacks(reorganizaLista);
        meuRecycler.postDelayed(reorganizaLista, ATRASO_REORGANIZAR_LISTA_MS);
    }

    private void reorganizaListaAposDeslize() {
        if (binding == null) return;

        trabalhos.sort(ORDEM_PRODUCAO);
        aplicarFiltros(false);
    }

    @NonNull
    private TrabalhoProducao getTrabalhoProducao() {
        TrabalhoProducao producao = new TrabalhoProducao();

        producao.setId(trabalhoSelecionado.getId());
        producao.setIdTrabalho(trabalhoSelecionado.getIdTrabalho());
        producao.setExperiencia(trabalhoSelecionado.getExperiencia());
        producao.setEstado(trabalhoSelecionado.getEstado());
        producao.setTipoLicenca(trabalhoSelecionado.getTipoLicenca());
        producao.setRecorrencia(trabalhoSelecionado.getRecorrencia());
        producao.setCriadoEm(trabalhoSelecionado.getCriadoEm());
        producao.setIniciadoEm(trabalhoSelecionado.getIniciadoEm());
        producao.setFinalizadoEm(trabalhoSelecionado.getFinalizadoEm());

        return producao;
    }

    private static int defineNovoEstado(TrabalhoProducao trabalho, int direcao) {
        if (trabalho.ehProduzir() || trabalho.ehFeito()) return  1;
        if (trabalho.ehProduzindo() && direcao == ItemTouchHelper.RIGHT) return 0;
        if (trabalho.ehProduzindo() && direcao == ItemTouchHelper.LEFT) return 2;

        return 0;
    }

    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            try {
                String mensagem = null;

                if (planejamentoProducaoService != null) {
                    try {
                        planejamentoProducaoService.incluirMaisVendidos();
                    } catch (ProducaoException e) {
                        mensagem = e.getMessage();
                    }

                    ResumoPlanejamento resumo =
                        planejamentoProducaoService.incluirComunsProfissoesPriorizadas();
                    if (resumo.temNovidades()) {
                        mensagem = mensagem == null ?
                            resumo.paraMensagem() :
                            mensagem + " · " + resumo.paraMensagem();
                    }
                }

                producaoViewModel.sincronizaProducao();

                if (mensagem != null) mostraMensagemAncorada(mensagem);
            } finally {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void configuraBotaoInsereTrabalho() {
        floatingActionButton.setOnClickListener(v -> controlador.navigate(
            vaiParaNovaProducao()
        ));
    }

    private void inicializaComponentes() {
        filtroAtual = null;
        trabalhos = new ArrayList<>();
        trabalhosFiltrados = new ArrayList<>();
        meuRecycler = binding.listaTrabalhoRecyclerView;
        swipeRefreshLayout = binding.swipeRefreshLayoutTrabalhos;
        indicadorProgresso = binding.indicadorProgressoListaTrabalhosFragment;
        estadoVazio = binding.estadoVazio;
        floatingActionButton = binding.floatingActionButton;
        chipGroupPeriodo = binding.chipGroupPeriodoProducao;
        txtValorPeriodo = binding.txtValorPeriodoProducao;

        controlador = Navigation.findNavController(binding.getRoot());
        context = requireContext().getApplicationContext();

        trabalhoRepo = TrabalhoRepository.getInstancia(context);
        estoqueRepo = TrabalhoEstoqueRepository.getInstance(context);
        producaoRepo = TrabalhoProducaoRepository.getInstance(context);
        profissaoPersonagemRepo = ProfissaoPersonagemRepository.getInstance(context);

        ViewModelFactory viewModelFactory = new ViewModelFactory(
            context
        );
        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        producaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoProducaoViewModel.class);

        filtroViewModel = new ViewModelProvider(
            requireActivity()
        ).get(FiltroViewModel.class);

        estadoAppViewModel = new ViewModelProvider(
            requireActivity()
        ).get(EstadoAppViewModel.class);

        trabalhoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoViewModel.class);
    }

    private void configuraRecycler() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(context));
        configuraAdapter();
    }

    private void configuraAdapter() {
        trabalhoAdapter = new ListaTrabalhoProducaoAdapter(context);
        meuRecycler.setAdapter(trabalhoAdapter);
        configurarHideOnScroll(meuRecycler, estadoAppViewModel);

        trabalhoAdapter.setOnItemClickListener(this::vaiParaDetalhesProducaoActivity);
        trabalhoAdapter.setOnItemLongClickListener(new OnItemLongClickListenerTrabalhoProducao() {
            @Override
            public void onItemLongClick(int posicao) {
                if (trabalhoAdapter.isModoSelecao()) {
                    alternaSelecao(posicao);
                    return;
                }

                iniciaModoSelecao(posicao);
            }

            @Override
            public void onItemSelecaoClick(int posicao) {
                alternaSelecao(posicao);
            }
        });
    }

    private void iniciaModoSelecao(int posicao) {
        trabalhoAdapter.iniciaSelecao(posicao);

        modoSelecao = ((AppCompatActivity) requireActivity()).startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_selecao_producao, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                mode.setTitle(
                    getString(
                        R.string.stringSelecionadosProducao,
                        trabalhoAdapter.getQuantidadeSelecionados()
                    )
                );
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() != R.id.itemMenuExcluirSelecionados) return false;

                confirmaExclusaoSelecionados();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                modoSelecao = null;
                trabalhoAdapter.encerraSelecao();
            }
        });
    }

    private void alternaSelecao(int posicao) {
        trabalhoAdapter.alternaSelecao(posicao);

        if (trabalhoAdapter.getQuantidadeSelecionados() == 0) {
            finalizaModoSelecao();
            return;
        }

        if (modoSelecao != null) modoSelecao.invalidate();
    }

    private void finalizaModoSelecao() {
        if (modoSelecao != null) modoSelecao.finish();
    }

    private void confirmaExclusaoSelecionados() {
        List<TrabalhoProducao> selecionados = trabalhoAdapter.getSelecionados();
        if (selecionados.isEmpty()) return;

        ConfirmacaoDialog dialog = ConfirmacaoDialog.novaInstancia(
            getString(R.string.stringExcluirProducoesSelecionadas),
            getString(
                R.string.stringConfirmaExclusaoProducoesSelecionadas,
                selecionados.size()
            ),
            () -> producaoViewModel.removeTrabalhosProducao(selecionados),
            () -> {}
        );

        dialog.show(getParentFragmentManager(), "confirmacao_exclusao_multipla");
    }

    private void vaiParaDetalhesProducaoActivity(TrabalhoProducao trabalho) {
         NavDirections acao = vaiParaDetalhesProducao(trabalho);
         controlador.navigate(acao);
    }

    private void aplicarFiltros() {
        aplicarFiltros(true);
    }

    private void aplicarFiltros(boolean rolarParaTopo) {
        ArrayList<TrabalhoProducao> base;

        if (filtroAtual == null) {
            base = (ArrayList<TrabalhoProducao>) trabalhos.clone();
        } else {
            base = filtrarTrabalhos(trabalhos, filtroAtual);

            // getEstado() é específico de TrabalhoProducao (para produzir/produzindo/feito), então
            // não faz parte do filtro genérico de Utilitario.filtrarTrabalhos — aplicado à parte.
            boolean temEstado = filtroAtual.getEstado() != -1;
            if (temEstado) {
                base.removeIf(trabalho -> trabalho.getEstado() != filtroAtual.getEstado());
            }
        }

        trabalhosFiltrados = filtrarPorPeriodo(base);

        trabalhoAdapter.atualiza(trabalhosFiltrados);
        atualizaVisibilidadeListaVazia(trabalhosFiltrados.isEmpty());

        if (trabalhoAdapter.isModoSelecao() && trabalhoAdapter.getQuantidadeSelecionados() == 0) {
            finalizaModoSelecao();
        }

        if (rolarParaTopo) {
            meuRecycler.smoothScrollToPosition(0);
        }
    }

    // Trabalhos para produzir/produzindo aparecem sempre, independentemente do período
    // selecionado — apenas os concluídos são restritos ao intervalo escolhido (por finalizadoEm).
    // O período é controlado direto na tela (fora do modal de filtro), então é aplicado sempre,
    // independente de haver ou não um FiltroTrabalho ativo.
    private ArrayList<TrabalhoProducao> filtrarPorPeriodo(ArrayList<TrabalhoProducao> trabalhos) {
        if (intervaloPeriodoAtual == null) return trabalhos;

        long dataInicio = intervaloPeriodoAtual[0];
        long dataFim = intervaloPeriodoAtual[1];

        ArrayList<TrabalhoProducao> resultado = new ArrayList<>();

        for (TrabalhoProducao trabalho : trabalhos) {
            if (!trabalho.ehFeito()) {
                resultado.add(trabalho);
                continue;
            }

            Long finalizadoEm = trabalho.getFinalizadoEm();

            if (finalizadoEm != null && finalizadoEm >= dataInicio && finalizadoEm <= dataFim) {
                resultado.add(trabalho);
            }
        }

        return resultado;
    }

    private void configuraFiltroPeriodo() {
        controleFiltroPeriodo = new ControleFiltroPeriodo(
            this,
            chipGroupPeriodo,
            txtValorPeriodo,
            (dataInicio, dataFim, tipo) -> {
                intervaloPeriodoAtual = new long[]{dataInicio, dataFim};

                if (trabalhoAdapter != null) {
                    aplicarFiltros();
                }
            }
        );

        controleFiltroPeriodo.configurar();
    }

    private void atualizaVisibilidadeListaVazia(boolean listaVazia) {
        if (listaVazia) {
            estadoVazio.setVisibility(View.VISIBLE);
            return;
        }

        estadoVazio.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        finalizaModoSelecao();
        meuRecycler.removeCallbacks(reorganizaLista);
        removeObservadorProducao();
        removeObservadorPersonagem();
        removeObservadorTrabalho();
        binding = null;
    }

    private void removeObservadorTrabalho() {
        if (trabalhoViewModel == null) return;
        trabalhoViewModel.removeObservador();
    }

    private void removeObservadorPersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }

    private void removeObservadorProducao() {
        if (producaoViewModel == null) return;
        producaoViewModel.removeObservador();
    }
}