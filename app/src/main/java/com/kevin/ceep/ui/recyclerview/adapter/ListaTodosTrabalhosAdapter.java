package com.kevin.ceep.ui.recyclerview.adapter;

import static com.kevin.ceep.ui.activity.Constantes.CODIGO_REQUISICAO_ALTERA_TRABALHO;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.kevin.ceep.R;
import com.kevin.ceep.model.ProfissaoTrabalho;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.ui.fragment.ListaTodosTrabalhosFragmentDirections;
import com.kevin.ceep.ui.fragment.ListaTodosTrabalhosFragmentDirections.ActionListaTodosTrabalhosFragmentToTrabalhoEspecificoFragment;

import java.util.ArrayList;

public class ListaTodosTrabalhosAdapter extends RecyclerView.Adapter<ListaTodosTrabalhosAdapter.ProfissaoTrabalhoViewHolder> {
    private final ArrayList<ProfissaoTrabalho> profissoes;
    private ArrayList<Trabalho> trabalhos;
    private final Context context;
    public static int posicaoPai = -1;

    public ListaTodosTrabalhosAdapter(ArrayList<ProfissaoTrabalho> profissaoTrabalhos, Context context) {
        this.profissoes = profissaoTrabalhos;
        this.context = context;
    }
    public void atualiza(ArrayList<ProfissaoTrabalho> profissoesAtualizada) {
        DiffUtil.DiffResult diffResult= DiffUtil.calculateDiff(new ItemDiffCallback(profissoes, profissoesAtualizada));
        profissoes.clear();
        profissoes.addAll(profissoesAtualizada);
        diffResult.dispatchUpdatesTo(this);
    }
    @NonNull
    @Override
    public ProfissaoTrabalhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_profissao_trabalho, parent, false);
        return new ProfissaoTrabalhoViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfissaoTrabalhoViewHolder holder, int position) {
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
        trabalhoEspecificoAdapter.setOnItemClickListener((view, trabalho, posicao) -> {
            ActionListaTodosTrabalhosFragmentToTrabalhoEspecificoFragment acao = ListaTodosTrabalhosFragmentDirections.actionListaTodosTrabalhosFragmentToTrabalhoEspecificoFragment(null);
            acao.setCodigoRequisicao(CODIGO_REQUISICAO_ALTERA_TRABALHO);
            acao.setTrabalho(trabalho);
            Navigation.findNavController(view).navigate(acao);
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
    private static class ItemDiffCallback extends DiffUtil.Callback {
        private final ArrayList<ProfissaoTrabalho> listaAntiga;
        private final ArrayList<ProfissaoTrabalho> listaNova;
        public ItemDiffCallback(ArrayList<ProfissaoTrabalho> listaAntiga, ArrayList<ProfissaoTrabalho> listaNova) {
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
            return listaAntiga.get(oldItemPosition).getNome().equals(listaNova.get(newItemPosition).getNome());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return listaAntiga.get(oldItemPosition).equals(listaNova.get(newItemPosition));
        }
    }
}
