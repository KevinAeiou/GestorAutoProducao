package com.kevin.gestorproducao.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.kevin.gestorproducao.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Bottom sheet do seletor de Mês: mostra os 12 meses de um ano, com setas para trocar de ano.
public class SeletorMesDialogFragment extends BottomSheetDialogFragment {
    private static final String CHAVE_VALOR_SELECIONADO = "valor_selecionado";

    public interface OnMesSelecionadoListener {
        void aoSelecionarMes(long valorMillis);
    }

    private OnMesSelecionadoListener listener;
    private Calendar anoExibido;
    private long valorSelecionado;

    private TextView txtAno;
    private ChipGroup chipGroupMeses;

    public static SeletorMesDialogFragment novaInstancia(long valorSelecionado) {
        Bundle args = new Bundle();
        args.putLong(CHAVE_VALOR_SELECIONADO, valorSelecionado);

        SeletorMesDialogFragment dialogo = new SeletorMesDialogFragment();
        dialogo.setArguments(args);

        return dialogo;
    }

    public void setOnMesSelecionadoListener(OnMesSelecionadoListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.dialog_seletor_mes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        valorSelecionado = requireArguments().getLong(CHAVE_VALOR_SELECIONADO);

        anoExibido = Calendar.getInstance();
        anoExibido.setTimeInMillis(valorSelecionado);

        txtAno = view.findViewById(R.id.txtAnoSeletorMes);
        chipGroupMeses = view.findViewById(R.id.chipGroupMesesSeletorMes);

        view.findViewById(R.id.btnAnoAnteriorSeletorMes).setOnClickListener(v -> {
            anoExibido.add(Calendar.YEAR, -1);
            atualizarExibicao();
        });

        view.findViewById(R.id.btnAnoProximoSeletorMes).setOnClickListener(v -> {
            anoExibido.add(Calendar.YEAR, 1);
            atualizarExibicao();
        });

        atualizarExibicao();
    }

    private void atualizarExibicao() {
        txtAno.setText(String.valueOf(anoExibido.get(Calendar.YEAR)));
        popularChipsMeses();
    }

    private void popularChipsMeses() {
        chipGroupMeses.removeAllViews();

        Calendar calendario = (Calendar) anoExibido.clone();
        calendario.set(Calendar.DAY_OF_MONTH, 1);
        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat sdfMes = new SimpleDateFormat("MMMM", Locale.getDefault());
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int mes = 0; mes < 12; mes++) {
            calendario.set(Calendar.MONTH, mes);
            long valorMes = calendario.getTimeInMillis();

            String texto = sdfMes.format(calendario.getTime());
            String label = Character.toUpperCase(texto.charAt(0)) + texto.substring(1);

            Chip chip = (Chip) inflater.inflate(R.layout.custom_chip, chipGroupMeses, false);
            chip.setText(label);
            chip.setCheckable(true);
            chip.setChecked(mesmoMes(valorMes, valorSelecionado));
            chip.setOnClickListener(v -> {
                if (listener != null) {
                    listener.aoSelecionarMes(valorMes);
                }
                dismiss();
            });

            chipGroupMeses.addView(chip);
        }
    }

    private boolean mesmoMes(long valorA, long valorB) {
        Calendar calA = Calendar.getInstance();
        calA.setTimeInMillis(valorA);

        Calendar calB = Calendar.getInstance();
        calB.setTimeInMillis(valorB);

        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR)
            && calA.get(Calendar.MONTH) == calB.get(Calendar.MONTH);
    }
}
