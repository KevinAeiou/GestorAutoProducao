package com.kevin.gestorproducao.service;

import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PARA_PRODUZIR;

import com.kevin.gestorproducao.model.TrabalhoProducao;

public class ProducaoFluxoService {
    private final ConsumoMateriaisService consumoMateriaisService;
    private final ProducaoEstoqueService producaoEstoqueService;
    private final ProfissaoPersonagemService profissaoPersonagemService;
    private final PlanejamentoProducaoService planejamentoProducaoService;

    public ProducaoFluxoService(
        ConsumoMateriaisService consumoMateriaisService,
        ProducaoEstoqueService producaoEstoqueService,
        ProfissaoPersonagemService profissaoPersonagemService,
        PlanejamentoProducaoService planejamentoProducaoService
    ) {
        this.consumoMateriaisService = consumoMateriaisService;
        this.producaoEstoqueService = producaoEstoqueService;
        this.profissaoPersonagemService = profissaoPersonagemService;
        this.planejamentoProducaoService = planejamentoProducaoService;
    }

    public void processarPosModificacao(
        TrabalhoProducao trabalho,
        int estadoAnterior
    ) {
        if (trabalho.ehProduzindo() && estadoAnterior == CODIGO_TRABALHO_PARA_PRODUZIR) {
            if (trabalho.ehComum() && !trabalho.ehProducaoDeRecursos()) {
                consumoMateriaisService.consumirMateriais(trabalho);
                return;
            }

            if (trabalho.ehMelhorado() || trabalho.ehRaro()) {
                consumoMateriaisService.consumirTrabalhosNecessarios(trabalho);
                return;
            }

            return;
        }

        if (trabalho.ehFeito()) {
            producaoEstoqueService.adicionarAoEstoque(trabalho);
            profissaoPersonagemService.incrementarExperiencia(
                trabalho,
                estadoAnterior
            );

            if (trabalho.ehMelhorado()) {
                planejamentoProducaoService.incluirRaro(trabalho.getIdTrabalho());
            }
        }
    }
}
