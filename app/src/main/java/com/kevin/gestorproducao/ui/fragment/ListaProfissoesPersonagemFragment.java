package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.fragment.ListaProfissoesPersonagemFragmentDirections.vaiParaDetalhesProfissaoPersonagem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentListaProfissoesPersonagemBinding;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.ui.fragment.ListaProfissoesPersonagemFragmentDirections.VaiParaDetalhesProfissaoPersonagem;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaProfissaoPersonagemAdapter;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.EstadoAppViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoPersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;

public class ListaProfissoesPersonagemFragment
    extends BaseFragment<FragmentListaProfissoesPersonagemBinding>
{
    private ListaProfissaoPersonagemAdapter profissaoPersonagemAdapter;
    private ArrayList<ProfissaoPersonagem> profissoes;
    private RecyclerView meuRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private ProfissaoPersonagemViewModel profissaoPersonagemViewModel;
    private PersonagemViewModel personagemViewModel;
    private EstadoAppViewModel estadoAppViewModel;

    public ListaProfissoesPersonagemFragment() {
    }
    @Override
    protected FragmentListaProfissoesPersonagemBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentListaProfissoesPersonagemBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        configuraRecyclerView();
        configuraSwipeRefreshLayout();
        configuraPersonagemSelecionado();
        observarProfissoes();

        profissaoPersonagemViewModel.carregarSeNecessario();
        profissaoPersonagemViewModel.atualizaProfissoes();
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            true,
            false,
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
            resultado -> {
                if (resultado == null) return;

                profissaoPersonagemViewModel.setIdPersonagem(resultado.getId());
            }
        );
    }

    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        configurarHideOnScroll(meuRecycler, estadoAppViewModel);

        configuraAdapter();
    }

    private void configuraAdapter() {
        profissaoPersonagemAdapter = new ListaProfissaoPersonagemAdapter(requireContext());
        meuRecycler.setAdapter(profissaoPersonagemAdapter);

        configuraCliqueItemProfissao();
    }

    private void configuraCliqueItemProfissao() {
        profissaoPersonagemAdapter.setOnItemClickListener(
            (profissao, adapterPosition) -> {
                VaiParaDetalhesProfissaoPersonagem acao = vaiParaDetalhesProfissaoPersonagem(profissao);
                NavController navController = Navigation.findNavController(binding.getRoot());
                navController.navigate(acao);
            }
        );
    }

    private void observarProfissoes() {
        profissaoPersonagemViewModel.getProfissoesPersonagem().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getDado() != null) {
                    profissoes = resultado.getDado();

                    indicadorProgresso.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    profissaoPersonagemAdapter.atualiza(profissoes);
                }
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
                }
            }
        );

        profissaoPersonagemViewModel.getSincronizacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
                    return;
                }

                profissaoPersonagemViewModel.atualizaProfissoes();
            }
        );
    }

    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            profissaoPersonagemAdapter.limpaLista();

            profissaoPersonagemViewModel.sincronizaProfissoes();
        });
    }

    private void inicializaComponentes() {
        profissoes = new ArrayList<>();
        meuRecycler = binding.recyclerViewListaProfissoesPersonagemFragment;
        swipeRefreshLayout = binding.swipeRefreshLayoutListaProfissoesPersonagemFragment;
        indicadorProgresso = binding.indicadorProgressoListaProfissoesPersonagemFragment;

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext());
        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        profissaoPersonagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(ProfissaoPersonagemViewModel.class);

        estadoAppViewModel = new ViewModelProvider(
            requireActivity()
        ).get(EstadoAppViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeOuvinteProfissao();
        removeOuvintePersonagem();
        binding = null;
    }

    private void removeOuvintePersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }

    private void removeOuvinteProfissao() {
        if (profissaoPersonagemViewModel == null) return;
        profissaoPersonagemViewModel.removeObservador();
    }
}