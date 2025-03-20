package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO;
import static com.kevin.ceep.utilitario.Utilitario.comparaString;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.databinding.FragmentListaTodosTrabalhosBinding;
import com.kevin.ceep.model.ProfissaoTrabalho;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.ui.fragment.ListaTodosTrabalhosFragmentDirections.ActionListaTodosTrabalhosFragmentToTrabalhoEspecificoFragment;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTodosTrabalhosAdapter;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoViewModel;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class ListaTodosTrabalhosFragment extends Fragment {
    private FragmentListaTodosTrabalhosBinding binding;
    private ListaTodosTrabalhosAdapter listaTodosTrabalhosAdapter;
    private FloatingActionButton botaoNovoTrabalho;
    private RecyclerView meuRecycler;
    private ArrayList<ProfissaoTrabalho> profissoesTrabalhos;
    private ArrayList<Trabalho> todosTrabalhos;
    private ProgressBar indicadorProgresso;
    private TrabalhoViewModel trabalhoViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListaTodosTrabalhosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        inicializaComponentes();
        configuraRecyclerView();
        configuraBotaoCadastraNovoTrabalho();
        configuraSwipeRefreshLayout();
    }

    private void inicializaComponentes() {
        profissoesTrabalhos = new ArrayList<>();
        botaoNovoTrabalho = binding.floatingButtonProfissoesTrabalhos;
        indicadorProgresso = binding.indicadorProgressoProfissoesTrabalhos;
        meuRecycler = binding.recyclerViewProfissoesTrabalhos;
        TrabalhoViewModelFactory trabalhoViewModelFactory = new TrabalhoViewModelFactory(new TrabalhoRepository(getContext()));
        trabalhoViewModel = new ViewModelProvider(this, trabalhoViewModelFactory).get(TrabalhoViewModel.class);
    }
    private void configuraBotaoCadastraNovoTrabalho() {
        botaoNovoTrabalho.setOnClickListener(view -> vaiParaCadastraNovoTrabalhoActivity());
    }

    private void vaiParaCadastraNovoTrabalhoActivity() {
        ActionListaTodosTrabalhosFragmentToTrabalhoEspecificoFragment acao = ListaTodosTrabalhosFragmentDirections.actionListaTodosTrabalhosFragmentToTrabalhoEspecificoFragment(null);
        acao.setCodigoRequisicao(CODIGO_REQUISICAO_INSERE_TRABALHO);
        Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    private void configuraSwipeRefreshLayout() {
        SwipeRefreshLayout swipeRefreshLayout = binding.swipeRefreshProfissoesTrabalhos;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            sincronizaTrabalhos();
            pegaTodosTrabalhos();
        });
    }

    private void filtraTrabalhosProfissao() {
        profissoesTrabalhos = new ArrayList<>();
        for (Trabalho trabalho : todosTrabalhos) {
            if (profissoesTrabalhos.isEmpty()){
                ArrayList<Trabalho> listaTrabalhosProfissao = new ArrayList<>();
                listaTrabalhosProfissao.add(trabalho);
                ProfissaoTrabalho profissaoTrabalho = new ProfissaoTrabalho(trabalho.getProfissao(), listaTrabalhosProfissao);
                profissoesTrabalhos.add(profissaoTrabalho);
                continue;
            }
            if (profissaoExiste(trabalho)) continue;
            ArrayList<Trabalho> listaTrabalhosProfissao = new ArrayList<>();
            listaTrabalhosProfissao.add(trabalho);
            ProfissaoTrabalho profissaoTrabalho = new ProfissaoTrabalho(trabalho.getProfissao(), listaTrabalhosProfissao);
            profissoesTrabalhos.add(profissaoTrabalho);
        }
        indicadorProgresso.setVisibility(View.GONE);
        listaTodosTrabalhosAdapter.atualiza(profissoesTrabalhos);
    }

    private boolean profissaoExiste(Trabalho trabalho) {
        for (ProfissaoTrabalho profissaoTrabalho : profissoesTrabalhos) {
            if (comparaString(profissaoTrabalho.getNome(), trabalho.getProfissao())) {
                profissaoTrabalho.getTrabalhos().add(trabalho);
                return true;
            }
        }
        return false;
    }

    private void pegaTodosTrabalhos() {
        todosTrabalhos = new ArrayList<>();
        trabalhoViewModel.pegaTodosTrabalhos().observe(getViewLifecycleOwner(), resultadoPegaTodosTrabalhos -> {
            if (resultadoPegaTodosTrabalhos.getDado() != null) {
                todosTrabalhos = resultadoPegaTodosTrabalhos.getDado();
                filtraTrabalhosProfissao();
            }
            if (resultadoPegaTodosTrabalhos.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoPegaTodosTrabalhos.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void sincronizaTrabalhos() {
        trabalhoViewModel.sincronizaTrabalhos().observe(getViewLifecycleOwner(), resultadoSincroniza -> {
            if (resultadoSincroniza.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoSincroniza.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter();
    }

    private void configuraAdapter() {
        listaTodosTrabalhosAdapter = new ListaTodosTrabalhosAdapter(profissoesTrabalhos, getContext());
        meuRecycler.setAdapter(listaTodosTrabalhosAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        sincronizaTrabalhos();
        pegaTodosTrabalhos();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}