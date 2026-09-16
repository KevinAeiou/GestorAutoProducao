package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.gestorproducao.model.TrabalhoChanceVenda;
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.model.VendaFiltro;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoVendaRepository;
import com.kevin.gestorproducao.repository.Resource;
import com.kevin.gestorproducao.service.VendaService;

import java.util.ArrayList;

public class TrabalhosVendidosViewModel extends ViewModel {
    private final TrabalhoVendaRepository repository;
    private final LiveData<Resource<ArrayList<TrabalhoVendido>>> maisVendidos;
    private final LiveData<Resource<ArrayList<TrabalhoVendido>>> vendasPorTrabalho;
    private final LiveData<Resource<ArrayList<TrabalhoChanceVenda>>> chanceVenda;
    private final MediatorLiveData<Resource<Void>> insercaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> modificacaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> remocaoResultado = new MediatorLiveData<>();
    private final MutableLiveData<String> idPersonagem = new MutableLiveData<>();
    private final MutableLiveData<String> idPersonagemChanceVenda = new MutableLiveData<>();
    private final MutableLiveData<String> idTrabalho = new MutableLiveData<>();
    private final MediatorLiveData<Resource<Void>> sincronizacaoResultado = new MediatorLiveData<>();
    private final VendaService vendaService;
    private final MutableLiveData<VendaFiltro> filtroVenda = new MutableLiveData<>();
    private Long dataInicioPeriodo;
    private Long dataFimPeriodo;
    private Long dataInicioPeriodoChanceVenda;
    private Long dataFimPeriodoChanceVenda;


    public TrabalhosVendidosViewModel(
        TrabalhoVendaRepository vendaRepository,
        TrabalhoEstoqueRepository estoqueRepository
    ) {
        this.repository = vendaRepository;
        this.vendaService = new VendaService(vendaRepository, estoqueRepository);

        maisVendidos = Transformations.switchMap(
            idPersonagem,
            id -> {
                if (id == null) return new MutableLiveData<>();

                return repository.recuperaMaisVendidos(id, dataInicioPeriodo, dataFimPeriodo);
            }
        );

        vendasPorTrabalho = Transformations.switchMap(
            filtroVenda,
            filtro -> {
                if (filtro == null) {
                    return new MutableLiveData<>();
                }

                return repository.recuperaVendasPorTrabalho(
                    filtro.getIdPersonagem(),
                    filtro.getIdTrabalho()
                );
            }
        );

        chanceVenda = Transformations.switchMap(
            idPersonagemChanceVenda,
            id -> {
                if (id == null) return new MutableLiveData<>();
                if (dataInicioPeriodoChanceVenda == null || dataFimPeriodoChanceVenda == null) {
                    return new MutableLiveData<>();
                }

                return repository.recuperaAnaliseChanceVenda(
                    id,
                    dataInicioPeriodoChanceVenda,
                    dataFimPeriodoChanceVenda
                );
            }
        );
    }

    public MediatorLiveData<Resource<Void>> getInsercaoResultado() {
        return insercaoResultado;
    }

    public LiveData<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getRemocaoResultado() {
        return remocaoResultado;
    }

    public MediatorLiveData<Resource<Void>> getSincronizacaoResultado() {
        return sincronizacaoResultado;
    }

    public void insereVenda(TrabalhoVendido trabalho) {
        LiveData<Resource<Void>> source = vendaService.realizarVenda(
            trabalho,
            idPersonagem.getValue()
        );

        insercaoResultado.addSource(source, resultado -> {
            insercaoResultado.setValue(resultado);
            insercaoResultado.removeSource(source);
        });
    }
    public void removeVenda(TrabalhoVendido trabalho) {
        LiveData<Resource<Void>> source = repository.removeTrabalho(
            trabalho,
            idPersonagem.getValue()
        );

        remocaoResultado.addSource(source, resultado -> {
            remocaoResultado.setValue(resultado);
            remocaoResultado.removeSource(source);
        });
    }

    public void modificaVenda(TrabalhoVendido trabalho) {
        LiveData<Resource<Void>> source = repository.modificaVenda(
            trabalho,
            idPersonagem.getValue()
        );

        modificacaoResultado.addSource(source, resultado -> {
            modificacaoResultado.setValue(resultado);
            modificacaoResultado.removeSource(source);
        });
    }

    public void removeObservador() {
        repository.removeOuvinte();
    }

    public void setIdPersonagem(String id) {
        if (id == null) return;

        if (id.equals(idPersonagem.getValue())) return;

        idPersonagem.setValue(id);
    }

    public void setIdTrabalho(String id) {
        idTrabalho.setValue(id);
    }

    public void atualizaMaisVendidos() {
        String id = idPersonagem.getValue();
        if (id == null) return;

        idPersonagem.setValue(id);
    }

    // dataInicio/dataFim são lidos por recuperaMaisVendidos (via switchMap) na próxima vez que
    // idPersonagem "emitir" — reaproveita o mesmo truque de atualizaMaisVendidos() de forçar o
    // switchMap a rodar de novo mesmo com o id inalterado.
    public void atualizaPeriodoVendas(Long dataInicio, Long dataFim) {
        this.dataInicioPeriodo = dataInicio;
        this.dataFimPeriodo = dataFim;

        atualizaMaisVendidos();
    }

    public void atualizaVendasPorTrabalho() {
        VendaFiltro atual = filtroVenda.getValue();

        if (atual != null) {
            filtroVenda.setValue(atual);
        }
    }

    public void sincronizaVendas() {
        LiveData<Resource<Void>> source = repository.sincronizaVendas(idPersonagem.getValue());

        sincronizacaoResultado.addSource(source, resultado -> {
            sincronizacaoResultado.setValue(resultado);
            sincronizacaoResultado.removeSource(source);
        });
    }

    public LiveData<Resource<ArrayList<TrabalhoVendido>>> getVendasPorTrabalho() {
        return vendasPorTrabalho;
    }

    public void carregarVendasPorTrabalho(
        String idPersonagem,
        String idTrabalho
    ) {
        VendaFiltro atual = filtroVenda.getValue();

        if (
            atual != null &&
                atual.getIdPersonagem().equals(idPersonagem) &&
                atual.getIdTrabalho().equals(idTrabalho)
        ) {
            return;
        }

        filtroVenda.setValue(new VendaFiltro(idPersonagem, idTrabalho));
    }

    public void carregarMaisVendidos(
        String idPersonagem
    ) {
        String atual = this.idPersonagem.getValue();

        if (atual != null && atual.equals(idPersonagem)) {
            return;
        }

        this.idPersonagem.setValue(idPersonagem);
    }

    public LiveData<Resource<ArrayList<TrabalhoVendido>>> getMaisVendidos() {
        return maisVendidos;
    }

    public void carregarSeNecessario() {
        if (maisVendidos.getValue() == null) {
            sincronizaVendas();
        }
    }

    public LiveData<Resource<ArrayList<TrabalhoChanceVenda>>> getChanceVenda() {
        return chanceVenda;
    }

    public void carregarChanceVenda(String idPersonagem) {
        String atual = idPersonagemChanceVenda.getValue();

        if (atual != null && atual.equals(idPersonagem)) {
            return;
        }

        idPersonagemChanceVenda.setValue(idPersonagem);
    }

    public void atualizaChanceVenda() {
        String id = idPersonagemChanceVenda.getValue();
        if (id == null) return;

        idPersonagemChanceVenda.setValue(id);
    }

    // Mesmo truque de atualizaPeriodoVendas(): dataInicio/dataFim são lidos por
    // recuperaAnaliseChanceVenda (via switchMap) na próxima vez que idPersonagemChanceVenda
    // "emitir" — forçado aqui mesmo com o id inalterado.
    public void atualizaPeriodoChanceVenda(Long dataInicio, Long dataFim) {
        this.dataInicioPeriodoChanceVenda = dataInicio;
        this.dataFimPeriodoChanceVenda = dataFim;

        atualizaChanceVenda();
    }
}
