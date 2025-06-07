package com.kevin.ceep.ui.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.kevin.ceep.databinding.FragmentSplashscreenBinding;
import com.kevin.ceep.ui.viewModel.ComponentesVisuais;
import com.kevin.ceep.ui.viewModel.EstadoAppViewModel;


@SuppressLint("CustomSplashScreen")
public class SplashscreenFragment extends Fragment {
    private FragmentSplashscreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashscreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configuraComponentesVisuais();
        SharedPreferences preferences = requireActivity().getSharedPreferences("user_preferences", MODE_PRIVATE);
        if (preferences.contains("ja_abriu_app")) {
            vaiParaEntraUsuarioActivity();
            return;
        }
        adicionarPreferenceJaAbriu(preferences);
        mostrarSplash();
    }

    private void configuraComponentesVisuais() {
        EstadoAppViewModel estadoAppViewModel = new ViewModelProvider(requireActivity()).get(EstadoAppViewModel.class);
        ComponentesVisuais componentesVisuais = new ComponentesVisuais();
        estadoAppViewModel.componentes.setValue(componentesVisuais);
    }

    private void mostrarSplash() {
        Handler handler = new Handler();
        handler.postDelayed(this::vaiParaEntraUsuarioActivity, 3000);
    }

    private void adicionarPreferenceJaAbriu(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("ja_abriu_app", true);
        editor.apply();
    }

    private void vaiParaEntraUsuarioActivity() {
        NavDirections acao = SplashscreenFragmentDirections.vaiDeSplashscreenParaEntrar();
        Navigation.findNavController(binding.getRoot()).navigate(acao);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
