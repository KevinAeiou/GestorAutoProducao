package com.kevin.gestorproducao;

import android.app.Application;

import com.kevin.gestorproducao.utilitario.TemaUtil;

public class GestorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TemaUtil.aplicaTemaSalvo(this);
    }
}
