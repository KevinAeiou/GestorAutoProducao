package com.kevin.gestorproducao.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.repository.Resource;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoVendaRepository;

public class VendaService {
    private final TrabalhoVendaRepository vendaRepository;
    private final TrabalhoEstoqueRepository estoqueRepository;

    public VendaService(
        TrabalhoVendaRepository vendaRepository,
        TrabalhoEstoqueRepository estoqueRepository
    ) {
        this.vendaRepository = vendaRepository;
        this.estoqueRepository = estoqueRepository;
    }

    public LiveData<Resource<Void>> realizarVenda(
        TrabalhoVendido trabalho,
        String idPersonagem
    ) {
        MediatorLiveData<Resource<Void>> resultado = new MediatorLiveData<>();

        LiveData<Resource<TrabalhoEstoque>> estoqueSource = estoqueRepository.recuperaTrabalhoEstoquePorIdTrabalho(
            trabalho.getIdTrabalho(),
            idPersonagem
        );

        resultado.addSource(estoqueSource, estoqueResult -> {
            resultado.removeSource(estoqueSource);

            if (estoqueResult.getErro() != null) {
                resultado.setValue(new Resource<>(null, estoqueResult.getErro()));
                return;
            }

            TrabalhoEstoque estoque = estoqueResult.getDado();

            int quantidadeAtual;

            if (estoque != null) {
                quantidadeAtual = estoque.getQuantidade();
                int quantidadeVenda = trabalho.getQuantidade();

                estoque.setQuantidade(quantidadeAtual - quantidadeVenda);

                LiveData<Resource<Void>> updateSource = estoqueRepository.modificaEstoque(
                    estoque,
                    idPersonagem
                );

                resultado.addSource(updateSource, updateResult -> {
                    resultado.removeSource(updateSource);

                    if (updateResult.getErro() != null) {
                        resultado.setValue(updateResult);
                    }

                });
            } else {
                quantidadeAtual = 0;
            }

            LiveData<Resource<Void>> vendaSource = vendaRepository.insereVenda(
                trabalho,
                idPersonagem
            );

            resultado.addSource(vendaSource, vendaResult -> {
                if (vendaResult.getErro() != null) {
                    if (estoque != null) {
                        estoque.setQuantidade(quantidadeAtual);

                        estoqueRepository.modificaEstoque(estoque, idPersonagem);
                    }

                    resultado.setValue(vendaResult);
                    return;
                }

                resultado.setValue(vendaResult);
            });
        });

        return resultado;
    }
}
