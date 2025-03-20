package com.kevin.ceep.ui.recyclerview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.ceep.R;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalhoProducao;

import java.util.ArrayList;

public class ListaTrabalhoProducaoAdapter extends RecyclerView.Adapter<ListaTrabalhoProducaoAdapter.TrabalhoProducaoViewHolder> {
    private final ArrayList<TrabalhoProducao> trabalhosProducao;
    private final Context context;
    private OnItemClickListenerTrabalhoProducao onItemClickListener;

    public ListaTrabalhoProducaoAdapter(Context context, ArrayList<TrabalhoProducao> trabalhosProducao) {
        this.trabalhosProducao = trabalhosProducao;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListenerTrabalhoProducao onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void atualiza(ArrayList<TrabalhoProducao> trabalhosProducaoAtualizada){
        DiffUtil.DiffResult diffResult= DiffUtil.calculateDiff(new ItemDiffCallback(trabalhosProducao, trabalhosProducaoAtualizada));
        trabalhosProducao.clear();
        trabalhosProducao.addAll(trabalhosProducaoAtualizada);
        diffResult.dispatchUpdatesTo(this);
    }
    @NonNull
    @Override
    public TrabalhoProducaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_trabalho_producao,parent,false);
        return new TrabalhoProducaoViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabalhoProducaoViewHolder holder, int position) {
        TrabalhoProducao trabalhoProducao = trabalhosProducao.get(position);
        holder.vincula(trabalhoProducao);
    }

    @Override
    public int getItemCount() {
        return trabalhosProducao.size();
    }
    public void adiciona(TrabalhoProducao trabalhoProducao, int posicao){
        if (posicao < 0 || posicao > trabalhosProducao.size()){
            return;
        }
        trabalhosProducao.add(posicao, trabalhoProducao);
        notifyItemInserted(posicao);
        notifyItemRangeChanged(posicao, trabalhosProducao.size());
    }
    public void remove(int posicao){
        if (posicao<0 || posicao>=trabalhosProducao.size()){
            return;
        }
        trabalhosProducao.remove(posicao);
        notifyItemRemoved(posicao);
        notifyItemRangeChanged(posicao,trabalhosProducao.size());
    }
    public void limpaLista() {
        atualiza(new ArrayList<>());
    }

    public class TrabalhoProducaoViewHolder extends RecyclerView.ViewHolder{

        private final CardView cardview_trabalho;
        private final TextView nome_trabalho;
        private final TextView tipo_licenca;
        private final TextView profissao_trabalho;
        private final TextView nivel_trabalho;
        private TrabalhoProducao trabalhoProducao;
        public TrabalhoProducaoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardview_trabalho = itemView.findViewById(R.id.itemCardViewTrabalho);
            nome_trabalho = itemView.findViewById(R.id.itemNomeTrabalho);
            tipo_licenca = itemView.findViewById(R.id.itemTipoLicenca);
            profissao_trabalho = itemView.findViewById(R.id.itemProfissaoTrabalho);
            nivel_trabalho = itemView.findViewById(R.id.itemNivelTrabalho);
            itemView.setOnClickListener(view -> onItemClickListener.onItemClick(trabalhoProducao));
        }

        public void vincula(TrabalhoProducao trabalhoProducao) {
            this.trabalhoProducao = trabalhoProducao;
            preencheCampo(trabalhoProducao);
        }

        private void preencheCampo(TrabalhoProducao trabalhoProducao) {
            nome_trabalho.setText(trabalhoProducao.getNome());
            configuraCorNomeTrabalhoProducao(trabalhoProducao);
            tipo_licenca.setText(trabalhoProducao.getTipoLicenca());
            configuraCorLicencaTrabalhoProducao(trabalhoProducao);
            profissao_trabalho.setText(this.trabalhoProducao.getProfissao());
            profissao_trabalho.setTextColor(Color.WHITE);
            nivel_trabalho.setText(String.valueOf(this.trabalhoProducao.getNivel()));
            nivel_trabalho.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_nivel));
            configuraCorCardViewTrabalho(this.trabalhoProducao);
        }

        private void configuraCorCardViewTrabalho(TrabalhoProducao trabalhoProducao) {
            Integer estado = trabalhoProducao.getEstado();
            if (estado == 0){
                cardview_trabalho.setCardBackgroundColor(ContextCompat.getColor(context,R.color.cor_background_card));
            }else if (estado==1){
                cardview_trabalho.setCardBackgroundColor(ContextCompat.getColor(context,R.color.cor_background_produzindo));
            }else if (estado==2){
                cardview_trabalho.setCardBackgroundColor(ContextCompat.getColor(context,R.color.cor_background_feito));
            }
        }

        private void configuraCorLicencaTrabalhoProducao(TrabalhoProducao trabalhoProducao) {
            String licenca = trabalhoProducao.getTipoLicenca();
            if (licenca != null) {
                if (licenca.equals("Licença de Artesanato de Novato")){
                    tipo_licenca.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_licenca_novato));
                } else if (licenca.equals("Licença de Artesanato de Aprendiz")) {
                    tipo_licenca.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_licenca_aprediz));
                }else{
                    tipo_licenca.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_licenca_mestre));
                }
            }
        }

        private void configuraCorNomeTrabalhoProducao(TrabalhoProducao trabalhoProducao) {
            String raridade = trabalhoProducao.getRaridade();
            switch (raridade) {
                case "Comum":
                    nome_trabalho.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_comum));
                    break;
                case "Raro":
                    nome_trabalho.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_raro));
                    break;
                case "Melhorado":
                    nome_trabalho.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_melhorado));
                    break;
                case "Especial":
                    nome_trabalho.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_especial));
                    break;
            }
        }
    }
    private static class ItemDiffCallback extends DiffUtil.Callback {
        private final ArrayList<TrabalhoProducao> listaAntiga;
        private final ArrayList<TrabalhoProducao> listaNova;
        public ItemDiffCallback(ArrayList<TrabalhoProducao> listaAntiga, ArrayList<TrabalhoProducao> listaNova) {
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
