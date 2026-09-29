package com.kevin.gestorproducao.ui.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.ui.viewModel.ComponentesVisuais;
import com.kevin.gestorproducao.ui.viewModel.EstadoAppViewModel;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public abstract class BaseFragment<T extends ViewBinding> extends Fragment {
    protected T binding;
    private EstadoAppViewModel estadoAppViewModel;
    private MaterialCardView cardStatusConexao;

    private ConnectivityManager connectivityManager;

    private ConnectivityManager.NetworkCallback networkCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflateBinding(inflater, container);
        estadoAppViewModel = new ViewModelProvider(
            requireActivity()
        ).get(EstadoAppViewModel.class);

        aplicarComponentesVisuais();

        cardStatusConexao = binding.getRoot().findViewById(R.id.cardStatusConexao);

        configurarMonitorConexao();

        return binding.getRoot();
    }

    private void configurarMonitorConexao() {
        connectivityManager = (ConnectivityManager) requireContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return;
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(@NonNull Network network) {

                requireActivity().runOnUiThread(() ->
                    esconderBarraSemInternet()
                );
            }

            @Override
            public void onLost(@NonNull Network network) {

                requireActivity().runOnUiThread(() -> {

                    if (estaSemInternet()) {
                        mostrarBarraSemInternet();
                    }
                });
            }
        };

        NetworkRequest request = new NetworkRequest.Builder().build();

        connectivityManager.registerNetworkCallback(
            request,
            networkCallback
        );

        if (estaSemInternet()) {
            mostrarBarraSemInternet();
        }
    }

    private void mostrarBarraSemInternet() {
        if (cardStatusConexao == null || cardStatusConexao.getVisibility() == View.VISIBLE) {
            return;
        }

        cardStatusConexao.setTranslationY(-cardStatusConexao.getHeight());

        cardStatusConexao.setVisibility(View.VISIBLE);

        cardStatusConexao.animate()
            .translationY(0)
            .setDuration(250)
            .setInterpolator(
                new AccelerateDecelerateInterpolator()
            )
            .start();
    }

    private void esconderBarraSemInternet() {
        if (cardStatusConexao == null || cardStatusConexao.getVisibility() != View.VISIBLE) {
            return;
        }

        cardStatusConexao.animate()
            .translationY(-cardStatusConexao.getHeight())
            .setDuration(250)
            .withEndAction(() ->
                cardStatusConexao.setVisibility(View.GONE)
            )
            .start();
    }

    protected abstract T inflateBinding(LayoutInflater inflater, ViewGroup container);

    protected ComponentesVisuais fornecerComponentesVisuais() {
        return new ComponentesVisuais(
            false,
            false,
            false,
            false,
            false,
            false,
            null,
            false
        );
    }

    protected void aplicarComponentesVisuais() {
        estadoAppViewModel.componentes.setValue(fornecerComponentesVisuais());
    }

    protected void mostraMensagemAncorada(String mensagem) {
        if (binding == null || getContext() == null) return;

        Snackbar snackbar = Snackbar.make(
            binding.getRoot(),
            mensagem,
            Snackbar.LENGTH_LONG
        );

        snackbar.show();
    }

    protected void configurarHideOnScroll(
        RecyclerView recyclerView,
        EstadoAppViewModel estadoAppViewModel
    ) {

        final boolean[] menuVisivel = {true};

        recyclerView.addOnScrollListener(
            new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(
                    @NonNull RecyclerView rv,
                    int dx,
                    int dy
                ) {
                super.onScrolled(rv, dx, dy);

                ComponentesVisuais atual = estadoAppViewModel.componentes.getValue();

                if (atual == null) return;

                if (dy > 10 && menuVisivel[0]) {

                    menuVisivel[0] = false;

                    estadoAppViewModel.componentes.setValue(
                        new ComponentesVisuais(
                            atual.appBar,
                            atual.menuNavegacaoLateral,
                            atual.itemMenuBusca,
                            atual.itemMenuConfirma,
                            false,
                            atual.selecaoPersonagem,
                            atual.titulo,
                            atual.itemMenuEdita
                        )
                    );

                } else if (dy < -10 && !menuVisivel[0]) {

                    menuVisivel[0] = true;

                    estadoAppViewModel.componentes.setValue(
                        new ComponentesVisuais(
                            atual.appBar,
                            atual.menuNavegacaoLateral,
                            atual.itemMenuBusca,
                            atual.itemMenuConfirma,
                            true,
                            atual.selecaoPersonagem,
                            atual.titulo,
                            atual.itemMenuEdita
                        )
                    );
                }
                }
            }
        );
    }

    protected void configurarMascaraMilhar(TextInputEditText editText) {

        editText.addTextChangedListener(new TextWatcher() {

            private boolean editando;

            @Override
            public void beforeTextChanged(
                CharSequence s,
                int start,
                int count,
                int after
            ) {}

            @Override
            public void onTextChanged(
                CharSequence s,
                int start,
                int before,
                int count
            ) {}

            public void afterTextChanged(Editable s) {

                if (editando) return;

                editando = true;

                try {

                    String valorLimpo = s.toString()
                        .replace(".", "")
                        .replace(",", "");

                    if (!valorLimpo.isEmpty()) {

                        long valor = Long.parseLong(valorLimpo);

                        NumberFormat formatador = NumberFormat.getInstance(
                            new Locale("pt", "BR")
                        );

                        String valorFormatado = formatador.format(valor);

                        editText.setText(valorFormatado);
                        editText.setSelection(valorFormatado.length());
                    }

                } catch (Exception ignored) {}

                editando = false;
            }
        });
    }

    protected int obterValorNumerico(TextInputEditText editText) {

        String valor = Objects.requireNonNull(
            editText.getText()
        ).toString();

        valor = valor.replace(".", "");

        if (valor.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(valor);
    }

    protected boolean validarConexao() {

        if (estaSemInternet()) {
            mostraMensagemAncorada(getString(R.string.stringErroConexao));
            return false;
        }

        return true;
    }

    private boolean estaSemInternet() {
        if (connectivityManager == null) {
            return true;
        }

        Network network = connectivityManager.getActiveNetwork();

        if (network == null) {
            return true;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

        return capabilities == null ||
            !(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    protected void iniciarLoadingBotao(
        MaterialButton botao,
        LinearLayout loadingDots
    ) {

        // Chamadas repetidas não podem sobrescrever o texto guardado com "".
        if (getContext() == null || botao.getTag(R.id.tagTextoOriginalBotao) != null) return;

        botao.setTag(R.id.tagTextoOriginalBotao, botao.getText());
        botao.setTag(R.id.tagIconeOriginalBotao, botao.getIcon());
        botao.setTag(R.id.tagLarguraMinimaOriginalBotao, botao.getMinWidth());

        // Pontos na cor do texto do botão: brancos no preenchido, vermelhos no destrutivo.
        ColorStateList corPontos = ColorStateList.valueOf(botao.getCurrentTextColor());

        // Sem texto e ícone um botão wrap_content encolheria; mantém a largura atual.
        botao.setMinWidth(botao.getWidth());
        botao.setEnabled(false);
        botao.setText("");
        botao.setIcon(null);

        loadingDots.setVisibility(View.VISIBLE);

        View dot1 = loadingDots.findViewById(R.id.dot1);
        View dot2 = loadingDots.findViewById(R.id.dot2);
        View dot3 = loadingDots.findViewById(R.id.dot3);
        dot1.setBackgroundTintList(corPontos);
        dot2.setBackgroundTintList(corPontos);
        dot3.setBackgroundTintList(corPontos);

        Animation anim1 = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.dot_wave
        );

        Animation anim2 = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.dot_wave
        );

        Animation anim3 = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.dot_wave
        );

        anim2.setStartOffset(150);
        anim3.setStartOffset(300);

        dot1.startAnimation(anim1);
        dot2.startAnimation(anim2);
        dot3.startAnimation(anim3);
    }

    protected void pararLoadingBotao(
        MaterialButton botao,
        LinearLayout loadingDots
    ) {

        botao.setEnabled(true);

        Object textoOriginal = botao.getTag(R.id.tagTextoOriginalBotao);
        if (textoOriginal == null) return;

        botao.setText((CharSequence) textoOriginal);
        botao.setIcon((Drawable) botao.getTag(R.id.tagIconeOriginalBotao));
        botao.setMinWidth((Integer) botao.getTag(R.id.tagLarguraMinimaOriginalBotao));
        botao.setTag(R.id.tagTextoOriginalBotao, null);
        botao.setTag(R.id.tagIconeOriginalBotao, null);
        botao.setTag(R.id.tagLarguraMinimaOriginalBotao, null);

        View dot1 = loadingDots.findViewById(R.id.dot1);
        View dot2 = loadingDots.findViewById(R.id.dot2);
        View dot3 = loadingDots.findViewById(R.id.dot3);

        dot1.clearAnimation();
        dot2.clearAnimation();
        dot3.clearAnimation();

        loadingDots.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

        binding = null;
    }
}
