package com.kevin.ceep.ui.recyclerview.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.ceep.R;
import com.kevin.ceep.model.TrabalhoVendido;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalhoVendido;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ListaTrabalhosVendidosAdapter extends RecyclerView.Adapter<ListaTrabalhosVendidosAdapter.TrabalhosVendidosViewHolder>{
    private final ArrayList<TrabalhoVendido> vendas;
    private final Context context;
    private OnItemClickListenerTrabalhoVendido onItemClickListener;
    public ListaTrabalhosVendidosAdapter(ArrayList<TrabalhoVendido> vendas, Context context) {
        this.vendas = vendas;
        this.context = context;
    }
    public void setOnItemClickListener(OnItemClickListenerTrabalhoVendido onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public  void atualiza(List<TrabalhoVendido> vendasAtualizadas) {
        DiffUtil.DiffResult diffResult= DiffUtil.calculateDiff(new ItemDiffCallback(vendas, vendasAtualizadas));
        vendas.clear();
        vendas.addAll(vendasAtualizadas);
        diffResult.dispatchUpdatesTo(this);
    }
    @NonNull
    @Override
    public TrabalhosVendidosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_trabalho_vendido, parent, false);
        return new TrabalhosVendidosViewHolder(viewCriada);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull TrabalhosVendidosViewHolder holder, int posicao) {
        TrabalhoVendido trabalhoVendido = vendas.get(posicao);
        holder.vincula(trabalhoVendido);
    }

    @Override
    public int getItemCount() {
        return vendas.size();
    }
    public void remove(int posicao){
        if (posicao < 0 || posicao >= vendas.size()) {
            return;
        }
        vendas.remove(posicao);
        notifyItemRemoved(posicao);
        notifyItemRangeChanged(posicao, vendas.size());
    }
    public void limpaLista() {
        atualiza(new ArrayList<>());
    }

    public void adiciona(TrabalhoVendido trabalhoVendidoRemovido, int itemPosicao) {
        if (itemPosicao < 0 || itemPosicao > vendas.size()){
            return;
        }
        vendas.add(itemPosicao, trabalhoVendidoRemovido);
        notifyItemInserted(itemPosicao);
        notifyItemRangeChanged(itemPosicao, vendas.size());
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
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void vincula(TrabalhoVendido trabalho) {
            this.trabalho = trabalho;
            preencheCampos(trabalho);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        private void preencheCampos(TrabalhoVendido trabalho) {
            configuraCorNomeTrabalhoProducao(trabalho);
            String nome = trabalho.getNome();
            if (nome == null) nome = "Indefinido";
            itemNome.setText(nome);
            itemData.setText(getDataParaExibicao(trabalho.getDataVenda()));
            itemValor.setText(context.getString(R.string.stringOuroValor, trabalho.getValor()));
            itemQuantidade.setText(context.getString(R.string.stringQuantidadeValor, trabalho.getQuantidade()));
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public String getDataParaExibicao(String dataOriginal) {
            try {
                DateTimeFormatter entrada = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter saida = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate data = LocalDate.parse(dataOriginal, entrada);
                return data.format(saida);
            } catch (DateTimeParseException e) {
                return dataOriginal;
            }
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

    private static class ItemDiffCallback extends DiffUtil.Callback {
        private final ArrayList<TrabalhoVendido> listaAntiga;
        private final List<TrabalhoVendido> listaNova;
        public ItemDiffCallback(ArrayList<TrabalhoVendido> listaAntiga, List<TrabalhoVendido> listaNova) {
            this.listaAntiga= listaAntiga;
            this.listaNova= listaNova;
        }

        @Override
        public int getOldListSize() {
            return listaAntiga.size();
        }

        @Override
        public int getNewListSize() {
            return listaNova.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return listaAntiga.get(oldItemPosition).getId().equals(listaNova.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return listaAntiga.get(oldItemPosition).equals(listaNova.get(newItemPosition));
        }
    }
}
