package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentDetalhesProfissaoPersonagemBinding;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoPersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;
import com.kevin.gestorproducao.utilitario.Formatador;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class DetalhesProfissaoPersonagemFragment
    extends BaseFragment<FragmentDetalhesProfissaoPersonagemBinding>
    implements MenuProvider
{
    private ProfissaoPersonagem profissaoRecebida;
    private TextInputEditText edtExperiencia;
    private SwitchMaterial swtPrioridade;
    private ProfissaoPersonagemViewModel profissaoPersonagemViewModel;
    private ArrayList<TrabalhoProducao> producao;
    private TrabalhoProducaoViewModel producaoViewModel;
    private PersonagemViewModel personagemViewModel;
    private CircularProgressIndicator indicadorAtual, indicadorMaximo, indicadorProduzindo, indicadorProduzir;
    private TextView txtExpNecessaria, txtExpProduzir, txtExpProduzindo, txtExpRelativa;
    private TextInputLayout txtExperiencia;
    private NavController controlador;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DetalhesProfissaoPersonagemFragmentArgs argumentos = DetalhesProfissaoPersonagemFragmentArgs.fromBundle(
            getArguments()
        );

        profissaoRecebida = argumentos.getProfissao();

    }

    @Override
    protected FragmentDetalhesProfissaoPersonagemBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesProfissaoPersonagemBinding.inflate(inflater, container, false);
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
        preencheCampos();
        configuraResultadoModificacao();
        observaPersonagem();
        observaProducao();
    }

    private void observaProducao() {
        producaoViewModel.getProducoes().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getDado() != null) {
                    producao = resultado.getDado().stream().filter(
                        trabalho ->
                            trabalho.getProfissao().equals(profissaoRecebida.getNome())
                    ).collect(Collectors.toCollection(ArrayList::new));

                    configuraBarraProgressoCircular();
                }

                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(resultado.getErro());
                }
            }
        );
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            false,
            false,
            true,
            false,
            false,
            profissaoRecebida.getNome(),
            false
        );
    }

    private void observaPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                producaoViewModel.setIdPersonagem(personagem.getId());
                profissaoPersonagemViewModel.setIdPersonagem(personagem.getId());

                producaoViewModel.recuperaProducaoPorProfissaoPersonagem();
            }
        );
    }

    private void configuraResultadoModificacao() {
        profissaoPersonagemViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado.getErro() == null) {
                    voltaParaListaProfissoes();
                }

                txtExperiencia.setError(resultado.getErro());
            }
        );
    }

    private void voltaParaListaProfissoes() {
        controlador.navigateUp();
    }

    private void preencheCampos() {
        edtExperiencia.setText(String.valueOf(profissaoRecebida.getExperiencia()));
        swtPrioridade.setChecked(profissaoRecebida.isPrioridade());
    }

    private void inicializaComponentes() {
        txtExperiencia = binding.txtExperienciaProfissaoFragment;
        edtExperiencia = binding.edtExperienciaProfissaoFragment;
        swtPrioridade = binding.swtPrioridadeProfissaoFragment;

        indicadorMaximo = binding.indicadorExperienciaMaxima;
        indicadorAtual = binding.indicadorExperienciaAtual;
        indicadorProduzindo = binding.indicadorExperienciaProduzindo;
        indicadorProduzir = binding.indicadorExperienciaProduzir;

        txtExpNecessaria = binding.txtExperienciaNecessariaProfissaoFragment;
        txtExpProduzir = binding.txtExperienciaProduzirProfissaoFragment;
        txtExpProduzindo = binding.txtExperienciaProduzindoProfissaoFragment;
        txtExpRelativa = binding.txtExperienciaRelativaProfissaoFragment;

        producao = new ArrayList<>();

        configurarMascaraMilhar(edtExperiencia);

        controlador = Navigation.findNavController(binding.getRoot());

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());
        profissaoPersonagemViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(ProfissaoPersonagemViewModel.class);

        producaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoProducaoViewModel.class);

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);
    }

    private void confirmarModificacao() {
        Integer experiencia = defineValorExperiencia();
        if (experiencia == null) return;

        ProfissaoPersonagem profissao = new ProfissaoPersonagem();
        profissao.setId(profissaoRecebida.getId());
        profissao.setExperiencia(experiencia);
        profissao.setPrioridade(swtPrioridade.isChecked());

        profissaoPersonagemViewModel.modificaExperienciaProfissao(profissao);
    }

    @Nullable
    private Integer defineValorExperiencia() {
        String experiencia = edtExperiencia.getText().toString().trim();

        if (experiencia.isEmpty()) {
            txtExperiencia.setError(getString(R.string.stringCampoObrigatorio));
            return null;
        }

        int novaExperiencia;
        try {
            novaExperiencia = obterValorNumerico(edtExperiencia);

            if (novaExperiencia < 0) {
                throw new NumberFormatException(getString(R.string.stringExperienciaNaoPodeSerNegativa));
            }
        } catch (NumberFormatException e) {
            txtExperiencia.setError(e.getMessage());
            return null;
        }

        if (Objects.equals(profissaoRecebida.getExperiencia(), novaExperiencia) &&
            swtPrioridade.isChecked() == profissaoRecebida.isPrioridade()
        ) {
            voltaParaListaProfissoes();
            return null;
        }

        return novaExperiencia;
    }

    private void configuraBarraProgressoCircular() {
        int xpNecessario = profissaoRecebida.getXpNecessario();

        indicadorMaximo.setMax(xpNecessario);
        indicadorAtual.setMax(xpNecessario);
        indicadorProduzindo.setMax(xpNecessario);
        indicadorProduzir.setMax(xpNecessario);

        int experienciaAtual = profissaoRecebida.getExperienciaRelativa();
        int experienciaProduzindo = 0;
        int experienciaProduzir = 0;
        for(TrabalhoProducao trabalho : producao) {
            if (trabalho.getProfissao().equals(profissaoRecebida.getNome())){
                if (trabalho.ehProduzindo()) {
                    experienciaProduzindo += trabalho.getExperiencia();
                }
                if (trabalho.ehProduzir()) {
                    experienciaProduzir += trabalho.getExperiencia();
                }
            }
        }
        txtExpNecessaria.setText(Formatador.formatarMilhar(xpNecessario));
        txtExpRelativa.setText(Formatador.formatarMilhar(experienciaAtual));
        txtExpProduzir.setText(Formatador.formatarMilhar(experienciaProduzir));
        txtExpProduzindo.setText(Formatador.formatarMilhar(experienciaProduzindo));

        txtExpRelativa.setTextColor(getContext().getColor(R.color.cor_background_feito));
        txtExpProduzir.setTextColor(getContext().getColor(R.color.cor_texto_licenca_principiante));
        txtExpProduzindo.setTextColor(getContext().getColor(R.color.cor_background_produzindo));
        configuraVisibilidadeTxt(xpNecessario, txtExpNecessaria);
        configuraVisibilidadeTxt(experienciaAtual, txtExpRelativa);
        configuraVisibilidadeTxt(experienciaProduzir, txtExpProduzir);
        configuraVisibilidadeTxt(experienciaProduzindo, txtExpProduzindo);
        configuraVisibilidadeTxt(xpNecessario, binding.txtLegendaExperienciaNecessariaProfissaoFragment);
        configuraVisibilidadeTxt(experienciaAtual, binding.txtLegendaExperienciaRelativaProfissaoFragment);
        configuraVisibilidadeTxt(experienciaProduzindo, binding.txtLegendaExperienciaProduzindoProfissaoFragment);
        configuraVisibilidadeTxt(experienciaProduzir, binding.txtLegendaExperienciaProduzirProfissaoFragment);


        experienciaProduzindo += experienciaAtual;
        experienciaProduzir += experienciaProduzindo;

        animateProgress(indicadorAtual, experienciaAtual);
        animateProgress(indicadorProduzir, experienciaProduzir);
        animateProgress(indicadorProduzindo, experienciaProduzindo);
    }

    private void configuraVisibilidadeTxt(int experiencia, TextView txtView) {
        int visibilidade = experiencia == 0 ? GONE : VISIBLE;
        txtView.setVisibility(visibilidade);
    }

    private void animateProgress(CircularProgressIndicator indicador, int experiencia) {
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
            indicador,
            "progress",
            0,
            experiencia
        );
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma) {
            confirmarModificacao();
            return true;
        }

        return false;
    }
}