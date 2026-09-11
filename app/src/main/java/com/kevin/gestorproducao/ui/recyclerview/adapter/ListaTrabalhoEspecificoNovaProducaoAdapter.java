package com.kevin.gestorproducao.ui.recyclerview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.Trabalho;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListener;
import com.kevin.gestorproducao.utilitario.Formatador;

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
        private Trabalho trabalhoEspecifico;

        public TrabalhoEspecificoNovaProducaoViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeTrabalhoEspecifico = itemView.findViewById(R.id.itemNomeTrabaloEspecifico);
            profissaoTrabalhoEspecifico = itemView.findViewById(R.id.itemProfissaoTrabalhoEspecifico);
            nivelTrabalhoEspecifico = itemView.findViewById(R.id.itemNivelTrabaloEspecifico);
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(trabalhoEspecifico, getBindingAdapterPosition()));
        }
        public void vincula(Trabalho trabalho){
            this.trabalhoEspecifico = trabalho;
            preencheCampo(trabalho);
        }
        private void preencheCampo(Trabalho trabalho) {
            nomeTrabalhoEspecifico.setText(trabalho.getNome());
            confiuraCorNomeTrabalho(trabalho);
            profissaoTrabalhoEspecifico.setText(trabalho.getProfissao());
            profissaoTrabalhoEspecifico.setTextColor(Color.WHITE);
            nivelTrabalhoEspecifico.setText(String.valueOf(trabalho.getNivel()));
            nivelTrabalhoEspecifico.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_nivel));
        }
        private void confiuraCorNomeTrabalho(Trabalho trabalho) {
            nomeTrabalhoEspecifico.setTextColor(Formatador.corPorRaridade(context, trabalho.getRaridade()));
        }
    }
}
