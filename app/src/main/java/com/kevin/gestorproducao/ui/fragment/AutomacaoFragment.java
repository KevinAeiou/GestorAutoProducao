package com.kevin.gestorproducao.ui.fragment;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.automacao.AutomacaoAccessibilityService;
import com.kevin.gestorproducao.automacao.AutomacaoProducaoService;
import com.kevin.gestorproducao.automacao.AutomacaoStatus;
import com.kevin.gestorproducao.databinding.FragmentAutomacaoBinding;
import com.kevin.gestorproducao.model.Personagem;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class AutomacaoFragment extends BaseFragment<FragmentAutomacaoBinding> {
    private PersonagemViewModel personagemViewModel;
    private ActivityResultLauncher<Intent> solicitacaoGravacaoTela;
    private boolean automacaoEmExecucao;

    @Override
    protected FragmentAutomacaoBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAutomacaoBinding.inflate(inflater, container, false);
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {
        return new ComponentesVisuais(
            true,
            true,
            false,
            false,
            true,
            false,
            getString(R.string.stringAutomacao),
            false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        registrarSolicitacaoGravacaoTela();
        configuraBotoes();
        observarPersonagens();
        observarStatusAutomacao();

        personagemViewModel.recuperaPersonagens();
    }

    private void inicializaComponentes() {
        ViewModelFactory viewModelFactory = new ViewModelFactory(requireContext().getApplicationContext());

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);
    }

    private void registrarSolicitacaoGravacaoTela() {
        solicitacaoGravacaoTela = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            resultado -> {
                if (resultado.getResultCode() != android.app.Activity.RESULT_OK || resultado.getData() == null) {
                    mostraMensagemAncorada(getString(R.string.stringAutomacaoErroPermissaoTela));
                    return;
                }

                Intent intentServico = new Intent(requireContext(), AutomacaoProducaoService.class);
                intentServico.setAction(AutomacaoProducaoService.ACAO_INICIAR);
                intentServico.putExtra(AutomacaoProducaoService.EXTRA_RESULT_CODE, resultado.getResultCode());
                intentServico.putExtra(AutomacaoProducaoService.EXTRA_RESULT_DATA, resultado.getData());

                ContextCompat.startForegroundService(requireContext(), intentServico);
            }
        );
    }

    private void configuraBotoes() {
        binding.btnAtivarAcessibilidade.setOnClickListener(v ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        );

        binding.btnIniciarPararAutomacao.setOnClickListener(v -> {
            if (automacaoEmExecucao) {
                pararAutomacao();
                return;
            }

            iniciarAutomacao();
        });
    }

    private void iniciarAutomacao() {
        if (!AutomacaoAccessibilityService.estaAtiva()) {
            mostraMensagemAncorada(getString(R.string.stringAutomacaoAvisoAcessibilidade));
            return;
        }

        MediaProjectionManager gerenciador = (MediaProjectionManager)
            requireContext().getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE);

        solicitacaoGravacaoTela.launch(gerenciador.createScreenCaptureIntent());
    }

    private void pararAutomacao() {
        Intent intentServico = new Intent(requireContext(), AutomacaoProducaoService.class);
        intentServico.setAction(AutomacaoProducaoService.ACAO_PARAR);

        requireContext().startService(intentServico);
    }

    private void observarPersonagens() {
        personagemViewModel.getPersonagens().observe(
            getViewLifecycleOwner(),
            resultado -> {
                ArrayList<Personagem> personagens = resultado.getDado();
                if (personagens == null) return;

                String nomes = personagens.stream()
                    .filter(Personagem::isAutoProducao)
                    .map(Personagem::getNome)
                    .collect(Collectors.joining(", "));

                binding.txtAutomacaoPersonagensIncluidos.setText(
                    TextUtils.isEmpty(nomes)
                        ? getString(R.string.stringAutomacaoNenhumPersonagem)
                        : getString(R.string.stringAutomacaoPersonagensIncluidos, nomes)
                );
            }
        );
    }

    private void observarStatusAutomacao() {
        AutomacaoStatus.status.observe(getViewLifecycleOwner(), evento -> {
            if (evento == null) return;

            automacaoEmExecucao = evento.fase == AutomacaoStatus.Fase.RODANDO;
            atualizaBotaoIniciarParar();

            binding.txtAutomacaoStatus.setText(montaTextoStatus(evento));
        });
    }

    private void atualizaBotaoIniciarParar() {
        MaterialButton botao = binding.btnIniciarPararAutomacao;

        botao.setText(automacaoEmExecucao
            ? getString(R.string.stringParar)
            : getString(R.string.stringIniciarAutomacao));
    }

    private String montaTextoStatus(AutomacaoStatus.Evento evento) {
        StringBuilder texto = new StringBuilder();

        switch (evento.fase) {
            case RODANDO:
                texto.append(getString(R.string.stringAutomacao)).append(": ");
                if (evento.personagemAtual != null) {
                    texto.append(evento.personagemAtual);
                    if (evento.trabalhoAtual != null) {
                        texto.append(" — ").append(evento.trabalhoAtual);
                    }
                    texto.append("\n");
                }
                break;
            case CONCLUIDO:
                texto.append(getString(R.string.stringAutomacao)).append(" ");
                break;
            case ERRO:
                texto.append("Erro: ");
                break;
            case PARADO:
            default:
                return getString(R.string.stringAutomacaoStatusParado);
        }

        if (evento.mensagem != null) {
            texto.append(evento.mensagem);
        }

        return texto.toString();
    }
}
