package com.kevin.gestorproducao.repository.helper;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.kevin.gestorproducao.repository.Resource;

import java.util.function.Consumer;

public class FirebaseTimeoutHelper {

    private static final long TIMEOUT_MS = 10000;

    public static MutableLiveData<Resource<Void>> execute(
        Consumer<Callback> operation
    ) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        Handler handler = new Handler(Looper.getMainLooper());

        Runnable timeoutRunnable = () -> liveData.postValue(
            new Resource<>(
                null,
                "A operação demorou muito. Tente novamente."
            )
        );

        handler.postDelayed(timeoutRunnable, TIMEOUT_MS);

        operation.accept(new Callback() {
            @Override
            public void sucesso() {
                handler.removeCallbacks(timeoutRunnable);
                liveData.postValue(
                    new Resource<>(null, null)
                );
            }

            @Override
            public void erro(String mensagem) {
                handler.removeCallbacks(timeoutRunnable);
                liveData.postValue(
                    new Resource<>(null, mensagem)
                );
            }
        });

        return liveData;
    }

    public interface Callback {
        void sucesso();
        void erro(String mensagem);
    }
}