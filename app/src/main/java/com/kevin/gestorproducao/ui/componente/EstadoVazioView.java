package com.kevin.gestorproducao.ui.componente;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.kevin.gestorproducao.R;

/**
 * Estado vazio padrão das listas: ícone em destaque, título e mensagem orientando o próximo passo.
 * Configurável via XML (app:icone, app:titulo, app:mensagem) ou pelos setters.
 */
public class EstadoVazioView extends LinearLayout {
    private static final long DURACAO_ENTRADA_MS = 180;

    private final ImageView icone;
    private final TextView titulo;
    private final TextView mensagem;

    public EstadoVazioView(@NonNull Context context) {
        this(context, null);
    }

    public EstadoVazioView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.componente_estado_vazio, this, true);

        icone = findViewById(R.id.iconeEstadoVazio);
        titulo = findViewById(R.id.txtTituloEstadoVazio);
        mensagem = findViewById(R.id.txtMensagemEstadoVazio);

        // TypedArray só é AutoCloseable a partir da API 31; minSdk é 24.
        TypedArray atributos = context.obtainStyledAttributes(attrs, R.styleable.EstadoVazioView);
        try {
            int iconeRes = atributos.getResourceId(R.styleable.EstadoVazioView_icone, 0);
            if (iconeRes != 0) icone.setImageResource(iconeRes);
            titulo.setText(atributos.getText(R.styleable.EstadoVazioView_titulo));
            setMensagem(atributos.getText(R.styleable.EstadoVazioView_mensagem));
        } finally {
            atributos.recycle();
        }
    }

    public void setIcone(@DrawableRes int iconeRes) {
        icone.setImageResource(iconeRes);
    }

    public void setTitulo(@StringRes int tituloRes) {
        titulo.setText(tituloRes);
    }

    public void setMensagem(@StringRes int mensagemRes) {
        setMensagem(getContext().getText(mensagemRes));
    }

    public void setMensagem(@Nullable CharSequence texto) {
        mensagem.setText(texto);
        mensagem.setVisibility(texto == null || texto.length() == 0 ? GONE : VISIBLE);
    }

    @Override
    public void setVisibility(int visibility) {
        boolean surgindo = visibility == VISIBLE && getVisibility() != VISIBLE;
        super.setVisibility(visibility);
        if (surgindo) {
            setAlpha(0f);
            setTranslationY(getResources().getDisplayMetrics().density * 8);
            animate().alpha(1f).translationY(0f).setDuration(DURACAO_ENTRADA_MS).start();
        }
    }
}
