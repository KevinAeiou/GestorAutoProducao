package com.kevin.gestorproducao.ui.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static androidx.navigation.ui.NavigationUI.setupWithNavController;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.ActivityMainBinding;
import com.kevin.gestorproducao.databinding.CabecalhoBinding;
import com.kevin.gestorproducao.model.Personagem;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.EstadoAppViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;
import com.kevin.gestorproducao.utilitario.TemaUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView txtCabecalhoNome, txtCabecalhoEstado, txtCabecalhoUso, txtCabecalhoEspacoProducao,
        txtCabecalhoAutoProducao, txtTituloCentral;
    private PersonagemViewModel personagemViewModel;
    private AppBarConfiguration appBarConfiguration;
    private NavController controlador;
    private Toolbar toolbar;
    private EstadoAppViewModel estadoAppViewModel;
    private FirebaseAuth auth;
    private BottomNavigationView menuInferior;
    private MaterialButton btnSair;

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inicializaComponentes();
        configuraComponentesVisuais();
        configuraMenu();
        configuraBotaoSair();

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            if (item.getItemId() == R.id.itemAlterarTema) {
                mostraDialogoAlterarTema();
                return true;
            }

            NavigationUI.onNavDestinationSelected(item, controlador);
            return true;
        });

        configuraToolbar();
        configuraAppBar();
        configuraTituloSelecaoPersonagem();
        observarPersonagem();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        personagemViewModel.sincronizaPersonagens(user.getUid());
    }

    private void configuraBotaoSair() {
        btnSair.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            fazerLogout();
        });
    }

    private void mostraDialogoAlterarTema() {
        String[] opcoes = getResources().getStringArray(R.array.arrayOpcoesTema);
        int indiceSelecionado = TemaUtil.pegaIndiceOpcaoSelecionada(this);

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.stringEscolhaOTema)
            .setSingleChoiceItems(opcoes, indiceSelecionado, (dialog, which) -> {
                TemaUtil.salvaEAplicaTema(this, TemaUtil.pegaModoNoturnoPorIndice(which));
                dialog.dismiss();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void configuraTituloSelecaoPersonagem() {
        txtTituloCentral.setOnClickListener(v ->
            controlador.navigate(R.id.vaiParaSelecaoPersonagem)
        );
    }

    private void fazerLogout() {
        auth.signOut();

        personagemViewModel.definePersonagemSelecionado(null);
        if (controlador.getCurrentDestination() != null) {
            controlador.navigate(
                R.id.vaiParaSlashScreen,
                null,
                new NavOptions.Builder().setPopUpTo(
                    R.id.listaTrabalhosProducao,
                    true
                    ).build()
            );
        }
    }

    private void configuraAppBar() {
        appBarConfiguration = new AppBarConfiguration.Builder(
            R.id.listaTrabalhosProducao,
            R.id.listaTrabalhosEstoque,
            R.id.listaTrabalhosVendidos,
            R.id.listaProfissoesPersonagem
        ).setOpenableLayout(drawerLayout).build();

        setupWithNavController(toolbar, controlador, appBarConfiguration);
        setupWithNavController(menuInferior, controlador);
    }

    private void configuraComponentesVisuais() {
        estadoAppViewModel.componentes.observe(
            this,
            componentes -> {
                mostraBarraAcao(componentes);
                mostraMenuNavegacaoLateral(componentes);
                mostraMenuNavegacaoInferior(componentes);
                invalidateOptionsMenu();
            }
        );
    }

    private void mostraMenuNavegacaoInferior(ComponentesVisuais componentes) {
        menuInferior.animate().cancel();

        if (componentes.menuNavegacaoInferior) {
            mostrarMenuInferior();
        } else {
            esconderMenuInferior();
        }
    }

    private void esconderMenuInferior() {
        menuInferior.animate().cancel();

        menuInferior.animate()
            .translationY(menuInferior.getHeight())
            .alpha(0.8f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> {
                menuInferior.setVisibility(GONE);
                menuInferior.setTranslationY(0);
            })
            .start();
    }

    private void mostrarMenuInferior() {
        menuInferior.animate().cancel();

        menuInferior.setVisibility(VISIBLE);

        menuInferior.animate()
            .translationY(0)
            .alpha(1f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void mostraMenuNavegacaoLateral(ComponentesVisuais componentes) {
        if (componentes.menuNavegacaoLateral) {
            navigationView.setVisibility(VISIBLE);
            return;
        }

        navigationView.setVisibility(GONE);
    }

    private void mostraBarraAcao(ComponentesVisuais componentes) {
        if (getSupportActionBar() == null) return;

        if (!componentes.appBar) {
            getSupportActionBar().hide();
            return;
        }

        getSupportActionBar().show();

        txtTituloCentral.setVisibility(GONE);

        if (componentes.selecaoPersonagem) {

            txtTituloCentral.setVisibility(VISIBLE);
            getSupportActionBar().setTitle("");

            return;
        }

        if (componentes.titulo != null) {
            getSupportActionBar().setTitle(componentes.titulo);
        } else {
            getSupportActionBar().setTitle("");
        }
    }

    private void configuraMenu() {
        addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();

                ComponentesVisuais componentes = estadoAppViewModel.componentes.getValue();
                if (componentes == null) return;

                if (componentes.itemMenuBusca) {
                    menuInflater.inflate(R.menu.menu_busca, menu);
                }

                if (componentes.itemMenuConfirma) {
                    menuInflater.inflate(R.menu.menu_confirma, menu);
                }
                if (componentes.itemMenuEdita) {
                    menuInflater.inflate(R.menu.menu_edita, menu);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                return false;
            }

        }, this, Lifecycle.State.RESUMED);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(controlador, appBarConfiguration) || super.onSupportNavigateUp();
    }

    private void configuraToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void inicializaComponentes() {
        toolbar = binding.appBarMain.toolbar;
        drawerLayout = binding.drawerLayout;
        navigationView = binding.navegacaoView;
        menuInferior = binding.navegacaoInferior;
        btnSair = binding.btnSair;

        CabecalhoBinding cabecalhoBinding = CabecalhoBinding.bind(
            navigationView.getHeaderView(0)
        );
        View header = navigationView.getHeaderView(0);
        int headerBasePaddingTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            v.setPadding(v.getPaddingLeft(), headerBasePaddingTop + topInset, v.getPaddingRight(), v.getPaddingBottom());

            return insets;
        });

        txtCabecalhoNome = cabecalhoBinding.txtCabecalhoNomePersonagem;
        txtCabecalhoEstado = cabecalhoBinding.txtCabecalhoEstadoPersonagem;
        txtCabecalhoUso = cabecalhoBinding.txtCabecalhoUsoPersonagem;
        txtCabecalhoAutoProducao = cabecalhoBinding.txtCabecalhoAutoProducaoPersonagem;
        txtCabecalhoEspacoProducao = cabecalhoBinding.txtCabecalhoEspacoProducaoPersonagem;
        txtTituloCentral = binding.appBarMain.txtTituloCentral;
        auth = FirebaseAuth.getInstance();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment_content_main);

        assert navHostFragment != null;
        controlador = navHostFragment.getNavController();

        estadoAppViewModel = new ViewModelProvider(
            this
        ).get(EstadoAppViewModel.class);

        ViewModelFactory personagemFactory = new ViewModelFactory(
            getApplicationContext()
        );
        personagemViewModel = new ViewModelProvider(
            this,
            personagemFactory
        ).get(PersonagemViewModel.class);
    }

    private void observarPersonagem() {
        personagemViewModel.getPersonagens().observe(
            this,
            resultado -> {
                if (resultado.getErro() == null) {
                    ArrayList<Personagem> personagens = resultado.getDado();
                    if (personagemViewModel.pegaPersonagemSelecionado().getValue() == null
                        && personagens != null
                        && !personagens.isEmpty()
                    ) {
                        personagemViewModel.definePersonagemSelecionado(personagens.get(0));
                    }
                }
            }
        );

        personagemViewModel.pegaPersonagemSelecionado().observe(
            this,
            personagem -> {
                if (personagem == null) return;

                txtTituloCentral.setText(personagem.getNome());

                String estado= getString(R.string.stringInativo);
                String uso= getString(R.string.stringInativo);
                String autoProducao= getString(R.string.stringInativo);

                if (personagem.getEstado()) estado = getString(R.string.stringAtivo);
                if (personagem.getUso()) uso = getString(R.string.stringAtivo);
                if (personagem.isAutoProducao()) autoProducao = getString(R.string.stringAtivo);

                txtCabecalhoNome.setText(personagem.getNome());
                txtCabecalhoEstado.setText(getString(R.string.stringEstadoValor,estado));
                txtCabecalhoUso.setText(getString(R.string.stringUsoValor,uso));
                txtCabecalhoAutoProducao.setText(getString(R.string.stringAutoProducaoValor, autoProducao));
                txtCabecalhoEspacoProducao.setText(getString(R.string.stringEspacoProducaoValor,personagem.getEspacoProducao()));
            }
        );

        personagemViewModel.getSincronizacaoResultado().observe(
            this,
            resultado -> {
                if (resultado.getErro() == null) {

                    personagemViewModel.recuperaPersonagens();
                }
            }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeOuvintePersonagem();
        binding = null;
    }

    private void removeOuvintePersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }
}
