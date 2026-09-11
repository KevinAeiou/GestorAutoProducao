package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE;
import static com.kevin.gestorproducao.ui.fragment.ListaEstoqueFragmentDirections.vaiDeEstoqueParaFiltro;
import static com.kevin.gestorproducao.ui.fragment.ListaEstoqueFragmentDirections.vaiDeEstoqueParaTrabalhos;
import static com.kevin.gestorproducao.ui.fragment.ListaEstoqueFragmentDirections.vaiParaDetalhesEstoque;
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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentListaTrabalhosEstoqueBinding;
import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.ui.fragment.ListaEstoqueFragmentDirections.VaiDeEstoqueParaTrabalhos;
import com.kevin.gestorproducao.ui.fragment.ListaEstoqueFragmentDirections.VaiParaDetalhesEstoque;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaTrabalhoEstoqueAdapter;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.EstadoAppViewModel;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;

public class ListaEstoqueFragment
    extends BaseFragment<FragmentListaTrabalhosEstoqueBinding>
    implements MenuProvider
{
    private ListaTrabalhoEstoqueAdapter estoqueAdapter;
    private RecyclerView meuRecycler;
    private ArrayList<TrabalhoEstoque> estoque, estoqueFiltrado;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorDeProgresso;
    private TrabalhoEstoqueViewModel estoqueViewModel;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
    private FiltroViewModel filtroViewModel;
    private FiltroTrabalho filtroAtual;
    private PersonagemViewModel personagemViewModel;
    private NavController controlador;
    private EstadoAppViewModel estadoAppViewModel;

    @Override
    protected FragmentListaTrabalhosEstoqueBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentListaTrabalhosEstoqueBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(
            this,
            getViewLifecycleOwner(),
            Lifecycle.State.RESUMED
        );

        inicializaComponentes();
        configuraPersonagemSelecionado();
        configuraRecycler();
        configuraSwipeRefreshLayout();
        configuraBotaoInsereTrabalho();
        observarFiltros();
        observarEstoque();

        estoqueViewModel.carregarSeNecessario();
        estoqueViewModel.atualizaEstoque();
    }

    private void observarEstoque() {
        estoqueViewModel.getEstoque().observe(
            getViewLifecycleOwner(),
            resultado -> {
                indicadorDeProgresso.setVisibility(GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (resultado.getDado() != null) {
                    estoque = resultado.getDado();

                    aplicarFiltros();
                }

                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(resultado.getErro());
                }
            }
        );

        estoqueViewModel.getRemocaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) mostraMensagemAncorada(resultado.getErro());
            }
        );

        estoqueViewModel.getSincronizacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada("Erro: "+resultado.getErro());
                    return;
                }

                estoqueViewModel.atualizaEstoque();
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

    private void configuraPersonagemSelecionado() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                estoqueViewModel.setIdPersonagem(personagem.getId());
            }
        );
    }

    private void configuraBotaoInsereTrabalho() {
        binding.floatingButtonFragmentTrabalhosEstoque.setOnClickListener(view -> {
            VaiDeEstoqueParaTrabalhos acao = vaiDeEstoqueParaTrabalhos();
            acao.setRequisicao(CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE);
            controlador.navigate(acao);
        });
    }

    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> estoqueViewModel.sincronizaEstoque());
    }

    private void aplicarFiltros() {
        if (filtroAtual == null) {
            estoqueFiltrado = (ArrayList<TrabalhoEstoque>) estoque.clone();

            estoqueAdapter.atualiza(estoqueFiltrado);
            atualizaVisibilidadeListaVazia(estoqueFiltrado.isEmpty());
            meuRecycler.smoothScrollToPosition(0);
            return;
        }

        estoqueFiltrado = filtrarTrabalhos(estoque, filtroAtual);

        estoqueAdapter.atualiza(estoqueFiltrado);
        atualizaVisibilidadeListaVazia(estoqueFiltrado.isEmpty());
        meuRecycler.smoothScrollToPosition(0);
    }


    private void atualizaVisibilidadeListaVazia(boolean listaVazia) {
        if (listaVazia) {
            iconeListaVazia.setVisibility(VISIBLE);
            txtListaVazia.setVisibility(VISIBLE);
            return;
        }

        iconeListaVazia.setVisibility(GONE);
        txtListaVazia.setVisibility(GONE);
    }

    private void inicializaComponentes() {
        estoque = new ArrayList<>();
        estoqueFiltrado = new ArrayList<>();
        meuRecycler = binding.listaTrabalhoEstoqueRecyclerView;
        swipeRefreshLayout = binding.swipeRefreshLayoutTrabalhosEstoque;
        indicadorDeProgresso = binding.indicadorProgressoListaEstoqueFragment;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext());
        personagemViewModel = new ViewModelProvider(
                requireActivity(),
                viewModelFactory
        ).get(PersonagemViewModel.class);

        filtroViewModel = new ViewModelProvider(
                requireActivity()
        ).get(FiltroViewModel.class);

        estoqueViewModel = new ViewModelProvider(
                requireActivity(),
                viewModelFactory
        ).get(TrabalhoEstoqueViewModel.class);

        estadoAppViewModel = new ViewModelProvider(
                requireActivity()
        ).get(EstadoAppViewModel.class);
    }
    private void configuraRecycler() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        configurarHideOnScroll(meuRecycler, estadoAppViewModel);

        configuraAdapter();
    }
    private void configuraAdapter() {
        estoqueAdapter = new ListaTrabalhoEstoqueAdapter(requireContext());
        meuRecycler.setAdapter(estoqueAdapter);

        configuraCliqueItemEstoque();
    }

    private void configuraCliqueItemEstoque() {
        estoqueAdapter.setOnItemClickListener((trabalho, posicao) -> {
            if (controlador.getCurrentDestination().getId() == R.id.listaTrabalhosEstoque) {
                VaiParaDetalhesEstoque acao = vaiParaDetalhesEstoque(trabalho);
                controlador.navigate(acao);
            }
        });
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            if (controlador.getCurrentDestination().getId() == R.id.listaTrabalhosEstoque)
                controlador.navigate(vaiDeEstoqueParaFiltro());
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeOuvintePersonagem();
        removeOuvinteEstoque();
        binding = null;
    }

    private void removeOuvinteEstoque() {
        if (estoqueViewModel == null) return;
        estoqueViewModel.removeObservador();
    }

    private void removeOuvintePersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }
}