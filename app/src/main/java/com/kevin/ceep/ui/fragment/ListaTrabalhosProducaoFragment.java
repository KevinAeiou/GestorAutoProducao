package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_NOME_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentListaTrabalhosProducaoBinding;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.TrabalhoProducaoRepository;
import com.kevin.ceep.ui.activity.ListaNovaProducaoActivity;
import com.kevin.ceep.ui.activity.TrabalhoEspecificoActivity;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEspecificoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoProducaoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;
import com.kevin.ceep.ui.viewModel.TrabalhoProducaoViewModel;
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
    private TrabalhoProducaoViewModel trabalhoProducaoViewModel;

    public ListaTrabalhosProducaoFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recebeDadosIntent();
    }

    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaTrabalhosProducaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inicializaComponentes();
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

    private void recebeDadosIntent() {
        Bundle dadosRecebidos = getArguments();
        if (dadosRecebidos != null) {
            if (dadosRecebidos.containsKey(CHAVE_PERSONAGEM)){
                personagemId = dadosRecebidos.getString(CHAVE_PERSONAGEM);
                if (personagemId != null) {
                    TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(new TrabalhoProducaoRepository(getContext(), personagemId));
                    trabalhoProducaoViewModel = new ViewModelProvider(this, trabalhoProducaoViewModelFactory).get(TrabalhoProducaoViewModel.class);
                }
            }
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
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (personagemId != null){
                sincronizaTrabalhos();
            }
        });
    }
    private void configuraBotaoInsereTrabalho() {
        binding.floatingActionButton.setOnClickListener(v -> vaiParaListaNovaProducaoActivity());
    }
    private void vaiParaListaNovaProducaoActivity() {
        Intent iniciaVaiParaListaNovaProducaoActivity =
                new Intent(getContext(), ListaNovaProducaoActivity.class);
        iniciaVaiParaListaNovaProducaoActivity.putExtra(CHAVE_PERSONAGEM, personagemId);
        startActivity(iniciaVaiParaListaNovaProducaoActivity);
    }

    private void inicializaComponentes() {
        trabalhos = new ArrayList<>();
        recyclerView = binding.listaTrabalhoRecyclerView;
        swipeRefreshLayout = binding.swipeRefreshLayoutTrabalhos;
        indicadorProgresso = binding.indicadorProgressoListaTrabalhosFragment;
        grupoChipsEstados = binding.chipGrupId;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
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
        trabalhoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Trabalho trabalho, int adapterPosition) {
                vaiParaTrabalhoEspecificoActivity(trabalho);
            }

            @Override
            public void onItemClick(ListaTrabalhoEspecificoAdapter trabalhoEspecificoAdapter) {

            }

            @Override
            public void onItemClick(TrabalhoEstoque trabalhoEstoque, int adapterPosition, int botaoId) {

            }
        });
    }
    private void vaiParaTrabalhoEspecificoActivity(Trabalho trabalho) {
        Intent iniciaTrabalhoEspecificoActivity=
                new Intent(getActivity(), TrabalhoEspecificoActivity.class);
        iniciaTrabalhoEspecificoActivity.putExtra(CHAVE_TRABALHO, CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO);
        iniciaTrabalhoEspecificoActivity.putExtra(CHAVE_NOME_TRABALHO, trabalho);
        iniciaTrabalhoEspecificoActivity.putExtra(CHAVE_PERSONAGEM, personagemId);
        startActivity(iniciaTrabalhoEspecificoActivity);
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
        if (personagemId != null){
            sincronizaTrabalhos();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}