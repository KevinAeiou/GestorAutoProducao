package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.fragment.ListaProfissoesFragmentDirections.vaiDeProfissoesParaDetalhesProfissao;
import static com.kevin.gestorproducao.ui.fragment.ListaProfissoesFragmentDirections.vaiDeProfissoesParaVisualizacaoProfissao;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentListaProfissoesBinding;
import com.kevin.gestorproducao.model.ProfissaoBase;
import com.kevin.gestorproducao.model.Usuario;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaProfissaoAdapter;
import com.kevin.gestorproducao.ui.viewModel.AutenticacaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;

public class ListaProfissoesFragment extends BaseFragment<FragmentListaProfissoesBinding> {

    private ListaProfissaoAdapter profissoesAdapter;
    private ArrayList<ProfissaoBase> profissoes;
    private RecyclerView meuRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private ProfissaoViewModel profissaoViewModel;
    private NavController controlador;
    private FloatingActionButton btnInsere;
    private AutenticacaoViewModel autenticacaoViewModel;

    public ListaProfissoesFragment() {}

    @Override
    protected FragmentListaProfissoesBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentListaProfissoesBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        configuraRecyclerView();
        configuraSwipeRefreshLayout();
        observarProfissoes();
        observarUsuario();

        profissaoViewModel.carregarSeNecessario();
        profissaoViewModel.recuperaProfissoes();
    }

    private void configuraBotaoInsere() {
        btnInsere.setVisibility(VISIBLE);
        btnInsere.setOnClickListener(view ->
            controlador.navigate(vaiDeProfissoesParaDetalhesProfissao())
        );
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            true,
            false,
            false,
            false,
            false,
            getString(R.string.stringProfissoes),
            false
        );
    }

    private void inicializaComponentes() {
        profissoes = new ArrayList<>();
        meuRecycler = binding.recyclerViewListaProfissoesFragment;
        swipeRefreshLayout = binding.swipeRefreshLayoutListaProfissoesFragment;
        indicadorProgresso = binding.indicadorProgressoListaProfissoesFragment;
        btnInsere = binding.floatingButtonFragmentProfissoes;
        controlador = Navigation.findNavController(binding.getRoot());

        btnInsere.setVisibility(GONE);

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext());

        profissaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(ProfissaoViewModel.class);

        autenticacaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(AutenticacaoViewModel.class);
    }

    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        configuraAdapter();
    }

    private void configuraAdapter() {
        profissoesAdapter = new ListaProfissaoAdapter();
        meuRecycler.setAdapter(profissoesAdapter);

        configuraCliqueItemProfissao();
    }

    private void configuraCliqueItemProfissao() {
        profissoesAdapter.setOnItemClickListener(
            (profissao, posicao) ->
                controlador.navigate(vaiDeProfissoesParaVisualizacaoProfissao(profissao)));
    }

    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            profissoesAdapter.limpaLista();

            profissaoViewModel.sincronizaProfissoes();
        });
    }

    private void observarProfissoes() {
        profissaoViewModel.getProfissoes().observe(
            getViewLifecycleOwner(),
            resultadoProfissoes -> {
                if (resultadoProfissoes.getDado() != null) {
                    indicadorProgresso.setVisibility(GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    profissoes = resultadoProfissoes.getDado();
                    profissoesAdapter.atualiza(profissoes);
                }
                if (resultadoProfissoes.getErro() != null) {
                    mostraMensagemAncorada("Erro" + resultadoProfissoes.getErro());
                }
            }
        );

        profissaoViewModel.getSincronizacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada("Erro: " + resultado.getErro());
                }

                profissaoViewModel.recuperaProfissoes();
            }
        );
    }

    private void observarUsuario() {
        autenticacaoViewModel.getUsuarioAtual().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado == null) return;

                Usuario usuario = resultado.getDado();

                if (usuario == null) return;

                if (usuario.isAdministrador()) {
                    configuraBotaoInsere();
                }
            }
        );

        autenticacaoViewModel.recuperaUsuarioAtual();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeOuvinteProfissao();
        binding = null;
    }

    private void removeOuvinteProfissao() {
        if (profissaoViewModel == null) return;
        profissaoViewModel.removeObservador();
    }

}