package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_PRODUCAO;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
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
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentListaTrabalhosProducaoBinding;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.repository.TrabalhoProducaoRepository;
import com.kevin.ceep.ui.fragment.ListaTrabalhosProducaoFragmentDirections.VaiParaListaTrabalhos;
import com.kevin.ceep.ui.fragment.ListaTrabalhosProducaoFragmentDirections.VaiParaTrabalhoEspecifico;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoProducaoAdapter;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoProducaoViewModelFactory;

import java.util.ArrayList;

public class ListaTrabalhosProducaoFragment extends Fragment {
    private FragmentListaTrabalhosProducaoBinding binding;
    private ListaTrabalhoProducaoAdapter trabalhoAdapter;
    private RecyclerView recyclerView;
    private ArrayList<TrabalhoProducao> trabalhos, trabalhosFiltrados;
    private String personagemId;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private ChipGroup grupoChipsEstados;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
    private PersonagemViewModel personagemViewModel;
    private TrabalhoProducaoViewModel trabalhoProducaoViewModel;
    private EstadoAppViewModel estadoAppViewModel;

    public ListaTrabalhosProducaoFragment() {
    }

    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaTrabalhosProducaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser usuarioID = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioID == null) {
            Log.d("fluxo", "Vai para splashscreen");
            NavDirections acao = ListaTrabalhosProducaoFragmentDirections.vaiParaSlashScreen();
            Navigation.findNavController(binding.getRoot()).navigate(acao);
            return;
        }
        inicializaComponentes();
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.navigationMenu = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        configuraRecyclerView();
        configuraSwipeRefreshLayout();
        configuraBotaoInsereTrabalho();
        configuraDeslizeItem();
        configuraChipSelecionado();
    }

    private void configuraChipSelecionado() {
        grupoChipsEstados.setOnCheckedStateChangeListener((group, checkedId) -> filtraListaPorEstado(checkedId.get(0)));
    }

    private void filtraListaPorEstado(int checkedId) {
        int estado = -1;
        switch (checkedId){
            case (R.id.chipFiltroTodos):
                break;
            case (R.id.chipFiltroProduzir):
                estado = 0;
                break;
            case (R.id.chipFiltroProduzindo):
                estado = 1;
                break;
            case (R.id.chipFiltroPronto):
                estado = 2;
                break;
        }
        trabalhosFiltrados = filtroListaChip(estado);
        if (trabalhosFiltrados.isEmpty()) {
            trabalhoAdapter.limpaLista();
            iconeListaVazia.setVisibility(View.VISIBLE);
            txtListaVazia.setVisibility(View.VISIBLE);
        } else {
            iconeListaVazia.setVisibility(View.GONE);
            txtListaVazia.setVisibility(View.GONE);
            trabalhoAdapter.atualiza(trabalhosFiltrados);
        }
    }
    private ArrayList<TrabalhoProducao> filtroListaChip(int estado) {
        ArrayList<TrabalhoProducao> listaFiltrada = new ArrayList<>();
        if (estado == -1){
            listaFiltrada = (ArrayList<TrabalhoProducao>) trabalhos.clone();
        }else {
            for (TrabalhoProducao item : trabalhos) {
                if (item.getEstado() == estado) {
                    listaFiltrada.add(item);
                }
            }
        }
        return listaFiltrada;
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
                ListaTrabalhoProducaoAdapter trabalhoAdapter = (ListaTrabalhoProducaoAdapter) recyclerView.getAdapter();
                if (trabalhoAdapter != null) {
                    TrabalhoProducao trabalhoremovido = trabalhosFiltrados.get(itemPosicao);
                    trabalhoAdapter.remove(itemPosicao);
                    Snackbar snackbarDesfazer = Snackbar.make(binding.getRoot(), trabalhoremovido.getNome()+ " excluido", Snackbar.LENGTH_LONG);
                    snackbarDesfazer.addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (event != DISMISS_EVENT_ACTION){
                                removeTrabalhoDoBanco(trabalhoremovido);
                                removeTrabalhoDaLista(trabalhoremovido);
                            }
                        }
                    });
                    snackbarDesfazer.setAnchorView(binding.floatingActionButton);
                    snackbarDesfazer.setAction(getString(R.string.stringDesfazer), v -> trabalhoAdapter.adiciona(trabalhoremovido, itemPosicao));
                    snackbarDesfazer.show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void removeTrabalhoDaLista(TrabalhoProducao trabalhoremovido) {
        trabalhos.remove(trabalhoremovido);
    }

    private void removeTrabalhoDoBanco(TrabalhoProducao trabalhoRemovido) {
        trabalhoProducaoViewModel.deletaTrabalhoProducao(trabalhoRemovido).observe(this, resultadoRemoveTrabalho -> {
            if (resultadoRemoveTrabalho.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoRemoveTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), personagemSelecionado -> {
            if (personagemSelecionado == null) return;
            personagemId = personagemSelecionado.getId();
            sincronizaTrabalhos();
        }));
    }
    private void configuraBotaoInsereTrabalho() {
        binding.floatingActionButton.setOnClickListener(v -> {
            personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), personagemSelecionado -> {
                if (personagemSelecionado == null) return;
                personagemId = personagemSelecionado.getId();
            });
            if (personagemId == null) {
                Snackbar.make(binding.getRoot(), "Selecione um personagem para continuar", Snackbar.LENGTH_LONG).setAnchorView(binding.floatingActionButton).show();
                return;
            }
            VaiParaListaTrabalhos acao = ListaTrabalhosProducaoFragmentDirections.vaiParaListaTrabalhos(personagemId);
            acao.setRequisicao(CODIGO_REQUISICAO_INSERE_TRABALHO_PRODUCAO);
            Navigation.findNavController(v).navigate(acao);
        });
    }

    private void inicializaComponentes() {
        trabalhos = new ArrayList<>();
        recyclerView = binding.listaTrabalhoRecyclerView;
        swipeRefreshLayout = binding.swipeRefreshLayoutTrabalhos;
        indicadorProgresso = binding.indicadorProgressoListaTrabalhosFragment;
        grupoChipsEstados = binding.chipGrupId;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(new PersonagemRepository(getContext()));
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
        estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
    }
    private void atualizaListaTrabalho() {
        int chipId = grupoChipsEstados.getCheckedChipId();
        filtraListaPorEstado(chipId);
    }
    private void configuraRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter(recyclerView);
    }
    private void configuraAdapter(RecyclerView listaTrabalhos) {
        trabalhoAdapter = new ListaTrabalhoProducaoAdapter(getContext(), trabalhos);
        listaTrabalhos.setAdapter(trabalhoAdapter);
        trabalhoAdapter.setOnItemClickListener(this::vaiParaTrabalhoEspecificoActivity);
    }
    private void vaiParaTrabalhoEspecificoActivity(TrabalhoProducao trabalho) {
         VaiParaTrabalhoEspecifico acao = ListaTrabalhosProducaoFragmentDirections.vaiParaTrabalhoEspecifico(personagemId);
         acao.setTrabalhoProducao(trabalho);
         acao.setCodigoRequisicao(CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO);
         Navigation.findNavController(binding.getRoot()).navigate(acao);
    }
    private void pegaTodosTrabalhos() {
        trabalhoProducaoViewModel.pegaTodosTrabalhosProducao().observe(getViewLifecycleOwner(), resultadoTodosTrabalhos -> {
            if (resultadoTodosTrabalhos.getDado() != null) {
                trabalhos = resultadoTodosTrabalhos.getDado();
                indicadorProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                atualizaListaTrabalho();
            }
            if (resultadoTodosTrabalhos.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoTodosTrabalhos.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void sincronizaTrabalhos() {
        trabalhoProducaoViewModel.sicronizaTrabalhosProducao().observe(getViewLifecycleOwner(), resultadoSincronizaTrabalhosProducao -> {
            if (resultadoSincronizaTrabalhosProducao.getErro() != null) {
                Snackbar.make(binding.getRoot(), "Erro: "+resultadoSincronizaTrabalhosProducao.getErro(), Snackbar.LENGTH_LONG).show();
            }
            pegaTodosTrabalhos();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), personagemSelecionado -> {
            if (personagemSelecionado == null) return;
            personagemId = personagemSelecionado.getId();
            TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(new TrabalhoProducaoRepository(getContext(), personagemId));
            trabalhoProducaoViewModel = new ViewModelProvider(this, trabalhoProducaoViewModelFactory).get(TrabalhoProducaoViewModel.class);
            pegaTodosTrabalhos();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}