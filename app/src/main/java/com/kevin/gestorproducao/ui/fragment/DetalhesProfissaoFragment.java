package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentDetalhesProfissaoBinding;
import com.kevin.gestorproducao.model.ProfissaoBase;

import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.Objects;

public class DetalhesProfissaoFragment
    extends BaseFragment<FragmentDetalhesProfissaoBinding>
{
    private ProfissaoBase profissao;
    private TextInputLayout txtNomeProfissao;
    private TextInputEditText edtDescricaoProfissao;
    private ProfissaoViewModel profissaoViewModel;
    private MaterialButton btnExcluiProfissao, btnConfirmarProfissao;
    private LinearLayout loadingBotaoConfirmar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DetalhesProfissaoFragmentArgs argumentos = DetalhesProfissaoFragmentArgs.fromBundle(
            getArguments()
        );

        profissao = argumentos.getProfissao();
    }

    @Override
    protected FragmentDetalhesProfissaoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesProfissaoBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        configuraBotaoExcluir();
        configuraBotaoConfirmar();
        preencheCampos();
        observaProfissao();
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {
        String titulo =
            profissao == null ?
            getString(R.string.stringNovaProfissao) :
            getString(R.string.stringEditar);

        return new ComponentesVisuais(
            true,
            false,
            false,
            false,
            false,
            false,
            titulo,
            false
        );
    }

    private void configuraBotaoExcluir() {
        btnExcluiProfissao.setOnClickListener(v -> {
            btnExcluiProfissao.setEnabled(false);
            iniciarLoadingBotao(
                btnConfirmarProfissao,
                loadingBotaoConfirmar
            );

            ConfirmacaoDialog dialog = ConfirmacaoDialog.novaInstancia(
                getString(R.string.stringExcluirProfissao),
                getString(R.string.stringConfirmaExclusaoProfissao),
                () -> profissaoViewModel.removeProfissao(profissao),
                () -> pararLoadingBotao(btnConfirmarProfissao, loadingBotaoConfirmar)
            );

            dialog.show(getParentFragmentManager(), "confirmacao_exclusao");
        });
    }

    private void observaProfissao() {
        profissaoViewModel.getResultadoInsercao().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    profissaoViewModel.removeResultadoInsercao();
                    voltaParaListaProfissoes();
                    return;
                }

                mostraMensagemAncorada(resultado.getErro());
            }
        );

        profissaoViewModel.getResultadoRemocao().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    profissaoViewModel.removeResultadoRemocao();
                    voltaParaListaProfissoes();
                    return;
                }

                btnExcluiProfissao.setEnabled(true);
                mostraMensagemAncorada(resultado.getErro());
            }
        );

        profissaoViewModel.getResultadoModificacao().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    profissaoViewModel.removeResultadoModificacao();
                    voltaParaListaProfissoes();
                    return;
                }

                txtNomeProfissao.setError(resultado.getErro());
            }
        );
    }

    private void voltaParaListaProfissoes() {
        NavController controlador = Navigation.findNavController(binding.getRoot());
        controlador.navigateUp();
    }

    private void salvaProfissao() {
        String nome = edtDescricaoProfissao.getText().toString().trim();

        if (nome.isEmpty()) {
            txtNomeProfissao.setError(getString(R.string.stringCampoObrigatorio));
            pararLoadingBotao(
                btnConfirmarProfissao,
                loadingBotaoConfirmar
            );
            return;
        }

        if (profissao == null) {
            ProfissaoBase novaProfissao = new ProfissaoBase();
            novaProfissao.setNome(nome);

            profissaoViewModel.insereProfissao(novaProfissao);
            return;
        }

        if (Objects.equals(profissao.getNome(), nome)) {
            voltaParaListaProfissoes();
            return;
        }

        profissao.setNome(nome);
        profissaoViewModel.modificaProfissao(profissao);
    }

    private void preencheCampos() {
        if (profissao == null) return;

        btnExcluiProfissao.setVisibility(VISIBLE);
        edtDescricaoProfissao.setText(profissao.getNome());
    }

    private void inicializaComponentes() {
        txtNomeProfissao = binding.txtNomeProfissaoFragment;
        edtDescricaoProfissao = binding.edtDescricaoProfissaoFragment;
        btnExcluiProfissao = binding.btnExcluiProfissao;
        btnConfirmarProfissao = binding.btnConfirmarProfissao;
        loadingBotaoConfirmar = binding.loadingDotsConfirmar.getRoot();

        btnExcluiProfissao.setVisibility(GONE);

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext());

        profissaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(ProfissaoViewModel.class);
    }

    private void configuraBotaoConfirmar() {
        btnConfirmarProfissao.setOnClickListener(v -> {
            iniciarLoadingBotao(
                btnConfirmarProfissao,
                loadingBotaoConfirmar
            );

            salvaProfissao();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        profissao = null;

        if (profissaoViewModel != null) {
            profissaoViewModel.removeObservador();
        }
    }
}