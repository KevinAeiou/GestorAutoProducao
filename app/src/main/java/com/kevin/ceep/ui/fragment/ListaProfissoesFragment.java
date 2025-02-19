package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.Constantes.CHAVE_PERSONAGEM;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.databinding.FragmentListaProfissoesBinding;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.repository.ProfissaoRepository;
import com.kevin.ceep.ui.recyclerview.adapter.ListaProfissaoAdapter;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.ProfissaoViewModelFactory;

import java.util.ArrayList;

public class ListaProfissoesFragment extends Fragment {
    private FragmentListaProfissoesBinding binding;
    private ListaProfissaoAdapter listaProfissaoAdapter;
    private String personagemId;
    private ArrayList<Profissao> todasProfissoes;
    private RecyclerView meuRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private ProfissaoViewModel profissaoViewModel;
    private PersonagemViewModel personagemViewModel;

    public ListaProfissoesFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaProfissoesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.navigationMenu = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        inicializaComponentes();
        configuraRecyclerView();
        configuraSwipeRefreshLayout();
    }
    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter(meuRecycler);
    }

    private void configuraAdapter(RecyclerView meuRecycler) {
        listaProfissaoAdapter = new ListaProfissaoAdapter(getContext(), todasProfissoes);
        meuRecycler.setAdapter(listaProfissaoAdapter);
        listaProfissaoAdapter.setOnItemClickListener((profissao, adapterPosition) -> {
            ProfissaoFragment profissaoFragment = new ProfissaoFragment();
            Bundle argumento = new Bundle();
            argumento.putString(CHAVE_PERSONAGEM, personagemId);
            argumento.putSerializable("profissao", profissao);
            profissaoFragment.setArguments(argumento);
            profissaoFragment.show(getChildFragmentManager(), "profissaoFragment");
        });
    }
    private void pegaTodasProfissoes() {
        profissaoViewModel.pegaTodasProfissoes().observe(getViewLifecycleOwner(), resultadoTodasProfissoes -> {
            if (resultadoTodasProfissoes.getDado() != null) {
                todasProfissoes = resultadoTodasProfissoes.getDado();
                indicadorProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                listaProfissaoAdapter.atualiza(todasProfissoes);
            }
            if (resultadoTodasProfissoes.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoTodasProfissoes.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            listaProfissaoAdapter.limpaLista();
            if (personagemId != null){
                pegaTodasProfissoes();
            }
        });
    }
    private void inicializaComponentes() {
        todasProfissoes = new ArrayList<>();
        meuRecycler = binding.recyclerViewListaProfissoesFragment;
        swipeRefreshLayout = binding.swipeRefreshLayoutListaProfissoesFragment;
        indicadorProgresso = binding.indicadorProgressoListaProfissoesFragment;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(new PersonagemRepository(getContext()));
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), resultadoPegaPersonagem -> {
            if (resultadoPegaPersonagem == null) return;
            personagemId = resultadoPegaPersonagem.getId();
            ProfissaoViewModelFactory profissaoViewModelFactory = new ProfissaoViewModelFactory(new ProfissaoRepository(personagemId));
            profissaoViewModel = new ViewModelProvider(this, profissaoViewModelFactory).get(ProfissaoViewModel.class);
            pegaTodasProfissoes();
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        binding = null;
    }
}