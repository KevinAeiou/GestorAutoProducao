package com.kevin.gestorproducao.automacao.motor;

import com.kevin.gestorproducao.utilitario.Utilitario;

import java.util.Collections;
import java.util.List;

// Representação, independente do motor de OCR usado, do texto lido em um frame da tela do
// jogo — mantém DetectorEstadoTela e NavegadorJogo testáveis sem depender do ML Kit.
public class TextoDetectado {
    private final List<Linha> linhas;

    public TextoDetectado(List<Linha> linhas) {
        this.linhas = linhas == null ? Collections.emptyList() : linhas;
    }

    public List<Linha> getLinhas() {
        return linhas;
    }

    public boolean contem(String alvo) {
        return encontra(alvo) != null;
    }

    public Linha encontra(String alvo) {
        for (Linha linha : linhas) {
            if (Utilitario.stringContemString(linha.getTexto(), alvo)) {
                return linha;
            }
        }

        return null;
    }

    public static class Linha {
        private final String texto;
        private final Retangulo posicao;

        public Linha(String texto, Retangulo posicao) {
            this.texto = texto;
            this.posicao = posicao;
        }

        public String getTexto() {
            return texto;
        }

        public Retangulo getPosicao() {
            return posicao;
        }
    }
}
