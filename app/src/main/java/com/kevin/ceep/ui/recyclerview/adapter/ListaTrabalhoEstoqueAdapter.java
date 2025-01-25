package com.kevin.ceep.ui.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.kevin.ceep.R;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalhoEstoque;

import java.util.ArrayList;
import java.util.List;

public class ListaTrabalhoEstoqueAdapter extends RecyclerView.Adapter<ListaTrabalhoEstoqueAdapter.TrabalhoEstoqueViewHolder> {

    private List<TrabalhoEstoque> trabalhosEstoque;
    private final Context context;
    private OnItemClickListenerTrabalhoEstoque onItemClickListener;

    public ListaTrabalhoEstoqueAdapter(List<TrabalhoEstoque> trabalhosEstoque, Context context) {
        this.trabalhosEstoque = trabalhosEstoque;
        this.context = context;
    }
    public void setOnItemClickListener(OnItemClickListenerTrabalhoEstoque onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    @NonNull
    @Override
    public TrabalhoEstoqueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_trabalho_estoque, parent, false);
        return new TrabalhoEstoqueViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabalhoEstoqueViewHolder holder, int position) {
        TrabalhoEstoque trabalhoEstoque = trabalhosEstoque.get(position);
        holder.vincula(trabalhoEstoque);
    }

    @Override
    public int getItemCount() {
        return trabalhosEstoque.size();
    }

    public void altera(int posicao, TrabalhoEstoque trabalhoEstoque){
        trabalhosEstoque.set(posicao,trabalhoEstoque);
        notifyItemChanged(posicao);
    }

    public void atualiza(ArrayList<TrabalhoEstoque> todosTrabalhosEstoque) {
        this.trabalhosEstoque = todosTrabalhosEstoque;
        notifyDataSetChanged();
    }

    public void adiciona(TrabalhoEstoque trabalhoremovido, int itemPosicao) {
        if (itemPosicao < 0 || itemPosicao > trabalhosEstoque.size()){
            return;
        }
        trabalhosEstoque.add(itemPosicao, trabalhoremovido);
        notifyItemInserted(itemPosicao);
        notifyItemRangeChanged(itemPosicao, trabalhosEstoque.size());
    }

    public void remove(int itemPosicao) {
        if (itemPosicao<0 || itemPosicao>= trabalhosEstoque.size()){
            return;
        }
        trabalhosEstoque.remove(itemPosicao);
        notifyItemRemoved(itemPosicao);
        notifyItemRangeChanged(itemPosicao,trabalhosEstoque.size());
    }

    public class TrabalhoEstoqueViewHolder extends RecyclerView.ViewHolder{
        private final TextView nomeTrabalho;
        private final TextView profissaoTrabalho;
        private final TextView nivelTrabalho;
        private final MaterialTextView quantidadeTrabalho;
        private TrabalhoEstoque trabalhoEstoque;

        public TrabalhoEstoqueViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeTrabalho = itemView.findViewById(R.id.itemNomeTrabalhoEstoque);
            profissaoTrabalho = itemView.findViewById(R.id.itemProfissaoTrabalhoEstoque);
            nivelTrabalho = itemView.findViewById(R.id.itemNivelTrabalhoEstoque);
            quantidadeTrabalho = itemView.findViewById(R.id.itemTxtQuantidadeTrabalhoEstoque);
            ImageButton botaoMenosUm = itemView.findViewById(R.id.itemBotaoMenosUm);
            ImageButton botaoMenosCinquenta = itemView.findViewById(R.id.itemBotaoMenosCinquenta);
            ImageButton botaoMaisUm = itemView.findViewById(R.id.itemBotaoMaisUm);
            ImageButton botaoMaisCinquenta = itemView.findViewById(R.id.itemBotaoMaisCinquenta);
            botaoMenosUm.setOnClickListener(v -> onItemClickListener.onItemClick(trabalhoEstoque,getAdapterPosition(), R.id.itemBotaoMenosUm));
            botaoMenosCinquenta.setOnClickListener(v -> onItemClickListener.onItemClick(trabalhoEstoque,getAdapterPosition(), R.id.itemBotaoMenosCinquenta));
            botaoMaisUm.setOnClickListener(v -> onItemClickListener.onItemClick(trabalhoEstoque,getAdapterPosition(), R.id.itemBotaoMaisUm));
            botaoMaisCinquenta.setOnClickListener(v -> onItemClickListener.onItemClick(trabalhoEstoque,getAdapterPosition(), R.id.itemBotaoMaisCinquenta));

        }
        public void vincula(TrabalhoEstoque trabalhoEstoque){
            this.trabalhoEstoque = trabalhoEstoque;
            preencheCampos(trabalhoEstoque);
        }
        private void preencheCampos(TrabalhoEstoque trabalhoEstoque) {
                confiuraCorNomeTrabalho(trabalhoEstoque);
                nomeTrabalho.setText(trabalhoEstoque.getNome());
                profissaoTrabalho.setText(trabalhoEstoque.getProfissao());
                nivelTrabalho.setText(context.getString(R.string.stringNivelValor, trabalhoEstoque.getNivel()));
                quantidadeTrabalho.setText(String.valueOf(trabalhoEstoque.getQuantidade()));
        }
        private void confiuraCorNomeTrabalho(TrabalhoEstoque trabalhoEstoque) {
            String raridade = trabalhoEstoque.getRaridade();
            switch (raridade) {
                case "Comum":
                    nomeTrabalho.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_comum));
                    break;
                case "Melhorado":
                    nomeTrabalho.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_melhorado));
                    break;
                case "Raro":
                    nomeTrabalho.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_raro));
                    break;
                case "Especial":
                    nomeTrabalho.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_especial));
                    break;
            }
        }
    }

}
