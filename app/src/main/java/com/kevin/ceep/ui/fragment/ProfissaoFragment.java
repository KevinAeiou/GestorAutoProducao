package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.kevin.ceep.databinding.FragmentProfissaoBinding;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.repository.ProfissaoRepository;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;
import com.kevin.ceep.ui.viewModel.factory.ProfissaoViewModelFactory;

import java.util.Objects;

public class ProfissaoFragment extends DialogFragment {
    private FragmentProfissaoBinding binding;
    private Profissao profissaoRecebido;
    private TextInputEditText edtExperiencia;
    private SwitchMaterial swtPrioridade;
    private ProfissaoViewModel profissaoViewModel;
    private String idPersonagem;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle argumento = getArguments();
        if (argumento != null) {
            idPersonagem = argumento.getString(CHAVE_PERSONAGEM);
            if (argumento.containsKey("profissao")) {
                profissaoRecebido = new Profissao();
                profissaoRecebido = (Profissao) argumento.getSerializable("profissao");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfissaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView txtNomePersonagem = binding.txtNomeProfissaoFragment;
        edtExperiencia = binding.edtExperienciaProfissaoFragment;
        swtPrioridade = binding.swtPrioridadeProfissaoFragment;
        ProfissaoViewModelFactory profissaoViewModelFactory = new ProfissaoViewModelFactory(new ProfissaoRepository(idPersonagem));
        profissaoViewModel = new ViewModelProvider(this, profissaoViewModelFactory).get(ProfissaoViewModel.class);

        txtNomePersonagem.setText(profissaoRecebido.getNome());
        edtExperiencia.setText(String.valueOf(profissaoRecebido.getExperiencia()));
        swtPrioridade.setChecked(profissaoRecebido.isPrioridade());
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        String experiencia = Objects.requireNonNull(edtExperiencia.getText()).toString();
        if (experiencia.isEmpty() || experiencia.equals(String.valueOf(profissaoRecebido.getExperiencia())) && swtPrioridade.isChecked() == profissaoRecebido.isPrioridade()) {
            return;
        }
        Profissao profissaoModificada = new Profissao();
        profissaoModificada.setId(profissaoRecebido.getId());
        profissaoModificada.setNome(profissaoRecebido.getNome());
        profissaoModificada.setExperiencia(Integer.parseInt(experiencia));
        profissaoModificada.setPrioridade(swtPrioridade.isChecked());
        profissaoViewModel.modificaExperienciaProfissao(profissaoModificada);
    }
}