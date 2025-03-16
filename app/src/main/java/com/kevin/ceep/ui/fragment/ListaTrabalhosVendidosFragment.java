package com.kevin.ceep.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentListaTrabalhosVendidosBinding;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.ui.fragment.ListaTrabalhosVendidosFragmentDirections.VaiDeTrabalhosVendidosParaDetalhesTrabalhoVendido;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhosVendidosAdapter;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhosVendidosViewModelFactory;

import java.util.ArrayList;

public class ListaTrabalhosVendidosFragment extends Fragment {
    private FragmentListaTrabalhosVendidosBinding binding;
    private ListaTrabalhosVendidosAdapter trabalhosVendidosAdapter;
    private String personagemId;
    private ArrayList<TrabalhoVendido> trabalhosVendidos;
    private RecyclerView meuRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private TrabalhosVendidosViewModel trabalhosVendidosViewModel;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
    private PersonagemViewModel personagemViewModel;

    public ListaTrabalhosVendidosFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaTrabalhosVendidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.menuNavegacaoInferior = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        inicializaComponentes();
        configuraRecyclerView();
        configuraSwipeRefreshLayout();
        configuraDeslizeItem();
    }
    private void configuraDeslizeItem() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int itemPosicao = viewHolder.getAdapterPosition();
                trabalhosVendidosAdapter = (ListaTrabalhosVendidosAdapter) meuRecycler.getAdapter();
                if (trabalhosVendidosAdapter != null) {
                    TrabalhoVendido trabalhoVendidoRemovido = trabalhosVendidos.get(itemPosicao);
                    trabalhosVendidosAdapter.remove(itemPosicao);
                    Snackbar snackbarDesfazer = Snackbar.make(binding.getRoot(), trabalhoVendidoRemovido.getDescricao()+ " excluido", Snackbar.LENGTH_LONG);
                    snackbarDesfazer.addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (event != DISMISS_EVENT_ACTION){
                                removeTrabalhoDoBanco(trabalhoVendidoRemovido);
                                removeTrabalhoDaLista(trabalhoVendidoRemovido);
                            }
                        }
                    });
                    snackbarDesfazer.setAction(getString(R.string.stringDesfazer), v -> trabalhosVendidosAdapter.adiciona(trabalhoVendidoRemovido, itemPosicao));
                    snackbarDesfazer.show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(meuRecycler);
    }

    private void removeTrabalhoDaLista(TrabalhoVendido trabalhoVendidoRemovido) {
        trabalhosVendidos.remove(trabalhoVendidoRemovido);
    }

    private void removeTrabalhoDoBanco(TrabalhoVendido trabalhoRemovido) {
        trabalhosVendidosViewModel.removeTrabalhoVendido(trabalhoRemovido).observe(getViewLifecycleOwner(), resultadoRemoveTrabalho -> {
            if (resultadoRemoveTrabalho.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoRemoveTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            trabalhosVendidosAdapter.limpaLista();
            if (personagemId != null){
                sincronizaTrabalhos();
            }
        });
    }

    private void pegaTodosProdutosVendidos() {
        trabalhosVendidosViewModel.pegaTodosTrabalhosVendidos().observe(getViewLifecycleOwner(), resultadoTodosTrabalhos -> {
            if (resultadoTodosTrabalhos.getDado() != null) {
                trabalhosVendidos = resultadoTodosTrabalhos.getDado();
                if (trabalhosVendidos.isEmpty()) {
                    iconeListaVazia.setVisibility(View.VISIBLE);
                    txtListaVazia.setVisibility(View.VISIBLE);
                } else {
                    iconeListaVazia.setVisibility(View.GONE);
                    txtListaVazia.setVisibility(View.GONE);
                }
                indicadorProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                trabalhosVendidosAdapter.atualiza(trabalhosVendidos);
            }
            if (resultadoTodosTrabalhos.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoTodosTrabalhos.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void configuraRecyclerView() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter();
    }

    private void configuraAdapter() {
        trabalhosVendidosAdapter = new ListaTrabalhosVendidosAdapter(trabalhosVendidos, getContext());
        meuRecycler.setAdapter(trabalhosVendidosAdapter);
        trabalhosVendidosAdapter.setOnItemClickListener(this::vaiParaDetalhesTrabalhoVendido);
    }

    private void vaiParaDetalhesTrabalhoVendido(TrabalhoVendido trabalhoVendido) {
        VaiDeTrabalhosVendidosParaDetalhesTrabalhoVendido acao = ListaTrabalhosVendidosFragmentDirections.vaiDeTrabalhosVendidosParaDetalhesTrabalhoVendido(
                trabalhoVendido,
                personagemId);
        Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    private void inicializaComponentes() {
        trabalhosVendidos = new ArrayList<>();
        meuRecycler = binding.recyclerViewListaProdutosVendidos;
        swipeRefreshLayout = binding.swipeRefreshLayoutProdutosVendidos;
        indicadorProgresso = binding.indicadorProgressoListaProdutosVendidosFragment;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(new PersonagemRepository(getContext()));
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), resultadoPegaPersonagem -> {
            if (resultadoPegaPersonagem == null) return;
            personagemId = resultadoPegaPersonagem.getId();
            TrabalhosVendidosViewModelFactory trabalhosVendidosViewModelFactory = new TrabalhosVendidosViewModelFactory(new TrabalhoVendidoRepository(getContext(), personagemId));
            trabalhosVendidosViewModel = new ViewModelProvider(this, trabalhosVendidosViewModelFactory).get(TrabalhosVendidosViewModel.class);
            pegaTodosProdutosVendidos();
        });
    }

    private void sincronizaTrabalhos() {
        trabalhosVendidosViewModel.sincronizaTrabalhos().observe(getViewLifecycleOwner(), resultadoSincronizaTrabalhos -> {
            if (resultadoSincronizaTrabalhos.getErro() == null) {
                pegaTodosProdutosVendidos();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        binding = null;
    }
}