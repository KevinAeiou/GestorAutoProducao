package com.kevin.gestorproducao.automacao.ocr;

import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.kevin.gestorproducao.automacao.motor.Retangulo;
import com.kevin.gestorproducao.automacao.motor.TextoDetectado;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

// Isola o ML Kit do resto da automação: NavegadorJogo/DetectorEstadoTela só conhecem
// TextoDetectado, então o motor de OCR pode ser trocado sem tocar na lógica de navegação.
public class LeitorTextoTela {
    private final TextRecognizer reconhecedor = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    );
    private final Executor executor;

    // Sem um Executor explícito, o ML Kit entrega o resultado na main thread — o que tiraria o
    // restante do passo da automação (leitura do texto, decisão de navegação) da HandlerThread
    // dedicada do orquestrador. Passar o Executor da própria HandlerThread mantém tudo no
    // mesmo fio, como pedido no plano (evitar ANRs durante captura/OCR).
    public LeitorTextoTela(Executor executor) {
        this.executor = executor;
    }

    public void reconhecer(Bitmap frame, Consumer<TextoDetectado> callback) {
        InputImage imagem = InputImage.fromBitmap(frame, 0);

        reconhecedor.process(imagem)
            .addOnSuccessListener(executor, texto -> callback.accept(converter(texto)))
            .addOnFailureListener(executor, erro -> callback.accept(new TextoDetectado(new ArrayList<>())));
    }

    @NonNull
    private TextoDetectado converter(Text texto) {
        List<TextoDetectado.Linha> linhas = new ArrayList<>();

        for (Text.TextBlock bloco : texto.getTextBlocks()) {
            for (Text.Line linha : bloco.getLines()) {
                Rect caixa = linha.getBoundingBox();
                if (caixa == null) continue;

                linhas.add(new TextoDetectado.Linha(
                    linha.getText(),
                    new Retangulo(caixa.left, caixa.top, caixa.right, caixa.bottom)
                ));
            }
        }

        return new TextoDetectado(linhas);
    }

    public void fechar() {
        reconhecedor.close();
    }
}
