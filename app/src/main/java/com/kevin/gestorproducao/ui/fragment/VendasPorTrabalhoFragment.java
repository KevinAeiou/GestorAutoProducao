package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_VENDAS;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS;
import static com.kevin.gestorproducao.ui.fragment.VendasPorTrabalhoFragmentDirections.vaiDeVendasPorTrabalhoParaDetalhesVenda;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentVendasPorTrabalhoBinding;
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.ui.fragment.VendasPorTrabalhoFragmentDirections.VaiDeVendasPorTrabalhoParaDetalhesVenda;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaVendasPorTrabalho;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

public class VendasPorTrabalhoFragment
    extends BaseFragment<FragmentVendasPorTrabalhoBinding>
{
    private ListaVendasPorTrabalho vendasAdapter;
    private TrabalhoVendido trabalhoRecebido;
    private RecyclerView meuRecycler;
    private ProgressBar indicadorProgresso;
    private TrabalhosVendidosViewModel vendaViewModel;
    private PersonagemViewModel personagemViewModel;
    private NavController controlador;
    private FloatingActionButton btnInserir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VendasPorTrabalhoFragmentArgs argumentos = VendasPorTrabalhoFragmentArgs.fromBundle(
            getArguments()
        );

        trabalhoRecebido = argumentos.getTrabalho();
    }

    @Override
    protected FragmentVendasPorTrabalhoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentVendasPorTrabalhoBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        configuraRecyclerView();
        configurarBotaoInserirVenda();
        observarVendas();
        observarPersonagem();
    }

    private void configurarBotaoInserirVenda() {
        btnInserir.setOnClickListener(v -> {
            VaiDeVendasPorTrabalhoParaDetalhesVenda acao = vaiDeVendasPorTrabalhoParaDetalhesVenda(
                trabalhoRecebido
            );
            acao.setCodigoRequisicao(CODIGO_REQUISICAO_INSERE_TRABALHO_VENDAS);
            controlador.navigate(acao);
        });
    }

    private void inicializaComponentes() {
        indicadorProgresso = binding.indicadorProgressoVendasPorTrabalho;
        meuRecycler = binding.recyclerViewVendasPorTrabalho;
        btnInserir = binding.botaoFlutuanteVendasPorTrabalho;

        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext());

        vendaViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhosVendidosViewModel.class);

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            false,
            false,
            false,
            false,
            false,
            trabalhoRecebido.getNome(),
            false
        );
    }

    private void observarVendas() {
        vendaViewModel.getVendasPorTrabalho().observe(
            getViewLifecycleOwner(),
            resultado -> {
                indicadorProgresso.setVisibility(View.GONE);

                if (resultado.getDado() != null) {
                    if (!resultado.getDado().isEmpty()) {
                        TrabalhoVendido trabalho = resultado.getDado().get(0);
                        trabalhoRecebido.setIdTrabalho(trabalho.getIdTrabalho());
                        trabalhoRecebido.setDescricao(trabalho.getDescricao());
                        trabalhoRecebido.setQuantidade(trabalho.getQuantidade());
                        trabalhoRecebido.setValor(trabalho.getValor());
                    }

                    vendasAdapter.atualiza(resultado.getDado());
                    meuRecycler.smoothScrollToPosition(0);
                }

                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
                }
            }
        );
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                vendaViewModel.carregarVendasPorTrabalho(
                    resultado.getId(),
                    trabalhoRecebido.getId()
                );
            }
        );
    }

    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        configuraAdapter();
    }

    private void configuraAdapter() {
        vendasAdapter = new ListaVendasPorTrabalho(requireContext());
        meuRecycler.setAdapter(vendasAdapter);
        vendasAdapter.setOnItemClickListener(this::vaiParaDetalhes);
    }

    private void vaiParaDetalhes(TrabalhoVendido trabalho) {
        VaiDeVendasPorTrabalhoParaDetalhesVenda acao = vaiDeVendasPorTrabalhoParaDetalhesVenda(trabalho);
        acao.setCodigoRequisicao(CODIGO_REQUISICAO_ALTERA_VENDAS);
        controlador.navigate(acao);
    }
}