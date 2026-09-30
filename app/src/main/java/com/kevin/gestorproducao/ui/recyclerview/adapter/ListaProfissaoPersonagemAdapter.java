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
import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListenerProfissaoPersonagem;
import com.kevin.gestorproducao.utilitario.Formatador;

import java.util.List;

public class ListaProfissaoPersonagemAdapter extends BaseListAdapter<
    ProfissaoPersonagem,
    ListaProfissaoPersonagemAdapter.ProfissaoViewHolder
> {

    private final Context context;
    private OnItemClickListenerProfissaoPersonagem onItemClickListener;

    public ListaProfissaoPersonagemAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListenerProfissaoPersonagem onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    @NonNull
    @Override
    public ProfissaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context).inflate(
            R.layout.item_profissao_personagem,
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
    protected DiffUtil.Callback getDiffCallback(List<ProfissaoPersonagem> antiga, List<ProfissaoPersonagem> nova) {
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
        private final TextView experiencia_profissao;
        private final TextView nivelProfissao;
        private final View prioridadeTarja;
        private ProfissaoPersonagem profissaoPersonagem;
        public ProfissaoViewHolder(@NonNull View itemView) {
            super(itemView);
            nome_profissao = itemView.findViewById(R.id.itemNomeProfissaoPersonagem);
            experiencia_profissao = itemView.findViewById(R.id.itemExperienciaProfissaoPersonagem);
            nivelProfissao = itemView.findViewById(R.id.itemNivelProfissaoPersonagem);
            prioridadeTarja = itemView.findViewById(R.id.itemPrioridadeTarja);
            itemView.setOnClickListener(view ->
                onItemClickListener.onItemClick(profissaoPersonagem, getAbsoluteAdapterPosition())
            );
        }
        public void vincula(ProfissaoPersonagem profissaoPersonagem) {
            this.profissaoPersonagem = profissaoPersonagem;
            preencheCampo(profissaoPersonagem);
        }
        private void preencheCampo(ProfissaoPersonagem profissaoPersonagem) {
            String barraExperiencia =
                Formatador.formatarMilhar(
                    profissaoPersonagem.getExperiencia()
                ) +
                " / " +
                Formatador.formatarMilhar(
                    profissaoPersonagem.getXpMaximo()
                );
            experiencia_profissao.setText(barraExperiencia);
            nome_profissao.setText(profissaoPersonagem.getNome());
            nivelProfissao.setText(context.getString(R.string.stringNivelBadge, profissaoPersonagem.getNivel()));
            configuraTarjaPrioridade(profissaoPersonagem);
        }

        private void configuraTarjaPrioridade(ProfissaoPersonagem profissaoPersonagem) {
            int corTarja = profissaoPersonagem.isPrioridade() ?
                ContextCompat.getColor(context, R.color.cor_background_feito) :
                MaterialColors.getColor(prioridadeTarja, com.google.android.material.R.attr.colorOutlineVariant);
            prioridadeTarja.setBackgroundColor(corTarja);
        }
    }
}
