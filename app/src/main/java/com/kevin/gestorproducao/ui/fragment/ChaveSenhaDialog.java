package com.kevin.gestorproducao.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.rules.exception.SenhaPersonagemException;
import com.kevin.gestorproducao.utilitario.ChaveSenhaStore;

public class ChaveSenhaDialog extends DialogFragment {
    public interface OnChaveSalvaListener {
        void onChaveSalva();
    }

    private OnChaveSalvaListener listener;

    public static ChaveSenhaDialog novaInstancia(OnChaveSalvaListener listener) {
        ChaveSenhaDialog dialog = new ChaveSenhaDialog();
        dialog.listener = listener;

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View conteudo = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_chave_senha, null);
        TextInputLayout campoTxt = conteudo.findViewById(R.id.txtChaveSenha);
        TextInputEditText campo = conteudo.findViewById(R.id.edtChaveSenha);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.stringChaveSenhasTitulo)
            .setMessage(R.string.stringChaveSenhasDescricao)
            .setView(conteudo)
            .setNegativeButton(R.string.stringCancelar, null)
            .setPositiveButton(R.string.stringConfirmar, null)
            .create();

        dialog.setOnShowListener(d -> dialog
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(v -> salvaChave(dialog, campo, campoTxt))
        );

        return dialog;
    }

    private void salvaChave(
        AlertDialog dialog,
        TextInputEditText campo,
        TextInputLayout campoTxt
    ) {
        try {
            ChaveSenhaStore.salvar(requireContext(), String.valueOf(campo.getText()));
        } catch (SenhaPersonagemException e) {
            campoTxt.setError(getString(R.string.stringChaveSenhasInvalida));
            return;
        }

        dialog.dismiss();
        if (listener != null) listener.onChaveSalva();
    }
}
