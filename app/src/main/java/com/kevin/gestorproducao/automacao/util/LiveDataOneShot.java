package com.kevin.gestorproducao.automacao.util;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.function.Consumer;

// As chamadas dos Repository existentes (TrabalhoProducaoRepository, PersonagemRepository, ...)
// devolvem LiveData, que só pode ser observado a partir da main thread — mas o orquestrador da
// automação roda em sua própria HandlerThread. Esta classe faz a ponte: posta a observação na
// main thread e repassa o primeiro valor não nulo de volta pelo callback, removendo o observer
// (mesmo padrão "uma vez só" que MediatorLiveData.addSource/removeSource já usa nos ViewModels).
public final class LiveDataOneShot {
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private LiveDataOneShot() {}

    public static <T> void observar(LiveData<T> liveData, Consumer<T> callback) {
        MAIN_HANDLER.post(() -> {
            Observer<T>[] referencia = new Observer[1];

            referencia[0] = valor -> {
                if (valor == null) return;

                liveData.removeObserver(referencia[0]);
                callback.accept(valor);
            };

            liveData.observeForever(referencia[0]);
        });
    }
}
