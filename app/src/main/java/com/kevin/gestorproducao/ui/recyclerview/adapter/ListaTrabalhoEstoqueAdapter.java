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
        private final TextView quantidadeTrabalho;
        private final TextView nivelTrabalho;
        private TrabalhoEstoque trabalho;

        public TrabalhoEstoqueViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeTrabalho = itemView.findViewById(R.id.itemNomeTrabalhoEstoque);
            quantidadeTrabalho = itemView.findViewById(R.id.itemQuantidadeTrabalhoEstoque);
            nivelTrabalho = itemView.findViewById(R.id.itemNivelTrabalhoEstoque);
            itemView.setOnClickListener(v ->
                onItemClickListener.onItemClick(trabalho, getBindingAdapterPosition())
            );
        }
        public void vincula(TrabalhoEstoque trabalho){
            this.trabalho = trabalho;
            preencheCampos(trabalho);
        }
        private void preencheCampos(TrabalhoEstoque trabalho) {
            configuraCorNomeTrabalho(trabalho);

            nomeTrabalho.setText(trabalho.getNome());
            String quantidade = context.getString(
                R.string.stringQuantidadeUndidade,
                Formatador.formatarMilhar(trabalho.getQuantidade())
            );
            quantidadeTrabalho.setText(quantidade);
            nivelTrabalho.setText(String.valueOf(this.trabalho.getNivel()));
            nivelTrabalho.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_nivel));
        }
        private void configuraCorNomeTrabalho(TrabalhoEstoque trabalho) {
            nomeTrabalho.setTextColor(Formatador.corPorRaridade(context, trabalho.getRaridade()));
        }
    }
}
