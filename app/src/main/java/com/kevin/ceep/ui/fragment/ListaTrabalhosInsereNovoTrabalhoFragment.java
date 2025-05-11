package com.kevin.ceep.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_PRODUCAO;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INVALIDA;
import static com.kevin.ceep.utilitario.Utilitario.stringContemString;

import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentListaTrabalhosInsereNovoTrabalhoBinding;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.repository.TrabalhoEstoqueRepository;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.ui.fragment.ListaTrabalhosInsereNovoTrabalhoFragmentDirections.VaiDeTrabalhosParaDetalhesVenda;
import com.kevin.ceep.ui.fragment.ListaTrabalhosInsereNovoTrabalhoFragmentDirections.VaiParaConfirmaTrabalho;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEspecificoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEspecificoNovaProducaoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.ListaNovaProducaoViewModel;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.ceep.ui.viewModel.factory.ListaNovaProducaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.ProfissaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoEstoqueViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaTrabalhosInsereNovoTrabalhoFragment
        extends BaseFragment<FragmentListaTrabalhosInsereNovoTrabalhoBinding>
        implements MenuProvider{
    private ProgressBar indicadorProgresso;
    private RecyclerView meuRecycler;
    private ListaTrabalhoEspecificoNovaProducaoAdapter listaTrabalhoEspecificoAdapter;
    private String idPersonagem, textoFiltro;
    private ChipGroup grupoChipsProfissoes;
    private ArrayList<String> listaProfissoes;
    private ArrayList<Trabalho> todosTrabalhos, listaTrabalhosFiltrada;
    private ListaNovaProducaoViewModel novaProducaoViewModel;
    private TextView txtListaVazia;
    private ImageView iconeListaVazia;
    private int codigoRequisicao = CODIGO_REQUISICAO_INVALIDA;
    @Override
    protected FragmentListaTrabalhosInsereNovoTrabalhoBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentListaTrabalhosInsereNovoTrabalhoBinding.inflate(
                inflater,
                container,
                false
        );
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        configuraComponentesVisuais();
        inicializaComponentes();
        pegaTodosTrabalhos();
        recebeDadosIntent();
        configuraMeuRecycler();
        configuraChipSelecionado();
    }

    private void configuraComponentesVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuBusca = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        configuraComportamentoVisivilidadeGrupoChips(menu);
    }

    private void configuraComportamentoVisivilidadeGrupoChips(@NonNull Menu menu) {
        MenuItem itemBusca= menu.findItem(R.id.itemMenuBusca);
        SearchView visualizacaoBusca= (SearchView) itemBusca.getActionView();
        assert visualizacaoBusca != null;
        visualizacaoBusca.setOnQueryTextFocusChangeListener((view, b) -> {
            if (b) {
                grupoChipsProfissoes.setVisibility(VISIBLE);
                return;
            }
            grupoChipsProfissoes.setVisibility(GONE);
        });
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            configuraComportamentoBuscaPorTexto(menuItem);
            return true;
        }
        return false;
    }

    private void configuraComportamentoBuscaPorTexto(@NonNull MenuItem menuItem) {
        SearchView busca = (SearchView) menuItem.getActionView();
        assert busca != null;
        busca.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String texto) {
                if (texto == null) return false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textoFiltro = texto;
                    filtroLista();
                }
                return false;
            }
        });
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
        if (profissoesSelecionadas.isEmpty()) {
            listaTrabalhosFiltrada = (ArrayList<Trabalho>) todosTrabalhos.clone();
        } else {
            ArrayList<Trabalho> listaProfissaoEspecifica;
            for (String profissao : profissoesSelecionadas) {
                listaProfissaoEspecifica = (ArrayList<Trabalho>) todosTrabalhos.stream().filter(
                        trabalho -> stringContemString(trabalho.getProfissao(), profissao))
                        .collect(Collectors.toList());
                listaTrabalhosFiltrada.addAll(listaProfissaoEspecifica);
            }
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
        for (String profissao : listaProfissoes) {
            adicionaChip(profissao);
        }
    }

    private void adicionaChip(String profissao) {
        Chip novoChip= new Chip(new ContextThemeWrapper(requireContext(), R.style.estiloChip), null, 0);
        novoChip.setText(profissao);
        novoChip.setId(listaProfissoes.indexOf(profissao));
        novoChip.setCheckable(true);
        grupoChipsProfissoes.addView(novoChip);
    }

    private void configuraListaDeProfissoes() {
        listaProfissoes.clear();
        ProfissaoViewModelFactory profissaoViewModelFactory = new ProfissaoViewModelFactory(idPersonagem);
        ProfissaoViewModel profissaoViewModel = new ViewModelProvider(this, profissaoViewModelFactory).get(ProfissaoViewModel.class);
        profissaoViewModel.recuperaProfissoes().observe(getViewLifecycleOwner(), resultadoProfissoes -> {
            if (resultadoProfissoes.getErro() == null) {
                for (Profissao profissao : resultadoProfissoes.getDado()) {
                    listaProfissoes.add(profissao.getNome());
                }
                configuraGrupoChipsProfissoes();
                return;
            }
            mostraMensagem(resultadoProfissoes.getErro());
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtroLista() {
        if (!textoFiltro.isEmpty()) {
            ArrayList<Trabalho> listaFiltrada =
                    (ArrayList<Trabalho>) listaTrabalhosFiltrada.stream().filter(
                            trabalho -> stringContemString(trabalho.getNome(), textoFiltro))
                            .collect(Collectors.toList());
            atualizaVisibilidadeListaVazia(listaFiltrada.isEmpty());
            listaTrabalhoEspecificoAdapter.atualizaLista(listaFiltrada);
        } else {
            listaTrabalhoEspecificoAdapter.atualizaLista(listaTrabalhosFiltrada);
        }
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

    private void recebeDadosIntent() {
        idPersonagem = ListaTrabalhosInsereNovoTrabalhoFragmentArgs.fromBundle(getArguments()).getIdPersonagem();
        codigoRequisicao = ListaTrabalhosInsereNovoTrabalhoFragmentArgs.fromBundle(getArguments()).getRequisicao();
    }

    private void configuraMeuRecycler() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter();
    }

    private void configuraAdapter() {
        if (listaTrabalhoEspecificoAdapter == null) {
            listaTrabalhoEspecificoAdapter = new ListaTrabalhoEspecificoNovaProducaoAdapter(getContext(), todosTrabalhos);
            meuRecycler.setAdapter(listaTrabalhoEspecificoAdapter);
        }
        listaTrabalhoEspecificoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Trabalho trabalho, int adapterPosition) {
                aoTrabalhoSelecionado(trabalho);
            }

            @Override
            public void onItemClick(ListaTrabalhoEspecificoAdapter trabalhoEspecificoAdapter) {

            }
        });
    }

    private void aoTrabalhoSelecionado(Trabalho trabalho) {
        switch (codigoRequisicao) {
            case CODIGO_REQUISICAO_INSERE_TRABALHO_PRODUCAO:
                vaiParaConfirmaTrabalhoFragment(trabalho);
                break;
            case CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE:
                TrabalhoEstoqueViewModelFactory trabalhoEstoqueViewModelFactory = new TrabalhoEstoqueViewModelFactory(new TrabalhoEstoqueRepository(idPersonagem));
                TrabalhoEstoqueViewModel trabalhoEstoqueViewModel = new ViewModelProvider(getViewModelStore(), trabalhoEstoqueViewModelFactory).get(TrabalhoEstoqueViewModel.class);
                TrabalhoEstoque trabalhoEstoque = new TrabalhoEstoque();
                trabalhoEstoque.setIdTrabalho(trabalho.getId());
                trabalhoEstoque.setQuantidade(1);
                trabalhoEstoqueViewModel.getTrabalhoPorId().observe(getViewLifecycleOwner(), resultadoTrabalhoEncontrado -> {
                    if (resultadoTrabalhoEncontrado.getErro() == null) {
                        TrabalhoEstoque trabalhoEncontrado = resultadoTrabalhoEncontrado.getDado();
                        if (trabalhoEncontrado == null) {
                            trabalhoEstoqueViewModel.getInsercaoResultado().observe(getViewLifecycleOwner(), resultadoInsereTrabalho -> {
                                if (resultadoInsereTrabalho.getErro() != null) {
                                    mostraMensagem(resultadoInsereTrabalho.getErro());
                                    return;
                                }
                                voltaParaListaEstoqueFragment();
                            });
                            trabalhoEstoqueViewModel.insereTrabalhoEstoque(trabalhoEstoque);
                            return;
                        }
                        trabalhoEncontrado.setQuantidade(trabalhoEncontrado.getQuantidade() + 1);
                        trabalhoEstoqueViewModel.getModificacaoResultado().observe(
                                getViewLifecycleOwner(),
                                resultadoInsereTrabalho -> {
                                    if (resultadoInsereTrabalho.getErro() != null) {
                                        mostraMensagem(resultadoInsereTrabalho.getErro());
                                        return;
                                    }
                                    voltaParaListaEstoqueFragment();
                                });
                        trabalhoEstoqueViewModel.modificaTrabalhoEstoque(trabalhoEncontrado);
                    }
                });
                trabalhoEstoqueViewModel.recuperaTrabalhoEstoquePorIdTrabalho(trabalhoEstoque.getIdTrabalho());
                break;
            case CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS:
                vaiParaDetalhesVenda(trabalho);
                break;
        }
    }

    private void vaiParaDetalhesVenda(Trabalho trabalho) {
        TrabalhoVendido trabalhoVendido = new TrabalhoVendido();
        trabalhoVendido.setIdTrabalho(trabalho.getId());
        VaiDeTrabalhosParaDetalhesVenda acao = ListaTrabalhosInsereNovoTrabalhoFragmentDirections.vaiDeTrabalhosParaDetalhesVenda(trabalhoVendido, idPersonagem);
        acao.setCodigoRequisicao(codigoRequisicao);
        Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    private void voltaParaListaEstoqueFragment() {
        NavDirections acao = ListaTrabalhosInsereNovoTrabalhoFragmentDirections.vaiDeTrabalhosParaEstoque();
        Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    private void vaiParaConfirmaTrabalhoFragment(Trabalho trabalho) {
        try {
            VaiParaConfirmaTrabalho acao = ListaTrabalhosInsereNovoTrabalhoFragmentDirections.vaiParaConfirmaTrabalho(idPersonagem);
            acao.setTrabalho(trabalho);
            Navigation.findNavController(requireView()).navigate(acao);
        } catch (IllegalArgumentException e) {
            mostraMensagem("Erro na navegação");
        }
    }

    private void inicializaComponentes() {
        indicadorProgresso = binding.indicadorProgressoListaNovaProducao;
        meuRecycler = binding.recyclerViewListaNovaProducao;
        grupoChipsProfissoes = binding.grupoProfissoesChipListaNovaProducao;
        listaProfissoes = new ArrayList<>();
        todosTrabalhos = new ArrayList<>();
        listaTrabalhosFiltrada = new ArrayList<>();
        ListaNovaProducaoViewModelFactory listaNovaProducaoViewModelFactory = new ListaNovaProducaoViewModelFactory(TrabalhoRepository.getInstancia(getContext()));
        novaProducaoViewModel = new ViewModelProvider(this, listaNovaProducaoViewModelFactory).get(ListaNovaProducaoViewModel.class);
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        textoFiltro = "";
    }

    private void pegaTodosTrabalhos() {
        novaProducaoViewModel.pegaTodosTrabalhos().observe(getViewLifecycleOwner(), resultadoPegaTodosTrabalhos -> {
            if (resultadoPegaTodosTrabalhos.getDado() != null) {
                todosTrabalhos = resultadoPegaTodosTrabalhos.getDado();
                listaTrabalhosFiltrada = (ArrayList<Trabalho>) todosTrabalhos.clone();
                indicadorProgresso.setVisibility(GONE);
                atualizaVisibilidadeListaVazia(listaTrabalhosFiltrada.isEmpty());
                configuraListaDeProfissoes();
                listaTrabalhoEspecificoAdapter.atualizaLista(listaTrabalhosFiltrada);
            }
            if (resultadoPegaTodosTrabalhos.getErro() != null) {
                mostraMensagem(resultadoPegaTodosTrabalhos.getErro());
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        meuRecycler.setAdapter(null);
        binding = null;
        novaProducaoViewModel.removeOuvinte();
    }
}