package com.kevin.gestorproducao.service;

import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoProducaoRepository;
import com.kevin.gestorproducao.repository.TrabalhoVendaRepository;

public class TrabalhoService {
    private final TrabalhoEstoqueRepository estoqueRepository;
    private final TrabalhoVendaRepository vendaRepository;
    private final TrabalhoProducaoRepository producaoRepository;

    public TrabalhoService(
        TrabalhoVendaRepository vendaRepository,
        TrabalhoProducaoRepository producaoRepository,
        TrabalhoEstoqueRepository estoqueRepository
    ) {
        this.vendaRepository = vendaRepository;
        this.producaoRepository = producaoRepository;
        this.estoqueRepository = estoqueRepository;
    }

    public void processarPosRemocao(String idTrabalho) {
        estoqueRepository.removeReferenciaTrabalhoEspecifico(idTrabalho);
        vendaRepository.removeReferenciaTrabalhoEspecfico(idTrabalho);
        producaoRepository.removeReferenciaTrabalhoEspecifico(idTrabalho);
    }
}
