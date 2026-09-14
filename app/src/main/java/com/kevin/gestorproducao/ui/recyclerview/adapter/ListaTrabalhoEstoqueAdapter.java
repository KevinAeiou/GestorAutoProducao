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
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.TrabalhoEstoque;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalhoEstoque;
import com.kevin.gestorproducao.utilitario.Formatador;

import java.util.List;

public class ListaTrabalhoEstoqueAdapter
    extends BaseListAdapter<TrabalhoEstoque, ListaTrabalhoEstoqueAdapter.TrabalhoEstoqueViewHolder>
{
    private final Context context;
    private OnItemClickListenerTrabalhoEstoque onItemClickListener;

    public ListaTrabalhoEstoqueAdapter(Context context) {
        this.context = context;
    }
    public void setOnItemClickListener(OnItemClickListenerTrabalhoEstoque onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    @NonNull
    @Override
    public TrabalhoEstoqueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context).inflate(
            R.layout.item_trabalho_estoque,
            parent,
            false
        );
        return new TrabalhoEstoqueViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabalhoEstoqueViewHolder holder, int position) {
        holder.vincula(getItem(position));
    }

    @Override
    protected DiffUtil.Callback getDiffCallback(List<TrabalhoEstoque> antiga, List<TrabalhoEstoque> nova) {
        return new DiffUtil.Callback() {
            @Override public int getOldListSize() { return antiga.size(); }
            @Override public int getNewListSize() { return nova.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return antiga.get(oldPos).getId().equals(nova.get(newPos).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return antiga.get(oldPos).equals(nova.get(newPos));
            }
        };
    }

    public class TrabalhoEstoqueViewHolder extends RecyclerView.ViewHolder{
        private final TextView nomeTrabalho;
        private final TextView profissaoTrabalho;
        private final TextView quantidadeTrabalho;
        private final TextView nivelTrabalho;
        private final View raridadeTarja;
        private TrabalhoEstoque trabalho;

        public TrabalhoEstoqueViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeTrabalho = itemView.findViewById(R.id.itemNomeTrabalhoEstoque);
            profissaoTrabalho = itemView.findViewById(R.id.itemProfissaoTrabalhoEstoque);
            quantidadeTrabalho = itemView.findViewById(R.id.itemQuantidadeTrabalhoEstoque);
            nivelTrabalho = itemView.findViewById(R.id.itemNivelTrabalhoEstoque);
            raridadeTarja = itemView.findViewById(R.id.itemRaridadeTarjaEstoque);
            itemView.setOnClickListener(v ->
                onItemClickListener.onItemClick(trabalho, getBindingAdapterPosition())
            );
        }
        public void vincula(TrabalhoEstoque trabalho){
            this.trabalho = trabalho;
            preencheCampos(trabalho);
        }
        private void preencheCampos(TrabalhoEstoque trabalho) {
            configuraCorRaridade(trabalho);

            nomeTrabalho.setText(trabalho.getNome());
            profissaoTrabalho.setText(trabalho.getProfissao());
            String quantidade = context.getString(
                R.string.stringQuantidadeUndidade,
                Formatador.formatarMilhar(trabalho.getQuantidade())
            );
            quantidadeTrabalho.setText(quantidade);
            nivelTrabalho.setText(context.getString(R.string.stringNivelBadge, trabalho.getNivel()));
        }
        private void configuraCorRaridade(TrabalhoEstoque trabalho) {
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
                corRaridade = MaterialColors.getColor(nomeTrabalho, com.google.android.material.R.attr.colorOnSurface);
                corTarja = MaterialColors.getColor(raridadeTarja, com.google.android.material.R.attr.colorOutlineVariant);
            }
            nomeTrabalho.setTextColor(corRaridade);
            raridadeTarja.setBackgroundColor(corTarja);
        }
    }
}
