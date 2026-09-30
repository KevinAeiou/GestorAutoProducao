package com.kevin.gestorproducao.service;

import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_FEITO;
import static com.kevin.gestorproducao.ui.activity.Constantes.CODIGO_TRABALHO_PRODUZINDO;

import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;

public class ProfissaoPersonagemService {
    private final String idPersonagem;
    private final ProfissaoPersonagemRepository profissaoPersonagemRepo;

    public ProfissaoPersonagemService(
        String idPersonagem,
        ProfissaoPersonagemRepository profissaoPersonagemRepo
    ) {
        this.idPersonagem = idPersonagem;
        this.profissaoPersonagemRepo = profissaoPersonagemRepo;
    }

    public void incrementarExperiencia(TrabalhoProducao trabalho, int estadoAnterior) {
        if (trabalho.getEstado() == CODIGO_TRABALHO_FEITO && estadoAnterior == CODIGO_TRABALHO_PRODUZINDO) {
            String nomeProfissao = trabalho.getProfissao();

            ProfissaoPersonagem profissao = profissaoPersonagemRepo.recuperaProfissaoPorNome(
                idPersonagem,
                nomeProfissao
            );

            if (profissao != null) {
                int experienciaAtual = profissao.getExperiencia();
                int experienciaGanha = trabalho.getExperiencia();
                profissao.setExperiencia(experienciaAtual + experienciaGanha);

                profissaoPersonagemRepo.modificaProfissaoPersonagem(profissao, idPersonagem);
            }
        }
    }
}
