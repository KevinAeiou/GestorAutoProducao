package com.kevin.ceep.ui.activity;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_REQUISICAO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TITULO_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_ALTERA_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_INSERE_TRABALHO;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.ActivityMainBinding;
import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.ui.fragment.ListaEstoqueFragment;
import com.kevin.ceep.ui.fragment.ListaTrabalhosVendidosFragment;
import com.kevin.ceep.ui.fragment.ListaProfissoesFragment;
import com.kevin.ceep.ui.fragment.ListaTrabalhosProducaoFragment;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private List<Personagem> personagens;
    private NavigationView navigationView;
    private Personagem personagemSelecionado;
    private TextView txtCabecalhoEstado, txtCabecalhoUso, txtCabecalhoEspacoProducao;
    private AutoCompleteTextView autoCompleteCabecalhoNome;
    private PersonagemViewModel personagemViewModel;
    private int itemNavegacao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inicializaComponentes();
        configuraToolbar();
        configuraClickAutoComplete();
        navigationView.bringToFront();
        configuraToogle();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(itemNavegacao);
    }

    private void configuraClickAutoComplete() {
        autoCompleteCabecalhoNome.setOnItemClickListener((adapterView, view, i, l) -> {
            personagemViewModel.definePersonagemSelecionado(personagens.get(i));
            definePersonagemSelecionado();
            atualizaCabecalhoPersonagemSelecionado();
        });
    }

    @Override
    protected void onResume() {
        sincronizaPersonagens();
        super.onResume();
    }

    private void configuraToogle() {
        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, drawerLayout, R.string.abre_menu_navegacao, R.string.fecha_menu_navegacao);
        drawerLayout.addDrawerListener(toogle);
        toogle.syncState();
    }

    private void configuraToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void inicializaComponentes() {
        setTitle(CHAVE_TITULO_TRABALHO);
        itemNavegacao = R.id.listaTrabalhosProducao;
        itemNavegacao = recebeDadosIntent(itemNavegacao);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navegacao_view);
        View cabecalho = navigationView.getHeaderView(0);
        autoCompleteCabecalhoNome = cabecalho.findViewById(R.id.autoCompleteCabecalhoNomePersonagem);
        txtCabecalhoEstado = cabecalho.findViewById(R.id.txtCabecalhoEstadoPersonagem);
        txtCabecalhoUso = cabecalho.findViewById(R.id.txtCabecalhoUsoPersonagem);
        txtCabecalhoEspacoProducao = cabecalho.findViewById(R.id.txtCabecalhoEspacoProducaoPersonagem);
        personagemSelecionado = null;
        personagens = new ArrayList<>();
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(new PersonagemRepository(getApplicationContext()));
        personagemViewModel = new ViewModelProvider(this, personagemViewModelFactory).get(PersonagemViewModel.class);
    }

    private void atualizaCabecalhoPersonagemSelecionado() {
        personagemViewModel.pegaPersonagemSelecionado().observe(this, personagem -> {
            if (personagem == null) return;
            String estado= "Inativo";
            String uso= "Inativo";
            if (personagem.getEstado()) estado = "Ativo";
            if (personagem.getUso()) uso = "Ativo";
            txtCabecalhoEstado.setText(getString(R.string.stringEstadoValor,estado));
            txtCabecalhoUso.setText(getString(R.string.stringUsoValor,uso));
            txtCabecalhoEspacoProducao.setText(getString(R.string.stringEspacoProducaoValor,personagem.getEspacoProducao()));
            personagemSelecionado = personagem;
            mostraFragmentSelecionado(Objects.requireNonNull(navigationView.getCheckedItem()));
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void mostraFragmentSelecionado(MenuItem itemNavegacao) {
        Fragment fragmentoSelecionado = null;
        Bundle argumento = new Bundle();
        argumento.putString(CHAVE_PERSONAGEM, personagemSelecionado.getId());
        switch (itemNavegacao.getItemId()){
            case R.id.listaTrabalhosProducao:
                fragmentoSelecionado = new ListaTrabalhosProducaoFragment();
                fragmentoSelecionado.setArguments(argumento);
                break;
            case R.id.listaEstoque:
                fragmentoSelecionado = new ListaEstoqueFragment();
                fragmentoSelecionado.setArguments(argumento);
                break;
            case R.id.listaProdutosVendidos:
                fragmentoSelecionado = new ListaTrabalhosVendidosFragment();
                fragmentoSelecionado.setArguments(argumento);
                break;
            case R.id.listaProfissoes:
                fragmentoSelecionado = new ListaProfissoesFragment();
                fragmentoSelecionado.setArguments(argumento);
                break;
            case R.id.nav_configuracao:
                vaiParaAtributosPersonagem(CODIGO_REQUISICAO_ALTERA_TRABALHO);
                break;
            case R.id.nav_novo_personagem:
                vaiParaAtributosPersonagem(CODIGO_REQUISICAO_INSERE_TRABALHO);
                break;
            case R.id.nav_novo_trabalho:
                vaiParaListaTodosTrabalhos();
                break;
            case R.id.nav_sair:
                FirebaseAuth.getInstance().signOut();
                vaiParaEntraActivity();
                break;
        }
        if (fragmentoSelecionado != null) {
            reposicionaFragmento(fragmentoSelecionado);
        }
    }

    private void vaiParaListaTodosTrabalhos() {
        Intent iniciaVaiParaListaTodosTrabalhos = new Intent(getApplicationContext(), ListaTodosTrabalhosActivity.class);
        startActivity(iniciaVaiParaListaTodosTrabalhos);
    }

    private void vaiParaAtributosPersonagem(int codigoRequisicao) {
        Intent iniciaVaiParaAtributosPersonagem = new Intent(getApplicationContext(), AtributosPersonagemActivity.class);
        iniciaVaiParaAtributosPersonagem.putExtra(CHAVE_PERSONAGEM, personagemSelecionado);
        iniciaVaiParaAtributosPersonagem.putExtra(CHAVE_REQUISICAO, codigoRequisicao);
        startActivity(iniciaVaiParaAtributosPersonagem);
    }

    private int recebeDadosIntent(int itemNavegacao) {
        Intent dadosRecebidos = getIntent();
        String idPersonagemRecebido;
        if (dadosRecebidos.hasExtra(CHAVE_PERSONAGEM)){
            idPersonagemRecebido = (String) dadosRecebidos.getSerializableExtra(CHAVE_PERSONAGEM);
            if (idPersonagemRecebido != null){
                itemNavegacao = R.id.listaTrabalhosProducao;
            }
        }
        return itemNavegacao;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        item.setChecked(true);
        mostraFragmentSelecionado(item);
        return true;
    }

    private void vaiParaEntraActivity() {
        Intent vaiParaEntraActivity = new Intent(getApplicationContext(),
                EntrarUsuarioActivity.class);
        startActivity(vaiParaEntraActivity, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        finish();
    }
    private void reposicionaFragmento(Fragment fragmento) {
        FragmentManager gerenciadorDeFragmento = getSupportFragmentManager();
        FragmentTransaction transicaoDeFragmento = gerenciadorDeFragmento.beginTransaction();
        transicaoDeFragmento.replace(R.id.nav_host_fragment_content_main, fragmento);
        transicaoDeFragmento.commit();
    }
    private void pegaTodosPersonagens() {
        personagens.clear();
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

    private void definePersonagemSelecionado() {
        personagemViewModel.pegaPersonagemSelecionado().observe(this, personagem -> personagemSelecionado = personagem);
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
