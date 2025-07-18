package com.kevin.ceep.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_ID_PERSONAGEM;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.FragmentProfissaoBinding;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.ui.viewModel.ProfissaoViewModel;
import com.kevin.ceep.ui.viewModel.factory.ProfissaoViewModelFactory;

import java.util.ArrayList;
import java.util.Objects;

public class ProfissaoFragment extends DialogFragment {
    private FragmentProfissaoBinding binding;
    private Profissao profissaoRecebido;
    private TextInputEditText edtExperiencia;
    private SwitchMaterial swtPrioridade;
    private ProfissaoViewModel profissaoViewModel;
    private String idPersonagem;
    private ArrayList<TrabalhoProducao> producao;
    private CircularProgressIndicator indicadorAtual, indicadorMaximo, indicadorProduzindo, indicadorProduzir;
    private TextView txtExpNecessaria, txtExpProduzir, txtExpProduzindo, txtExpRelativa;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle argumento = getArguments();
        if (argumento != null) {
            idPersonagem = argumento.getString(CHAVE_ID_PERSONAGEM);
            if (argumento.containsKey("profissao")) {
                profissaoRecebido = new Profissao();
                producao = (ArrayList<TrabalhoProducao>) argumento.getSerializable("producao");
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
        indicadorMaximo = binding.indicadorExperienciaMaxima;
        indicadorAtual = binding.indicadorExperienciaAtual;
        indicadorProduzindo = binding.indicadorExperienciaProduzindo;
        indicadorProduzir = binding.indicadorExperienciaProduzir;
        txtExpNecessaria = binding.txtExperienciaNecessariaProfissaoFragment;
        txtExpProduzir = binding.txtExperienciaProduzirProfissaoFragment;
        txtExpProduzindo = binding.txtExperienciaProduzindoProfissaoFragment;
        txtExpRelativa = binding.txtExperienciaRelativaProfissaoFragment;
        ProfissaoViewModelFactory profissaoViewModelFactory = new ProfissaoViewModelFactory(idPersonagem);
        profissaoViewModel = new ViewModelProvider(this, profissaoViewModelFactory).get(ProfissaoViewModel.class);

        txtNomePersonagem.setText(profissaoRecebido.getNome());
        edtExperiencia.setText(String.valueOf(profissaoRecebido.getExperiencia()));
        swtPrioridade.setChecked(profissaoRecebido.isPrioridade());

        configuraBarraProgressoCircular();
    }

    private void configuraBarraProgressoCircular() {
        int xpNecessario = defineValorMaximoIndicadores();
        int experienciaRelativa = configuraIndicadorRelativo();
        int experienciaProduzindo = configuraIndicadorProduzindo(experienciaRelativa);
        int experienciaProduzir = configuraIndicadorProduzir(experienciaProduzindo);
        configuraIndicadorNecessaria(xpNecessario, experienciaProduzir);
        animaIndicadores(experienciaRelativa, experienciaProduzir, experienciaProduzindo);
    }

    private void animaIndicadores(int experienciaRelativa, int experienciaProduzir, int experienciaProduzindo) {
        animateProgress(indicadorAtual, experienciaRelativa);
        animateProgress(indicadorProduzir, experienciaProduzir);
        animateProgress(indicadorProduzindo, experienciaProduzindo);
    }

    private void configuraIndicadorNecessaria(int xpNecessario, int totalEsperado) {
        txtExpNecessaria.setText(String.valueOf(xpNecessario - totalEsperado));
        configuraVisibilidadeTxt(xpNecessario, txtExpNecessaria);
        configuraVisibilidadeTxt(xpNecessario, binding.txtLegendaExperienciaNecessariaProfissaoFragment);
    }

    private int configuraIndicadorProduzir(int experienciaProduzindo) {
        txtExpProduzir.setTextColor(getContext().getColor(R.color.cor_texto_licenca_principiante));
        int experienciaProduzir = 0;
        for(TrabalhoProducao trabalho : producao) {
            if (trabalho.ehProduzir()) {
                experienciaProduzir += trabalho.getExperiencia();
            }
        }
        configuraVisibilidadeTxt(experienciaProduzir, txtExpProduzir);
        configuraVisibilidadeTxt(experienciaProduzir, binding.txtLegendaExperienciaProduzirProfissaoFragment);
        txtExpProduzir.setText(String.valueOf(experienciaProduzir));
        return experienciaProduzir + experienciaProduzindo;
    }

    private int configuraIndicadorRelativo() {
        txtExpRelativa.setTextColor(getContext().getColor(R.color.cor_background_feito));
        int experienciaAtual = profissaoRecebido.getExperienciaRelativa();
        txtExpRelativa.setText(String.valueOf(experienciaAtual));
        configuraVisibilidadeTxt(experienciaAtual, txtExpRelativa);
        configuraVisibilidadeTxt(experienciaAtual, binding.txtLegendaExperienciaRelativaProfissaoFragment);
        return experienciaAtual;
    }

    private int configuraIndicadorProduzindo(int experienciaRelativa) {
        txtExpProduzindo.setTextColor(getContext().getColor(R.color.cor_background_produzindo));
        int experienciaProduzindo = 0;
        for(TrabalhoProducao trabalho : producao) {
            if (trabalho.ehProduzindo()) {
                experienciaProduzindo += trabalho.getExperiencia();
            }
        }
        txtExpProduzindo.setText(String.valueOf(experienciaProduzindo));
        configuraVisibilidadeTxt(experienciaProduzindo, binding.txtLegendaExperienciaProduzindoProfissaoFragment);
        configuraVisibilidadeTxt(experienciaProduzindo, txtExpProduzindo);
        return experienciaProduzindo + experienciaRelativa;
    }

    private int defineValorMaximoIndicadores() {
        int xpNecessario = profissaoRecebido.getXpNecessario();
        indicadorMaximo.setMax(xpNecessario);
        indicadorAtual.setMax(xpNecessario);
        indicadorProduzindo.setMax(xpNecessario);
        indicadorProduzir.setMax(xpNecessario);
        return xpNecessario;
    }

    private void configuraVisibilidadeTxt(int experiencia, TextView txtView) {
        int visibilidade = experiencia == 0 ? GONE : VISIBLE;
        txtView.setVisibility(visibilidade);
    }

    private void animateProgress(CircularProgressIndicator indicador, int experiencia) {
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
                indicador,
                "progress",
                0,
                experiencia
        );
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}