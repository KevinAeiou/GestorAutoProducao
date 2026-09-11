package com.kevin.gestorproducao.service;

import android.content.Context;

import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;

// Achado M1: este bloco de 5 services era montado do zero, idêntico, dentro do observer de
// personagem selecionado de 3 fragments (ConfirmaProducaoFragment, DetalhesProducaoFragment,
// ListaTrabalhosProducaoFragment) — o personagem só é conhecido em tempo de execução, então a
// montagem não cabe no ViewModelFactory (que só tem o Context). Centralizado aqui em vez disso.
public final class ProducaoServicosFactory {
    private ProducaoServicosFactory() {}

    public static ServicosProducaoPersonagem cria(
        TrabalhoRepository trabalhoRepo,
        TrabalhoEstoqueRepository estoqueRepo,
        TrabalhoProducaoRepository producaoRepo,
        ProfissaoPersonagemRepository profissaoPersonagemRepo,
        String idPersonagem,
        Context context
    ) {
        ConsumoMateriaisService consumoMateriaisService = new ConsumoMateriaisService(
            trabalhoRepo,
            estoqueRepo,
            idPersonagem,
            context
        );

        ProducaoEstoqueService producaoEstoqueService = new ProducaoEstoqueService(
            trabalhoRepo,
            estoqueRepo,
            idPersonagem,
            context
        );

        ProfissaoPersonagemService profissaoPersonagemService = new ProfissaoPersonagemService(
            idPersonagem,
            profissaoPersonagemRepo
        );

        PlanejamentoProducaoService planejamentoProducaoService = new PlanejamentoProducaoService(
            trabalhoRepo,
            estoqueRepo,
            producaoRepo,
            profissaoPersonagemRepo,
            idPersonagem,
            context
        );

        ProducaoFluxoService producaoFluxoService = new ProducaoFluxoService(
            consumoMateriaisService,
            producaoEstoqueService,
            profissaoPersonagemService,
            planejamentoProducaoService
        );

        return new ServicosProducaoPersonagem(planejamentoProducaoService, producaoFluxoService);
    }
}
