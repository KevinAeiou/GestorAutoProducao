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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.kevin.gestorproducao.service.ServicosProducaoPersonagem;
import com.kevin.gestorproducao.ui.fragment.ListaTrabalhosProducaoFragmentDirections.VaiDeProducaoParaFiltro;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaTrabalhoProducaoAdapter;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.EstadoAppViewModel;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;

public class ListaTrabalhosProducaoFragment
    extends BaseFragment<FragmentListaTrabalhosProducaoBinding>
    implements MenuProvider
{
    private ListaTrabalhoProducaoAdapter trabalhoAdapter;
    private RecyclerView meuRecycler;
    private ArrayList<TrabalhoProducao> trabalhos, trabalhosFiltrados;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
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
                    mostraMensagemAncorada("Erro: " + resultado.getErro());
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

                mostraMensagemAncorada("Erro: "+ resultado.getErro());
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
                int posicao = viewHolder.getBindingAdapterPosition();
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
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int posicao = viewHolder.getBindingAdapterPosition();
                trabalhoSelecionado = trabalhosFiltrados.get(posicao);

                estadoAnterior = trabalhoSelecionado.getEstado();
                trabalhoSelecionado.atualizarEstado(defineNovoEstado(trabalhoSelecionado, direction));
                trabalhoSelecionado.marcarModificacao();
                meuRecycler.getAdapter().notifyItemChanged(posicao);

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
                if (planejamentoProducaoService != null) {
                    planejamentoProducaoService.incluirMaisVendidos();
                    planejamentoProducaoService.incluirComunsProfissoesPriorizadas();
                }

                producaoViewModel.sincronizaProducao();
            } catch (ProducaoException e) {
                mostraMensagemAncorada(e.getMessage());
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
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        floatingActionButton = binding.floatingActionButton;

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
    }

    private void vaiParaDetalhesProducaoActivity(TrabalhoProducao trabalho) {
         NavDirections acao = vaiParaDetalhesProducao(trabalho);
         controlador.navigate(acao);
    }

    private void aplicarFiltros() {
        if (filtroAtual == null) {
            trabalhosFiltrados = (ArrayList<TrabalhoProducao>) trabalhos.clone();

            trabalhoAdapter.atualiza(trabalhosFiltrados);
            atualizaVisibilidadeListaVazia(trabalhosFiltrados.isEmpty());
            meuRecycler.smoothScrollToPosition(0);
            return;
        }

        trabalhosFiltrados = filtrarTrabalhos(trabalhos, filtroAtual);

        // getEstado() é específico de TrabalhoProducao (para produzir/produzindo/feito), então
        // não faz parte do filtro genérico de Utilitario.filtrarTrabalhos — aplicado à parte.
        boolean temEstado = filtroAtual.getEstado() != -1;
        if (temEstado) {
            trabalhosFiltrados.removeIf(trabalho -> trabalho.getEstado() != filtroAtual.getEstado());
        }

        trabalhoAdapter.atualiza(trabalhosFiltrados);
        atualizaVisibilidadeListaVazia(trabalhosFiltrados.isEmpty());
        meuRecycler.smoothScrollToPosition(0);
    }

    private void atualizaVisibilidadeListaVazia(boolean listaVazia) {
        if (listaVazia) {
            iconeListaVazia.setVisibility(View.VISIBLE);
            txtListaVazia.setVisibility(View.VISIBLE);
            return;
        }

        iconeListaVazia.setVisibility(View.GONE);
        txtListaVazia.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

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