package com.kevin.gestorproducao.service;

import static com.kevin.gestorproducao.utilitario.Utilitario.comparaString;

import android.content.Context;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.Recurso;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.repository.TrabalhoEstoqueRepository;
import com.kevin.gestorproducao.repository.TrabalhoRepository;
import com.kevin.gestorproducao.rules.CatalogoRecursosRaros;

import java.util.Map;

public class ProducaoEstoqueService {
    private final TrabalhoRepository trabalhoRepository;
    private final TrabalhoEstoqueRepository estoqueRepository;
    private final String idPersonagem;
    private final Context context;

    public ProducaoEstoqueService(
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

    public void adicionarAoEstoque(TrabalhoProducao trabalhoSelecionado) {
        TrabalhoEstoque trabalhoEstoque = estoqueRepository.recuperaTrabalhoPorId(
            idPersonagem,
            trabalhoSelecionado.getIdTrabalho()
        );

        if (trabalhoSelecionado.ehProducaoDeRecursos()) {
            if (trabalhoSelecionado.ehComum()) {
                if (trabalhoEstoque == null) {
                    TrabalhoEstoque novoTrabalho = new TrabalhoEstoque();
                    novoTrabalho.setIdTrabalho(trabalhoSelecionado.getIdTrabalho());

                    novoTrabalho.setRaridade(trabalhoSelecionado.getRaridade());
                    novoTrabalho.setNome(trabalhoSelecionado.getNome());
                    novoTrabalho.setNomeProducao(trabalhoSelecionado.getNomeProducao());
                    novoTrabalho.setProfissao(trabalhoSelecionado.getProfissao());
                    novoTrabalho.incrementaQuantidade();

                    if (comparaString(
                        trabalhoSelecionado.getTipoLicenca(),
                        context.getString(R.string.licencaAprendiz))
                    ) {
                        novoTrabalho.setQuantidade(novoTrabalho.getQuantidade() * 2);
                    }

                    estoqueRepository.insereEstoque(novoTrabalho, idPersonagem);
                    return;
                }

                int qtdAtual = trabalhoEstoque.getQuantidade();

                trabalhoEstoque.setRaridade(trabalhoSelecionado.getRaridade());
                trabalhoEstoque.setNome(trabalhoSelecionado.getNome());
                trabalhoEstoque.setNomeProducao(trabalhoSelecionado.getNomeProducao());
                trabalhoEstoque.setProfissao(trabalhoSelecionado.getProfissao());
                trabalhoEstoque.incrementaQuantidade();
                if (comparaString(
                    trabalhoSelecionado.getTipoLicenca(),
                    context.getString(R.string.licencaAprendiz))
                ) {
                    trabalhoEstoque.setQuantidade(
                        (trabalhoEstoque.getQuantidade() * 2) + qtdAtual
                    );
                } else {
                    trabalhoEstoque.setQuantidade(
                        trabalhoEstoque.getQuantidade() + qtdAtual
                    );
                }

                estoqueRepository.modificaEstoque(trabalhoEstoque, idPersonagem);
                return;
            }

            if (trabalhoSelecionado.ehRaro()) {
                Map<Recurso, Integer> recursosProduzidos = CatalogoRecursosRaros.getRecursos(
                    trabalhoSelecionado.getProfissao(),
                    trabalhoSelecionado.getNome()
                );

                for (Map.Entry<Recurso, Integer> entry: recursosProduzidos.entrySet()) {
                    Recurso recurso = entry.getKey();
                    int quantidade = entry.getValue();

                    Trabalho trabalhoEncontrado = trabalhoRepository.recuperaTrabalhoPorNome(
                        recurso.getKey()
                    );

                    if (trabalhoEncontrado == null) continue;

                    int quantidadeFinal = quantidade;

                    if (comparaString(
                        trabalhoSelecionado.getTipoLicenca(),
                        context.getString(R.string.licencaAprendiz)
                    )) {
                        quantidadeFinal *= 2;
                    }

                    TrabalhoEstoque existente = estoqueRepository.recuperaTrabalhoPorId(
                        idPersonagem,
                        trabalhoEncontrado.getId()
                    );

                    if (existente == null) {
                        TrabalhoEstoque novo = new TrabalhoEstoque();
                        novo.setIdTrabalho(trabalhoEncontrado.getId());
                        novo.setQuantidade(quantidadeFinal);

                        estoqueRepository.insereEstoque(novo, idPersonagem);
                        continue;
                    }

                    existente.setQuantidade(
                        existente.getQuantidade() + quantidadeFinal
                    );

                    estoqueRepository.modificaEstoque(existente, idPersonagem);
                }
            }

            return;
        }

        if (trabalhoEstoque == null) {
            TrabalhoEstoque novoTrabalho = new TrabalhoEstoque();
            novoTrabalho.setIdTrabalho(trabalhoSelecionado.getIdTrabalho());

            estoqueRepository.insereEstoque(novoTrabalho, idPersonagem);
            return;
        }

        trabalhoEstoque.incrementaQuantidade();
        estoqueRepository.modificaEstoque(trabalhoEstoque, idPersonagem);
    }
}
