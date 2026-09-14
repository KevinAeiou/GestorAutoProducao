package com.kevin.gestorproducao.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.kevin.gestorproducao.R;

// Bottom sheet genérico de seleção única usado pelos seletores de Mês e Ano do filtro de
// período: recebe pares (rótulo, valor em millis) e devolve o valor escolhido.
public class SeletorOpcaoPeriodoDialogFragment extends BottomSheetDialogFragment {
    private static final String CHAVE_LABELS = "labels";
    private static final String CHAVE_VALORES = "valores";
    private static final String CHAVE_SELECIONADO = "selecionado";

    public interface OnOpcaoSelecionadaListener {
        void aoSelecionarOpcao(long valorMillis);
    }

    private OnOpcaoSelecionadaListener listener;

    public static SeletorOpcaoPeriodoDialogFragment novaInstancia(
        String[] labels,
        long[] valores,
        long valorSelecionado
    ) {
        Bundle args = new Bundle();
        args.putStringArray(CHAVE_LABELS, labels);
        args.putLongArray(CHAVE_VALORES, valores);
        args.putLong(CHAVE_SELECIONADO, valorSelecionado);

        SeletorOpcaoPeriodoDialogFragment dialogo = new SeletorOpcaoPeriodoDialogFragment();
        dialogo.setArguments(args);

        return dialogo;
    }

    public void setOnOpcaoSelecionadaListener(OnOpcaoSelecionadaListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.dialog_seletor_opcao_periodo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        String[] labels = args.getStringArray(CHAVE_LABELS);
        long[] valores = args.getLongArray(CHAVE_VALORES);
        long selecionado = args.getLong(CHAVE_SELECIONADO);

        ChipGroup chipGroup = view.findViewById(R.id.chipGroupOpcoesPeriodo);
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int i = 0; i < labels.length; i++) {
            long valor = valores[i];

            Chip chip = (Chip) inflater.inflate(R.layout.custom_chip, chipGroup, false);
            chip.setText(labels[i]);
            chip.setCheckable(true);
            chip.setChecked(valor == selecionado);
            chip.setOnClickListener(v -> {
                if (listener != null) {
                    listener.aoSelecionarOpcao(valor);
                }
                dismiss();
            });

            chipGroup.addView(chip);
        }
    }
}
