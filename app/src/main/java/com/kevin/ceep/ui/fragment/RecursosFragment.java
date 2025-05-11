package com.kevin.ceep.ui.fragment;

import static android.view.View.GONE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentRecursosBinding;
import com.kevin.ceep.model.RecursoAvancado;
import com.kevin.ceep.repository.PersonagemRepository;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;
import com.kevin.ceep.ui.viewModel.PersonagemViewModel;
import com.kevin.ceep.ui.viewModel.RecursosProducaoViewModel;
import com.kevin.ceep.ui.viewModel.factory.PersonagemViewModelFactory;
import com.kevin.ceep.ui.viewModel.factory.RecursosProducaoViewModelFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecursosFragment
        extends BaseFragment<FragmentRecursosBinding>
        implements MenuProvider {
    public static final String ID_SUBSTANCIA_COMUM = "6ac21d44-1e8d-4bf8-bd62-53248e568417";
    public static final String ID_SUBSTANCIA_COMPOSTA = "6250e394-4a82-4ccb-b697-c788b9094c41";
    public static final String ID_SUBSTANCIA_ENERGIA = "b2f158f9-5b52-444a-a27b-7ac1284063c6";
    public static final String ID_SUBSTANCIA_ETEREA = "e12c1346-9343-414e-a0b5-631e494423b2";
    public static final String ID_ESSENCIA_COMUM = "e580e375-abc1-44f8-b332-774b7f1a490c";
    public static final String ID_ESSENCIA_COMPOSTA = "94b66657-c7c6-41c0-b6f0-922614182549";
    public static final String ID_ESSENCIA_ENERGIA = "c9751ecc-f528-4a80-88c3-d2a8af2804fa";
    public static final String ID_ESSENCIA_ETEREA = "7c27a18c-fc60-484c-9545-99030a623129";
    public static final String ID_CATALISADOR_COMUM = "b7f69638-c9b7-4c69-865e-cbacef5c45b1";
    public static final String ID_CATALISADOR_COMPOSTO = "3a085587-5093-471d-9187-27b2370e4b38";
    public static final String ID_CATALISADOR_ENERGIA = "259d5a95-72fd-4b36-b17f-c7b6a2a6897f";
    public static final String ID_CATALISADOR_ETEREO = "2d8c434a-50eb-4269-bc70-725ded6bc7e9";
    private String idPersonagem;
    private PersonagemViewModel personagemViewModel;
    private RecursosProducaoViewModel recursosProducaoViewModel;
    private TextInputEditText edtSubstanciaComum, edtSubstanciaComposta, edtSubstanciaEnergia, edtSubstanciaEterea,
            edtEssenciaComum, edtEssenciaComposta, edtEssenciaEnergia, edtEssenciaEterea,
            edtCatalisadorComum, edtCatalisadorComposta, edtCatalisadorEnergia, edtCatalisadorEterea;
    private TextInputLayout txtSubstanciaComum, txtSubstanciaComposta, txtSubstanciaEnergia, txtSubstanciaEterea,
            txtEssenciaComum, txtEssenciaComposta, txtEssenciaEnergia, txtEssenciaEterea,
            txtCatalisadorComum, txtCatalisadorComposta, txtCatalisadorEnergia, txtCatalisadorEterea;
    private ArrayList<RecursoAvancado> recursosRecebidos;

    public RecursosFragment() {
    }

    @Override
    protected FragmentRecursosBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentRecursosBinding.inflate(
                inflater,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);
        configuraComponentesVisuais();
        inicializaComponentes();
    }
    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma && camposValidos()) {
            ArrayList<RecursoAvancado> recursosModificados = defineRecursosModificados();
            if (recursosEhModificados(recursosModificados)) {
                modificaRecursos(recursosModificados);
                return true;
            }
            voltaParaListaProducao();
            return true;
        }
        return false;
    }

    private void modificaRecursos(ArrayList<RecursoAvancado> recursosModificados) {
        recursosProducaoViewModel.modificaListaRecursos(recursosModificados).observe(getViewLifecycleOwner(), resultadoModificaRecursos -> {
            if (resultadoModificaRecursos.getErro() == null) {
                mostraMensagem("Recursos modificados com sucesso!");
                voltaParaListaProducao();
                return;
            }
            mostraMensagem("Erro: " + resultadoModificaRecursos.getErro());
        });
    }

    private void voltaParaListaProducao() {
        Navigation.findNavController(binding.getRoot()).navigate(ConfirmaTrabalhoFragmentDirections.vaiParaListaTrabalhosProducao());
    }

    private boolean recursosEhModificados(ArrayList<RecursoAvancado> recursosModificados) {
        Map<String, RecursoAvancado> mapaRecursosRecebidos = new HashMap<>();
        for (RecursoAvancado recurso : recursosRecebidos) {
            if (recurso != null && recurso.getId() != null) {
                mapaRecursosRecebidos.put(recurso.getId(), recurso);
            }
        }
        for (RecursoAvancado recursoModificado : recursosModificados) {
            if (recursoModificado == null || recursoModificado.getId() == null) {
                continue;
            }
            RecursoAvancado recursoRecebido = mapaRecursosRecebidos.get(recursoModificado.getId());
            if (recursoRecebido != null && !recursoModificado.equals(recursoRecebido)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<RecursoAvancado> defineRecursosModificados() {
        ArrayList<RecursoAvancado> recursosModificados = new ArrayList<>();
        for (RecursoAvancado recursoRecebido : recursosRecebidos) {
            RecursoAvancado recursoModificado = defineRecursoModificado(recursoRecebido);
            recursosModificados.add(recursoModificado);
        }
        return recursosModificados;
    }

    private RecursoAvancado defineRecursoModificado(RecursoAvancado recursoRecebido) {
        int valor = defineValorRecursoModificado(recursoRecebido);
        RecursoAvancado recurso = new RecursoAvancado();
        recurso.setId(recursoRecebido.getId());
        recurso.setQuantidade(recursoRecebido.getQuantidade());
        recurso.setValor(valor);
        return recurso;
    }

    private int defineValorRecursoModificado(RecursoAvancado recurso) {
        int valor = 0;
        switch (recurso.getId()) {
            case ID_SUBSTANCIA_COMUM:
                valor = Integer.parseInt(edtSubstanciaComum.getText().toString().trim());
                break;
            case ID_SUBSTANCIA_COMPOSTA:
                valor = Integer.parseInt(edtSubstanciaComposta.getText().toString().trim());
                break;
            case ID_SUBSTANCIA_ENERGIA:
                valor = Integer.parseInt(edtSubstanciaEnergia.getText().toString().trim());
                break;
            case ID_SUBSTANCIA_ETEREA:
                valor = Integer.parseInt(edtSubstanciaEterea.getText().toString().trim());
                break;
            case ID_ESSENCIA_COMUM:
                valor = Integer.parseInt(edtEssenciaComum.getText().toString().trim());
                break;
            case ID_ESSENCIA_COMPOSTA:
                valor = Integer.parseInt(edtEssenciaComposta.getText().toString().trim());
                break;
            case ID_ESSENCIA_ENERGIA:
                valor = Integer.parseInt(edtEssenciaEnergia.getText().toString().trim());
                break;
            case ID_ESSENCIA_ETEREA:
                valor = Integer.parseInt(edtEssenciaEterea.getText().toString().trim());
                break;
            case ID_CATALISADOR_COMUM:
                valor = Integer.parseInt(edtCatalisadorComum.getText().toString().trim());
                break;
            case ID_CATALISADOR_COMPOSTO:
                valor = Integer.parseInt(edtCatalisadorComposta.getText().toString().trim());
                break;
            case ID_CATALISADOR_ENERGIA:
                valor = Integer.parseInt(edtCatalisadorEnergia.getText().toString().trim());
                break;
            case ID_CATALISADOR_ETEREO:
                valor = Integer.parseInt(edtCatalisadorEterea.getText().toString().trim());
                break;
        }
        return valor;
    }

    private boolean camposValidos() {
        return verificaCampo(txtSubstanciaComum, edtSubstanciaComum) & verificaCampo(txtSubstanciaComposta, edtSubstanciaComposta) & verificaCampo(txtSubstanciaEnergia, edtSubstanciaEnergia) & verificaCampo(txtSubstanciaEterea, edtSubstanciaEterea) &
            verificaCampo(txtEssenciaComum, edtEssenciaComum) & verificaCampo(txtEssenciaComposta, edtEssenciaComposta) & verificaCampo(txtEssenciaEnergia, edtEssenciaEnergia) & verificaCampo(txtEssenciaEterea, edtEssenciaEterea) &
            verificaCampo(txtCatalisadorComum, edtCatalisadorComum) & verificaCampo(txtCatalisadorComposta, edtCatalisadorComposta) & verificaCampo(txtCatalisadorEnergia, edtCatalisadorEnergia) & verificaCampo(txtCatalisadorEterea, edtCatalisadorEterea);
    }

    private boolean verificaCampo(TextInputLayout rotulo, TextInputEditText campo) {
        String valor = campo.getText().toString().trim();
        if (valor.isEmpty() || Integer.parseInt(valor) < 0) {
            rotulo.setError(getString(R.string.stringCampoInvalido));
            return false;
        }
        rotulo.setErrorEnabled(false);
        return true;
    }

    private void inicializaComponentes() {
        idPersonagem = "";
        edtSubstanciaComum = binding.edtInputSubstanciaComumRecurso;
        edtSubstanciaComposta = binding.edtInputSubstanciaCompostoRecurso;
        edtSubstanciaEnergia = binding.edtInputSubstanciaEnergiaRecurso;
        edtSubstanciaEterea = binding.edtInputSubstanciaEtereoRecurso;
        edtEssenciaComum = binding.edtInputEssenciaComumRecurso;
        edtEssenciaComposta = binding.edtInputEssenciaCompostoRecurso;
        edtEssenciaEnergia = binding.edtInputEssenciaEnergiaRecurso;
        edtEssenciaEterea = binding.edtInputEssenciaEtereoRecurso;
        edtCatalisadorComum = binding.edtInputCatalisadorComumRecurso;
        edtCatalisadorComposta = binding.edtInputCatalisadorCompostoRecurso;
        edtCatalisadorEnergia = binding.edtInputCatalisadorEnergiaRecurso;
        edtCatalisadorEterea = binding.edtInputCatalisadorEtereoRecurso;
        txtSubstanciaComum = binding.txtInputSubstanciaComumRecursos;
        txtSubstanciaComposta = binding.txtInputSubstanciaCompostoRecursos;
        txtSubstanciaEnergia = binding.txtInputSubstanciaEnergiaRecursos;
        txtSubstanciaEterea = binding.txtInputSubstanciaEtereoRecursos;
        txtEssenciaComum = binding.txtInputEssenciaComumRecursos;
        txtEssenciaComposta = binding.txtInputEssenciaCompostoRecursos;
        txtEssenciaEnergia = binding.txtInputEssenciaEnergiaRecursos;
        txtEssenciaEterea = binding.txtInputEssenciaEtereoRecursos;
        txtCatalisadorComum = binding.txtInputCatalizadorComumRecursos;
        txtCatalisadorComposta = binding.txtInputCatalizadorCompostoRecursos;
        txtCatalisadorEnergia = binding.txtInputCatalizadorEnergiaRecursos;
        txtCatalisadorEterea = binding.txtInputCatalizadorEtereoRecursos;
        recursosRecebidos = new ArrayList<>();
        PersonagemViewModelFactory personagemViewModelFactory = new PersonagemViewModelFactory(PersonagemRepository.getInstance());
        personagemViewModel = new ViewModelProvider(requireActivity(), personagemViewModelFactory).get(PersonagemViewModel.class);
    }

    private void configuraComponentesVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        componentesVisuais.appBar = true;
        componentesVisuais.menuNavegacaoLateral = false;
        componentesVisuais.menuNavegacaoInferior = false;
        componentesVisuais.itemMenuConfirma = true;
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    @Override
    public void onResume() {
        super.onResume();
        personagemViewModel.pegaPersonagemSelecionado().observe(getViewLifecycleOwner(), resultadoRecuperaPersonagem -> {
            if (resultadoRecuperaPersonagem == null) return;
            binding.indicadorProgressoRecursosFragment.setVisibility(GONE);
            idPersonagem = resultadoRecuperaPersonagem.getId();
            RecursosProducaoViewModelFactory recursosProducaoViewModelFactory = new RecursosProducaoViewModelFactory(idPersonagem, getContext());
            recursosProducaoViewModel = new ViewModelProvider(this, recursosProducaoViewModelFactory).get(RecursosProducaoViewModel.class);
            recursosProducaoViewModel.recuperaRecursos().observe(getViewLifecycleOwner(), resultadoRecuperaRecursos -> {
                if (resultadoRecuperaRecursos.getErro() == null) {
                    recursosRecebidos = resultadoRecuperaRecursos.getDado();
                    for (RecursoAvancado recurso : recursosRecebidos) {
                        switch (recurso.getId()) {
                            case ID_SUBSTANCIA_COMUM:
                                edtSubstanciaComum.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_SUBSTANCIA_COMPOSTA:
                                edtSubstanciaComposta.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_SUBSTANCIA_ENERGIA:
                                edtSubstanciaEnergia.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_SUBSTANCIA_ETEREA:
                                edtSubstanciaEterea.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_ESSENCIA_COMUM:
                                edtEssenciaComum.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_ESSENCIA_COMPOSTA:
                                edtEssenciaComposta.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_ESSENCIA_ENERGIA:
                                edtEssenciaEnergia.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_ESSENCIA_ETEREA:
                                edtEssenciaEterea.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_CATALISADOR_COMUM:
                                edtCatalisadorComum.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_CATALISADOR_COMPOSTO:
                                edtCatalisadorComposta.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_CATALISADOR_ENERGIA:
                                edtCatalisadorEnergia.setText(String.valueOf(recurso.getValor()));
                                break;
                            case ID_CATALISADOR_ETEREO:
                                edtCatalisadorEterea.setText(String.valueOf(recurso.getValor()));
                                break;
                        }
                    }
                    return;
                }
                mostraMensagem("Erro: " + resultadoRecuperaRecursos.getErro());
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeOuvinteRecursos();
        binding = null;
    }

    private void removeOuvinteRecursos() {
        if (recursosProducaoViewModel == null) return;
        recursosProducaoViewModel.removeOuvinte();
    }
}