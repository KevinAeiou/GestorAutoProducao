package com.kevin.gestorproducao.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kevin.gestorproducao.R;

public class ConfirmacaoDialog extends DialogFragment {
    public interface OnConfirmarListener {
        void onConfirmar();
    }

    public interface OnCancelarListener {
        void onCancelar();
    }

    private String titulo;
    private String mensagem;
    private OnConfirmarListener listenerConfirmar;
    private OnCancelarListener listenerCancelar;

    public static ConfirmacaoDialog novaInstancia(
        String titulo,
        String mensagem,
        OnConfirmarListener listenerConfirmar,
        OnCancelarListener listenerCancelar
    ) {
        ConfirmacaoDialog dialog = new ConfirmacaoDialog();
        dialog.titulo = titulo;
        dialog.mensagem = mensagem;
        dialog.listenerConfirmar = listenerConfirmar;
        dialog.listenerCancelar = listenerCancelar;

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setMessage(mensagem)
            .setNegativeButton(R.string.stringCancelar, (dialogInterface, which) -> {
                if (listenerCancelar != null) listenerCancelar.onCancelar();
                dismiss();
            })
            .setPositiveButton(R.string.stringExcluir, (dialogInterface, which) -> {
                if (listenerConfirmar != null) listenerConfirmar.onConfirmar();
            })
            .create();

        dialog.setOnShowListener(d -> {
            Button botaoExcluir = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (botaoExcluir == null) return;

            botaoExcluir.setTextColor(
                MaterialColors.getColor(botaoExcluir, R.attr.colorError)
            );
        });

        return dialog;
    }

    @Override
    public void onCancel(@NonNull android.content.DialogInterface dialog) {
        super.onCancel(dialog);

        if (listenerCancelar != null) {
            listenerCancelar.onCancelar();
        }
    }
}
