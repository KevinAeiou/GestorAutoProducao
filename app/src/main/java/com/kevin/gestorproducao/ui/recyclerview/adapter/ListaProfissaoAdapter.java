package com.kevin.gestorproducao.ui.recyclerview.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.ProfissaoBase;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListenerProfissao;

import java.util.List;

public class ListaProfissaoAdapter extends BaseListAdapter<
    ProfissaoBase,
    ListaProfissaoAdapter.ProfissaoViewHolder
> {
    private OnItemClickListenerProfissao onItemClickListener;

    public ListaProfissaoAdapter() {
    }

    public void setOnItemClickListener(OnItemClickListenerProfissao onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ProfissaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(parent.getContext()).inflate(
            R.layout.item_profissao,
            parent,
            false
        );
        return new ProfissaoViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfissaoViewHolder holder, int position) {
        holder.vincula(getItem(position));
    }

    @Override
    protected DiffUtil.Callback getDiffCallback(List<ProfissaoBase> antiga, List<ProfissaoBase> nova) {
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

    public class ProfissaoViewHolder extends RecyclerView.ViewHolder {
        private final TextView nome_profissao;
        private ProfissaoBase profissao;

        public ProfissaoViewHolder(@NonNull View itemView) {
            super(itemView);

            nome_profissao = itemView.findViewById(R.id.itemNomeProfissao);
            itemView.setOnClickListener(view ->
                onItemClickListener.onItemClick(profissao, getAbsoluteAdapterPosition())
            );
        }

        public void vincula(ProfissaoBase profissao) {
            this.profissao = profissao;
            preencheCampo(profissao);
        }

        private void preencheCampo(ProfissaoBase profissaoPersonagem) {
            nome_profissao.setText(profissaoPersonagem.getNome());
        }
    }
}
