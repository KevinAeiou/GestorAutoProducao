package com.kevin.gestorproducao.ui.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentSplashscreenBinding;


@SuppressLint("CustomSplashScreen")
public class SplashscreenFragment extends BaseFragment<FragmentSplashscreenBinding> {
    private NavController navController;
    private Handler handler;

    @Override
    protected FragmentSplashscreenBinding inflateBinding(
        LayoutInflater inflater,
        ViewGroup container
    ) {
        return FragmentSplashscreenBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(
        @NonNull View view,
        Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(binding.getRoot());
        handler = new Handler();

        SharedPreferences preferences = requireActivity().getSharedPreferences(
            "user_preferences",
            MODE_PRIVATE
        );

        boolean jaAbriuApp = preferences.getBoolean("ja_abriu_app", false);

        if (jaAbriuApp) {
            decidirProximaTela();
            return;
        }

        adicionarPreferenceJaAbriu(preferences);
        mostrarSplash();
    }

    private void decidirProximaTela() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            irParaLogin();
            return;
        }

        irParaProducao();
    }

    private void irParaProducao() {
        NavDirections acao = SplashscreenFragmentDirections.vaiDeSplashscreenParaProducao();

        navController.navigate(acao);
    }

    private void irParaLogin() {
        if (navController.getCurrentDestination() != null &&
            navController.getCurrentDestination().getId() == R.id.splashscreenFragment
        ) {
            NavDirections acao = SplashscreenFragmentDirections.vaiDeSplashscreenParaEntrar();

            navController.navigate(acao);
        }
    }

    private void mostrarSplash() {
        handler.postDelayed(this::irParaLogin, 3000);
    }

    private void adicionarPreferenceJaAbriu(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("ja_abriu_app", true);
        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        handler.removeCallbacksAndMessages(null);
    }
}
