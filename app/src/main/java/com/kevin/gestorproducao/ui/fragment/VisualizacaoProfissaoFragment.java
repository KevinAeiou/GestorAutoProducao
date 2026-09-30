package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.fragment.VisualizacaoProfissaoFragmentDirections.vaiDeVisualizacaoParaDetalhesProfissao;

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

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentVisualizacaoProfissaoBinding;
import com.kevin.gestorproducao.model.ProfissaoBase;
import com.kevin.gestorproducao.model.Usuario;
import com.kevin.gestorproducao.ui.fragment.VisualizacaoProfissaoFragmentDirections.VaiDeVisualizacaoParaDetalhesProfissao;
import com.kevin.gestorproducao.ui.viewModel.AutenticacaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

public class VisualizacaoProfissaoFragment
    extends BaseFragment<FragmentVisualizacaoProfissaoBinding>
    implements MenuProvider
{
    private ProfissaoBase profissaoRecebida;
    private TextView txtNome;
    private AutenticacaoViewModel autenticacaoViewModel;
    private boolean isAdministrador = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        VisualizacaoProfissaoFragmentArgs argumentos = VisualizacaoProfissaoFragmentArgs.fromBundle(
            getArguments()
        );

        profissaoRecebida = argumentos.getProfissao();
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
    protected FragmentVisualizacaoProfissaoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentVisualizacaoProfissaoBinding.inflate(inflater, container, false);
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
            getString(R.string.stringDetalhes),
            isAdministrador
        );
    }

    private void inicializaComponentes() {
        txtNome = binding.txtNomeProfissao;

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        autenticacaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(AutenticacaoViewModel.class);
    }

    private void preencheCampos() {
        txtNome.setText(profissaoRecebida.getNome());
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

    private void vaiParaDetalhesTrabalho() {
        NavController controlador = Navigation.findNavController(binding.getRoot());

        VaiDeVisualizacaoParaDetalhesProfissao acao = vaiDeVisualizacaoParaDetalhesProfissao();
        acao.setProfissao(profissaoRecebida);

        controlador.navigate(acao);
    }
}