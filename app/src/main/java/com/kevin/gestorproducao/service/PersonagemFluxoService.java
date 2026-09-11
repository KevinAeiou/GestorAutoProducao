package com.kevin.gestorproducao.service;

import com.kevin.gestorproducao.model.Personagem;
import com.kevin.gestorproducao.repository.PersonagemRepository;
import com.kevin.gestorproducao.repository.ProfissaoPersonagemRepository;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoVendaRepository;

public class PersonagemFluxoService {
    private final PersonagemRepository personagemRepository;
    private final ProfissaoPersonagemRepository profissaoPersonagemRepository;
    private final TrabalhoProducaoRepository producaoRepository;
    private final TrabalhoEstoqueRepository estoqueRepository;
    private final TrabalhoVendaRepository vendaRepository;

    public PersonagemFluxoService(
        ProfissaoPersonagemRepository profissaoPersonagemRepository,
        PersonagemRepository personagemRepository,
        TrabalhoProducaoRepository producaoRepository,
        TrabalhoEstoqueRepository estoqueRepository,
        TrabalhoVendaRepository vendaRepository
    ) {
        this.profissaoPersonagemRepository = profissaoPersonagemRepository;
        this.personagemRepository = personagemRepository;
        this.producaoRepository = producaoRepository;
        this.estoqueRepository = estoqueRepository;
        this.vendaRepository = vendaRepository;
    }

    public void processarPosInsercao(Personagem personagem) {
        personagemRepository.inserePersonagem(personagem);
        profissaoPersonagemRepository.insereProfissoesNovoPersonagem(personagem.getId());
    }

    public void processarPosRemocao(String idPersonagem) {
        // Chamado só após o Personagem em si já ter sido removido (ver ModificaPersonagemFragment):
        // o índice de posse em Usuarios2 precisa sair por último, pois as regras do Realtime
        // Database usam esse índice para autorizar a remoção do próprio Personagem.
        personagemRepository.removePersonagemUsuario(idPersonagem);
        producaoRepository.removeProducoes(idPersonagem);
        estoqueRepository.removeEstoque(idPersonagem);
        vendaRepository.removeVendas(idPersonagem);
        profissaoPersonagemRepository.removeProfissoesPersonagem(idPersonagem);
    }
}
