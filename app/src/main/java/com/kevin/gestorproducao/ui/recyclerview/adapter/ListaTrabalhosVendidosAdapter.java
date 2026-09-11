package com.kevin.gestorproducao.ui.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalhoVendido;
import com.kevin.gestorproducao.utilitario.Formatador;

import java.util.List;

public class ListaTrabalhosVendidosAdapter
    extends BaseListAdapter<TrabalhoVendido, ListaTrabalhosVendidosAdapter.TrabalhosVendidosViewHolder>
{
    private final Context context;
    private OnItemClickListenerTrabalhoVendido onItemClickListener;
    public ListaTrabalhosVendidosAdapter(Context context) {
        this.context = context;
    }
    public void setOnItemClickListener(OnItemClickListenerTrabalhoVendido onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TrabalhosVendidosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context).inflate(
            R.layout.item_trabalho_vendido,
            parent,
            false
        );
        return new TrabalhosVendidosViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabalhosVendidosViewHolder holder, int posicao) {
        holder.vincula(getItem(posicao));
    }

    @Override
    protected DiffUtil.Callback getDiffCallback(
        List<TrabalhoVendido> antiga,
        List<TrabalhoVendido> nova
    ) {
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

    public class TrabalhosVendidosViewHolder extends RecyclerView.ViewHolder{
        private final TextView itemNome;
        private final TextView itemValor;
        private final TextView itemQuantidade;
        private TrabalhoVendido trabalho;
        public TrabalhosVendidosViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNome = itemView.findViewById(R.id.itemNomeTrabalhoVendido);
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
            itemValor.setText(context.getString(
                R.string.stringOuroValor,
                Formatador.formatarMilhar(trabalho.getValor())
            ));
            itemQuantidade.setText(context.getString(
                R.string.stringQuantidadeUndidade,
                Formatador.formatarMilhar(trabalho.getQuantidade())
            ));
        }

        private void configuraCorNomeTrabalhoProducao(TrabalhoVendido trabalhoProducao) {
            itemNome.setTextColor(Formatador.corPorRaridade(context, trabalhoProducao.getRaridade()));
        }
    }
}
