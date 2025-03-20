package com.kevin.ceep.ui.recyclerview.adapter;

import android.content.Context;
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
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListenerProfissao;

import java.util.ArrayList;

public class ListaProfissaoAdapter extends RecyclerView.Adapter<ListaProfissaoAdapter.ProfissaoViewHolder> {

    private final ArrayList<Profissao> profissoes;
    private final Context context;
    private OnItemClickListenerProfissao onItemClickListener;

    public ListaProfissaoAdapter(Context context, ArrayList<Profissao> profissao) {
        this.profissoes = profissao;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListenerProfissao onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    @NonNull
    @Override
    public ProfissaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_profissao,parent,false);
        return new ProfissaoViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfissaoViewHolder holder, int position) {
        Profissao profissao = profissoes.get(position);
        holder.vincula(profissao);
    }

    @Override
    public int getItemCount() {
        return profissoes.size();
    }

    public void atualiza(ArrayList<Profissao> profissoesAtualizadas) {
        DiffUtil.DiffResult diffResult= DiffUtil.calculateDiff(new ItemDiffCallback(profissoes, profissoesAtualizadas));
        profissoes.clear();
        profissoes.addAll(profissoesAtualizadas);
        diffResult.dispatchUpdatesTo(this);
    }

    public void limpaLista() {
        atualiza(new ArrayList<>());
    }
    public class ProfissaoViewHolder extends RecyclerView.ViewHolder {

        private final TextView nome_profissao;
        private final TextView experiencia_profissao;
        private final TextView nivelProfissao;
        private final CardView cardProfissao;
        private Profissao profissao;
        public ProfissaoViewHolder(@NonNull View itemView) {
            super(itemView);
            nome_profissao = itemView.findViewById(R.id.itemNomeProfissao);
            experiencia_profissao = itemView.findViewById(R.id.itemExperienciaProfissao);
            nivelProfissao = itemView.findViewById(R.id.itemNivelProfissao);
            cardProfissao = itemView.findViewById(R.id.cardViewProfissao);
            itemView.setOnClickListener(view -> onItemClickListener.onItemClick(profissao, getAdapterPosition()));
        }
        public void vincula(Profissao profissao) {
            this.profissao = profissao;
            preencheCampo(profissao);
        }
        private void preencheCampo(Profissao profissao) {
            String barraExperiencia = profissao.getExperiencia() + " / " + profissao.getXpMaximo(profissao.getNivel());
            experiencia_profissao.setText(barraExperiencia);
            nome_profissao.setText(profissao.getNome());
            if (profissao.isPrioridade()) {
                cardProfissao.setCardBackgroundColor(ContextCompat.getColor(context,R.color.cor_background_feito));
            } else {
                cardProfissao.setCardBackgroundColor(ContextCompat.getColor(context,R.color.cor_background_card));
            }
            nivelProfissao.setText(String.valueOf(profissao.getNivel()));
        }
    }
    private static class ItemDiffCallback extends DiffUtil.Callback {
        private final ArrayList<Profissao> listaAntiga;
        private final ArrayList<Profissao> listaNova;
        public ItemDiffCallback(ArrayList<Profissao> listaAntiga, ArrayList<Profissao> listaNova) {
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
