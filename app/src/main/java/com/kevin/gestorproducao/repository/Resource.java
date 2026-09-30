package com.kevin.gestorproducao.repository;

public class Resource<T> {

    T dado;
    String erro;
    public Resource(T dado, String erro) {
        this.dado = dado;
        this.erro = erro;
    }
    public T getDado() {
        return dado;
    }

    public String getErro() {
        return erro;
    }

}
