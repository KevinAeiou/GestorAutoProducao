package com.kevin.ceep.ui.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.ActivityMainBinding;
import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.ui.fragment.ConfirmaTrabalhoFragmentArgs;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private List<Personagem> personagens;
    private NavigationView navigationView;
    private Personagem personagemSelecionado;
    private TextView txtCabecalhoEstado, txtCabecalhoUso, txtCabecalhoEspacoProducao, txtCabecalhoAutoProducao;
    private AutoCompleteTextView autoCompleteCabecalhoNome;
    private PersonagemViewModel personagemViewModel;
    private AppBarConfiguration appBarConfiguration;
    private NavController controlador;
    private Toolbar toolbar;
    private EstadoAppViewModel estadoAppViewModel;

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inicializaComponentes();
        configuraClickAutoComplete();
        configuraToolbar();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        controlador = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.listaTrabalhosProducao, R.id.listaTrabalhosEstoque, R.id.listaTrabalhosVendidos, R.id.listaProfissoes).setOpenableLayout(drawerLayout).build();
        NavigationUI.setupWithNavController(toolbar, controlador, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, controlador);
        NavigationUI.setupWithNavController(binding.navegacaoInferior, controlador);
        controlador.addOnDestinationChangedListener((navController, navDestination, bundle) -> {
            if (navDestination.getId() == R.id.splashscreenFragment) {
                FirebaseAuth.getInstance().signOut();
            }
            if (navDestination.getId() == R.id.confirmaTrabalhoFragment) {
                assert bundle != null;
                Trabalho trabalho = ConfirmaTrabalhoFragmentArgs.fromBundle(bundle).getTrabalho();
                navDestination.setLabel(trabalho.getNome());
            }
            configuraComponentesVisuais();
        });
    }

    private void configuraComponentesVisuais() {
        estadoAppViewModel.componentes.observe(this, componentes -> {
            mostraBarraAcao(componentes);
            mostraMenuNavegacaoLateral(componentes);
            mostraMenuNavegacaoInferior(componentes);
            configuraMenuBarraAcao(componentes);
        });
    }

    private void mostraMenuNavegacaoInferior(ComponentesVisuais componentes) {
        if (componentes.menuNavegacaoInferior) {
            binding.navegacaoInferior.setVisibility(VISIBLE);
            return;
        }
        binding.navegacaoInferior.setVisibility(GONE);
    }

    private void mostraMenuNavegacaoLateral(ComponentesVisuais componentes) {
        if (componentes.menuNavegacaoLateral) {
            binding.navegacaoView.setVisibility(VISIBLE);
            return;
        }
        binding.navegacaoView.setVisibility(GONE);
    }

    private void mostraBarraAcao(ComponentesVisuais componentes) {
        if (componentes.appBar) {
            getSupportActionBar().show();
            return;
        }
        getSupportActionBar().hide();
    }

    private void configuraMenuBarraAcao(ComponentesVisuais componentes) {
        if (componentes.itemMenuBusca || componentes.itemMenuConfirma) {
            addMenuProvider(new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menu.clear();
                    if (componentes.itemMenuBusca) menuInflater.inflate(R.menu.menu_busca, menu);
                    if (componentes.itemMenuConfirma) menuInflater.inflate(R.menu.menu_confirma, menu);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    return false;
                }
            });
            return;
        }
        addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(controlador, appBarConfiguration) || super.onSupportNavigateUp();
    }

    private void configuraClickAutoComplete() {
        autoCompleteCabecalhoNome.setOnItemClickListener((adapterView, view, i, l) -> {
            personagemViewModel.definePersonagemSelecionado(personagens.get(i));
            atualizaCabecalhoPersonagemSelecionado();
            controlador.navigate(controlador.getCurrentDestination().getId());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sincronizaPersonagens();
    }
    private void configuraToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void inicializaComponentes() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navegacao_view);
        View cabecalho = navigationView.getHeaderView(0);
        autoCompleteCabecalhoNome = cabecalho.findViewById(R.id.autoCompleteCabecalhoNomePersonagem);
        txtCabecalhoEstado = cabecalho.findViewById(R.id.txtCabecalhoEstadoPersonagem);
        txtCabecalhoUso = cabecalho.findViewById(R.id.txtCabecalhoUsoPersonagem);
        txtCabecalhoAutoProducao = cabecalho.findViewById(R.id.txtCabecalhoAutoProducaoPersonagem);
        txtCabecalhoEspacoProducao = cabecalho.findViewById(R.id.txtCabecalhoEspacoProducaoPersonagem);
        personagemSelecionado = null;
        personagens = new ArrayList<>();
        estadoAppViewModel = new ViewModelProvider(this).get(EstadoAppViewModel.class);
    }

    private void atualizaCabecalhoPersonagemSelecionado() {
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(new PersonagemRepository(getApplicationContext()));
        personagemViewModel = new ViewModelProvider(this, personagemViewModelFactory).get(PersonagemViewModel.class);
        personagemViewModel.pegaPersonagemSelecionado().observe(this, personagem -> {
            if (personagem == null) return;
            String estado= getString(R.string.stringInativo);
            String uso= getString(R.string.stringInativo);
            String autoProducao= getString(R.string.stringInativo);
            if (personagem.getEstado()) estado = getString(R.string.stringAtivo);
            if (personagem.getUso()) uso = getString(R.string.stringAtivo);
            if (personagem.isAutoProducao()) autoProducao = getString(R.string.stringAtivo);
            txtCabecalhoEstado.setText(getString(R.string.stringEstadoValor,estado));
            txtCabecalhoUso.setText(getString(R.string.stringUsoValor,uso));
            txtCabecalhoAutoProducao.setText(getString(R.string.stringAutoProducaoValor, autoProducao));
            txtCabecalhoEspacoProducao.setText(getString(R.string.stringEspacoProducaoValor,personagem.getEspacoProducao()));
            personagemSelecionado = personagem;

        });
    }
    private void pegaTodosPersonagens() {
        personagens.clear();
        FirebaseUser usuarioID = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioID == null) return;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(new PersonagemRepository(getApplicationContext()));
        personagemViewModel = new ViewModelProvider(this, personagemViewModelFactory).get(PersonagemViewModel.class);
        personagemViewModel.pegaTodosPersonagens().observe(this, resultadoPersonagens -> {
            if (resultadoPersonagens.getDado() != null) {
                personagens = resultadoPersonagens.getDado();
            }
            if (resultadoPersonagens.getErro() != null) {
                Snackbar.make(getApplicationContext(), Objects.requireNonNull(getCurrentFocus()), "Erro: "+resultadoPersonagens.getErro(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void sincronizaPersonagens() {
        FirebaseUser usuarioID = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioID == null) return;
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(new PersonagemRepository(getApplicationContext()));
        personagemViewModel = new ViewModelProvider(this, personagemViewModelFactory).get(PersonagemViewModel.class);
        personagemViewModel.sincronizaPersonagens().observe(this, resultadoSincroniza -> {
            if (resultadoSincroniza.getErro() != null) {
                Snackbar.make(getApplicationContext(), Objects.requireNonNull(getCurrentFocus()), "Erro: "+resultadoSincroniza.getErro(), Snackbar.LENGTH_LONG).show();
            }
            pegaTodosPersonagens();
            if (personagemSelecionado == null) personagemViewModel.definePersonagemSelecionado(personagens.get(0));
            atualizaCabecalhoPersonagemSelecionado();
            configuraDropDownPersonagens();
        });
    }

    private void configuraDropDownPersonagens() {
        ArrayList<String> nomesPersonagens = new ArrayList<>();
        for (Personagem personagem : personagens) {
            nomesPersonagens.add(personagem.getNome());
        }
        ArrayAdapter<String> adapterPersonagens = new ArrayAdapter<>(this, R.layout.item_dropdrown, nomesPersonagens);
        adapterPersonagens.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteCabecalhoNome.setText(personagemSelecionado.getNome());
        autoCompleteCabecalhoNome.setAdapter(adapterPersonagens);
    }
}
