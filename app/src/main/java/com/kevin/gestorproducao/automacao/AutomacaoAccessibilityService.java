package com.kevin.gestorproducao.automacao;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

// Único mecanismo do Android para simular toques em outro app sem root. Só usa dispatchGesture —
// não lê a árvore de acessibilidade de nenhum app (accessibility config declara
// canRetrieveWindowContent="false"), então a detecção de tela fica inteiramente a cargo do OCR.
public class AutomacaoAccessibilityService extends AccessibilityService {
    private static final long DURACAO_TOQUE_MS = 80;

    private static volatile AutomacaoAccessibilityService instancia;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instancia = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    public boolean onUnbind(Intent intent) {
        instancia = null;
        return super.onUnbind(intent);
    }

    public static boolean estaAtiva() {
        return instancia != null;
    }

    // handler define em qual thread o callback roda — passar null faria o Android entregar o
    // resultado na main thread, tirando o passo seguinte da automação da HandlerThread dedicada
    // do orquestrador (quem chama deve passar o Handler dessa HandlerThread).
    public static void tocar(int x, int y, @NonNull Handler handler, @NonNull Consumer<Boolean> callback) {
        AutomacaoAccessibilityService servico = instancia;

        if (servico == null) {
            callback.accept(false);
            return;
        }

        Path caminho = new Path();
        caminho.moveTo(x, y);

        GestureDescription.StrokeDescription toque = new GestureDescription.StrokeDescription(
            caminho, 0, DURACAO_TOQUE_MS
        );

        GestureDescription gesto = new GestureDescription.Builder()
            .addStroke(toque)
            .build();

        servico.dispatchGesture(gesto, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                callback.accept(true);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                callback.accept(false);
            }
        }, handler);
    }
}
