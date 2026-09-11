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

import com.google.android.material.card.MaterialCardView;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalhoProducao;
import com.kevin.gestorproducao.utilitario.Formatador;

import java.util.List;

public class ListaTrabalhoProducaoAdapter
    extends BaseListAdapter<TrabalhoProducao, ListaTrabalhoProducaoAdapter.TrabalhoProducaoViewHolder>
{
    private final Context context;
    private OnItemClickListenerTrabalhoProducao onItemClickListener;

    public ListaTrabalhoProducaoAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListenerTrabalhoProducao onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TrabalhoProducaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(parent.getContext()).inflate(
            R.layout.item_trabalho_producao,
            parent,
            false
        );
        return new TrabalhoProducaoViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabalhoProducaoViewHolder holder, int position) {
        holder.vincula(getItem(position));
    }

    @Override
    protected DiffUtil.Callback getDiffCallback(
        List<TrabalhoProducao> antiga,
        List<TrabalhoProducao> nova
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

    public class TrabalhoProducaoViewHolder extends RecyclerView.ViewHolder{

        private final MaterialCardView cardview_trabalho;
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
                if (licenca.equals(context.getString(R.string.licencaNovato))){
                    tipo_licenca.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_licenca_novato));
                } else if (licenca.equals(context.getString(R.string.licencaAprendiz))) {
                    tipo_licenca.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_licenca_aprediz));
                }else{
                    tipo_licenca.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_licenca_mestre));
                }
            }
        }

        private void configuraCorNomeTrabalhoProducao(TrabalhoProducao trabalhoProducao) {
            nome_trabalho.setTextColor(Formatador.corPorRaridade(context, trabalhoProducao.getRaridade()));
        }
    }
}
