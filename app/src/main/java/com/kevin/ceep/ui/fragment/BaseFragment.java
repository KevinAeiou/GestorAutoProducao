package com.kevin.ceep.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.snackbar.Snackbar;

public abstract class BaseFragment<T extends ViewBinding> extends Fragment {
    protected T binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflateBinding(inflater, container);
        return binding.getRoot();
    }
    protected abstract T inflateBinding(LayoutInflater inflater, ViewGroup container);
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    protected void mostraMensagemAncorada(String mensagem, View anchorView) {
        if (binding == null || getContext() == null || anchorView == null) return;
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_LONG)
                .setAnchorView(anchorView)
                .show();
    }
    protected void mostraMensagem(String mensagem) {
        if (binding == null || getContext() == null) return;
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_LONG)
                .show();
    }
}
