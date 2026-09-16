package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.utilitario.Utilitario.calcularIntervaloPeriodo;
import static com.kevin.gestorproducao.utilitario.Utilitario.formatarData;

import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.PeriodoFiltro;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

// Controle reutilizável do filtro por período (Dia/Semana/Mês/Ano/Personalizado): duas linhas —
// chips com o tipo e um texto clicável com o valor atual, padrão "Mês atual" — usado tanto na
// listagem de produção quanto na de vendas. Cada tela decide o que fazer quando o período muda
// (filtrar em memória, refazer uma consulta etc.) via o listener passado no construtor.
public class ControleFiltroPeriodo {
    public interface OnPeriodoAlteradoListener {
        void aoAlterarPeriodo(long dataInicio, long dataFim, PeriodoFiltro tipo);
    }

    private final Fragment fragment;
    private final ChipGroup chipGroupPeriodo;
    private final TextView txtValorPeriodo;
    private final OnPeriodoAlteradoListener listener;

    private PeriodoFiltro tipoPeriodoSelecionado;
    private Long dataReferenciaPeriodo;
    private Long dataInicioPersonalizada;
    private Long dataFimPersonalizada;
    private long[] intervaloPeriodoAtual;

    public ControleFiltroPeriodo(
        Fragment fragment,
        ChipGroup chipGroupPeriodo,
        TextView txtValorPeriodo,
        OnPeriodoAlteradoListener listener
    ) {
        this(fragment, chipGroupPeriodo, txtValorPeriodo, listener, PeriodoFiltro.MES, null, null);
    }

    // periodoInicial (+ intervalo personalizado inicial, quando periodoInicial é PERSONALIZADO)
    // permite abrir o controle já preenchido com um período diferente do padrão "mês atual" —
    // ex.: a análise de "chance de venda" abre em Personalizado com os últimos 6 meses.
    public ControleFiltroPeriodo(
        Fragment fragment,
        ChipGroup chipGroupPeriodo,
        TextView txtValorPeriodo,
        OnPeriodoAlteradoListener listener,
        PeriodoFiltro periodoInicial,
        Long dataInicioPersonalizadaInicial,
        Long dataFimPersonalizadaInicial
    ) {
        this.fragment = fragment;
        this.chipGroupPeriodo = chipGroupPeriodo;
        this.txtValorPeriodo = txtValorPeriodo;
        this.listener = listener;
        this.tipoPeriodoSelecionado = periodoInicial;
        this.dataInicioPersonalizada = dataInicioPersonalizadaInicial;
        this.dataFimPersonalizada = dataFimPersonalizadaInicial;
    }

    public void configurar() {
        popularChipsPeriodo();

        chipGroupPeriodo.setOnCheckedStateChangeListener((group, checkedIds) -> {
            PeriodoFiltro novoTipo = periodoSelecionadoNoChip();

            if (novoTipo == tipoPeriodoSelecionado) return;

            tipoPeriodoSelecionado = novoTipo;
            dataReferenciaPeriodo = null;
            dataInicioPersonalizada = null;
            dataFimPersonalizada = null;

            atualizarPeriodo();

            if (tipoPeriodoSelecionado == PeriodoFiltro.PERSONALIZADO) {
                abrirSeletorIntervaloPersonalizado();
            }
        });

        txtValorPeriodo.setOnClickListener(v -> abrirSeletorPeriodo());

        atualizarPeriodo();
    }

    public long[] getIntervaloAtual() {
        return intervaloPeriodoAtual;
    }

    private void popularChipsPeriodo() {
        chipGroupPeriodo.removeAllViews();

        List<PeriodoFiltro> periodos = Arrays.asList(
            PeriodoFiltro.DIA,
            PeriodoFiltro.SEMANA,
            PeriodoFiltro.MES,
            PeriodoFiltro.ANO,
            PeriodoFiltro.PERSONALIZADO
        );

        LayoutInflater inflater = LayoutInflater.from(fragment.requireContext());

        for (PeriodoFiltro periodo : periodos) {
            Chip chip = (Chip) inflater.inflate(R.layout.custom_chip, chipGroupPeriodo, false);
            chip.setText(tituloPeriodo(periodo));
            chip.setCheckable(true);
            chip.setTag(periodo);
            chip.setChecked(periodo == tipoPeriodoSelecionado);

            chipGroupPeriodo.addView(chip);
        }
    }

    private String tituloPeriodo(PeriodoFiltro periodo) {
        switch (periodo) {
            case DIA: return fragment.getString(R.string.string_periodo_dia);
            case SEMANA: return fragment.getString(R.string.string_periodo_semana);
            case ANO: return fragment.getString(R.string.string_periodo_ano);
            case PERSONALIZADO: return fragment.getString(R.string.string_periodo_personalizado);
            default: return fragment.getString(R.string.string_periodo_mes);
        }
    }

    private PeriodoFiltro periodoSelecionadoNoChip() {
        for (int i = 0; i < chipGroupPeriodo.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupPeriodo.getChildAt(i);

            if (chip.isChecked()) {
                return (PeriodoFiltro) chip.getTag();
            }
        }

        return PeriodoFiltro.MES;
    }

    private void atualizarPeriodo() {
        intervaloPeriodoAtual = calcularIntervaloPeriodo(
            tipoPeriodoSelecionado,
            dataReferenciaPeriodo,
            dataInicioPersonalizada,
            dataFimPersonalizada
        );

        txtValorPeriodo.setText(formatarValorPeriodo());

        if (listener != null) {
            listener.aoAlterarPeriodo(intervaloPeriodoAtual[0], intervaloPeriodoAtual[1], tipoPeriodoSelecionado);
        }
    }

    private String formatarValorPeriodo() {
        if (tipoPeriodoSelecionado == PeriodoFiltro.PERSONALIZADO) {
            if (dataInicioPersonalizada == null || dataFimPersonalizada == null) {
                return fragment.getString(R.string.string_selecionar_periodo);
            }

            return formatarData(dataInicioPersonalizada) + " - " + formatarData(dataFimPersonalizada);
        }

        if (tipoPeriodoSelecionado == PeriodoFiltro.SEMANA) {
            return formatarData(intervaloPeriodoAtual[0]) + " - " + formatarData(intervaloPeriodoAtual[1]);
        }

        long referencia = dataReferenciaPeriodo != null ? dataReferenciaPeriodo : System.currentTimeMillis();

        if (tipoPeriodoSelecionado == PeriodoFiltro.DIA) {
            return formatarData(referencia);
        }

        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(referencia);

        if (tipoPeriodoSelecionado == PeriodoFiltro.ANO) {
            return String.valueOf(calendario.get(Calendar.YEAR));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM 'de' yyyy", Locale.getDefault());
        String texto = sdf.format(calendario.getTime());

        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private void abrirSeletorPeriodo() {
        switch (tipoPeriodoSelecionado) {
            case SEMANA:
                abrirSeletorSemana();
                return;
            case MES:
                abrirSeletorMes();
                return;
            case ANO:
                abrirSeletorAno();
                return;
            case PERSONALIZADO:
                abrirSeletorIntervaloPersonalizado();
                return;
            default:
                abrirSeletorDia();
        }
    }

    // Dia: só os dias do mês atual são navegáveis/selecionáveis.
    private void abrirSeletorDia() {
        long selecaoInicial = dataReferenciaPeriodo != null ? dataReferenciaPeriodo : System.currentTimeMillis();

        MaterialDatePicker<Long> seletor = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.string_selecionar_periodo)
            .setCalendarConstraints(restricaoMesAtual())
            .setSelection(paraUtc(selecaoInicial))
            .build();

        seletor.addOnPositiveButtonClickListener(selecaoUtc -> {
            dataReferenciaPeriodo = deUtcParaLocal(selecaoUtc);
            atualizarPeriodo();
        });

        seletor.show(fragment.getChildFragmentManager(), "seletor_periodo_dia");
    }

    // Semana: calendário navegável com a semana do dia de referência destacada; clicar em
    // qualquer dia dela confirma a semana inteira (SeletorSemanaDialogFragment resolve isso).
    private void abrirSeletorSemana() {
        long diaReferencia = dataReferenciaPeriodo != null ? dataReferenciaPeriodo : System.currentTimeMillis();

        SeletorSemanaDialogFragment dialogo = SeletorSemanaDialogFragment.novaInstancia(diaReferencia);
        dialogo.setOnSemanaSelecionadaListener(diaSelecionado -> {
            dataReferenciaPeriodo = diaSelecionado;
            atualizarPeriodo();
        });

        dialogo.show(fragment.getChildFragmentManager(), "seletor_periodo_semana");
    }

    // Mês: os 12 meses de um ano, com setas no próprio diálogo para trocar de ano.
    private void abrirSeletorMes() {
        long referencia = dataReferenciaPeriodo != null ? dataReferenciaPeriodo : System.currentTimeMillis();

        SeletorMesDialogFragment dialogo = SeletorMesDialogFragment.novaInstancia(referencia);
        dialogo.setOnMesSelecionadoListener(valor -> {
            dataReferenciaPeriodo = valor;
            atualizarPeriodo();
        });

        dialogo.show(fragment.getChildFragmentManager(), "seletor_periodo_mes");
    }

    // Ano: apenas os últimos 10 anos (o atual e os 9 anteriores).
    private void abrirSeletorAno() {
        Calendar calendario = Calendar.getInstance();
        int anoAtual = calendario.get(Calendar.YEAR);

        String[] labels = new String[10];
        long[] valores = new long[10];

        for (int i = 0; i < 10; i++) {
            int ano = anoAtual - i;

            Calendar calendarioAno = Calendar.getInstance();
            calendarioAno.set(ano, Calendar.JANUARY, 1, 0, 0, 0);
            calendarioAno.set(Calendar.MILLISECOND, 0);

            labels[i] = String.valueOf(ano);
            valores[i] = calendarioAno.getTimeInMillis();
        }

        long referencia = dataReferenciaPeriodo != null ? dataReferenciaPeriodo : System.currentTimeMillis();
        Calendar referenciaCalendario = Calendar.getInstance();
        referenciaCalendario.setTimeInMillis(referencia);

        int indice = anoAtual - referenciaCalendario.get(Calendar.YEAR);
        long valorSelecionado = (indice >= 0 && indice < 10) ? valores[indice] : valores[0];

        SeletorOpcaoPeriodoDialogFragment dialogo = SeletorOpcaoPeriodoDialogFragment.novaInstancia(
            labels,
            valores,
            valorSelecionado
        );
        dialogo.setOnOpcaoSelecionadaListener(valor -> {
            dataReferenciaPeriodo = valor;
            atualizarPeriodo();
        });

        dialogo.show(fragment.getChildFragmentManager(), "seletor_periodo_ano");
    }

    // Personalizado: abre mostrando o mês atual, sem restrição de intervalo selecionável.
    private void abrirSeletorIntervaloPersonalizado() {
        long[] intervaloMesAtual = calcularIntervaloPeriodo(PeriodoFiltro.MES, null, null, null);

        CalendarConstraints restricao = new CalendarConstraints.Builder()
            .setOpenAt(paraUtc(intervaloMesAtual[0]))
            .build();

        MaterialDatePicker<Pair<Long, Long>> seletor = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.string_selecionar_periodo)
            .setCalendarConstraints(restricao)
            .build();

        seletor.addOnPositiveButtonClickListener(selecao -> {
            dataInicioPersonalizada = inicioDoDiaLocal(selecao.first);
            dataFimPersonalizada = fimDoDiaLocal(selecao.second);
            atualizarPeriodo();
        });

        seletor.show(fragment.getChildFragmentManager(), "seletor_periodo_personalizado");
    }

    private static CalendarConstraints restricaoMesAtual() {
        long[] intervaloMes = calcularIntervaloPeriodo(PeriodoFiltro.MES, null, null, null);

        return new CalendarConstraints.Builder()
            .setStart(paraUtc(intervaloMes[0]))
            .setEnd(paraUtc(intervaloMes[1]))
            .setOpenAt(paraUtc(System.currentTimeMillis()))
            .build();
    }

    private static long paraUtc(long dataLocalMillis) {
        Calendar local = Calendar.getInstance();
        local.setTimeInMillis(dataLocalMillis);

        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH));

        return utc.getTimeInMillis();
    }

    private static long deUtcParaLocal(long dataUtcMillis) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(dataUtcMillis);

        Calendar local = Calendar.getInstance();
        local.clear();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH));

        return local.getTimeInMillis();
    }

    private static long inicioDoDiaLocal(long dataSelecionadaUtc) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(dataSelecionadaUtc);

        Calendar local = Calendar.getInstance();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        local.set(Calendar.MILLISECOND, 0);

        return local.getTimeInMillis();
    }

    private static long fimDoDiaLocal(long dataSelecionadaUtc) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(dataSelecionadaUtc);

        Calendar local = Calendar.getInstance();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        local.set(Calendar.MILLISECOND, 999);

        return local.getTimeInMillis();
    }
}
