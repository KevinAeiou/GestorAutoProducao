package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.GONE;

import static com.kevin.gestorproducao.ui.fragment.RecursosFragmentDirections.vaiParaListaTrabalhosProducao;

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
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentRecursosBinding;
import com.kevin.gestorproducao.model.RecursoComumAvancado;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.RecursosProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecursosFragment
    extends BaseFragment<FragmentRecursosBinding>
    implements MenuProvider
{
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
    private PersonagemViewModel personagemViewModel;
    private RecursosProducaoViewModel recursosProducaoViewModel;
    private TextInputEditText edtSubstanciaComum, edtSubstanciaComposta, edtSubstanciaEnergia, edtSubstanciaEterea,
        edtEssenciaComum, edtEssenciaComposta, edtEssenciaEnergia, edtEssenciaEterea,
        edtCatalisadorComum, edtCatalisadorComposta, edtCatalisadorEnergia, edtCatalisadorEterea;
    private TextInputLayout txtSubstanciaComum, txtSubstanciaComposta, txtSubstanciaEnergia, txtSubstanciaEterea,
        txtEssenciaComum, txtEssenciaComposta, txtEssenciaEnergia, txtEssenciaEterea,
        txtCatalisadorComum, txtCatalisadorComposta, txtCatalisadorEnergia, txtCatalisadorEterea;
    private ArrayList<RecursoComumAvancado> recursosRecebidos;

    @Override
    protected FragmentRecursosBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentRecursosBinding.inflate(inflater, container,false);
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
        observarPersonagem();
        observarRecursos();
    }

    private void observarRecursos() {
        recursosProducaoViewModel.getRecursos().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    recursosRecebidos = resultado.getDado();
                    for (RecursoComumAvancado recurso : recursosRecebidos) {
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

                mostraMensagemAncorada("Erro: " + resultado.getErro());
            }
        );

        recursosProducaoViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() == null) {
                    mostraMensagemAncorada("Recursos modificados com sucesso!");
                    voltaParaListaProducao();
                    return;
                }

                mostraMensagemAncorada("Erro: " + resultado.getErro());
            }
        );
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            resultado -> {
            if (resultado == null) return;

            binding.indicadorProgressoRecursosFragment.setVisibility(GONE);
            recursosProducaoViewModel.setIdPersonagem(resultado.getId());
        });
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuConfirma && camposValidos()) {
            ArrayList<RecursoComumAvancado> recursosModificados = defineRecursosModificados();
            if (recursosEhModificados(recursosModificados)) {
                recursosProducaoViewModel.modificaListaRecursos(recursosModificados);
                return true;
            }
            voltaParaListaProducao();
            return true;
        }
        return false;
    }

    private void voltaParaListaProducao() {
        Navigation.findNavController(binding.getRoot()).navigate(vaiParaListaTrabalhosProducao());
    }

    private boolean recursosEhModificados(ArrayList<RecursoComumAvancado> recursosModificados) {
        Map<String, RecursoComumAvancado> mapaRecursosRecebidos = new HashMap<>();

        for (RecursoComumAvancado recurso : recursosRecebidos) {
            if (recurso != null && recurso.getId() != null) {
                mapaRecursosRecebidos.put(recurso.getId(), recurso);
            }
        }

        for (RecursoComumAvancado recursoModificado : recursosModificados) {
            if (recursoModificado == null || recursoModificado.getId() == null) {
                continue;
            }
            RecursoComumAvancado recursoRecebido = mapaRecursosRecebidos.get(recursoModificado.getId());
            if (recursoRecebido != null && !recursoModificado.equals(recursoRecebido)) {
                return true;
            }
        }

        return false;
    }

    private ArrayList<RecursoComumAvancado> defineRecursosModificados() {
        ArrayList<RecursoComumAvancado> recursosModificados = new ArrayList<>();

        for (RecursoComumAvancado recursoRecebido : recursosRecebidos) {
            RecursoComumAvancado recursoModificado = defineRecursoModificado(recursoRecebido);
            recursosModificados.add(recursoModificado);
        }

        return recursosModificados;
    }

    private RecursoComumAvancado defineRecursoModificado(RecursoComumAvancado recursoRecebido) {
        int valor = defineValorRecursoModificado(recursoRecebido);

        RecursoComumAvancado recurso = new RecursoComumAvancado();
        recurso.setId(recursoRecebido.getId());
        recurso.setQuantidade(recursoRecebido.getQuantidade());
        recurso.setValor(valor);

        return recurso;
    }

    private int defineValorRecursoModificado(RecursoComumAvancado recurso) {
        int valor = 0;

        switch (recurso.getId()) {
            case ID_SUBSTANCIA_COMUM:
                valor = obterValorNumerico(edtSubstanciaComum);
                break;
            case ID_SUBSTANCIA_COMPOSTA:
                valor = obterValorNumerico(edtSubstanciaComposta);
                break;
            case ID_SUBSTANCIA_ENERGIA:
                valor = obterValorNumerico(edtSubstanciaEnergia);
                break;
            case ID_SUBSTANCIA_ETEREA:
                valor = obterValorNumerico(edtSubstanciaEterea);
                break;
            case ID_ESSENCIA_COMUM:
                valor = obterValorNumerico(edtEssenciaComum);
                break;
            case ID_ESSENCIA_COMPOSTA:
                valor = obterValorNumerico(edtEssenciaComposta);
                break;
            case ID_ESSENCIA_ENERGIA:
                valor = obterValorNumerico(edtEssenciaEnergia);
                break;
            case ID_ESSENCIA_ETEREA:
                valor = obterValorNumerico(edtEssenciaEterea);
                break;
            case ID_CATALISADOR_COMUM:
                valor = obterValorNumerico(edtCatalisadorComum);
                break;
            case ID_CATALISADOR_COMPOSTO:
                valor = obterValorNumerico(edtCatalisadorComposta);
                break;
            case ID_CATALISADOR_ENERGIA:
                valor = obterValorNumerico(edtCatalisadorEnergia);
                break;
            case ID_CATALISADOR_ETEREO:
                valor = obterValorNumerico(edtCatalisadorEterea);
                break;
        }

        return valor;
    }

    private boolean camposValidos() {
        return verificaCampo(txtSubstanciaComum, edtSubstanciaComum) &
            verificaCampo(txtSubstanciaComposta, edtSubstanciaComposta) &
            verificaCampo(txtSubstanciaEnergia, edtSubstanciaEnergia) &
            verificaCampo(txtSubstanciaEterea, edtSubstanciaEterea) &
            verificaCampo(txtEssenciaComum, edtEssenciaComum) &
            verificaCampo(txtEssenciaComposta, edtEssenciaComposta) &
            verificaCampo(txtEssenciaEnergia, edtEssenciaEnergia) &
            verificaCampo(txtEssenciaEterea, edtEssenciaEterea) &
            verificaCampo(txtCatalisadorComum, edtCatalisadorComum) &
            verificaCampo(txtCatalisadorComposta, edtCatalisadorComposta) &
            verificaCampo(txtCatalisadorEnergia, edtCatalisadorEnergia) &
            verificaCampo(txtCatalisadorEterea, edtCatalisadorEterea);
    }

    private boolean verificaCampo(TextInputLayout rotulo, TextInputEditText campo) {
        String valor = campo.getText().toString().trim();
        if (valor.isEmpty() || obterValorNumerico(campo) < 0) {
            rotulo.setError(getString(R.string.stringCampoInvalido));
            return false;
        }

        rotulo.setErrorEnabled(false);
        return true;
    }

    private void inicializaComponentes() {
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

        configurarMascaraMilhar(edtSubstanciaComum);
        configurarMascaraMilhar(edtSubstanciaComposta);
        configurarMascaraMilhar(edtSubstanciaEnergia);
        configurarMascaraMilhar(edtSubstanciaEterea);
        configurarMascaraMilhar(edtEssenciaComum);
        configurarMascaraMilhar(edtEssenciaComposta);
        configurarMascaraMilhar(edtEssenciaEnergia);
        configurarMascaraMilhar(edtEssenciaEterea);
        configurarMascaraMilhar(edtCatalisadorComum);
        configurarMascaraMilhar(edtCatalisadorComposta);
        configurarMascaraMilhar(edtCatalisadorEnergia);
        configurarMascaraMilhar(edtCatalisadorEterea);

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        recursosProducaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(RecursosProducaoViewModel.class);
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
            getString(R.string.stringRecursos),
            false
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeObservadorRecurso();
        removerObservadorPersonagem();
    }

    private void removerObservadorPersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }

    private void removeObservadorRecurso() {
        if (recursosProducaoViewModel == null) return;
        recursosProducaoViewModel.removeObservador();
    }
}