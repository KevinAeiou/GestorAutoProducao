package com.kevin.gestorproducao.service;

import android.content.Context;

import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;

import java.util.List;
import java.util.Map;

public class ConsumoMateriaisService {
    private final TrabalhoRepository trabalhoRepository;
    private final TrabalhoEstoqueRepository estoqueRepository;
    private final String idPersonagem;
    private final Context context;

    public ConsumoMateriaisService(
        TrabalhoRepository trabalhoRepository,
        TrabalhoEstoqueRepository estoqueRepository,
        String idPersonagem,
        Context context
    ) {
        this.trabalhoRepository = trabalhoRepository;
        this.estoqueRepository = estoqueRepository;
        this.idPersonagem = idPersonagem;
        this.context = context;
    }

    public void consumirMateriais(TrabalhoProducao trabalho) {
        Map<String, Integer> necessarios = trabalho.getMapMateriaisNecessarios(context);

        int index = 0;
        for (Map.Entry<String, Integer> material: necessarios.entrySet()) {
            Trabalho encontrado = trabalhoRepository.recuperaTrabalhoPorNome(material.getKey());

            if (encontrado == null) continue;

            if(encontrado.getNivel() == 1 && trabalho.getNivel() > 14) continue;
            if(encontrado.getNivel() == 8 && trabalho.getNivel() < 16) continue;

            TrabalhoEstoque trabalhoEstoque = estoqueRepository.recuperaTrabalhoPorId(
                idPersonagem,
                encontrado.getId()
            );

            if (trabalhoEstoque == null) continue;

            int quantidade = material.getValue();

            if (index == 0 || index == 3) {
                quantidade += 2;
            } else if (index == 1 || index == 4) {
                quantidade += 1;
            }

            trabalhoEstoque.decrementaQuantidade(quantidade);
            estoqueRepository.modificaEstoque(trabalhoEstoque, idPersonagem);

            index++;
        }
    }

    public void consumirTrabalhosNecessarios(TrabalhoProducao trabalho) {
        Trabalho encontrado = trabalhoRepository.recuperaTrabalhoPorId(trabalho.getIdTrabalho());

        if (encontrado == null) return;

        List<String> necessarios = encontrado.getListaTrabalhosNecessarios();

        for (String idTrabalho: necessarios) {
            TrabalhoEstoque trabalhoEstoque = estoqueRepository.recuperaTrabalhoPorId(
                idPersonagem,
                idTrabalho
            );

            if (trabalhoEstoque == null) continue;

            trabalhoEstoque.decrementaQuantidade(1);
            estoqueRepository.modificaEstoque(trabalhoEstoque, idPersonagem);
        }
    }
}
