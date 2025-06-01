package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.Constantes.CHAVE_ID_PERSONAGEM;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.kevin.ceep.databinding.FragmentListaProfissoesBinding;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.ui.recyclerview.adapter.ListaProfissaoAdapter;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.ProfissaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoProducaoViewModelFactory;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ListaProfissoesFragment
        extends BaseFragment<FragmentListaProfissoesBinding> {
    private ListaProfissaoAdapter listaProfissaoAdapter;
    private String idPersonagem;
    private ArrayList<Profissao> todasProfissoes;
    private ArrayList<TrabalhoProducao> producao;
    private RecyclerView meuRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private ProfissaoViewModel profissaoViewModel;
    private PersonagemViewModel personagemViewModel;
    private TrabalhoProducaoViewModel trabalhoProducaoViewModel;

    public ListaProfissoesFragment() {
    }
    @Override
    protected FragmentListaProfissoesBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentListaProfissoesBinding.inflate(
                inflater,
                container,
                false
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configuraComponentesVisuais();
        inicializaComponentes();
        configuraRecyclerView();
        configuraSwipeRefreshLayout();
        configuraPersonagemSelecionado();
    }

    private void configuraComponentesVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.menuNavegacaoLateral = true;
        componentesVisuais.menuNavegacaoInferior = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    private void configuraPersonagemSelecionado() {
        personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), resultadoPegaPersonagem -> {
            if (resultadoPegaPersonagem == null) return;
            atualizarViewModel(resultadoPegaPersonagem.getId());
            TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(resultadoPegaPersonagem.getId());
            trabalhoProducaoViewModel = new ViewModelProvider(
                    this,
                    trabalhoProducaoViewModelFactory
            ).get(resultadoPegaPersonagem.getId(), TrabalhoProducaoViewModel.class);
            trabalhoProducaoViewModel.getTrabalhosProducao().observe(
                    getViewLifecycleOwner(),
                    resultadoRecuperaProducao -> {
                        if (resultadoRecuperaProducao.getDado() != null) {
                            producao = resultadoRecuperaProducao.getDado();
                        }
                        if (resultadoRecuperaProducao.getErro() == null) {
                            return;
                        }
                        mostraMensagem(resultadoRecuperaProducao.getErro());
                    });
            trabalhoProducaoViewModel.recuperaTrabalhosProducao();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter(meuRecycler);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraAdapter(RecyclerView meuRecycler) {
        listaProfissaoAdapter = new ListaProfissaoAdapter(getContext(), todasProfissoes);
        meuRecycler.setAdapter(listaProfissaoAdapter);
        configuraCliqueItemProfissao();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraCliqueItemProfissao() {
        listaProfissaoAdapter.setOnItemClickListener((profissao, adapterPosition) -> {
            ArrayList<TrabalhoProducao> producaoFiltrada = producao.stream()
                    .filter(trabalho -> trabalho.getProfissao().equals(profissao.getNome()))
                    .collect(Collectors.toCollection(ArrayList::new));
            ProfissaoFragment profissaoFragment = new ProfissaoFragment();
            Bundle argumento = new Bundle();
            argumento.putString(CHAVE_ID_PERSONAGEM, idPersonagem);
            argumento.putSerializable("profissao", profissao);
            argumento.putSerializable("producao", producaoFiltrada);
            profissaoFragment.setArguments(argumento);
            profissaoFragment.show(getChildFragmentManager(), "profissaoFragment");
        });
    }

    private void recuperaProfissoes() {
        profissaoViewModel.getRecuperacaoProfissoes().observe(getViewLifecycleOwner(), resultadoTodasProfissoes -> {
            if (resultadoTodasProfissoes.getDado() != null) {
                todasProfissoes = resultadoTodasProfissoes.getDado();
                if (todasProfissoes.isEmpty()) {
                    profissaoViewModel.getInsercaoResultado().observe(getViewLifecycleOwner(), resultadoInsereProfissoes -> {
                        if (resultadoInsereProfissoes.getErro() != null) {
                            mostraMensagem("Erro: "+resultadoTodasProfissoes.getErro());
                        }
                    });
                    profissaoViewModel.insereProfissoes();
                }
                indicadorProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                listaProfissaoAdapter.atualiza(todasProfissoes);
            }
            if (resultadoTodasProfissoes.getErro() != null) {
                mostraMensagem("Erro: "+resultadoTodasProfissoes.getErro());
            }
        });
        profissaoViewModel.recuperaProfissoes();
    }
    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            listaProfissaoAdapter.limpaLista();
            if (idPersonagem.isEmpty()) return;
            recuperaProfissoes();
        });
    }
    private void inicializaComponentes() {
        idPersonagem= "";
        todasProfissoes = new ArrayList<>();
        producao = new ArrayList<>();
        meuRecycler = binding.recyclerViewListaProfissoesFragment;
        swipeRefreshLayout = binding.swipeRefreshLayoutListaProfissoesFragment;
        indicadorProgresso = binding.indicadorProgressoListaProfissoesFragment;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(PersonagemRepository.getInstance());
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        recuperaProfissoes();
    }

    private void atualizarViewModel(String novoIdPersonagem) {
        if (idPersonagem.equals(novoIdPersonagem)) return;
        idPersonagem= novoIdPersonagem;
        ProfissaoViewModelFactory profissaoViewModelFactory = new ProfissaoViewModelFactory(idPersonagem);
        profissaoViewModel = new ViewModelProvider(this, profissaoViewModelFactory).get(idPersonagem, ProfissaoViewModel.class);
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
        personagemViewModel.removeOuvinte();
    }

    private void removeOuvinteProfissao() {
        if (profissaoViewModel == null) return;
        profissaoViewModel.removeOuvinte();
    }
}