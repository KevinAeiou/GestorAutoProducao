package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO;
import static com.kevin.gestorproducao.ui.fragment.ListaTodosTrabalhosFragmentDirections.vaiDeTrabalhosParaDetalhesTrabalho;
import static com.kevin.gestorproducao.ui.fragment.ListaTodosTrabalhosFragmentDirections.vaiDeTrabalhosParaFiltro;
import static com.kevin.gestorproducao.ui.fragment.ListaTodosTrabalhosFragmentDirections.vaiDeTrabalhosParaVisualizacaoTrabalho;
import static com.kevin.gestorproducao.utilitario.Utilitario.filtrarTrabalhos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentListaTodosTrabalhosBinding;
import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.model.Usuario;
import com.kevin.gestorproducao.ui.fragment.ListaTodosTrabalhosFragmentDirections.VaiDeTrabalhosParaDetalhesTrabalho;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaTrabalhoEspecificoNovaProducaoAdapter;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListener;
import com.kevin.gestorproducao.ui.viewModel.AutenticacaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;

public class ListaTodosTrabalhosFragment
    extends BaseFragment<FragmentListaTodosTrabalhosBinding>
    implements MenuProvider
{
    private ListaTrabalhoEspecificoNovaProducaoAdapter trabalhosAdapter;
    private FloatingActionButton botaoNovoTrabalho;
    private RecyclerView meuRecycler;
    private ArrayList<Trabalho> trabalhos, trabalhosFiltrados;
    private ProgressBar indicadorProgresso;
    private TrabalhoViewModel trabalhoViewModel;
    private FiltroViewModel filtroViewModel;
    private TextView txtListaVazia;
    private ImageView iconeListaVazia;
    private NavController controlador;
    private FiltroTrabalho filtroAtual;
    private AutenticacaoViewModel autenticacaoViewModel;

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
        observarTrabalhos();
        observarFiltros();
        observarUsuario();

        trabalhoViewModel.carregarSeNecessario();
        trabalhoViewModel.recuperaTrabalhos();
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

    private void aplicarFiltros() {
        if (filtroAtual == null) {
            trabalhosFiltrados = (ArrayList<Trabalho>) trabalhos.clone();

            trabalhosAdapter.atualiza(trabalhosFiltrados);
            atualizaVisibilidadeListaVazia(trabalhosFiltrados.isEmpty());
            meuRecycler.smoothScrollToPosition(0);
            return;
        }

        trabalhosFiltrados = filtrarTrabalhos(trabalhos, filtroAtual);

        trabalhosAdapter.atualiza(trabalhosFiltrados);
        atualizaVisibilidadeListaVazia(trabalhosFiltrados.isEmpty());
        meuRecycler.smoothScrollToPosition(0);
    }

    private void observarUsuario() {
        autenticacaoViewModel.getUsuarioAtual().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado == null) return;

                Usuario usuario = resultado.getDado();

                if (usuario == null) return;

                if (usuario.isAdministrador()) {
                    configuraBotaoCadastraNovoTrabalho();
                }
            }
        );

        autenticacaoViewModel.recuperaUsuarioAtual();
    }

    private void observarTrabalhos() {
        trabalhoViewModel.getTrabalhos().observe(
            getViewLifecycleOwner(),
            resultado -> {
                indicadorProgresso.setVisibility(View.GONE);

                if (resultado.getDado() != null) {
                    trabalhos = resultado.getDado();

                    aplicarFiltros();
                }
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada("Erro: "+resultado.getErro());
                }
            }
        );

        trabalhoViewModel.getSincronizacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada("Erro: "+resultado.getErro());
                    return;
                }

                trabalhoViewModel.recuperaTrabalhos();
            }
        );
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            if (controlador.getCurrentDestination().getId() == R.id.listaTrabalhos)
                controlador.navigate(vaiDeTrabalhosParaFiltro());
            return true;
        }
        return false;
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            false,
            true,
            false,
            false,
            false,
            "Trabalhos",
            false
        );
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
        trabalhos = new ArrayList<>();
        trabalhosFiltrados = new ArrayList<>();
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        botaoNovoTrabalho = binding.floatingButtonProfissoesTrabalhos;
        indicadorProgresso = binding.indicadorProgressoTrabalhos;
        meuRecycler = binding.recyclerViewTrabalhos;
        controlador = Navigation.findNavController(binding.getRoot());

        botaoNovoTrabalho.setVisibility(GONE);

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext());

        trabalhoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoViewModel.class);

        filtroViewModel = new ViewModelProvider(
            requireActivity()
        ).get(FiltroViewModel.class);

        autenticacaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(AutenticacaoViewModel.class);
    }
    private void configuraBotaoCadastraNovoTrabalho() {
        botaoNovoTrabalho.setVisibility(VISIBLE);
        botaoNovoTrabalho.setOnClickListener(getClickListener());
    }

    private OnClickListener getClickListener() {
        return view -> vaiParaDetalhesTrabalho();
    }

    private void vaiParaDetalhesTrabalho() {
        VaiDeTrabalhosParaDetalhesTrabalho acao = vaiDeTrabalhosParaDetalhesTrabalho();
        acao.setCodigoRequisicao(CODIGO_REQUISICAO_INSERE_TRABALHO);
        acao.setTrabalho(null);
        controlador.navigate(acao);
    }

    private void configuraSwipeRefreshLayout() {
        SwipeRefreshLayout swipeRefreshLayout = binding.swipeRefreshTrabalhos;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            trabalhoViewModel.sincronizaTrabalhos();
        });
    }

    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter();
    }

    private void configuraAdapter() {
        trabalhosAdapter = new ListaTrabalhoEspecificoNovaProducaoAdapter(requireContext());
        meuRecycler.setAdapter(trabalhosAdapter);

        trabalhosAdapter.setOnItemClickListener(getItemClickListener());
    }

    @NonNull
    private OnItemClickListener getItemClickListener() {
        return (trabalho, adapterPosition) ->
            controlador.navigate(vaiDeTrabalhosParaVisualizacaoTrabalho(trabalho));
    }

    @Override
    protected FragmentListaTodosTrabalhosBinding inflateBinding(
            LayoutInflater inflater,
            ViewGroup container
    ) {
        return FragmentListaTodosTrabalhosBinding.inflate(
                inflater,
                container,
                false
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeOuvinteTrabalho();
        binding = null;
    }


    private void removeOuvinteTrabalho() {
        if (trabalhoViewModel == null) return;
        trabalhoViewModel.removeObservador();
    }

}