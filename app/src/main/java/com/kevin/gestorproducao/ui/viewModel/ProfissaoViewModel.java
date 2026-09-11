package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.gestorproducao.model.ProfissaoBase;
import com.kevin.gestorproducao.repository.ProfissaoRepository;
import com.kevin.gestorproducao.repository.Resource;

import java.util.ArrayList;

public class ProfissaoViewModel extends ViewModel {
    private final ProfissaoRepository repository;
    private final MutableLiveData<Boolean> triggerRecuperaProfissoes = new MutableLiveData<>();
    private final LiveData<Resource<ArrayList<ProfissaoBase>>> profissoes;
    private final MediatorLiveData<Resource<Void>> sincronizacaoResultado = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> resultadoModificacao = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> resultadoInsercao = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> resultadoRemocao = new MediatorLiveData<>();

    public ProfissaoViewModel(ProfissaoRepository repository) {
        this.repository = repository;
        profissoes = Transformations.switchMap(
            triggerRecuperaProfissoes,
            trigger -> repository.recuperaProfissoes()
        );
    }

    public MediatorLiveData<Resource<Void>> getSincronizacaoResultado() {
        return sincronizacaoResultado;
    }

    public LiveData<Resource<ArrayList<ProfissaoBase>>> getProfissoes() {
        return profissoes;
    }

    public MediatorLiveData<Resource<Void>> getResultadoModificacao() {
        return resultadoModificacao;
    }

    public MediatorLiveData<Resource<Void>> getResultadoInsercao() {
        return resultadoInsercao;
    }
    public MediatorLiveData<Resource<Void>> getResultadoRemocao() {
        return resultadoRemocao;
    }

    public void recuperaProfissoes() {
        triggerRecuperaProfissoes.setValue(true);
    }

    public void sincronizaProfissoes() {
        LiveData<Resource<Void>> source = repository.sincronizaProfissoes();

        sincronizacaoResultado.addSource(source, resultado -> {
            sincronizacaoResultado.setValue(resultado);
            sincronizacaoResultado.removeSource(source);
        });
    }

    public void modificaProfissao(ProfissaoBase profissao) {
        LiveData<Resource<Void>> source = repository.modificaProfissao(profissao);

        resultadoModificacao.addSource(source, resultado -> {
            resultadoModificacao.setValue(resultado);
            resultadoModificacao.removeSource(source);
        });
    }

    public void removeObservador() {
        repository.removeOuvinte();
    }

    public void removeResultadoModificacao() {
        resultadoModificacao.setValue(null);
    }

    public void insereProfissao(ProfissaoBase profissao) {
        LiveData<Resource<Void>> source = repository.insereProfissao(profissao);

        resultadoInsercao.addSource(source, resultado -> {
            resultadoInsercao.setValue(resultado);
            resultadoInsercao.removeSource(source);
        });
    }

    public void removeResultadoInsercao() {
        resultadoInsercao.setValue(null);
    }

    public void removeProfissao(ProfissaoBase profissao) {
        LiveData<Resource<Void>> source = repository.removeProfissao(profissao);

        resultadoRemocao.addSource(source, resultado -> {
            resultadoRemocao.setValue(resultado);
            resultadoRemocao.removeSource(source);
        });
    }

    public void removeResultadoRemocao() {
        resultadoRemocao.setValue(null);
    }

    public void carregarSeNecessario() {
        if (profissoes.getValue() == null) {
            sincronizaProfissoes();
        }
    }
}
