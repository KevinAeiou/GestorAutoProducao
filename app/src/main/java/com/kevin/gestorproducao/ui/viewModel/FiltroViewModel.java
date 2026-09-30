package com.kevin.gestorproducao.ui.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kevin.gestorproducao.model.FiltroTrabalho;

public class FiltroViewModel extends ViewModel {
    private final MutableLiveData<FiltroTrabalho> filtro = new MutableLiveData<>();

    public MutableLiveData<FiltroTrabalho> getFiltro() {
        return filtro;
    }

    public void aplicarFiltro(FiltroTrabalho novoFiltro) {
        filtro.setValue(novoFiltro);
    }

    public void removeObservador() {
        filtro.setValue(null);
    }

}
