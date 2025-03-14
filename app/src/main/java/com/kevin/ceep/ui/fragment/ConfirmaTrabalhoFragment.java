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
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.kevin.ceep.R;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.repository.TrabalhoProducaoRepository;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoViewModel;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoProducaoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoViewModelFactory;
import com.kevin.ceep.databinding.FragmentConfirmaTrabalhoBinding;

import java.util.Arrays;
import java.util.List;

public class ConfirmaTrabalhoFragment extends Fragment {
    private FragmentConfirmaTrabalhoBinding binding;
    private AutoCompleteTextView autoCompleteLicenca,autoCompleteQuantidade;
    private String idPersonagem;
    private Trabalho trabalhoRecebido;
    private int contador;
    private String nomesTrabalhosNecessarios;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmaTrabalhoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        recebeDados();
        preencheCampos();
        configuraBotaoCadastraTrabalho();
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
//                else {
//                    Snackbar.make(binding.getRoot(), id + " " + resultadoPegaTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
//                }
                if (!nomesTrabalhosNecessarios.isEmpty())
                    binding.txtTrabalhoNecessarioConfirmaTrabalho.setText(nomesTrabalhosNecessarios);
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
        configuraDropDrow();
    }
    private void configuraDropDrow() {
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
        AppCompatButton botaoCadastraTrabalho = binding.botaoCadastraConfirmaTrabalho;
        botaoCadastraTrabalho.setOnClickListener(view -> {
            botaoCadastraTrabalho.setEnabled(false);
            adicionaTrabalhoProducao();
        });
    }

    private void adicionaTrabalhoProducao() {
        TrabalhoProducaoViewModelFactory trabalhoProducaoViewModelFactory = new TrabalhoProducaoViewModelFactory(new TrabalhoProducaoRepository(getContext(), idPersonagem));
        TrabalhoProducaoViewModel trabalhoProducaoViewModel = new ViewModelProvider(this, trabalhoProducaoViewModelFactory).get(TrabalhoProducaoViewModel.class);
        int quantidadeSelecionada = Integer.parseInt(autoCompleteQuantidade.getText().toString());
        contador = 1;
        for (int x = 0; x < quantidadeSelecionada; x ++){
            TrabalhoProducao novoTrabalho = defineNovoModeloTrabalhoProducao();
            trabalhoProducaoViewModel.insereTrabalhoProducao(novoTrabalho).observe(getViewLifecycleOwner(), resposta -> {
                if (resposta.getErro() == null) {
                    if (contador == quantidadeSelecionada) {
                        NavDirections acao = ConfirmaTrabalhoFragmentDirections.vaiParaListaTrabalhosProducao();
                        Navigation.findNavController(binding.getRoot()).navigate(acao);
                    }
                    contador += 1;
                }
            });
        }
    }

    private TrabalhoProducao defineNovoModeloTrabalhoProducao() {
        CheckBox checkRecorrencia = binding.checkBoxProducaoRecorrenteConfirmaTrabalho;
        TrabalhoProducao trabalhoProducao = new TrabalhoProducao();
        trabalhoProducao.setIdTrabalho(trabalhoRecebido.getId());
        trabalhoProducao.setTipoLicenca(autoCompleteLicenca.getText().toString());
        trabalhoProducao.setRecorrencia(checkRecorrencia.isChecked());
        trabalhoProducao.setEstado(0);
        return trabalhoProducao;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}