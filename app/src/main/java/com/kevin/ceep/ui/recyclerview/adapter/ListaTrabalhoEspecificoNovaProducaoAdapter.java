package com.kevin.ceep.ui.recyclerview.adapter;

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

import com.kevin.ceep.R;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;

import java.util.ArrayList;

public class ListaTrabalhoEspecificoNovaProducaoAdapter extends RecyclerView.Adapter<ListaTrabalhoEspecificoNovaProducaoAdapter.TrabalhoEspecificoNovaProducaoViewHolder> {
    private final ArrayList<Trabalho> trabalhos;
    private final Context context;
    private OnItemClickListener onItemClickListener;

    public ListaTrabalhoEspecificoNovaProducaoAdapter(Context context, ArrayList<Trabalho> trabalhos) {
        this.trabalhos = trabalhos;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void atualizaLista(ArrayList<Trabalho> trabalhosAtualizada) {
        DiffUtil.DiffResult diffResult= DiffUtil.calculateDiff(new ItemDiffCallback(trabalhos, trabalhosAtualizada));
        trabalhos.clear();
        trabalhos.addAll(trabalhosAtualizada);
        diffResult.dispatchUpdatesTo(this);
    }
    @NonNull
    @Override
    public TrabalhoEspecificoNovaProducaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_trabalho_especifico,parent,false);
        return new TrabalhoEspecificoNovaProducaoViewHolder(viewCriada);
    }
    @Override
    public void onBindViewHolder(@NonNull TrabalhoEspecificoNovaProducaoViewHolder holder, int position) {
        Trabalho trabalho = trabalhos.get(position);
        holder.vincula(trabalho);
    }

    @Override
    public int getItemCount() {
        if (trabalhos == null) return 0;
        return trabalhos.size();
    }

    public class TrabalhoEspecificoNovaProducaoViewHolder extends RecyclerView.ViewHolder {
        private final TextView nomeTrabalhoEspecifico;
        private final TextView profissaoTrabalhoEspecifico;
        private final TextView raridadeTrabalhoEspecifico;
        private final TextView trabalhoNecessarioTrabalhoEspecifico;
        private final TextView experienciaTrabalhoEspecifico;
        private final TextView nivelTrabalhoEspecifico;
        private Trabalho trabalhoEspecifico;

        public TrabalhoEspecificoNovaProducaoViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeTrabalhoEspecifico = itemView.findViewById(R.id.itemNomeTrabaloEspecifico);
            profissaoTrabalhoEspecifico = itemView.findViewById(R.id.itemProfissaoTrabalhoEspecifico);
            nivelTrabalhoEspecifico = itemView.findViewById(R.id.itemNivelTrabaloEspecifico);
            raridadeTrabalhoEspecifico = itemView.findViewById(R.id.itemRaridadeTrabalhoEspecifico);
            trabalhoNecessarioTrabalhoEspecifico = itemView.findViewById(R.id.itemTrabalhoNecessarioTrabalhoEspecifico);
            experienciaTrabalhoEspecifico = itemView.findViewById(R.id.itemExperienciaTrabaloEspecifico);
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(trabalhoEspecifico, getAdapterPosition()));
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
            experienciaTrabalhoEspecifico.setText(context.getString(R.string.stringExperienciaValor, trabalho.getExperiencia()));
            raridadeTrabalhoEspecifico.setText(trabalho.getRaridade());
            String trabalhoNecessario = trabalho.getTrabalhoNecessario();
            if (trabalhoNecessario == null || trabalhoNecessario.isEmpty()) {
                trabalhoNecessarioTrabalhoEspecifico.setVisibility(View.GONE);
                return;
            }
            String[] idTrabalhosNecessarios = trabalhoNecessario.split(",");
            String nomeTrabalhoNecessario1 = "Não encontrado", nomeTrabalhoNecessario2 = "Não encontrado";
            for (Trabalho trabalhoEncontrado : trabalhos) {
                if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[0])) {
                    nomeTrabalhoNecessario1 = trabalhoEncontrado.getNome();
                    break;
                }
            }
            trabalhoNecessarioTrabalhoEspecifico.setText(nomeTrabalhoNecessario1);
            if (idTrabalhosNecessarios.length > 1) {
                for (Trabalho trabalhoEncontrado : trabalhos) {
                    if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[1])) {
                        nomeTrabalhoNecessario2 = trabalhoEncontrado.getNome();
                        break;
                    }
                }
                trabalhoNecessarioTrabalhoEspecifico.setText(nomeTrabalhoNecessario1 + "," + nomeTrabalhoNecessario2);
            }
        }
        private void confiuraCorNomeTrabalho(Trabalho trabalho) {
            switch (trabalho.getRaridade()) {
                case "Comum":
                    nomeTrabalhoEspecifico.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_comum));
                    break;
                case "Melhorado":
                    nomeTrabalhoEspecifico.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_melhorado));
                    break;
                case "Raro":
                    nomeTrabalhoEspecifico.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_raro));
                    break;
                case "Especial":
                    nomeTrabalhoEspecifico.setTextColor(ContextCompat.getColor(context, R.color.cor_texto_raridade_especial));
                    break;
            }
        }
    }
    private static class ItemDiffCallback extends DiffUtil.Callback {
        private final ArrayList<Trabalho> listaAntiga;
        private final ArrayList<Trabalho> listaNova;
        public ItemDiffCallback(ArrayList<Trabalho> listaAntiga, ArrayList<Trabalho> listaNova) {
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
