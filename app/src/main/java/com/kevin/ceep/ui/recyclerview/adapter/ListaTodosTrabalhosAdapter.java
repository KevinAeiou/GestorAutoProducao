package com.kevin.ceep.ui.recyclerview.adapter;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_NOME_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_ALTERA_TRABALHO;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.kevin.ceep.R;
import com.kevin.ceep.model.ProfissaoTrabalho;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.ui.activity.TrabalhoEspecificoActivity;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;

import java.util.List;

public class ListaTodosTrabalhosAdapter extends RecyclerView.Adapter<ListaTodosTrabalhosAdapter.ProfissaoTrabalhoViewHolder> {
    private List<ProfissaoTrabalho> profissoes;
    private List<Trabalho> trabalhos;
    private final Context context;
    public static int posicaoPai = -1;

    public ListaTodosTrabalhosAdapter(List<ProfissaoTrabalho> profissaoTrabalhos, Context context) {
        this.profissoes = profissaoTrabalhos;
        this.context = context;
    }
    public void atualiza(List<ProfissaoTrabalho> listaFiltrada) {
        this.profissoes = listaFiltrada;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ProfissaoTrabalhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_profissao_trabalho, parent, false);
        return new ProfissaoTrabalhoViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull ListaTodosTrabalhosAdapter.ProfissaoTrabalhoViewHolder holder, int position) {
        ProfissaoTrabalho profissaoTrabalho = profissoes.get(position);
        trabalhos = profissaoTrabalho.getTrabalhos();
        holder.vincula(profissaoTrabalho);
        configuraRecyclerViewExpancivel(holder, profissaoTrabalho);
    }

    private void configuraRecyclerViewExpancivel(@NonNull ProfissaoTrabalhoViewHolder holder, ProfissaoTrabalho profissaoTrabalho) {
        ListaTrabalhoEspecificoAdapter trabalhoEspecificoAdapter = new ListaTrabalhoEspecificoAdapter(context, trabalhos, profissoes);
        holder.recyclerViewExpansivelItemProfissaoTrabalho.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewExpansivelItemProfissaoTrabalho.setHasFixedSize(true);
        holder.recyclerViewExpansivelItemProfissaoTrabalho.setAdapter(trabalhoEspecificoAdapter);
        holder.linearLayoutItemProfissaoTrabalho.setOnClickListener(view -> {
            posicaoPai = holder.getAdapterPosition();
            profissaoTrabalho.setExpandable(!profissaoTrabalho.isExpandable());
            notifyItemChanged(holder.getAdapterPosition());
        });
        trabalhoEspecificoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Trabalho trabalho, int adapterPosition) {
                Intent iniciaVaiParaCadastraNovoTrabalho = new Intent(context,
                        TrabalhoEspecificoActivity.class);
                iniciaVaiParaCadastraNovoTrabalho.putExtra(CHAVE_TRABALHO, CODIGO_REQUISICAO_ALTERA_TRABALHO);
                iniciaVaiParaCadastraNovoTrabalho.putExtra(CHAVE_NOME_TRABALHO, trabalho);
                iniciaVaiParaCadastraNovoTrabalho.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(iniciaVaiParaCadastraNovoTrabalho);
            }

            @Override
            public void onItemClick(ListaTrabalhoEspecificoAdapter trabalhoEspecificoAdapter) {

            }

            @Override
            public void onItemClick(TrabalhoEstoque trabalhoEstoque, int adapterPosition, int botaoId) {

            }
        });
        boolean isExpandable = profissaoTrabalho.isExpandable();
        holder.constraintLayoutItemProfissaoTrabalho.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
        if (isExpandable) {
            holder.shapeableImageViewItemProfissaoTrabalho.setImageResource(R.drawable.ic_cima);
        } else {
            holder.shapeableImageViewItemProfissaoTrabalho.setImageResource(R.drawable.ic_baixo);
        }
    }
    @Override
    public int getItemCount() {
        return profissoes.size();
    }
    public static class ProfissaoTrabalhoViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayoutCompat linearLayoutItemProfissaoTrabalho;
        private final ConstraintLayout constraintLayoutItemProfissaoTrabalho;
        private final RecyclerView recyclerViewExpansivelItemProfissaoTrabalho;
        private final MaterialTextView txtNomeItemProfissaoTrabalho;
        private final ShapeableImageView shapeableImageViewItemProfissaoTrabalho;
        public ProfissaoTrabalhoViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayoutItemProfissaoTrabalho = itemView.findViewById(R.id.linearLayoutItemProfissaoTrabalho);
            constraintLayoutItemProfissaoTrabalho = itemView.findViewById(R.id.constraintLayoutExpansivelItemProfissaoTrabalho);
            recyclerViewExpansivelItemProfissaoTrabalho = itemView.findViewById(R.id.recyclerViewItemProfissaoTrabalho);
            txtNomeItemProfissaoTrabalho = itemView.findViewById(R.id.txtProfissaoItemProfissaoTrabalho);
            shapeableImageViewItemProfissaoTrabalho = itemView.findViewById(R.id.imgExpandeItemProfissaoTrabalho);
        }

        public void vincula(ProfissaoTrabalho profissaoTrabalho) {
            preencheCampos(profissaoTrabalho);
        }

        private void preencheCampos(ProfissaoTrabalho profissaoTrabalho) {
            txtNomeItemProfissaoTrabalho.setText(profissaoTrabalho.getNome());
        }
    }
}
