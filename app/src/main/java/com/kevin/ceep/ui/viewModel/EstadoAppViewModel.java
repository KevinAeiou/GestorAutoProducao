package com.kevin.ceep.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EstadoAppViewModel extends ViewModel {
    public final MutableLiveData<ComponentesVisuais> componentes = new MutableLiveData<>();

}
