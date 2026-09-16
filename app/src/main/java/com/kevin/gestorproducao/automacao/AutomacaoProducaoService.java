package com.kevin.gestorproducao.automacao;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.automacao.captura.CapturaTelaManager;
import com.kevin.gestorproducao.ui.activity.MainActivity;

// Dono do MediaProjection e da HandlerThread onde o AutomacaoOrquestrador roda. O consentimento
// de gravação de tela (resultCode + resultData) não sobrevive à morte do processo, então este
// serviço não é sticky — se o Android o matar, a automação simplesmente para e precisa ser
// reiniciada manualmente pelo usuário (não há como retomar sem um novo consentimento).
public class AutomacaoProducaoService extends Service {
    public static final String ACAO_INICIAR = "com.kevin.gestorproducao.automacao.INICIAR";
    public static final String ACAO_PARAR = "com.kevin.gestorproducao.automacao.PARAR";
    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_DATA = "resultData";

    private static final String CANAL_ID = "automacao_producao";
    private static final int NOTIFICACAO_ID = 5501;

    private HandlerThread handlerThread;
    private Handler handler;
    private CapturaTelaManager capturaTelaManager;
    private AutomacaoOrquestrador orquestrador;
    private volatile boolean parando;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || ACAO_PARAR.equals(intent.getAction())) {
            pararServico();
            return START_NOT_STICKY;
        }

        criarCanalNotificacao();
        startForeground(NOTIFICACAO_ID, criarNotificacao(getString(R.string.stringAutomacaoIniciando)));

        int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        Intent resultData = obterResultData(intent);

        if (resultData == null) {
            AutomacaoStatus.publicar(new AutomacaoStatus.Evento(
                AutomacaoStatus.Fase.ERRO, null, null, getString(R.string.stringAutomacaoErroPermissaoTela)
            ));
            pararServico();
            return START_NOT_STICKY;
        }

        iniciarAutomacao(resultCode, resultData);
        return START_NOT_STICKY;
    }

    @Nullable
    private Intent obterResultData(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent.class);
        }

        return intent.getParcelableExtra(EXTRA_RESULT_DATA);
    }

    private void iniciarAutomacao(int resultCode, Intent resultData) {
        handlerThread = new HandlerThread("AutomacaoProducaoThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        MediaProjectionManager projectionManager =
            (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        MediaProjection mediaProjection = projectionManager.getMediaProjection(resultCode, resultData);

        handler.post(() -> {
            // Guarda contra o caso raro de "Parar" ser tocado entre o post e sua execução —
            // sem isso, a automação poderia começar a rodar depois do usuário já ter pedido
            // para parar, e o MediaProjection criado aqui nunca seria liberado.
            if (parando) return;

            capturaTelaManager = new CapturaTelaManager(this, mediaProjection, handler);
            capturaTelaManager.iniciar();

            orquestrador = new AutomacaoOrquestrador(this, handler, capturaTelaManager);
            orquestrador.iniciar(this::pararServico);
        });
    }

    private void pararServico() {
        if (parando) return;
        parando = true;

        if (orquestrador != null) {
            orquestrador.parar();
        }

        if (capturaTelaManager != null) {
            capturaTelaManager.encerrar();
        }

        if (handlerThread != null) {
            handlerThread.quitSafely();
        }

        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        pararServico();
        super.onDestroy();
    }

    private void criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = getSystemService(NotificationManager.class);
        NotificationChannel canal = new NotificationChannel(
            CANAL_ID,
            getString(R.string.stringAutomacao),
            NotificationManager.IMPORTANCE_LOW
        );
        manager.createNotificationChannel(canal);
    }

    private Notification criarNotificacao(String texto) {
        Intent intentParar = new Intent(this, AutomacaoProducaoService.class);
        intentParar.setAction(ACAO_PARAR);
        PendingIntent pendingParar = PendingIntent.getService(
            this, 0, intentParar, PendingIntent.FLAG_IMMUTABLE
        );

        Intent intentAbrirApp = new Intent(this, MainActivity.class);
        PendingIntent pendingAbrirApp = PendingIntent.getActivity(
            this, 0, intentAbrirApp, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CANAL_ID)
            .setContentTitle(getString(R.string.stringAutomacao))
            .setContentText(texto)
            .setSmallIcon(R.drawable.ic_producao)
            .setContentIntent(pendingAbrirApp)
            .addAction(0, getString(R.string.stringParar), pendingParar)
            .setOngoing(true)
            .build();
    }
}
