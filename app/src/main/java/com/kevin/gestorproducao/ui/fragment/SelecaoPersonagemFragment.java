package com.kevin.gestorproducao.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.kevin.gestorproducao.databinding.FragmentSelecaoPersonagemBinding;
import com.kevin.gestorproducao.model.Personagem;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;

public class SelecaoPersonagemFragment extends DialogFragment {
    private FragmentSelecaoPersonagemBinding binding;
    private PersonagemViewModel personagemViewModel;
    private FiltroViewModel filtroViewModel;
    private RadioGroup radioGroup;
    private MaterialButton btnConfirmar;
    private ImageButton btnFechar;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = FragmentSelecaoPersonagemBinding.inflate(getLayoutInflater());

        inicializaComponentes();
        observarPersonagem();
        configuraBotaoCancelar();
        configuraBotaoConfirmar();
        return new MaterialAlertDialogBuilder(
            requireContext()
        ).setView(binding.getRoot()).create();
    }

    private void configuraBotaoConfirmar() {
        btnConfirmar.setOnClickListener(v -> {
            View personagemRadio = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
            if (personagemRadio != null) {
                Personagem personagem = (Personagem) personagemRadio.getTag();
                personagemViewModel.definePersonagemSelecionado(personagem);
                filtroViewModel.removeObservador();
            }

            dismiss();
        });
    }

    private void configuraBotaoCancelar() {
        btnFechar.setOnClickListener(v -> dismiss());
    }

    private void selecionarRadio(Personagem personagem) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            MaterialRadioButton radio = (MaterialRadioButton) radioGroup.getChildAt(i);
            Personagem personagemRadio = (Personagem) radio.getTag();

            boolean selecionado = personagem.getId().equals(personagemRadio.getId());

            if (selecionado) {
                radioGroup.clearCheck();
                radio.setChecked(true);
                return;
            }
        }
    }

    private void observarPersonagem() {
        personagemViewModel.recuperaPersonagens();

        // Achado M8: observava com requireActivity() em vez de "this" — o observer ficava
        // preso ao tempo de vida da Activity inteira (não só deste diálogo), retendo
        // binding/radioGroup mesmo depois do diálogo fechado. DialogFragment não tem
        // getViewLifecycleOwner() disponível aqui porque a view não é criada via
        // onCreateView (é montada em onCreateDialog), então "this" é o LifecycleOwner certo.
        personagemViewModel.getPersonagens().observe(
            this,
            resultado -> {
                if (resultado.getErro() == null) {
                    configuraRadioGroup(resultado.getDado());

                    Personagem personagem = personagemViewModel.pegaPersonagemSelecionado().getValue();

                    if (personagem != null) {
                        selecionarRadio(personagem);
                    }
                    return;
                }
                Snackbar.make(binding.getRoot(), "Erro: "+resultado.getErro(), Snackbar.LENGTH_LONG).show();
            }
        );
    }

    private void configuraRadioGroup(ArrayList<Personagem> personagens) {
        radioGroup.removeAllViews();

        MaterialRadioButton radio;
        for (Personagem personagem : personagens) {
            radio = criaRadio(personagem);

            radioGroup.addView(radio);
        }
    }

    private MaterialRadioButton criaRadio(Personagem personagem) {
        MaterialRadioButton radio = new MaterialRadioButton(radioGroup.getContext());
        radio.setText(personagem.getNome());
        radio.setTag(personagem);
        RadioGroup.LayoutParams layout = new RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.MATCH_PARENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        );
        radio.setLayoutParams(layout);
        radio.setClickable(true);

        return radio;
    }

    private void inicializaComponentes() {
        radioGroup = binding.radioGroupSelecaoPersonagem;
        btnFechar = binding.btnFecharSelecaoPersonagem;
        btnConfirmar = binding.btnConfirmarSelecaoPersonagem;

        ViewModelFactory viewModelFactory = new ViewModelFactory(requireActivity());

        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        filtroViewModel = new ViewModelProvider(
            requireActivity()
        ).get(FiltroViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}