package com.kevin.ceep.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.kevin.ceep.R;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoViewModel;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoProducaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoViewModelFactory;
import com.kevin.ceep.databinding.FragmentConfirmaTrabalhoBinding;

public class ConfirmaTrabalhoFragment extends Fragment {
    private FragmentConfirmaTrabalhoBinding binding;
    private AutoCompleteTextView autoCompleteLicenca,autoCompleteQuantidade;
    private String idPersonagem;
    private Trabalho trabalhoRecebido;
    private int contador;
    private String nomesTrabalhosNecessarios;
    private AppCompatButton botaoCadastraTrabalho;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmaTrabalhoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configuraCamposVisuais();
        recebeDados();
        preencheCampos();
        configuraBotaoCadastraTrabalho();
    }

    private void configuraCamposVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    private void preencheCampos() {
        if (trabalhoRecebido == null) return;
        binding.txtProfissaoConfirmaTrabalho.setText(trabalhoRecebido.getProfissao());
        TrabalhoViewModelFactory trabalhoViewModelFactory = new TrabalhoViewModelFactory(new TrabalhoRepository(getContext()));
        TrabalhoViewModel trabalhoViewModel = new ViewModelProvider(this, trabalhoViewModelFactory).get(TrabalhoViewModel.class);
        String[] idsTrabalhosNecessarios = trabalhoRecebido.getTrabalhoNecessario().split(",");
        nomesTrabalhosNecessarios = "";
        for (String id : idsTrabalhosNecessarios) {
            trabalhoViewModel.pegaTrabalhoPorId(id).observe(getViewLifecycleOwner(), resultadoPegaTrabalho -> {
                if (resultadoPegaTrabalho.getErro() == null) {
                    nomesTrabalhosNecessarios += resultadoPegaTrabalho.getDado().getNome();
                }
                if (!nomesTrabalhosNecessarios.isEmpty()) binding.txtTrabalhoNecessarioConfirmaTrabalho.setText(nomesTrabalhosNecessarios);
            });
        }
    }

    private void recebeDados() {
        idPersonagem = ConfirmaTrabalhoFragmentArgs.fromBundle(getArguments()).getIdPersonagem();
        trabalhoRecebido = ConfirmaTrabalhoFragmentArgs.fromBundle(getArguments()).getTrabalho();
    }
    @Override
    public void onResume() {
        super.onResume();
        configuraDropDrown();
    }
    private void configuraDropDrown() {
        autoCompleteLicenca = binding.txtAutoCompleteLicencaConfirmaTrabalho;
        autoCompleteQuantidade = binding.txtAutoCompleteQuantidadeConfirmaTrabalho;
        String[] licencas = getResources().getStringArray(R.array.licencas_completas);
        String[] quantidade = getResources().getStringArray(R.array.quantidade);
        ArrayAdapter<String> adapterLicenca = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, licencas);
        ArrayAdapter<String> adapterQuantidade = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, quantidade);
        adapterLicenca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterQuantidade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteLicenca.setText(licencas[3]);
        autoCompleteLicenca.setAdapter(adapterLicenca);
        autoCompleteQuantidade.setAdapter(adapterQuantidade);
    }

    private void configuraBotaoCadastraTrabalho() {
        botaoCadastraTrabalho = binding.botaoCadastraConfirmaTrabalho;
        botaoCadastraTrabalho.setOnClickListener(view -> {
            botaoCadastraTrabalho.setEnabled(false);
            insereTrabalhoProducaoXVezes();
        });
    }

    private void insereTrabalhoProducaoXVezes() {
        int quantidadeSelecionada = Integer.parseInt(autoCompleteQuantidade.getText().toString());
        contador = 0;
        for (int x = 0; x < quantidadeSelecionada; x ++){
            insereTrabalhoProducao(quantidadeSelecionada);
        }
    }

    private void insereTrabalhoProducao(int quantidadeSelecionada) {
        TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(idPersonagem);
        TrabalhoProducaoViewModel trabalhoProducaoViewModel = new ViewModelProvider(this, trabalhoProducaoViewModelFactory).get(idPersonagem, TrabalhoProducaoViewModel.class);
        TrabalhoProducao novoTrabalho = defineNovoTrabalhoProducao();
        trabalhoProducaoViewModel.insereTrabalhoProducao(novoTrabalho).observe(getViewLifecycleOwner(), resposta -> {
            if (resposta.getErro() == null) {
                contador += 1;
                if (contador >= quantidadeSelecionada) {
                    Snackbar.make(binding.getRoot(), trabalhoRecebido.getNome() + " foi inserido com sucesso!", Snackbar.LENGTH_LONG).show();
                    voltaParaListaProducao();
                }
                return;
            }
            botaoCadastraTrabalho.setEnabled(true);
            Snackbar.make(binding.getRoot(), "Erro ao inserir " + trabalhoRecebido.getNome(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void voltaParaListaProducao() {
        Navigation.findNavController(binding.getRoot()).navigate(ConfirmaTrabalhoFragmentDirections.vaiParaListaTrabalhosProducao());
    }

    private TrabalhoProducao defineNovoTrabalhoProducao() {
        CheckBox checkRecorrencia = binding.checkBoxProducaoRecorrenteConfirmaTrabalho;
        TrabalhoProducao trabalhoProducao = new TrabalhoProducao();
        trabalhoProducao.setIdTrabalho(trabalhoRecebido.getId());
        trabalhoProducao.setTipoLicenca(autoCompleteLicenca.getText().toString());
        trabalhoProducao.setRecorrencia(checkRecorrencia.isChecked());
        trabalhoProducao.setEstado(0);
        return trabalhoProducao;
    }

    @Override
    public void onDestroyView() {
        autoCompleteLicenca = null;
        autoCompleteQuantidade = null;
        super.onDestroyView();
        binding = null;
    }
}