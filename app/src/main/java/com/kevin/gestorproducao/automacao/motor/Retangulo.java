package com.kevin.gestorproducao.automacao.motor;

public class Retangulo {
    public final int left;
    public final int top;
    public final int right;
    public final int bottom;

    public Retangulo(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public int centroX() {
        return (left + right) / 2;
    }

    public int centroY() {
        return (top + bottom) / 2;
    }
}
