package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.fragment.ListaEstoqueFragmentDirections.vaiParaListaTrabalhosEstoque;

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
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentDetalhesEstoqueBinding;
import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.Objects;

public class DetalhesEstoqueFragment
    extends BaseFragment<FragmentDetalhesEstoqueBinding>
{
    private TrabalhoEstoque trabalho;
    private TextView txtNomeTrabalho, txtProfissaoTrabalho, txtNivelTrabalho;
    private View heroRaridadeTarja;
    private TextInputLayout txtQuantidadeTrabalho;
    private TextInputEditText edtQuantidadeTrabalho;
    private TrabalhoEstoqueViewModel estoqueViewModel;
    private PersonagemViewModel personagemViewModel;
    private MaterialButton btnExcluir, btnConfirmar;
    private LinearLayout loadingBotaoConfirmar;
    private LinearLayout loadingBotaoExcluir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DetalhesEstoqueFragmentArgs argumentos = DetalhesEstoqueFragmentArgs.fromBundle(getArguments());

        trabalho = argumentos.getTrabalho();
    }

    @Override
    protected FragmentDetalhesEstoqueBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentDetalhesEstoqueBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        preencheCampos();
        configuraBotaoExcluir();
        configuraBotaoConfirmar();
        observarEstoque();
        observarPersonagem();
    }

    private void configuraBotaoConfirmar() {
        btnConfirmar.setOnClickListener(v -> {
            iniciarLoadingBotao(
                btnConfirmar,
                loadingBotaoConfirmar
            );

            verificaModificacao();
        });
    }

    private void verificaModificacao() {
        Integer quantidadeNova = defineValorQuantidade();
        if (quantidadeNova == null) {
            pararLoadingBotao(btnConfirmar, loadingBotaoConfirmar);
            return;
        }

        TrabalhoEstoque trabalho = defineTrabalhoModificado(quantidadeNova);

        estoqueViewModel.modificaEstoque(trabalho);
    }

    private TrabalhoEstoque defineTrabalhoModificado(Integer quantidade) {
        TrabalhoEstoque trabalhoModificado = new TrabalhoEstoque();

        trabalhoModificado.setId(trabalho.getId());
        trabalhoModificado.setIdTrabalho(trabalho.getIdTrabalho());
        trabalhoModificado.setQuantidade(quantidade);

        return trabalhoModificado;
    }

    private void configuraBotaoExcluir() {
        btnExcluir.setOnClickListener(v -> {
            ConfirmacaoDialog dialog = ConfirmacaoDialog.novaInstancia(
                getString(R.string.stringExcluirTrabalhoEmEstoque),
                getString(R.string.stringConfirmaExclusaoTrabalho),
                () -> {
                    iniciarLoadingBotao(btnExcluir, loadingBotaoExcluir);
                    estoqueViewModel.removeTrabalhoEstoque(trabalho);
                },
                () -> {}
            );

            dialog.show(getParentFragmentManager(), "confirmacao_exclusao");
        });
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
            trabalho.getNome(),
            false
        );
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                estoqueViewModel.setIdPersonagem(personagem.getId());
            }
        );
    }

    private void observarEstoque() {
        estoqueViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                paraProgresso();

                if (resultado.getErro() == null) {
                    voltaParaEstoque();
                    return;
                }

                pararLoadingBotao(
                    btnConfirmar,
                    loadingBotaoConfirmar
                );

                txtQuantidadeTrabalho.setError(resultado.getErro());
            }
        );

        estoqueViewModel.getRemocaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    voltaParaEstoque();
                    mostraMensagemAncorada(getString(R.string.stringItemRemovidoComSucesso));
                    return;
                }

                pararLoadingBotao(btnExcluir, loadingBotaoExcluir);
                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );
    }

    private void paraProgresso() {
        btnExcluir.setEnabled(true);
    }

    private void voltaParaEstoque() {
        NavController controlador = Navigation.findNavController(binding.getRoot());
        controlador.navigate(vaiParaListaTrabalhosEstoque());
    }

    private void preencheCampos() {
        txtNomeTrabalho.setText(trabalho.getNome());
        txtProfissaoTrabalho.setText(trabalho.getProfissao());
        txtNivelTrabalho.setText(getString(R.string.stringNivelBadge, trabalho.getNivel()));
        edtQuantidadeTrabalho.setText(String.valueOf(trabalho.getQuantidade()));
        configuraRaridadeHero(trabalho);
    }

    private void configuraRaridadeHero(TrabalhoEstoque trabalho) {
        String raridade = trabalho.getRaridade();
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
            corRaridade = MaterialColors.getColor(txtNomeTrabalho, com.google.android.material.R.attr.colorOnSurface);
            corTarja = MaterialColors.getColor(heroRaridadeTarja, com.google.android.material.R.attr.colorOutlineVariant);
        }
        txtNomeTrabalho.setTextColor(corRaridade);
        heroRaridadeTarja.setBackgroundColor(corTarja);
    }

    @Nullable
    private Integer defineValorQuantidade() {
        String quantidade = edtQuantidadeTrabalho.getText().toString().trim();
        if (quantidade.isEmpty()) {
            txtQuantidadeTrabalho.setError(getString(R.string.stringCampoObrigatorio));
            return null;
        }

        int quantidadeNova;
        try {
            quantidadeNova = obterValorNumerico(edtQuantidadeTrabalho);

            if (quantidadeNova < 0) {
                throw new NumberFormatException(getString(R.string.stringQuantidadeNaoPodeSerNegativa));
            }
        } catch (NumberFormatException e) {
            txtQuantidadeTrabalho.setError(e.getMessage());
            return null;
        }

        if (Objects.equals(quantidadeNova, trabalho.getQuantidade())) {
            voltaParaEstoque();
            return null;
        }

        return quantidadeNova;
    }

    private void inicializaComponentes() {
        txtNomeTrabalho = binding.txtNomeTrabalho;
        txtProfissaoTrabalho = binding.txtProfissaoTrabalho;
        txtNivelTrabalho = binding.txtNivelTrabalho;
        heroRaridadeTarja = binding.heroRaridadeTarja;
        txtQuantidadeTrabalho = binding.txtQuantidadeEstoqueFragment;
        edtQuantidadeTrabalho = binding.edtQuantidadeEstoqueFragment;
        btnExcluir = binding.btnExcluiEstoque;
        btnConfirmar = binding.btnConfirmarEstoque;
        loadingBotaoConfirmar = binding.loadingDotsConfirmar.getRoot();
        loadingBotaoExcluir = binding.loadingDotsExcluir.getRoot();

        configurarMascaraMilhar(edtQuantidadeTrabalho);

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        estoqueViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoEstoqueViewModel.class);

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        trabalho = null;
        removerOuvinteEstoque();
        removerOuvintePersonagem();
    }

    private void removerOuvintePersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }

    private void removerOuvinteEstoque() {
        if (estoqueViewModel == null) return;
        estoqueViewModel.removeObservador();
    }
}