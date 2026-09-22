package com.kevin.gestorproducao.repository;

// Embrulha um valor de LiveData que deve ser tratado só uma vez (ex.: disparar uma navegação).
// Sem isso, um MediatorLiveData de escopo de activity reentrega o último valor para qualquer
// observer novo que se inscreva — e um Fragment volta a se inscrever toda vez que sua view é
// recriada (inclusive ao voltar de outra tela pela pilha de navegação), refazendo a ação.
public class Evento<T> {
    private final T conteudo;
    private boolean consumido;

    public Evento(T conteudo) {
        this.conteudo = conteudo;
    }

    public T consome() {
        if (consumido) return null;

        consumido = true;
        return conteudo;
    }
}
