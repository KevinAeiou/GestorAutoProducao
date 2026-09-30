package com.kevin.gestorproducao.ui.viewModel;

public class ComponentesVisuais {

    public final boolean appBar;
    public final boolean menuNavegacaoLateral;
    public final boolean itemMenuBusca;
    public final boolean itemMenuConfirma;
    public final boolean itemMenuEdita;
    public final boolean menuNavegacaoInferior;
    public final boolean selecaoPersonagem;
    public final String titulo;

    public ComponentesVisuais(
        boolean appBar,
        boolean menuNavegacaoLateral,
        boolean itemMenuBusca,
        boolean itemMenuConfirma,
        boolean menuNavegacaoInferior,
        boolean selecaoPersonagem,
        String titulo,
        boolean itemMenuEdita
    ) {
        this.appBar = appBar;
        this.menuNavegacaoLateral = menuNavegacaoLateral;
        this.itemMenuBusca = itemMenuBusca;
        this.itemMenuConfirma = itemMenuConfirma;
        this.menuNavegacaoInferior = menuNavegacaoInferior;
        this.selecaoPersonagem = selecaoPersonagem;
        this.itemMenuEdita = itemMenuEdita;
        this.titulo = titulo;
    }
}