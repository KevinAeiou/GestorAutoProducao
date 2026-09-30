package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_TRABALHO;
import static com.kevin.gestorproducao.ui.fragment.VisualizacaoTrabalhoFragmentDirections.vaiDeVisualizacaoParaDetalhesTrabalho;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentVisualizacaoTrabalhoBinding;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.model.Usuario;
import com.kevin.gestorproducao.ui.fragment.VisualizacaoTrabalhoFragmentDirections.VaiDeVisualizacaoParaDetalhesTrabalho;
import com.kevin.gestorproducao.ui.viewModel.AutenticacaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;

public class VisualizacaoTrabalhoFragment
    extends BaseFragment<FragmentVisualizacaoTrabalhoBinding>
    implements MenuProvider
{
    private Trabalho trabalhoRecebido;
    private TextView txtNome, txtNomeProducao, txtProfissao, txtExperiencia, txtNivel, txtRaridade;
    private View heroRaridadeTarja;
    private ChipGroup chipGroupTrabalhosNecessarios;
    private View layoutTrabalhosNecessarios;
    private TrabalhoViewModel trabalhoViewModel;
    private AutenticacaoViewModel autenticacaoViewModel;
    private boolean isAdministrador = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        VisualizacaoTrabalhoFragmentArgs argumentos = VisualizacaoTrabalhoFragmentArgs.fromBundle(
            getArguments()
        );

        trabalhoRecebido = argumentos.getTrabalho();
    }

    @Override
    protected FragmentVisualizacaoTrabalhoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentVisualizacaoTrabalhoBinding.inflate(inflater, container, false);
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
            isAdministrador
        );
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuEdita) {

            vaiParaDetalhesTrabalho();
            return true;
        }
        return false;
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
        observarUsuario();
    }

    private void inicializaComponentes() {
        txtNome = binding.txtNomeTrabalho;
        txtNomeProducao = binding.txtNomeProducaoTrabalho;
        txtProfissao = binding.txtProfissaoTrabalho;
        txtExperiencia = binding.txtExperienciaTrabalho;
        txtNivel = binding.txtNivelTrabalho;
        txtRaridade = binding.txtRaridadeTrabalho;
        heroRaridadeTarja = binding.heroRaridadeTarja;
        chipGroupTrabalhosNecessarios = binding.chipGroupTrabalhosNecessarios;
        layoutTrabalhosNecessarios = binding.layoutNecessarioTrabalho;
        layoutTrabalhosNecessarios.setVisibility(GONE);

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        trabalhoViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(TrabalhoViewModel.class);

        autenticacaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(AutenticacaoViewModel.class);
    }

    private void preencheCampos() {
        txtNome.setText(trabalhoRecebido.getNome());
        txtNomeProducao.setText(trabalhoRecebido.getNomeProducao());
        txtProfissao.setText(trabalhoRecebido.getProfissao());
        txtExperiencia.setText(String.valueOf(trabalhoRecebido.getExperiencia()));
        txtNivel.setText(String.valueOf(trabalhoRecebido.getNivel()));
        txtRaridade.setText(trabalhoRecebido.getRaridade());
        configuraRaridadeHero();

        if (!trabalhoRecebido.getListaTrabalhosNecessarios().isEmpty()) {
            layoutTrabalhosNecessarios.setVisibility(VISIBLE);

            ArrayList<Trabalho> trabalhos;
            trabalhos = trabalhoViewModel.recuperaTrabalhosNecessariosPorId(
                trabalhoRecebido.getListaTrabalhosNecessarios()
            );

            popularChipsTrabalhosNecessarios(trabalhos);
        }
    }

    private void configuraRaridadeHero() {
        String raridade = trabalhoRecebido.getRaridade();
        int corRaridade;
        int corTarja;
        if ("Melhorado".equals(raridade)) {
            corRaridade = ContextCompat.getColor(requireContext(), R.color.cor_producao_raridade_melhorado);
            corTarja = corRaridade;
        } else if ("Raro".equals(raridade)) {
            corRaridade = ContextCompat.getColor(requireContext(), R.color.cor_producao_raridade_raro);
            corTarja = corRaridade;
        } else if ("Especial".equals(raridade)) {
            corRaridade = ContextCompat.getColor(requireContext(), R.color.cor_producao_raridade_especial);
            corTarja = corRaridade;
        } else {
            corRaridade = MaterialColors.getColor(txtNome, com.google.android.material.R.attr.colorOnSurface);
            corTarja = MaterialColors.getColor(heroRaridadeTarja, com.google.android.material.R.attr.colorOutlineVariant);
        }
        txtNome.setTextColor(corRaridade);
        heroRaridadeTarja.setBackgroundColor(corTarja);
    }

    private void observarUsuario() {
        autenticacaoViewModel.getUsuarioAtual().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado == null) return;

                Usuario usuario = resultado.getDado();

                if (usuario == null) return;

                isAdministrador = usuario.isAdministrador();

                aplicarComponentesVisuais();

                requireActivity().invalidateOptionsMenu();
            }
        );

        autenticacaoViewModel.recuperaUsuarioAtual();
    }

    private void popularChipsTrabalhosNecessarios(ArrayList<Trabalho> trabalhos) {
        chipGroupTrabalhosNecessarios.removeAllViews();

        for(Trabalho trabalho : trabalhos) {
            Chip chip = new Chip(requireContext());
            chip.setText(trabalho.getNome());
            chip.setEnabled(false);
            chip.setTag(trabalho);

            chipGroupTrabalhosNecessarios.addView(chip);
        }
    }

    private void vaiParaDetalhesTrabalho() {
        NavController controlador = Navigation.findNavController(binding.getRoot());

        VaiDeVisualizacaoParaDetalhesTrabalho acao = vaiDeVisualizacaoParaDetalhesTrabalho();
        acao.setCodigoRequisicao(CODIGO_REQUISICAO_ALTERA_TRABALHO);
        acao.setTrabalho(trabalhoRecebido);

        controlador.navigate(acao);
    }
}
