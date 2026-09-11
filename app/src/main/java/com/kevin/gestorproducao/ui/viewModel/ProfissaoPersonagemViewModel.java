package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.Resource;

import java.util.ArrayList;

public class ProfissaoPersonagemViewModel extends ViewModel {
    private final ProfissaoPersonagemRepository repository;
    private final MediatorLiveData<Resource<Void>> modificacaoResultado = new MediatorLiveData<>();
    private final LiveData<Resource<ArrayList<ProfissaoPersonagem>>> profissoesPersonagem;
    private final MutableLiveData<String> idPersonagem = new MutableLiveData<>();
    private final MediatorLiveData<Resource<Void>> sincronizacaoResultado = new MediatorLiveData<>();

    public ProfissaoPersonagemViewModel(ProfissaoPersonagemRepository repository) {
        this.repository = repository;
        this.profissoesPersonagem = Transformations.switchMap(
            idPersonagem,
            id -> {
                if (id == null) return new MutableLiveData<>();

                return repository.recuperaProfissoesPersonagem(id);
            }
        );
    }

    public MediatorLiveData<Resource<Void>> getModificacaoResultado() {
        return modificacaoResultado;
    }

    public LiveData<Resource<ArrayList<ProfissaoPersonagem>>> getProfissoesPersonagem() {
        return profissoesPersonagem;
    }

    public MediatorLiveData<Resource<Void>> getSincronizacaoResultado() {
        return sincronizacaoResultado;
    }

    public void modificaExperienciaProfissao(
            ProfissaoPersonagem profissaoPersonagem
    ) {
        LiveData<Resource<Void>> source = repository.modificaProfissaoPersonagem(
            profissaoPersonagem,
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

    public void sincronizaProfissoes() {
        LiveData<Resource<Void>> source = repository.sincronizaProfissoes(idPersonagem.getValue());

        sincronizacaoResultado.addSource(source, resultado -> {
            sincronizacaoResultado.setValue(resultado);
            sincronizacaoResultado.removeSource(source);
        });
    }

    public void atualizaProfissoes() {
        String id = idPersonagem.getValue();
        if (id == null) return;

        idPersonagem.setValue(id);
    }

    public void carregarSeNecessario() {
        if (profissoesPersonagem.getValue() == null) {
            sincronizaProfissoes();
        }
    }
}
