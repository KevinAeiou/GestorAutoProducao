package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.utilitario.Utilitario.calcularIntervaloPeriodo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.PeriodoFiltro;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Bottom sheet do seletor de Semana: mostra o calendário de um mês (navegável) com a semana do
// dia de referência destacada, e qualquer dia clicado dentro dela confirma aquela semana inteira.
public class SeletorSemanaDialogFragment extends BottomSheetDialogFragment {
    private static final String CHAVE_DIA_REFERENCIA = "dia_referencia";

    public interface OnSemanaSelecionadaListener {
        void aoSelecionarSemana(long diaReferenciaMillis);
    }

    private OnSemanaSelecionadaListener listener;
    private long diaReferenciaSelecionado;
    private Calendar mesExibido;

    private TextView txtTitulo;
    private GridLayout gridDias;

    public static SeletorSemanaDialogFragment novaInstancia(long diaReferenciaAtual) {
        Bundle args = new Bundle();
        args.putLong(CHAVE_DIA_REFERENCIA, diaReferenciaAtual);

        SeletorSemanaDialogFragment dialogo = new SeletorSemanaDialogFragment();
        dialogo.setArguments(args);

        return dialogo;
    }

    public void setOnSemanaSelecionadaListener(OnSemanaSelecionadaListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.dialog_seletor_semana, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        diaReferenciaSelecionado = requireArguments().getLong(CHAVE_DIA_REFERENCIA);

        mesExibido = Calendar.getInstance();
        mesExibido.setTimeInMillis(diaReferenciaSelecionado);

        txtTitulo = view.findViewById(R.id.txtTituloSeletorSemana);
        GridLayout gridCabecalho = view.findViewById(R.id.gridCabecalhoDiasSemana);
        gridDias = view.findViewById(R.id.gridDiasMes);

        view.findViewById(R.id.btnMesAnteriorSeletorSemana).setOnClickListener(v -> {
            mesExibido.add(Calendar.MONTH, -1);
            atualizarExibicao();
        });

        view.findViewById(R.id.btnMesProximoSeletorSemana).setOnClickListener(v -> {
            mesExibido.add(Calendar.MONTH, 1);
            atualizarExibicao();
        });

        popularCabecalho(gridCabecalho);
        atualizarExibicao();
    }

    private void atualizarExibicao() {
        txtTitulo.setText(formatarTituloMes(mesExibido));
        popularDias(gridDias, mesExibido);
    }

    private String formatarTituloMes(Calendar mes) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM 'de' yyyy", Locale.getDefault());
        String texto = sdf.format(mes.getTime());

        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private void popularCabecalho(GridLayout grid) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(Calendar.DAY_OF_WEEK, calendario.getFirstDayOfWeek());

        SimpleDateFormat sdf = new SimpleDateFormat("EEEEE", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            TextView txt = new TextView(requireContext());
            String texto = sdf.format(calendario.getTime());
            txt.setText(Character.toUpperCase(texto.charAt(0)) + texto.substring(1));
            txt.setGravity(Gravity.CENTER);
            txt.setTypeface(txt.getTypeface(), Typeface.BOLD);
            txt.setLayoutParams(criaLayoutParamsCelula());

            grid.addView(txt);

            calendario.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void popularDias(GridLayout grid, Calendar mesReferencia) {
        grid.removeAllViews();

        Calendar calendario = (Calendar) mesReferencia.clone();
        calendario.set(Calendar.DAY_OF_MONTH, 1);

        int deslocamento = (
            calendario.get(Calendar.DAY_OF_WEEK) - calendario.getFirstDayOfWeek() + 7
        ) % 7;
        int diasNoMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);

        long[] semanaDestacada = calcularIntervaloPeriodo(
            PeriodoFiltro.SEMANA,
            diaReferenciaSelecionado,
            null,
            null
        );

        for (int i = 0; i < deslocamento; i++) {
            TextView celulaVazia = new TextView(requireContext());
            celulaVazia.setLayoutParams(criaLayoutParamsCelula());
            grid.addView(celulaVazia);
        }

        for (int dia = 1; dia <= diasNoMes; dia++) {
            calendario.set(Calendar.DAY_OF_MONTH, dia);
            long diaMillis = calendario.getTimeInMillis();

            boolean destacado = diaMillis >= semanaDestacada[0] && diaMillis <= semanaDestacada[1];

            grid.addView(criaCelulaDia(dia, diaMillis, destacado));
        }
    }

    private View criaCelulaDia(int dia, long diaMillis, boolean destacado) {
        TextView txt = new TextView(requireContext());
        txt.setText(String.valueOf(dia));
        txt.setGravity(Gravity.CENTER);
        txt.setLayoutParams(criaLayoutParamsCelula());

        if (destacado) {
            txt.setBackgroundResource(R.drawable.bg_dia_semana_selecionada);
        }

        txt.setOnClickListener(v -> {
            if (listener != null) {
                listener.aoSelecionarSemana(diaMillis);
            }
            dismiss();
        });

        return txt;
    }

    private GridLayout.LayoutParams criaLayoutParamsCelula() {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dpParaPx(40);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

        return params;
    }

    private int dpParaPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
