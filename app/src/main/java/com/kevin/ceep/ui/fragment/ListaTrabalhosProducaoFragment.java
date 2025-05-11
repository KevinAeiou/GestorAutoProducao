package com.kevin.ceep.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO;
import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_INSERE_TRABALHO_PRODUCAO;
import static com.kevin.ceep.utilitario.Utilitario.stringContemString;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import java.util.stream.Collectors;

public class ListaTrabalhosProducaoFragment extends Fragment implements MenuProvider {
    private FragmentListaTrabalhosProducaoBinding binding;
    private ListaTrabalhoProducaoAdapter trabalhoAdapter;
    private RecyclerView recyclerView;
    private ArrayList<TrabalhoProducao> trabalhos, trabalhosFiltrados;
    private String idPersonagem, textoFiltro;
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
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser usuarioID = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioID == null) {
            NavDirections acao = ListaTrabalhosProducaoFragmentDirections.vaiParaSlashScreen();
            Navigation.findNavController(binding.getRoot()).navigate(acao);
            return;
        }
        inicializaComponentes();
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuBusca = true;
        componentesVisuais.menuNavegacaoLateral = true;
        componentesVisuais.menuNavegacaoInferior = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        configuraRecyclerView();
        configuraSwipeRefreshLayout();
        configuraBotaoInsereTrabalho();
        configuraDeslizeItem();
        configuraChipSelecionado();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textoFiltro = texto;
                    filtroLista();
                    mostraListaFiltrada();
                }
                return false;
            }
        });
    }

    private void mostraListaFiltrada() {
        if (trabalhosFiltrados.isEmpty()) {
            iconeListaVazia.setVisibility(VISIBLE);
            txtListaVazia.setVisibility(VISIBLE);
            trabalhoAdapter.limpaLista();
            return;
        }
        txtListaVazia.setVisibility(GONE);
        iconeListaVazia.setVisibility(GONE);
        trabalhoAdapter.atualiza(trabalhosFiltrados);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtroLista() {
        if (textoFiltro.isEmpty()) return;
        trabalhosFiltrados = (ArrayList<TrabalhoProducao>) trabalhosFiltrados.stream().filter(trabalho -> stringContemString(trabalho.getNome(), textoFiltro)).collect(Collectors.toList());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraChipSelecionado() {
        grupoChipsEstados.setOnCheckedStateChangeListener((group, checkedId) -> filtraListaPorEstado(checkedId.get(0)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filtraListaPorEstado(int checkedId) {
        Log.d("trabalhoProducao", "filtraListaPorEstado: " + checkedId);
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
        filtroListaChip(estado);
        filtroLista();
        if (trabalhosFiltrados.isEmpty()) {
            trabalhoAdapter.limpaLista();
            iconeListaVazia.setVisibility(View.VISIBLE);
            txtListaVazia.setVisibility(View.VISIBLE);
            return;
        }
        iconeListaVazia.setVisibility(View.GONE);
        txtListaVazia.setVisibility(View.GONE);
        trabalhoAdapter.atualiza(trabalhosFiltrados);
    }
    private void filtroListaChip(int estado) {
        Log.d("trabalhoProducao", "Estado: " + estado);
        trabalhosFiltrados.clear();
        Log.d("trabalhoProducao", "Limpou a lista de trabalhos filtrados");
        if (estado == -1){
            Log.d("trabalhoProducao", "Estado é igual a -1, clonando lista de trabalhos");
            trabalhosFiltrados = (ArrayList<TrabalhoProducao>) trabalhos.clone();
            return;
        }
        Log.d("trabalhoProducao", "Tamanho da lista trabalhos: " + trabalhos.size());
        for (TrabalhoProducao item : trabalhos) {
            if (item.getEstado() == estado) {
                Log.d("trabalhoProducao", "Item inserido na lista filtrada: " + item.getId());
                trabalhosFiltrados.add(item);
                Log.d("trabalhoProducao", "Tamanho da lista filtrada: " + trabalhosFiltrados.size());
            }
        }
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
                    trabalhosFiltrados.remove(trabalhoremovido);
                    Snackbar snackbarDesfazer = Snackbar.make(binding.getRoot(), trabalhoremovido.getNome()+ " excluido", Snackbar.LENGTH_LONG);
                    snackbarDesfazer.addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (event != DISMISS_EVENT_ACTION){
                                removeTrabalhoProducao(trabalhoremovido);
                                removeTrabalhoDaLista(trabalhoremovido);
                            }
                        }
                    });
                    snackbarDesfazer.setAnchorView(binding.floatingActionButton);
                    snackbarDesfazer.setAction(getString(R.string.stringDesfazer), v -> {
                        trabalhoAdapter.adiciona(trabalhoremovido, itemPosicao);
                        trabalhosFiltrados.add(itemPosicao, trabalhoremovido);
                    });
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

    private void removeTrabalhoProducao(TrabalhoProducao trabalho) {
        trabalhoProducaoViewModel.getRemocaoResultado().observe(getViewLifecycleOwner(), resultadoRemoveTrabalho -> {
            if (resultadoRemoveTrabalho.getErro() == null) return;
            Snackbar.make(binding.getRoot(), "Erro: "+resultadoRemoveTrabalho.getErro(), Snackbar.LENGTH_LONG).setAnchorView(binding.floatingActionButton).show();
        });
        trabalhoProducaoViewModel.removeTrabalhoProducao(trabalho);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), personagemSelecionado -> {
            if (personagemSelecionado == null) return;
            idPersonagem = personagemSelecionado.getId();
            recuperaTrabalhosProducao();
        }));
    }
    private void configuraBotaoInsereTrabalho() {
        binding.floatingActionButton.setOnClickListener(v -> {
            personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), personagemSelecionado -> {
                if (personagemSelecionado == null) return;
                idPersonagem = personagemSelecionado.getId();
            });
            if (idPersonagem.isEmpty()) {
                Snackbar.make(binding.getRoot(), "Selecione um personagem para continuar", Snackbar.LENGTH_LONG).setAnchorView(binding.floatingActionButton).show();
                return;
            }
            VaiParaListaTrabalhos acao = ListaTrabalhosProducaoFragmentDirections.vaiParaListaTrabalhos(idPersonagem);
            acao.setRequisicao(CODIGO_REQUISICAO_INSERE_TRABALHO_PRODUCAO);
            Navigation.findNavController(v).navigate(acao);
        });
    }

    private void inicializaComponentes() {
        idPersonagem = "";
        textoFiltro = "";
        trabalhos = new ArrayList<>();
        trabalhosFiltrados = new ArrayList<>();
        recyclerView = binding.listaTrabalhoRecyclerView;
        swipeRefreshLayout = binding.swipeRefreshLayoutTrabalhos;
        indicadorProgresso = binding.indicadorProgressoListaTrabalhosFragment;
        grupoChipsEstados = binding.chipGrupId;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(PersonagemRepository.getInstance());
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
        estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
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
         VaiParaTrabalhoEspecifico acao = ListaTrabalhosProducaoFragmentDirections.vaiParaTrabalhoEspecifico(idPersonagem);
         acao.setTrabalhoProducao(trabalho);
         acao.setCodigoRequisicao(CODIGO_REQUISICAO_ALTERA_TRABALHO_PRODUCAO);
         Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), personagemSelecionado -> {
            if (personagemSelecionado == null) return;
            idPersonagem = personagemSelecionado.getId();
            TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(idPersonagem);
            trabalhoProducaoViewModel = new ViewModelProvider(this, trabalhoProducaoViewModelFactory).get(idPersonagem, TrabalhoProducaoViewModel.class);
            recuperaTrabalhosProducao();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void recuperaTrabalhosProducao() {
        trabalhos.clear();
        trabalhoProducaoViewModel.getTrabalhosProducao().observe(getViewLifecycleOwner(), resultadoTrabalhosRecuperados -> {
            if (resultadoTrabalhosRecuperados.getDado() != null) {
                trabalhos = resultadoTrabalhosRecuperados.getDado();
                Log.d("trabalhoProducao", "Tamanho da lista de trabalhos recuperados: " + trabalhos.size());
                trabalhosFiltrados= (ArrayList<TrabalhoProducao>) trabalhos.clone();
                Log.d("trabalhoProducao", "Limpou a lista de trabalhos filtrados");
                indicadorProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                atualizaListaTrabalho();
            }
            if (resultadoTrabalhosRecuperados.getErro() == null) return;
            Snackbar.make(
                            binding.getRoot(),
                            "Erro ao recuperar trabalhos do servidor: " + resultadoTrabalhosRecuperados.getErro(),
                            Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.floatingActionButton)
                    .show();
        });
        trabalhoProducaoViewModel.recuperaTrabalhosProducao();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeOuvinteProducao();
        removeOuvintePersonagem();
        binding = null;
    }

    private void removeOuvintePersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeOuvinte();
    }

    private void removeOuvinteProducao() {
        if (trabalhoProducaoViewModel == null) return;
        trabalhoProducaoViewModel.removeOuvinte();
    }
}