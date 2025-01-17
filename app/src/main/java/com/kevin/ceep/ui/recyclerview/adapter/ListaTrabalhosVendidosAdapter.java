package com.kevin.ceep.ui.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.ceep.R;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalhoVendido;

import java.util.List;

public class ListaTrabalhosVendidosAdapter extends RecyclerView.Adapter<ListaTrabalhosVendidosAdapter.TrabalhosVendidosViewHolder>{
    private List<TrabalhoVendido> listaTrabalhosVendidos;
    private final Context context;
    private OnItemClickListenerTrabalhoVendido onItemClickListener;
    public ListaTrabalhosVendidosAdapter(List<TrabalhoVendido> listaTrabalhosVendidos, Context context) {
        this.listaTrabalhosVendidos = listaTrabalhosVendidos;
        this.context = context;
    }
    public void setOnItemClickListener(OnItemClickListenerTrabalhoVendido onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public  void atualiza(List<TrabalhoVendido> listaFiltrada) {
        this.listaTrabalhosVendidos = listaFiltrada;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public TrabalhosVendidosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_trabalho_vendido, parent, false);
        return new TrabalhosVendidosViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabalhosVendidosViewHolder holder, int posicao) {
        TrabalhoVendido trabalhoVendido = listaTrabalhosVendidos.get(posicao);
        holder.vincula(trabalhoVendido);
    }

    @Override
    public int getItemCount() {
        return listaTrabalhosVendidos.size();
    }
    public void remove(int posicao){
        if (posicao < 0 || posicao >= listaTrabalhosVendidos.size()) {
            return;
        }
        listaTrabalhosVendidos.remove(posicao);
        notifyItemRemoved(posicao);
        notifyItemRangeChanged(posicao, listaTrabalhosVendidos.size());
    }
    public void limpaLista() {
        listaTrabalhosVendidos.clear();
        notifyDataSetChanged();
    }

    public void adiciona(TrabalhoVendido trabalhoVendidoRemovido, int itemPosicao) {
        if (itemPosicao < 0 || itemPosicao > listaTrabalhosVendidos.size()){
            return;
        }
        listaTrabalhosVendidos.add(itemPosicao, trabalhoVendidoRemovido);
        notifyItemInserted(itemPosicao);
        notifyItemRangeChanged(itemPosicao, listaTrabalhosVendidos.size());
    }

    public class TrabalhosVendidosViewHolder extends RecyclerView.ViewHolder{
        private final TextView itemNome;
        private final TextView itemData;
        private final TextView itemValor;
        private final TextView itemQuantidade;
        private TrabalhoVendido trabalho;
        public TrabalhosVendidosViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNome = itemView.findViewById(R.id.itemNomeTrabalhoVendido);
            itemData = itemView.findViewById(R.id.itemDataTrabalhoVendido);
            itemValor = itemView.findViewById(R.id.itemValorTrabalhoVendido);
            itemQuantidade = itemView.findViewById(R.id.itemQuantidadeTrabalhoVendido);
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(trabalho));
        }
        public void vincula(TrabalhoVendido trabalho) {
            this.trabalho = trabalho;
            preencheCampos(trabalho);
        }

        private void preencheCampos(TrabalhoVendido trabalho) {
            configuraCorNomeTrabalhoProducao(trabalho);
            String nome = trabalho.getNome();
            if (nome == null) nome = "Indefinido";
            itemNome.setText(nome);
            itemData.setText(trabalho.getDataVenda());
            itemValor.setText(context.getString(R.string.stringOuroValor, trabalho.getValor()));
            itemQuantidade.setText(context.getString(R.string.stringQuantidadeValor, trabalho.getQuantidade()));
        }

        private void configuraCorNomeTrabalhoProducao(TrabalhoVendido trabalhoProducao) {
            String raridade = trabalhoProducao.getRaridade();
            if (raridade != null) {
                switch (raridade) {
                    case "Comum":
                        itemNome.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_comum));
                        break;
                    case "Raro":
                        itemNome.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_raro));
                        break;
                    case "Melhorado":
                        itemNome.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_melhorado));
                        break;
                    case "Especial":
                        itemNome.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_especial));
                        break;
                }
            }
        }
    }
}
