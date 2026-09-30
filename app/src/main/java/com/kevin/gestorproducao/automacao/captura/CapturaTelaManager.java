package com.kevin.gestorproducao.automacao.captura;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class CapturaTelaManager {
    private final MediaProjection mediaProjection;
    private final Handler handler;
    private final int largura;
    private final int altura;
    private final int densidadeDpi;
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;

    public CapturaTelaManager(Context context, MediaProjection mediaProjection, Handler handler) {
        this.mediaProjection = mediaProjection;
        this.handler = handler;

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(metrics);

        this.largura = metrics.widthPixels;
        this.altura = metrics.heightPixels;
        this.densidadeDpi = metrics.densityDpi;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }

    public void iniciar() {
        imageReader = ImageReader.newInstance(largura, altura, PixelFormat.RGBA_8888, 2);

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "GestorAutoProducaoCaptura",
            largura,
            altura,
            densidadeDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.getSurface(),
            null,
            handler
        );
    }

    public void capturarFrame(Consumer<Bitmap> callback) {
        if (imageReader == null) {
            callback.accept(null);
            return;
        }

        Image image = imageReader.acquireLatestImage();

        if (image == null) {
            callback.accept(null);
            return;
        }

        try {
            callback.accept(converterParaBitmap(image));
        } finally {
            image.close();
        }
    }

    @Nullable
    private Bitmap converterParaBitmap(Image image) {
        Image.Plane plano = image.getPlanes()[0];
        ByteBuffer buffer = plano.getBuffer();
        int pixelStride = plano.getPixelStride();
        int rowStride = plano.getRowStride();
        int folgaLinha = rowStride - pixelStride * largura;

        Bitmap bitmapComFolga = Bitmap.createBitmap(
            largura + folgaLinha / pixelStride,
            altura,
            Bitmap.Config.ARGB_8888
        );
        bitmapComFolga.copyPixelsFromBuffer(buffer);

        return Bitmap.createBitmap(bitmapComFolga, 0, 0, largura, altura);
    }

    public void encerrar() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }

        if (mediaProjection != null) {
            mediaProjection.stop();
        }
    }
}
