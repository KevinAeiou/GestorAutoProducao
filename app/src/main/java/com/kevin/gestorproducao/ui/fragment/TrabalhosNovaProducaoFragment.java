package com.kevin.gestorproducao.ui.fragment;


import static com.kevin.gestorproducao.ui.fragment.TrabalhosNovaProducaoFragmentDirections.vaiDeNovaProducaoParaConfirmaTrabalho;
import static com.kevin.gestorproducao.ui.fragment.TrabalhosNovaProducaoFragmentDirections.vaiDeNovaProducaoParaFiltro;
import static com.kevin.gestorproducao.utilitario.Utilitario.filtrarTrabalhos;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentTrabalhosNovaProducaoBinding;
import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.ui.fragment.TrabalhosNovaProducaoFragmentDirections.VaiDeNovaProducaoParaConfirmaTrabalho;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaTrabalhoEspecificoNovaProducaoAdapter;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoPersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;
import java.util.Objects;

public class TrabalhosNovaProducaoFragment
    extends BaseFragment<FragmentTrabalhosNovaProducaoBinding>
    implements MenuProvider
{
    private ProgressBar indicadorProgresso;
    private RecyclerView meuRecycler;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
    private ListaTrabalhoEspecificoNovaProducaoAdapter trabalhosAdapter;
    private ArrayList<Trabalho> trabalhos, trabalhosFiltrados;
    private TrabalhoViewModel trabalhoViewModel;
    private FiltroViewModel filtroViewModel;
    private FiltroTrabalho filtroAtual;
    private NavController controlador;
    private ProfissaoPersonagemViewModel profissaoPersonagemViewModel;
    private PersonagemViewModel personagemViewModel;
    private ArrayList<ProfissaoPersonagem> profissoesPersonagem;

    @Override
    protected FragmentTrabalhosNovaProducaoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentTrabalhosNovaProducaoBinding.inflate(inflater, container, false);
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
        configuraRecycler();
        observarFiltros();
        observarTrabalhos();
        observarPersonagem();
        observarProfissoesPersonagem();
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

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                profissaoPersonagemViewModel.setIdPersonagem(personagem.getId());
            }
        );
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
                    mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
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

    private void configuraRecycler() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        configuraAdapter();
    }

    private void configuraAdapter() {
        trabalhosAdapter = new ListaTrabalhoEspecificoNovaProducaoAdapter(requireContext());
        meuRecycler.setAdapter(trabalhosAdapter);

        trabalhosAdapter.setOnItemClickListener(
            (trabalho, posicao) -> vaiParaConfirmaTrabalhoFragment(trabalho)
        );
    }

    private void vaiParaConfirmaTrabalhoFragment(Trabalho trabalho) {
        VaiDeNovaProducaoParaConfirmaTrabalho acao = vaiDeNovaProducaoParaConfirmaTrabalho();
        acao.setTrabalho(trabalho);

        controlador.navigate(acao);
    }

    private void inicializaComponentes() {
        indicadorProgresso = binding.indicadorProgressoTrabalhosNovaProducao;
        meuRecycler = binding.recyclerViewTrabalhosNovaProducao;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        trabalhos = new ArrayList<>();
        trabalhosFiltrados = new ArrayList<>();
        profissoesPersonagem = new ArrayList<>();

        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext());

        trabalhoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoViewModel.class);

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

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            false,
            true,
            false,
            false,
            false,
            getString(R.string.stringNovaProducao),
            false
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        trabalhoViewModel.recuperaTrabalhos();
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

    private void atualizaVisibilidadeListaVazia(boolean listaVazia) {
        if (listaVazia) {
            iconeListaVazia.setVisibility(View.VISIBLE);
            txtListaVazia.setVisibility(View.VISIBLE);
            return;
        }

        iconeListaVazia.setVisibility(View.GONE);
        txtListaVazia.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeOuvinteTrabalho();
        removeOuvinteFiltro();
        binding = null;
    }

    private void removeOuvinteTrabalho() {
        if (trabalhoViewModel == null) return;
        trabalhoViewModel.removeObservador();
    }

    private void removeOuvinteFiltro() {
        if (filtroViewModel == null) return;
        filtroViewModel.removeObservador();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            if (controlador.getCurrentDestination().getId() == R.id.trabalhosNovaProducaoFragment)
                controlador.navigate(vaiDeNovaProducaoParaFiltro());
            return true;
        }

        return false;
    }
}