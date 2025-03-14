package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_PRODUCAO;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INVALIDA;
import static com.kevin.ceep.utilitario.Utilitario.stringContemString;

import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentListaTrabalhosInsereNovoTrabalhoBinding;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.repository.TrabalhoEstoqueRepository;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.ui.fragment.ListaTrabalhosInsereNovoTrabalhoFragmentDirections.VaiParaConfirmaTrabalho;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEspecificoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEspecificoNovaProducaoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.ListaNovaProducaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.ceep.ui.viewModel.factory.ListaNovaProducaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoEstoqueViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaTrabalhosInsereNovoTrabalhoFragment extends Fragment implements MenuProvider{
    private FragmentListaTrabalhosInsereNovoTrabalhoBinding binding;
    private ProgressBar indicadorProgresso;
    private RecyclerView meuRecycler;
    private ListaTrabalhoEspecificoNovaProducaoAdapter listaTrabalhoEspecificoAdapter;
    private String personagemId, textoFiltro;
    private ChipGroup grupoChipsProfissoes;
    private ArrayList<String> listaProfissoes;
    private ArrayList<Trabalho> todosTrabalhos, listaTrabalhosFiltrada;
    private ListaNovaProducaoViewModel novaProducaoViewModel;
    private TextView txtListaVazia;
    private ImageView iconeListaVazia;
    private int codigoRequisicao = CODIGO_REQUISICAO_INVALIDA;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaTrabalhosInsereNovoTrabalhoBinding.inflate(inflater, container, false);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuBusca = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        inicializaComponentes();
        recebeDadosIntent();
        configuraMeuRecycler();
        configuraChipSelecionado();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            configuraCampoDeBusca(menuItem);
            return true;
        }
        return false;
    }

    private void configuraChipSelecionado() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grupoChipsProfissoes.setOnCheckedStateChangeListener((grupo, listaIds) -> filtraTrabalhoPorProfissaoSelecionada(listaIds));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtraTrabalhoPorProfissaoSelecionada(List<Integer> listaIds) {
        listaTrabalhosFiltrada.clear();
        List<String> profissoesSelecionadas = defineListaDeProfissoesSelecionadas(listaIds);
        if (!profissoesSelecionadas.isEmpty()) {
            ArrayList<Trabalho> listaProfissaoEspecifica;
            for (String profissao : profissoesSelecionadas) {
                listaProfissaoEspecifica = (ArrayList<Trabalho>) todosTrabalhos.stream().filter(
                        trabalho -> stringContemString(trabalho.getProfissao(), profissao))
                        .collect(Collectors.toList());
                listaTrabalhosFiltrada.addAll(listaProfissaoEspecifica);
            }
        } else {
            listaTrabalhosFiltrada = (ArrayList<Trabalho>) todosTrabalhos.clone();
        }
        filtroLista();
    }

    @NonNull
    private List<String> defineListaDeProfissoesSelecionadas(List<Integer> listaIds) {
        List<String> profissoesSelecionadas = new ArrayList<>();
        for (int id : listaIds) {
            profissoesSelecionadas.add(listaProfissoes.get(id));
        }
        return profissoesSelecionadas;
    }

    private void configuraGrupoChipsProfissoes() {
        grupoChipsProfissoes.removeAllViews();
        if (!listaProfissoes.isEmpty()) {
            int idProfissao = 0;
            for (String profissao : listaProfissoes) {
                Chip chipProfissao = (Chip) LayoutInflater.from(getContext()).inflate(R.layout.item_chip, null);
                chipProfissao.setText(profissao);
                chipProfissao.setId(idProfissao);
                grupoChipsProfissoes.addView(chipProfissao);
                idProfissao += 1;
            }
        }
    }

    private void configuraListaDeProfissoes() {
        listaProfissoes.clear();
        for (Trabalho trabalho : todosTrabalhos) {
            if (listaProfissoes.isEmpty()) {
                listaProfissoes.add(trabalho.getProfissao());
            } else {
                if (profissaoNaoExiste(trabalho)) {
                    listaProfissoes.add(trabalho.getProfissao());
                }
            }
        }
    }

    private boolean profissaoNaoExiste(Trabalho trabalho) {
        return !listaProfissoes.contains(trabalho.getProfissao());
    }

    private void configuraCampoDeBusca(MenuItem itemBusca) {
        androidx.appcompat.widget.SearchView busca = (androidx.appcompat.widget.SearchView) itemBusca.getActionView();
        assert busca != null;
        busca.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String texto) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textoFiltro = texto;
                    filtroLista();
                }
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtroLista() {
        if (!textoFiltro.isEmpty()) {
            ArrayList<Trabalho> listaFiltrada =
                    (ArrayList<Trabalho>) listaTrabalhosFiltrada.stream().filter(
                            trabalho -> stringContemString(trabalho.getNome(), textoFiltro))
                            .collect(Collectors.toList());
            if (listaFiltrada.isEmpty()) {
                iconeListaVazia.setVisibility(View.VISIBLE);
                txtListaVazia.setVisibility(View.VISIBLE);
            } else {
                txtListaVazia.setVisibility(View.GONE);
                iconeListaVazia.setVisibility(View.GONE);
            }
            listaTrabalhoEspecificoAdapter.atualizaLista(listaFiltrada);
        } else {
            listaTrabalhoEspecificoAdapter.atualizaLista(listaTrabalhosFiltrada);
        }
    }

    private void recebeDadosIntent() {
        personagemId = ListaTrabalhosInsereNovoTrabalhoFragmentArgs.fromBundle(getArguments()).getIdPersonagem();
        codigoRequisicao = ListaTrabalhosInsereNovoTrabalhoFragmentArgs.fromBundle(getArguments()).getRequisicao();
    }

    private void configuraMeuRecycler() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter();
    }

    private void configuraAdapter() {
        listaTrabalhoEspecificoAdapter = new ListaTrabalhoEspecificoNovaProducaoAdapter(getContext(), todosTrabalhos);
        meuRecycler.setAdapter(listaTrabalhoEspecificoAdapter);
        listaTrabalhoEspecificoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Trabalho trabalho, int adapterPosition) {
                if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO_PRODUCAO) vaiParaConfirmaTrabalhoFragment(trabalho);
                if (codigoRequisicao == CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE) {
                    TrabalhoEstoqueViewModelFactory trabalhoEstoqueViewModelFactory = new TrabalhoEstoqueViewModelFactory(new TrabalhoEstoqueRepository(getContext(), personagemId));
                    TrabalhoEstoqueViewModel trabalhoEstoqueViewModel = new ViewModelProvider(getViewModelStore(), trabalhoEstoqueViewModelFactory).get(TrabalhoEstoqueViewModel.class);
                    TrabalhoEstoque trabalhoEstoque = new TrabalhoEstoque();
                    trabalhoEstoque.setIdTrabalho(trabalho.getId());
                    trabalhoEstoque.setQuantidade(1);
                    TrabalhoEstoque trabalhoEncontrado = trabalhoEstoqueViewModel.pegaTrabalhoEstoquePorIdTrabalho(trabalhoEstoque.getIdTrabalho());
                    if (trabalhoEncontrado == null) {
                        trabalhoEstoqueViewModel.insereTrabalhoEstoque(trabalhoEstoque).observe(getViewLifecycleOwner(), resultadoInsereTrabalho -> {
                            if (resultadoInsereTrabalho.getErro() != null) {
                                Snackbar.make(binding.getRoot(), "Erro ao inserir: " + resultadoInsereTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            voltaParaListaEstoqueFragment();
                        });
                        return;
                    }
                    trabalhoEncontrado.setQuantidade(trabalhoEncontrado.getQuantidade() + 1);
                    trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEncontrado).observe(getViewLifecycleOwner(), resultadoInsereTrabalho -> {
                        if (resultadoInsereTrabalho.getErro() != null) {
                            Snackbar.make(binding.getRoot(), "Erro ao modificar: " + resultadoInsereTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        voltaParaListaEstoqueFragment();
                    });
                }
            }

            @Override
            public void onItemClick(ListaTrabalhoEspecificoAdapter trabalhoEspecificoAdapter) {

            }
        });
    }

    private void voltaParaListaEstoqueFragment() {
        NavDirections acao = ListaTrabalhosInsereNovoTrabalhoFragmentDirections.vaiDeTrabalhosParaEstoque();
        Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    private void vaiParaConfirmaTrabalhoFragment(Trabalho trabalho) {
        VaiParaConfirmaTrabalho acao = ListaTrabalhosInsereNovoTrabalhoFragmentDirections.vaiParaConfirmaTrabalho(personagemId);
        acao.setTrabalho(trabalho);
        Navigation.findNavController(requireView()).navigate(acao);
    }

    private void inicializaComponentes() {
        indicadorProgresso = binding.indicadorProgressoListaNovaProducao;
        meuRecycler = binding.recyclerViewListaNovaProducao;
        grupoChipsProfissoes = binding.grupoProfissoesChipListaNovaProducao;
        listaProfissoes = new ArrayList<>();
        todosTrabalhos = new ArrayList<>();
        listaTrabalhosFiltrada = new ArrayList<>();
        ListaNovaProducaoViewModelFactory listaNovaProducaoViewModelFactory = new ListaNovaProducaoViewModelFactory(new TrabalhoRepository(getContext()));
        novaProducaoViewModel = new ViewModelProvider(this, listaNovaProducaoViewModelFactory).get(ListaNovaProducaoViewModel.class);
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        textoFiltro = "";
    }

    private void pegaTodosTrabalhos() {
        novaProducaoViewModel.pegaTodosTrabalhos().observe(this, resultadoPegaTodosTrabalhos -> {
            if (resultadoPegaTodosTrabalhos.getDado() != null) {
                todosTrabalhos = resultadoPegaTodosTrabalhos.getDado();
                listaTrabalhosFiltrada = (ArrayList<Trabalho>) todosTrabalhos.clone();
                indicadorProgresso.setVisibility(View.GONE);
                if (listaTrabalhosFiltrada.isEmpty()) {
                    iconeListaVazia.setVisibility(View.VISIBLE);
                    txtListaVazia.setVisibility(View.VISIBLE);
                } else {
                    txtListaVazia.setVisibility(View.GONE);
                    iconeListaVazia.setVisibility(View.GONE);
                }
                configuraListaDeProfissoes();
                configuraGrupoChipsProfissoes();
                listaTrabalhoEspecificoAdapter.atualizaLista(listaTrabalhosFiltrada);
            }
            if (resultadoPegaTodosTrabalhos.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoPegaTodosTrabalhos.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        pegaTodosTrabalhos();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding = null;
    }
}