package com.kevin.gestorproducao.ui.viewModel.factory;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kevin.gestorproducao.repository.FirebaseAuthRepository;
import com.kevin.gestorproducao.repository.PersonagemRepository;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.ProfissaoRepository;
import com.kevin.gestorproducao.repository.RecursosProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;
import com.kevin.gestorproducao.repository.TrabalhoVendaRepository;
import com.kevin.gestorproducao.ui.viewModel.AutenticacaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoPersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.RecursosProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoEstoqueViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoProducaoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhoViewModel;
import com.kevin.gestorproducao.ui.viewModel.TrabalhosVendidosViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public ViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(TrabalhoViewModel.class)) {
            return (T) new TrabalhoViewModel(
                TrabalhoRepository.getInstancia(context)
            );
        }

        if (modelClass.isAssignableFrom(TrabalhoProducaoViewModel.class)) {
            return (T) new TrabalhoProducaoViewModel(
                TrabalhoProducaoRepository.getInstance(context)
            );
        }

        if (modelClass.isAssignableFrom(ProfissaoPersonagemViewModel.class)) {
            return (T) new ProfissaoPersonagemViewModel(
                ProfissaoPersonagemRepository.getInstance(context)
            );
        }

        if (modelClass.isAssignableFrom(PersonagemViewModel.class)) {
            return (T) new PersonagemViewModel(
                PersonagemRepository.getInstance(context)
            );
        }

        if (modelClass.isAssignableFrom(TrabalhoEstoqueViewModel.class)) {
            return (T) new TrabalhoEstoqueViewModel(
                TrabalhoEstoqueRepository.getInstance(context)
            );
        }

        if (modelClass.isAssignableFrom(TrabalhosVendidosViewModel.class)) {
            return (T) new TrabalhosVendidosViewModel(
                TrabalhoVendaRepository.getInstance(context),
                TrabalhoEstoqueRepository.getInstance(context)
            );
        }

        if (modelClass.isAssignableFrom(RecursosProducaoViewModel.class)) {
            return (T) new RecursosProducaoViewModel(
                RecursosProducaoRepository.getInstance(context)
            );
        }

        if (modelClass.isAssignableFrom(ProfissaoViewModel.class)) {
            return (T) new ProfissaoViewModel(
                ProfissaoRepository.getInstance(context)
            );
        }

        if (modelClass.isAssignableFrom(AutenticacaoViewModel.class)) {
            return (T) new AutenticacaoViewModel(
                FirebaseAuthRepository.getInstance()
            );
        }

        throw new IllegalArgumentException("Classe ViewModel desconhecida");
    }
}
