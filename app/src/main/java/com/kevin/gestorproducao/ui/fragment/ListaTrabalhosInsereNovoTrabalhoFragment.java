package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INVALIDA;
import static com.kevin.gestorproducao.ui.fragment.ListaTrabalhosInsereNovoTrabalhoFragmentDirections.vaiDeNovoTrabalhoParaDetalhesVenda;
import static com.kevin.gestorproducao.ui.fragment.ListaTrabalhosInsereNovoTrabalhoFragmentDirections.vaiDeNovoTrabalhoParaFiltro;
import static com.kevin.gestorproducao.ui.fragment.ListaTrabalhosInsereNovoTrabalhoFragmentDirections.vaiDeTrabalhosParaEstoque;
import static com.kevin.gestorproducao.utilitario.Utilitario.filtrarTrabalhos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentListaTrabalhosInsereNovoTrabalhoBinding;
import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.ui.componente.EstadoVazioView;
import com.kevin.gestorproducao.ui.fragment.ListaTrabalhosInsereNovoTrabalhoFragmentDirections.VaiDeNovoTrabalhoParaDetalhesVenda;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaTrabalhoEspecificoNovaProducaoAdapter;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoPersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;
import java.util.Objects;

public class ListaTrabalhosInsereNovoTrabalhoFragment
    extends BaseFragment<FragmentListaTrabalhosInsereNovoTrabalhoBinding>
    implements MenuProvider
{
    private ProgressBar indicadorProgresso;
    private RecyclerView meuRecycler;
    private ListaTrabalhoEspecificoNovaProducaoAdapter trabalhosAdapter;
    private ArrayList<Trabalho> trabalhos, trabalhosFiltrados;
    private ArrayList<ProfissaoPersonagem> profissoesPersonagem;
    private TrabalhoViewModel trabalhoViewModel;
    private PersonagemViewModel personagemViewModel;
    private TrabalhoEstoqueViewModel estoqueViewModel;
    private ProfissaoPersonagemViewModel profissaoPersonagemViewModel;
    private EstadoVazioView estadoVazio;
    private int codigoRequisicao = CODIGO_REQUISICAO_INVALIDA;
    private NavController controlador;
    private TrabalhoEstoque trabalhoEstoqueSelecionado;
    private FiltroViewModel filtroViewModel;
    private FiltroTrabalho filtroAtual;

    @Override
    protected FragmentListaTrabalhosInsereNovoTrabalhoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentListaTrabalhosInsereNovoTrabalhoBinding.inflate(
            inflater,
            container,
            false
        );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListaTrabalhosInsereNovoTrabalhoFragmentArgs argumentos = ListaTrabalhosInsereNovoTrabalhoFragmentArgs.fromBundle(
            getArguments()
        );

        codigoRequisicao = argumentos.getRequisicao();
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
        configuraMeuRecycler();
        observarTrabalhos();
        observarFiltros();
        observarEstoque();
        observarPersonagem();
        observarProfissoesPersonagem();

        trabalhoViewModel.carregarSeNecessario();
        trabalhoViewModel.recuperaTrabalhos();

    }

    private void observarProfissoesPersonagem() {
        profissaoPersonagemViewModel.getProfissoesPersonagem().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getDado() != null) {
                    profissoesPersonagem.clear();
                    profissoesPersonagem.addAll(resultado.getDado());

                    aplicarFiltros();
                }
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

    private void aplicarFiltros() {
        if (filtroAtual == null) {
            trabalhosFiltrados = (ArrayList<Trabalho>) trabalhos.clone();

            ordenarPorProfissoesPersonagem();
            trabalhosAdapter.atualiza(trabalhosFiltrados);
            atualizaVisibilidadeListaVazia(trabalhosFiltrados.isEmpty());
            meuRecycler.smoothScrollToPosition(0);
            return;
        }

        trabalhosFiltrados = filtrarTrabalhos(trabalhos, filtroAtual);

        ordenarPorProfissoesPersonagem();
        trabalhosAdapter.atualiza(trabalhosFiltrados);
        atualizaVisibilidadeListaVazia(trabalhosFiltrados.isEmpty());
        meuRecycler.smoothScrollToPosition(0);
    }

    private void ordenarPorProfissoesPersonagem() {
        trabalhosFiltrados.sort((t1, t2) -> {

            int indexT1 = getIndiceProfissao(t1.getProfissao());
            int indexT2 = getIndiceProfissao(t2.getProfissao());

            if (indexT1 != indexT2) {
                return Integer.compare(indexT1, indexT2);
            }

            if (!Objects.equals(t1.getRaridade(), t2.getRaridade())) {
                return t1.getRaridade().compareToIgnoreCase(t2.getRaridade());
            }

            if (!Objects.equals(t1.getNivel(), t2.getNivel())) {
                return Integer.compare(t1.getNivel(), t2.getNivel());
            }

            return t1.getNome().compareToIgnoreCase(t2.getNome());
        });
    }

    private int getIndiceProfissao(String nomeProfissao) {
        for (int i = 0; i < profissoesPersonagem.size(); i++) {

            ProfissaoPersonagem profissao = profissoesPersonagem.get(i);

            if (profissao.getNome().equalsIgnoreCase(nomeProfissao)) {
                return i;
            }
        }

        return Integer.MAX_VALUE;
    }

    private void observarEstoque() {
        estoqueViewModel.getInsercaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(resultado.getErro());
                    return;
                }


                voltaParaListaEstoqueFragment();
            }
        );

        estoqueViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultadoInsereTrabalho -> {
                if (resultadoInsereTrabalho.getErro() != null) {
                    mostraMensagemAncorada(resultadoInsereTrabalho.getErro());
                    return;
                }
                voltaParaListaEstoqueFragment();
            }
        );

        estoqueViewModel.getTrabalhoEstoqueResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    TrabalhoEstoque trabalho = resultado.getDado();
                    if (trabalho == null) {
                        estoqueViewModel.insereEstoque(trabalhoEstoqueSelecionado);
                        return;
                    }
                    trabalho.setQuantidade(trabalho.getQuantidade() + 1);
                    estoqueViewModel.modificaEstoque(trabalho);
                }
            }
        );
    }
    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                estoqueViewModel.setIdPersonagem(personagem.getId());
                profissaoPersonagemViewModel.setIdPersonagem(personagem.getId());
            }
        );
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {
        String titulo;

        switch (codigoRequisicao) {
            case CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE:
                titulo = getString(R.string.stringNovoEstoque);
                break;

            case CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS:
                titulo = getString(R.string.stringNovaVenda);
                break;
            default:
                titulo = getString(R.string.trabalhos);
                break;
        }

        return new ComponentesVisuais(
            true,
            false,
            true,
            false,
            false,
            false,
            titulo,
            false
        );
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            vaiParaFiltro();
            return true;
        }
        return false;
    }

    private void vaiParaFiltro() {
        if (controlador.getCurrentDestination().getId() == R.id.listaTrabalhosInsereNovoTrabalhoFragment) {
            controlador.navigate(vaiDeNovoTrabalhoParaFiltro());
        }
    }

    private void atualizaVisibilidadeListaVazia(boolean listaVazia) {
        if (listaVazia) {
            estadoVazio.setVisibility(VISIBLE);
            return;
        }
        estadoVazio.setVisibility(GONE);
    }

    private void configuraMeuRecycler() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        configuraAdapter();
    }

    private void configuraAdapter() {
        trabalhosAdapter = new ListaTrabalhoEspecificoNovaProducaoAdapter(requireContext());
        meuRecycler.setAdapter(trabalhosAdapter);

        trabalhosAdapter.setOnItemClickListener(
            (trabalho, adapterPosition) -> aoTrabalhoSelecionado(trabalho)
        );
    }

    private void aoTrabalhoSelecionado(Trabalho trabalho) {
        switch (codigoRequisicao) {
            case CODIGO_REQUISICAO_INSERE_TRABALHO_ESTOQUE:
                trabalhoEstoqueSelecionado.setIdTrabalho(trabalho.getId());
                trabalhoEstoqueSelecionado.setQuantidade(1);

                estoqueViewModel.getTrabalhoPorId(trabalho.getId());
                break;

            case CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS:
                vaiParaDetalhesVenda(trabalho);
                break;
        }
    }

    private void vaiParaDetalhesVenda(Trabalho trabalho) {
        TrabalhoVendido trabalhoVendido = new TrabalhoVendido();
        trabalhoVendido.setIdTrabalho(trabalho.getId());
        VaiDeNovoTrabalhoParaDetalhesVenda acao = vaiDeNovoTrabalhoParaDetalhesVenda(trabalhoVendido);
        acao.setCodigoRequisicao(codigoRequisicao);
        controlador.navigate(acao);
    }

    private void voltaParaListaEstoqueFragment() {
        NavDirections acao = vaiDeTrabalhosParaEstoque();
        controlador.navigate(acao);
    }

    private void inicializaComponentes() {
        trabalhos = new ArrayList<>();
        trabalhosFiltrados = new ArrayList<>();
        profissoesPersonagem = new ArrayList<>();
        trabalhoEstoqueSelecionado = new TrabalhoEstoque();

        indicadorProgresso = binding.indicadorProgressoListaNovaProducao;
        meuRecycler = binding.recyclerViewListaNovaProducao;
        estadoVazio = binding.estadoVazio;
        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext());

        trabalhoViewModel = new ViewModelProvider(
            getViewModelStore(),
            viewModelFactory
        ).get(TrabalhoViewModel.class);

        estoqueViewModel = new ViewModelProvider(
            getViewModelStore(),
            viewModelFactory
        ).get(TrabalhoEstoqueViewModel.class);

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        profissaoPersonagemViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(ProfissaoPersonagemViewModel.class);

        filtroViewModel = new ViewModelProvider(
            requireActivity()
        ).get(FiltroViewModel.class);
    }

    private void observarTrabalhos() {
        trabalhoViewModel.getTrabalhos().observe(
            getViewLifecycleOwner(),
            resultado -> {
                indicadorProgresso.setVisibility(GONE);

                if (resultado.getDado() != null) {
                    trabalhos = resultado.getDado();

                    aplicarFiltros();
                }

                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(resultado.getErro());
                }
            }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        meuRecycler.setAdapter(null);

        removerObservadorTrabalho();
        removerObservadorEstoque();
        removerObservadorPersonagem();
        removerObservadorFiltro();
    }

    private void removerObservadorFiltro() {
        if (filtroViewModel == null) return;
        filtroViewModel.removeObservador();
    }

    private void removerObservadorPersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }

    private void removerObservadorEstoque() {
        if (estoqueViewModel == null) return;
        estoqueViewModel.removeObservador();
    }

    private void removerObservadorTrabalho() {
        if (trabalhoViewModel == null) return;
        trabalhoViewModel.removeObservador();
    }
}
