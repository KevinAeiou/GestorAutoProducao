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
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListener;

import java.util.List;

public class ListaTrabalhoEspecificoNovaProducaoAdapter
    extends BaseListAdapter<Trabalho, ListaTrabalhoEspecificoNovaProducaoAdapter.TrabalhoEspecificoNovaProducaoViewHolder>
{
    private final Context context;
    private OnItemClickListener onItemClickListener;

    public ListaTrabalhoEspecificoNovaProducaoAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TrabalhoEspecificoNovaProducaoViewHolder onCreateViewHolder(
        @NonNull ViewGroup parent,
        int viewType
    ) {
        View viewCriada = LayoutInflater.from(context).inflate(
            R.layout.item_trabalho_especifico,
            parent,
            false
        );
        return new TrabalhoEspecificoNovaProducaoViewHolder(viewCriada);
    }
    @Override
    public void onBindViewHolder(@NonNull TrabalhoEspecificoNovaProducaoViewHolder holder, int position) {
        holder.vincula(getItem(position));
    }

    @Override
    protected DiffUtil.Callback getDiffCallback(List<Trabalho> antiga, List<Trabalho> nova) {
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

    public class TrabalhoEspecificoNovaProducaoViewHolder extends RecyclerView.ViewHolder {
        private final TextView nomeTrabalhoEspecifico;
        private final TextView profissaoTrabalhoEspecifico;
        private final TextView nivelTrabalhoEspecifico;
        private final View raridadeTarja;
        private Trabalho trabalhoEspecifico;

        public TrabalhoEspecificoNovaProducaoViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeTrabalhoEspecifico = itemView.findViewById(R.id.itemNomeTrabaloEspecifico);
            profissaoTrabalhoEspecifico = itemView.findViewById(R.id.itemProfissaoTrabalhoEspecifico);
            nivelTrabalhoEspecifico = itemView.findViewById(R.id.itemNivelTrabaloEspecifico);
            raridadeTarja = itemView.findViewById(R.id.itemRaridadeTarja);
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(trabalhoEspecifico, getBindingAdapterPosition()));
        }
        public void vincula(Trabalho trabalho){
            this.trabalhoEspecifico = trabalho;
            preencheCampo(trabalho);
        }
        private void preencheCampo(Trabalho trabalho) {
            nomeTrabalhoEspecifico.setText(trabalho.getNome());
            profissaoTrabalhoEspecifico.setText(trabalho.getProfissao());
            nivelTrabalhoEspecifico.setText(context.getString(R.string.stringNivelBadge, trabalho.getNivel()));
            configuraCorRaridade(trabalho);
        }
        private void configuraCorRaridade(Trabalho trabalho) {
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
                corRaridade = MaterialColors.getColor(nomeTrabalhoEspecifico, com.google.android.material.R.attr.colorOnSurface);
                corTarja = MaterialColors.getColor(raridadeTarja, com.google.android.material.R.attr.colorOutlineVariant);
            }
            nomeTrabalhoEspecifico.setTextColor(corRaridade);
            raridadeTarja.setBackgroundColor(corTarja);
        }
    }
}
