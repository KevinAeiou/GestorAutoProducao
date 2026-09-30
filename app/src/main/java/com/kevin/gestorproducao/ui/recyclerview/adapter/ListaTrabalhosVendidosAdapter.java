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
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalhoVendido;
import com.kevin.gestorproducao.utilitario.Formatador;

import java.util.List;

public class ListaTrabalhosVendidosAdapter
    extends BaseListAdapter<TrabalhoVendido, ListaTrabalhosVendidosAdapter.TrabalhosVendidosViewHolder>
{
    private final Context context;
    private OnItemClickListenerTrabalhoVendido onItemClickListener;
    public ListaTrabalhosVendidosAdapter(Context context) {
        this.context = context;
    }
    public void setOnItemClickListener(OnItemClickListenerTrabalhoVendido onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TrabalhosVendidosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context).inflate(
            R.layout.item_trabalho_vendido,
            parent,
            false
        );
        return new TrabalhosVendidosViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabalhosVendidosViewHolder holder, int posicao) {
        holder.vincula(getItem(posicao));
    }

    @Override
    protected DiffUtil.Callback getDiffCallback(
        List<TrabalhoVendido> antiga,
        List<TrabalhoVendido> nova
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
                return antiga.get(oldPos).equals(nova.get(newPos));
            }
        };
    }

    public class TrabalhosVendidosViewHolder extends RecyclerView.ViewHolder{
        private final TextView itemNome;
        private final TextView itemProfissao;
        private final TextView itemValor;
        private final TextView itemQuantidade;
        private final View raridadeTarja;
        private TrabalhoVendido trabalho;
        public TrabalhosVendidosViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNome = itemView.findViewById(R.id.itemNomeTrabalhoVendido);
            itemProfissao = itemView.findViewById(R.id.itemProfissaoTrabalhoVendido);
            itemValor = itemView.findViewById(R.id.itemValorTrabalhoVendido);
            itemQuantidade = itemView.findViewById(R.id.itemQuantidadeTrabalhoVendido);
            raridadeTarja = itemView.findViewById(R.id.itemRaridadeTarjaVendido);
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(trabalho));
        }

        public void vincula(TrabalhoVendido trabalho) {
            this.trabalho = trabalho;
            preencheCampos(trabalho);
        }

        private void preencheCampos(TrabalhoVendido trabalho) {
            configuraCorRaridade(trabalho);
            String nome = trabalho.getNome();
            if (nome == null) nome = context.getString(R.string.stringIndefinido);
            itemNome.setText(nome);
            itemProfissao.setText(trabalho.getProfissao());
            itemValor.setText(context.getString(
                R.string.stringOuroValor,
                Formatador.formatarMilhar(trabalho.getValor())
            ));
            itemQuantidade.setText(context.getString(
                R.string.stringQuantidadeUndidade,
                Formatador.formatarMilhar(trabalho.getQuantidade())
            ));
        }

        private void configuraCorRaridade(TrabalhoVendido trabalho) {
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
