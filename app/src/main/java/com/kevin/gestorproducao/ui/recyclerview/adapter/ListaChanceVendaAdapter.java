package com.kevin.gestorproducao.ui.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.TrabalhoChanceVenda;
import com.kevin.gestorproducao.utilitario.Formatador;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListaChanceVendaAdapter
    extends BaseListAdapter<TrabalhoChanceVenda, ListaChanceVendaAdapter.ChanceVendaViewHolder>
{
    private final Context context;

    public ListaChanceVendaAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ChanceVendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context).inflate(
            R.layout.item_chance_venda,
            parent,
            false
        );
        return new ChanceVendaViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull ChanceVendaViewHolder holder, int posicao) {
        holder.vincula(getItem(posicao), posicao + 1);
    }

    @Override
    protected DiffUtil.Callback getDiffCallback(
        List<TrabalhoChanceVenda> antiga,
        List<TrabalhoChanceVenda> nova
    ) {
        return new DiffUtil.Callback() {
            @Override public int getOldListSize() { return antiga.size(); }
            @Override public int getNewListSize() { return nova.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return antiga.get(oldPos).getId().equals(nova.get(newPos).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                // A posição exibida no card (1º, 2º...) vem do índice na lista, não é campo do
                // modelo. Sem o "oldPos == newPos" aqui, um item cujo conteúdo não mudou mas que
                // subiu/desceu de posição (ex.: ao trocar o filtro) só recebe um "move" do
                // DiffUtil — a view é reaproveitada sem chamar onBindViewHolder de novo, e o
                // número antigo fica na tela.
                return oldPos == newPos && antiga.get(oldPos).equals(nova.get(newPos));
            }
        };
    }

    public class ChanceVendaViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemPosicao;
        private final TextView itemNome;
        private final TextView itemProfissao;
        private final TextView itemEstoque;
        private final TextView itemVendasPeriodo;
        private final TextView itemLabelScore;
        private final LinearProgressIndicator itemScore;
        private final View raridadeTarja;

        public ChanceVendaViewHolder(@NonNull View itemView) {
            super(itemView);
            itemPosicao = itemView.findViewById(R.id.itemPosicaoChanceVenda);
            itemNome = itemView.findViewById(R.id.itemNomeChanceVenda);
            itemProfissao = itemView.findViewById(R.id.itemProfissaoChanceVenda);
            itemEstoque = itemView.findViewById(R.id.itemEstoqueChanceVenda);
            itemVendasPeriodo = itemView.findViewById(R.id.itemVendasPeriodoChanceVenda);
            itemLabelScore = itemView.findViewById(R.id.itemLabelScoreChanceVenda);
            itemScore = itemView.findViewById(R.id.itemScoreChanceVenda);
            raridadeTarja = itemView.findViewById(R.id.itemRaridadeTarjaChanceVenda);
        }

        public void vincula(TrabalhoChanceVenda trabalho, int posicao) {
            preencheCampos(trabalho, posicao);
        }

        private void preencheCampos(TrabalhoChanceVenda trabalho, int posicao) {
            configuraCorRaridade(trabalho);

            itemPosicao.setText(context.getString(R.string.string_posicao_ranking_valor, posicao));

            String nome = trabalho.getNome();
            if (nome == null) nome = context.getString(R.string.stringIndefinido);
            itemNome.setText(nome);
            itemProfissao.setText(trabalho.getProfissao());

            itemEstoque.setText(context.getString(
                R.string.string_estoque_quantidade_valor,
                Formatador.formatarMilhar(trabalho.getEstoqueAtual())
            ));

            itemVendasPeriodo.setText(formataVendasPeriodo(trabalho));

            int percentualScore = (int) Math.round(trabalho.getScore() * 100);
            itemLabelScore.setText(context.getString(
                R.string.string_chance_venda_percentual_valor,
                percentualScore
            ));
            itemScore.setProgress(percentualScore);
        }

        private String formataVendasPeriodo(TrabalhoChanceVenda trabalho) {
            String vendeu = context.getString(
                R.string.string_vendeu_no_periodo_valor,
                Formatador.formatarMilhar(trabalho.getQuantidadeVendidaPeriodo())
            );

            if (trabalho.nuncaVendidoNoPeriodo()) {
                return vendeu + " · " + context.getString(R.string.string_nunca_vendido_no_periodo);
            }

            long dias = TimeUnit.MILLISECONDS.toDays(
                System.currentTimeMillis() - trabalho.getUltimaVendaEm()
            );

            return vendeu + " · " + context.getString(R.string.string_ultima_venda_dias_valor, dias);
        }

        // Mesma paleta/lógica de ListaTrabalhosVendidosAdapter — cor_producao_raridade_* aqui,
        // não Formatador.corPorRaridade (cor_texto_raridade_*): essa paleta é pensada para texto
        // sobre fundo escuro, e "comum" nela é quase branco (#F5F5F5) — usada como cor de fundo
        // da tarja, ficava praticamente invisível sobre o card claro.
        private void configuraCorRaridade(TrabalhoChanceVenda trabalho) {
            String raridade = trabalho.getRaridade();
            int corRaridade;
            int corTarja;
            if ("Melhorado".equals(raridade)) {
                corRaridade = ContextCompat.getColor(context, R.color.cor_producao_raridade_melhorado);
                corTarja = corRaridade;
            } else if ("Raro".equals(raridade)) {
                corRaridade = ContextCompat.getColor(context, R.color.cor_producao_raridade_raro);
                corTarja = corRaridade;
            } else if ("Especial".equals(raridade)) {
                corRaridade = ContextCompat.getColor(context, R.color.cor_producao_raridade_especial);
                corTarja = corRaridade;
            } else {
                corRaridade = MaterialColors.getColor(itemNome, com.google.android.material.R.attr.colorOnSurface);
                corTarja = MaterialColors.getColor(raridadeTarja, com.google.android.material.R.attr.colorOutlineVariant);
            }
            itemNome.setTextColor(corRaridade);
            raridadeTarja.setBackgroundColor(corTarja);
        }
    }
}
