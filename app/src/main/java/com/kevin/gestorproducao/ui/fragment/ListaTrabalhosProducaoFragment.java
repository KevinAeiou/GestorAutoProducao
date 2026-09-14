package com.kevin.gestorproducao.ui.fragment;

import static com.kevin.gestorproducao.ui.fragment.ListaTrabalhosProducaoFragmentDirections.vaiDeProducaoParaFiltro;
import static com.kevin.gestorproducao.ui.fragment.ListaTrabalhosProducaoFragmentDirections.vaiParaDetalhesProducao;
import static com.kevin.gestorproducao.ui.fragment.ListaTrabalhosProducaoFragmentDirections.vaiParaNovaProducao;
import static com.kevin.gestorproducao.utilitario.Utilitario.calcularIntervaloPeriodo;
import static com.kevin.gestorproducao.utilitario.Utilitario.filtrarTrabalhos;
import static com.kevin.gestorproducao.utilitario.Utilitario.formatarData;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentListaTrabalhosProducaoBinding;
import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.PeriodoFiltro;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;
import com.kevin.gestorproducao.rules.exception.ProducaoException;
import com.kevin.gestorproducao.service.PlanejamentoProducaoService;
import com.kevin.gestorproducao.service.ProducaoFluxoService;
import com.kevin.gestorproducao.service.ProducaoServicosFactory;
import com.kevin.gestorproducao.service.ServicosProducaoPersonagem;
import com.kevin.gestorproducao.ui.fragment.ListaTrabalhosProducaoFragmentDirections.VaiDeProducaoParaFiltro;
import com.kevin.gestorproducao.ui.recyclerview.adapter.ListaTrabalhoProducaoAdapter;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.EstadoAppViewModel;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ListaTrabalhosProducaoFragment
    extends BaseFragment<FragmentListaTrabalhosProducaoBinding>
    implements MenuProvider
{
    private ListaTrabalhoProducaoAdapter trabalhoAdapter;
    private RecyclerView meuRecycler;
    private ArrayList<TrabalhoProducao> trabalhos, trabalhosFiltrados;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar indicadorProgresso;
    private ImageView iconeListaVazia;
    private TextView txtListaVazia;
    private FloatingActionButton floatingActionButton;
    private PersonagemViewModel personagemViewModel;
    private TrabalhoProducaoViewModel producaoViewModel;
    private TrabalhoViewModel trabalhoViewModel;
    private EstadoAppViewModel estadoAppViewModel;
    private FiltroViewModel filtroViewModel;
    private NavController controlador;
    private FiltroTrabalho filtroAtual;
    private TrabalhoProducao trabalhoSelecionado;
    private int estadoAnterior = -1;
    private float ultimoDX = 0;
    private ProducaoFluxoService producaoFluxoService;
    private Context context;
    private TrabalhoRepository trabalhoRepo;
    private TrabalhoEstoqueRepository estoqueRepo;
    private TrabalhoProducaoRepository producaoRepo;
    private ProfissaoPersonagemRepository profissaoPersonagemRepo;
    private PlanejamentoProducaoService planejamentoProducaoService;
    private ChipGroup chipGroupPeriodo;
    private TextView txtValorPeriodo;
    private PeriodoFiltro tipoPeriodoSelecionado = PeriodoFiltro.MES;
    private Long dataReferenciaPeriodo;
    private Long dataInicioPersonalizada;
    private Long dataFimPersonalizada;
    private long[] intervaloPeriodoAtual;

    @Override
    protected FragmentListaTrabalhosProducaoBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentListaTrabalhosProducaoBinding.inflate(inflater, container, false);
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

        controlador.getCurrentBackStackEntry()
            .getSavedStateHandle()
            .getLiveData("mensagem_sucesso")
            .observe(
                    getViewLifecycleOwner(),
                    mensagem -> {

                        if (mensagem == null) return;

                        mostraMensagemAncorada(mensagem.toString());

                        controlador.getCurrentBackStackEntry()
                                .getSavedStateHandle()
                                .remove("mensagem_sucesso");
                    }
            );

        configuraRecycler();
        configuraSwipeRefreshLayout();
        configuraBotaoInsereTrabalho();
        configuraDeslizeItem();
        configuraPersonagemSelecionado();
        configuraFiltroPeriodo();
        observarFiltros();
        observarProducao();

        producaoViewModel.carregarSeNecessario();
        producaoViewModel.atualizaProducao();
    }

    private void observarProducao() {
        producaoViewModel.getSincronizacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
                    return;
                }

                producaoViewModel.atualizaProducao();
            }
        );

        producaoViewModel.getProducoes().observe(
            getViewLifecycleOwner(),
            resultado -> {
                indicadorProgresso.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (resultado.getDado() != null) {
                    trabalhos = resultado.getDado();

                    aplicarFiltros();
                }

                if (resultado.getErro() != null) {
                    mostraMensagemAncorada(resultado.getErro());
                }
            }
        );

        producaoViewModel.getModificacaoResultado().observe(
            getViewLifecycleOwner(),
            resultado -> {

                if (resultado == null) return;

                if (resultado.getErro() == null) {
                    producaoViewModel.limpaModificacaoResultado();

                    producaoFluxoService.processarPosModificacao(
                        trabalhoSelecionado,
                        estadoAnterior
                    );

                    return;
                }

                mostraMensagemAncorada(getString(R.string.stringErroValor, resultado.getErro()));
            }
        );
    }

    private void observarFiltros() {
        filtroViewModel.getFiltro().observe(
            getViewLifecycleOwner(),
            filtro -> {
                filtroAtual = filtro;
                aplicarFiltros();
            }
        );
    }

    private void configuraPersonagemSelecionado() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                producaoViewModel.setIdPersonagem(personagem.getId());

                ServicosProducaoPersonagem servicos = ProducaoServicosFactory.cria(
                    trabalhoRepo,
                    estoqueRepo,
                    producaoRepo,
                    profissaoPersonagemRepo,
                    personagem.getId(),
                    context
                );

                planejamentoProducaoService = servicos.getPlanejamentoProducaoService();
                producaoFluxoService = servicos.getProducaoFluxoService();
            }
        );
    }

    @Override
    protected ComponentesVisuais fornecerComponentesVisuais() {

        return new ComponentesVisuais(
            true,
            true,
            true,
            false,
            true,
            true,
            null,
            false
        );
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuBusca) {
            if (controlador.getCurrentDestination().getId() == R.id.listaTrabalhosProducao) {
                VaiDeProducaoParaFiltro acao = vaiDeProducaoParaFiltro();
                acao.setEhProducao(true);
                controlador.navigate(acao);
            }
            return true;
        }
        return false;
    }

    private void configuraDeslizeItem() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT
        ) {
            @Override
            public boolean onMove(
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                @NonNull RecyclerView.ViewHolder target
            ) {
                return false;
            }

            @Override
            public int getSwipeDirs(
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder
            ) {
                int posicao = viewHolder.getBindingAdapterPosition();
                TrabalhoProducao trabalho = trabalhosFiltrados.get(posicao);

                if (trabalho.ehProduzir()) {
                    return ItemTouchHelper.LEFT;
                }

                if (trabalho.ehFeito()) {
                    return ItemTouchHelper.RIGHT;
                }

                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int posicao = viewHolder.getBindingAdapterPosition();
                trabalhoSelecionado = trabalhosFiltrados.get(posicao);

                estadoAnterior = trabalhoSelecionado.getEstado();
                trabalhoSelecionado.atualizarEstado(defineNovoEstado(trabalhoSelecionado, direction));
                trabalhoSelecionado.marcarModificacao();
                meuRecycler.getAdapter().notifyItemChanged(posicao);

                TrabalhoProducao producao = getTrabalhoProducao();

                producaoViewModel.modificaTrabalhoProducao(producao);
            }

            @Override
            public void onChildDraw(
                @NonNull Canvas c,
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                float dX,
                float dY,
                int actionState,
                boolean isCurrentlyActive
            ) {
                View itemView = viewHolder.itemView;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    if (isCurrentlyActive) {
                        ultimoDX = dX;
                        itemView.setTranslationX(dX);
                        return;
                    }

                    if (ultimoDX < 0) {
                        itemView.setTranslationX(itemView.getWidth());
                    } else {
                        itemView.setTranslationX(-itemView.getWidth());
                    }

                    itemView.animate()
                        .translationX(0)
                        .setDuration(250)
                        .start();
                    return;
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(meuRecycler);
    }

    @NonNull
    private TrabalhoProducao getTrabalhoProducao() {
        TrabalhoProducao producao = new TrabalhoProducao();

        producao.setId(trabalhoSelecionado.getId());
        producao.setIdTrabalho(trabalhoSelecionado.getIdTrabalho());
        producao.setExperiencia(trabalhoSelecionado.getExperiencia());
        producao.setEstado(trabalhoSelecionado.getEstado());
        producao.setTipoLicenca(trabalhoSelecionado.getTipoLicenca());
        producao.setRecorrencia(trabalhoSelecionado.getRecorrencia());
        producao.setCriadoEm(trabalhoSelecionado.getCriadoEm());
        producao.setIniciadoEm(trabalhoSelecionado.getIniciadoEm());
        producao.setFinalizadoEm(trabalhoSelecionado.getFinalizadoEm());

        return producao;
    }

    private static int defineNovoEstado(TrabalhoProducao trabalho, int direcao) {
        if (trabalho.ehProduzir() || trabalho.ehFeito()) return  1;
        if (trabalho.ehProduzindo() && direcao == ItemTouchHelper.RIGHT) return 0;
        if (trabalho.ehProduzindo() && direcao == ItemTouchHelper.LEFT) return 2;

        return 0;
    }

    private void configuraSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            try {
                if (planejamentoProducaoService != null) {
                    planejamentoProducaoService.incluirMaisVendidos();
                    planejamentoProducaoService.incluirComunsProfissoesPriorizadas();
                }

                producaoViewModel.sincronizaProducao();
            } catch (ProducaoException e) {
                mostraMensagemAncorada(e.getMessage());
            } finally {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void configuraBotaoInsereTrabalho() {
        floatingActionButton.setOnClickListener(v -> controlador.navigate(
            vaiParaNovaProducao()
        ));
    }

    private void inicializaComponentes() {
        filtroAtual = null;
        trabalhos = new ArrayList<>();
        trabalhosFiltrados = new ArrayList<>();
        meuRecycler = binding.listaTrabalhoRecyclerView;
        swipeRefreshLayout = binding.swipeRefreshLayoutTrabalhos;
        indicadorProgresso = binding.indicadorProgressoListaTrabalhosFragment;
        iconeListaVazia = binding.iconeVazia;
        txtListaVazia = binding.txtListaVazia;
        floatingActionButton = binding.floatingActionButton;
        chipGroupPeriodo = binding.chipGroupPeriodoProducao;
        txtValorPeriodo = binding.txtValorPeriodoProducao;

        controlador = Navigation.findNavController(binding.getRoot());
        context = requireContext().getApplicationContext();

        trabalhoRepo = TrabalhoRepository.getInstancia(context);
        estoqueRepo = TrabalhoEstoqueRepository.getInstance(context);
        producaoRepo = TrabalhoProducaoRepository.getInstance(context);
        profissaoPersonagemRepo = ProfissaoPersonagemRepository.getInstance(context);

        ViewModelFactory viewModelFactory = new ViewModelFactory(
            context
        );
        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        producaoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoProducaoViewModel.class);

        filtroViewModel = new ViewModelProvider(
            requireActivity()
        ).get(FiltroViewModel.class);

        estadoAppViewModel = new ViewModelProvider(
            requireActivity()
        ).get(EstadoAppViewModel.class);

        trabalhoViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(TrabalhoViewModel.class);
    }

    private void configuraRecycler() {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(context));
        configuraAdapter();
    }

    private void configuraAdapter() {
        trabalhoAdapter = new ListaTrabalhoProducaoAdapter(context);
        meuRecycler.setAdapter(trabalhoAdapter);
        configurarHideOnScroll(meuRecycler, estadoAppViewModel);

        trabalhoAdapter.setOnItemClickListener(this::vaiParaDetalhesProducaoActivity);
    }

    private void vaiParaDetalhesProducaoActivity(TrabalhoProducao trabalho) {
         NavDirections acao = vaiParaDetalhesProducao(trabalho);
         controlador.navigate(acao);
    }

    private void aplicarFiltros() {
        ArrayList<TrabalhoProducao> base;

        if (filtroAtual == null) {
            base = (ArrayList<TrabalhoProducao>) trabalhos.clone();
        } else {
            base = filtrarTrabalhos(trabalhos, filtroAtual);

            // getEstado() é específico de TrabalhoProducao (para produzir/produzindo/feito), então
            // não faz parte do filtro genérico de Utilitario.filtrarTrabalhos — aplicado à parte.
            boolean temEstado = filtroAtual.getEstado() != -1;
            if (temEstado) {
                base.removeIf(trabalho -> trabalho.getEstado() != filtroAtual.getEstado());
            }
        }

        trabalhosFiltrados = filtrarPorPeriodo(base);

        trabalhoAdapter.atualiza(trabalhosFiltrados);
        atualizaVisibilidadeListaVazia(trabalhosFiltrados.isEmpty());
        meuRecycler.smoothScrollToPosition(0);
    }

    // Trabalhos para produzir/produzindo aparecem sempre, independentemente do período
    // selecionado — apenas os concluídos são restritos ao intervalo escolhido (por finalizadoEm).
    // O período é controlado direto na tela (fora do modal de filtro), então é aplicado sempre,
    // independente de haver ou não um FiltroTrabalho ativo.
    private ArrayList<TrabalhoProducao> filtrarPorPeriodo(ArrayList<TrabalhoProducao> trabalhos) {
        if (intervaloPeriodoAtual == null) return trabalhos;

        long dataInicio = intervaloPeriodoAtual[0];
        long dataFim = intervaloPeriodoAtual[1];

        ArrayList<TrabalhoProducao> resultado = new ArrayList<>();

        for (TrabalhoProducao trabalho : trabalhos) {
            if (!trabalho.ehFeito()) {
                resultado.add(trabalho);
                continue;
            }

            Long finalizadoEm = trabalho.getFinalizadoEm();

            if (finalizadoEm != null && finalizadoEm >= dataInicio && finalizadoEm <= dataFim) {
                resultado.add(trabalho);
            }
        }

        return resultado;
    }

    private void configuraFiltroPeriodo() {
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

    private void popularChipsPeriodo() {
        chipGroupPeriodo.removeAllViews();

        List<PeriodoFiltro> periodos = Arrays.asList(
            PeriodoFiltro.DIA,
            PeriodoFiltro.SEMANA,
            PeriodoFiltro.MES,
            PeriodoFiltro.ANO,
            PeriodoFiltro.PERSONALIZADO
        );

        LayoutInflater inflater = LayoutInflater.from(requireContext());

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
            case DIA: return getString(R.string.string_periodo_dia);
            case SEMANA: return getString(R.string.string_periodo_semana);
            case ANO: return getString(R.string.string_periodo_ano);
            case PERSONALIZADO: return getString(R.string.string_periodo_personalizado);
            default: return getString(R.string.string_periodo_mes);
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

        if (trabalhoAdapter != null) {
            aplicarFiltros();
        }
    }

    private String formatarValorPeriodo() {
        if (tipoPeriodoSelecionado == PeriodoFiltro.PERSONALIZADO) {
            if (dataInicioPersonalizada == null || dataFimPersonalizada == null) {
                return getString(R.string.string_selecionar_periodo);
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

        seletor.show(getChildFragmentManager(), "seletor_periodo_producao_dia");
    }

    // Semana: calendário do mês atual com a semana do dia de referência destacada; clicar em
    // qualquer dia dela confirma a semana inteira (SeletorSemanaDialogFragment resolve isso).
    private void abrirSeletorSemana() {
        long diaReferencia = dataReferenciaPeriodo != null ? dataReferenciaPeriodo : System.currentTimeMillis();

        SeletorSemanaDialogFragment dialogo = SeletorSemanaDialogFragment.novaInstancia(diaReferencia);
        dialogo.setOnSemanaSelecionadaListener(diaSelecionado -> {
            dataReferenciaPeriodo = diaSelecionado;
            atualizarPeriodo();
        });

        dialogo.show(getChildFragmentManager(), "seletor_periodo_producao_semana");
    }

    // Mês: os 12 meses de um ano, com setas no próprio diálogo para trocar de ano.
    private void abrirSeletorMes() {
        long referencia = dataReferenciaPeriodo != null ? dataReferenciaPeriodo : System.currentTimeMillis();

        SeletorMesDialogFragment dialogo = SeletorMesDialogFragment.novaInstancia(referencia);
        dialogo.setOnMesSelecionadoListener(valor -> {
            dataReferenciaPeriodo = valor;
            atualizarPeriodo();
        });

        dialogo.show(getChildFragmentManager(), "seletor_periodo_producao_mes");
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

        dialogo.show(getChildFragmentManager(), "seletor_periodo_producao_ano");
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

        seletor.show(getChildFragmentManager(), "seletor_periodo_producao_personalizado");
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

    private void atualizaVisibilidadeListaVazia(boolean listaVazia) {
        if (listaVazia) {
            iconeListaVazia.setVisibility(View.VISIBLE);
            txtListaVazia.setVisibility(View.VISIBLE);
            return;
        }

        iconeListaVazia.setVisibility(View.GONE);
        txtListaVazia.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeObservadorProducao();
        removeObservadorPersonagem();
        removeObservadorTrabalho();
        binding = null;
    }

    private void removeObservadorTrabalho() {
        if (trabalhoViewModel == null) return;
        trabalhoViewModel.removeObservador();
    }

    private void removeObservadorPersonagem() {
        if (personagemViewModel == null) return;
        personagemViewModel.removeObservador();
    }

    private void removeObservadorProducao() {
        if (producaoViewModel == null) return;
        producaoViewModel.removeObservador();
    }
}