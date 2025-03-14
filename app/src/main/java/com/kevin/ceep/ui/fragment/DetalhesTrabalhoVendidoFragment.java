package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.fragment.DetalhesTrabalhoVendidoFragmentDirections.*;
import static com.kevin.ceep.utilitario.Utilitario.comparaString;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentDetalhesTrabalhoVendidoBinding;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.repository.TrabalhoRepository;
import com.kevin.ceep.repository.TrabalhoVendidoRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhoViewModel;
import com.kevin.ceep.ui.viewModel.TrabalhosVendidosViewModel;
import com.kevin.ceep.ui.viewModel.factory.TrabalhoViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.TrabalhosVendidosViewModelFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DetalhesTrabalhoVendidoFragment extends Fragment implements MenuProvider{
    private FragmentDetalhesTrabalhoVendidoBinding binding;
    private MaterialTextView txtDescricaoTrabalhoVendido, txtDataTrabalhoVendido, txtValorTrabalhoVendido, txtQuantidadeTrabalhoVendido;
    private AutoCompleteTextView autoCompleteNomeTrabalhoVendido;
    private TrabalhoVendido trabalhoRecebido;
    private String personagemId;
    private Trabalho trabalhoSelecionado;
    private TrabalhosVendidosViewModel trabalhosVendidosViewModel;
    private TrabalhoViewModel trabalhoViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetalhesTrabalhoVendidoBinding.inflate(inflater, container, false);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.itemMenuConfirma = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
        recebeDados();
        inicializaComponentes();
        preencheCampos();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma) {
            TrabalhoVendido trabalhoVendido = defineTrabalhoModificado();
            if (camposTrabalhoModificado(trabalhoVendido)) {
                MaterialAlertDialogBuilder dialogoDeAlerta = new MaterialAlertDialogBuilder(requireContext());
                dialogoDeAlerta.setMessage("Deseja confirmar alterações?");
                dialogoDeAlerta.setNegativeButton("Não", ((dialogInterface, i) -> voltaParaTrabalhosVendidos()));
                dialogoDeAlerta.setPositiveButton("Sim", (dialogInterface, i) -> modificaTrabalho(trabalhoVendido));
                dialogoDeAlerta.show();
                return true;
            }
            voltaParaTrabalhosVendidos();
        }
        return false;
    }

    private TrabalhoVendido defineTrabalhoModificado() {
        TrabalhoVendido trabalhoModificado = new TrabalhoVendido();
        trabalhoModificado.setId(trabalhoRecebido.getId());
        trabalhoModificado.setIdTrabalho(trabalhoSelecionado.getId());
        trabalhoModificado.setDescricao(trabalhoRecebido.getDescricao());
        trabalhoModificado.setDataVenda(trabalhoRecebido.getDataVenda());
        trabalhoModificado.setQuantidade(trabalhoRecebido.getQuantidade());
        trabalhoModificado.setValor(trabalhoRecebido.getValor());
        return trabalhoModificado;
    }

    private void modificaTrabalho(TrabalhoVendido trabalhoModificado) {
        trabalhosVendidosViewModel.modificaTrabalhoVendido(trabalhoModificado).observe(this, resultadoModificaTrabalho -> {
            if (resultadoModificaTrabalho.getErro() == null) {
                voltaParaTrabalhosVendidos();
                return;
            }
            Snackbar.make(binding.getRoot(), "Erro ao modificar trabalho: " + resultadoModificaTrabalho.getErro(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void voltaParaTrabalhosVendidos() {
        Navigation.findNavController(binding.getRoot()).navigate(vaiDeDetalhesTrabalhoVendidoParaTrabalhosVendidos());
    }

    private boolean camposTrabalhoModificado(TrabalhoVendido trabalhoVendido) {
        return !comparaString(trabalhoRecebido.getIdTrabalho(), trabalhoVendido.getIdTrabalho()) ||
            !comparaString(trabalhoRecebido.getDescricao(), trabalhoVendido.getDescricao()) ||
            !comparaString(trabalhoRecebido.getDataVenda(), trabalhoVendido.getDataVenda()) ||
            !(trabalhoRecebido.getQuantidade() == trabalhoVendido.getQuantidade()) ||
            !(trabalhoRecebido.getValor() == trabalhoVendido.getValor());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pegaTodosPersonagens() {
        trabalhoViewModel.pegaTodosTrabalhos().observe(this, resultadoPegaTrabalhos -> {
            ArrayList<Trabalho> todosTrabalhos;
            if (resultadoPegaTrabalhos.getDado() != null) {
                todosTrabalhos = resultadoPegaTrabalhos.getDado();
                configuraAutoCompleteIdPersonagem(todosTrabalhos);
            }
        });
    }

    private void recebeDados() {
        personagemId = DetalhesTrabalhoVendidoFragmentArgs.fromBundle(getArguments()).getIdPersonagem();
        trabalhoRecebido = DetalhesTrabalhoVendidoFragmentArgs.fromBundle(getArguments()).getTrabalhoVendido();
    }

    private void preencheCampos() {
        txtDescricaoTrabalhoVendido.setText(trabalhoRecebido.getDescricao());
        txtDataTrabalhoVendido.setText(trabalhoRecebido.getDataVenda());
        txtValorTrabalhoVendido.setText(getString(R.string.stringOuroValor, trabalhoRecebido.getValor()));
        txtQuantidadeTrabalhoVendido.setText(getString(R.string.stringQuantidadeValor, trabalhoRecebido.getQuantidade()));
    }

    private void inicializaComponentes() {
        txtDescricaoTrabalhoVendido = binding.txtDescricaoTrabalhoVendido;
        txtDataTrabalhoVendido = binding.txtDataTrabalhoVendido;
        txtValorTrabalhoVendido = binding.txtValorTrabalhoVendido;
        txtQuantidadeTrabalhoVendido = binding.txtQuantidadeTrabalhoVendido;
        autoCompleteNomeTrabalhoVendido = binding.autoCompleteNomeTrabalhoVendido;
        TrabalhoViewModelFactory trabalhoViewModelFactory = new TrabalhoViewModelFactory(new TrabalhoRepository(getContext()));
        trabalhoViewModel = new ViewModelProvider(this, trabalhoViewModelFactory).get(TrabalhoViewModel.class);
        TrabalhosVendidosViewModelFactory trabalhosVendidosViewModelFactory = new TrabalhosVendidosViewModelFactory(new TrabalhoVendidoRepository(
                getContext(),
                personagemId));
        trabalhosVendidosViewModel = new ViewModelProvider(this, trabalhosVendidosViewModelFactory).get(TrabalhosVendidosViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configuraAutoCompleteIdPersonagem(ArrayList<Trabalho> todosTrabalhos) {
        ArrayList<String> todosNomesTrabalhos = new ArrayList<>();
        for (int posicao = 0; posicao < todosTrabalhos.size(); posicao += 1) {
            todosNomesTrabalhos.add(todosTrabalhos.get(posicao).getNome());
            if (todosTrabalhos.get(posicao).getId().equals(trabalhoRecebido.getIdTrabalho())){
                autoCompleteNomeTrabalhoVendido.setText(todosTrabalhos.get(posicao).getNome());
                trabalhoSelecionado = todosTrabalhos.get(posicao);
            }
        }
        Collections.sort(todosNomesTrabalhos);
        todosTrabalhos.sort(Comparator.comparing(Trabalho::getNome));
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(requireContext(), R.layout.item_dropdrown, todosNomesTrabalhos);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteNomeTrabalhoVendido.setAdapter(adapterEstado);
        autoCompleteNomeTrabalhoVendido.setOnItemClickListener((parent, view, position, id) -> trabalhoSelecionado = todosTrabalhos.get(position));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        pegaTodosPersonagens();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}