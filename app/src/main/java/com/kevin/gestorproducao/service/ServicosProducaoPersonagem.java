package com.kevin.gestorproducao.service;

public class ServicosProducaoPersonagem {
    private final PlanejamentoProducaoService planejamentoProducaoService;
    private final ProducaoFluxoService producaoFluxoService;

    public ServicosProducaoPersonagem(
        PlanejamentoProducaoService planejamentoProducaoService,
        ProducaoFluxoService producaoFluxoService
    ) {
        this.planejamentoProducaoService = planejamentoProducaoService;
        this.producaoFluxoService = producaoFluxoService;
    }

    public PlanejamentoProducaoService getPlanejamentoProducaoService() {
        return planejamentoProducaoService;
    }

    public ProducaoFluxoService getProducaoFluxoService() {
        return producaoFluxoService;
    }
}
