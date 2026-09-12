package com.kevin.gestorproducao.ui.recyclerview.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.model.TrabalhoProducao;
import com.kevin.gestorproducao.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalhoProducao;

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

        private final TextView nome_trabalho;
        private final TextView tipo_licenca;
        private final TextView profissao_trabalho;
        private final TextView nivel_trabalho;
        private final View estado_tarja;
        private final MaterialCardView estado_selo;
        private final ImageView estado_icone;
        private final TextView estado_texto;
        private TrabalhoProducao trabalhoProducao;
        public TrabalhoProducaoViewHolder(@NonNull View itemView) {
            super(itemView);
            nome_trabalho = itemView.findViewById(R.id.itemNomeTrabalho);
            tipo_licenca = itemView.findViewById(R.id.itemTipoLicenca);
            profissao_trabalho = itemView.findViewById(R.id.itemProfissaoTrabalho);
            nivel_trabalho = itemView.findViewById(R.id.itemNivelTrabalho);
            estado_tarja = itemView.findViewById(R.id.itemEstadoTarja);
            estado_selo = itemView.findViewById(R.id.itemEstadoBadge);
            estado_icone = itemView.findViewById(R.id.itemEstadoIcone);
            estado_texto = itemView.findViewById(R.id.itemEstadoTexto);
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
            nivel_trabalho.setText(context.getString(R.string.stringNivelBadge, this.trabalhoProducao.getNivel()));
            configuraEstadoTrabalho(this.trabalhoProducao);
        }

        private void configuraEstadoTrabalho(TrabalhoProducao trabalhoProducao) {
            Integer estado = trabalhoProducao.getEstado();
            int corContainer;
            int corOnContainer;
            int corTarja;
            int icone;
            int textoLabel;
            if (estado == 0) {
                corContainer = R.color.cor_estado_para_produzir_container;
                corOnContainer = R.color.cor_estado_para_produzir_on_container;
                corTarja = R.color.cor_estado_para_produzir_tarja;
                icone = R.drawable.ic_estado_para_produzir;
                textoLabel = R.string.stringFiltroParaProduzir;
            } else if (estado == 1) {
                corContainer = R.color.cor_estado_produzindo_container;
                corOnContainer = R.color.cor_estado_produzindo_on_container;
                corTarja = R.color.cor_estado_produzindo_tarja;
                icone = R.drawable.ic_estado_produzindo;
                textoLabel = R.string.stringFiltroProduzindo;
            } else {
                corContainer = R.color.cor_estado_feito_container;
                corOnContainer = R.color.cor_estado_feito_on_container;
                corTarja = R.color.cor_estado_feito_tarja;
                icone = R.drawable.ic_estado_feito;
                textoLabel = R.string.stringFiltroFeito;
            }

            Context contextoView = itemView.getContext();
            int corOnContainerValor = ContextCompat.getColor(contextoView, corOnContainer);
            estado_tarja.setBackgroundColor(ContextCompat.getColor(contextoView, corTarja));
            estado_selo.setCardBackgroundColor(ContextCompat.getColor(contextoView, corContainer));
            estado_icone.setImageResource(icone);
            ImageViewCompat.setImageTintList(estado_icone, ColorStateList.valueOf(corOnContainerValor));
            estado_texto.setText(textoLabel);
            estado_texto.setTextColor(corOnContainerValor);
        }

        private void configuraCorLicencaTrabalhoProducao(TrabalhoProducao trabalhoProducao) {
            String licenca = trabalhoProducao.getTipoLicenca();
            if (licenca == null) {
                return;
            }

            int cor;
            if (licenca.equals(context.getString(R.string.licencaNovato))) {
                cor = R.color.cor_producao_licenca_novato;
            } else if (licenca.equals(context.getString(R.string.licencaAprendiz))) {
                cor = R.color.cor_producao_licenca_aprendiz;
            } else if (licenca.equals(context.getString(R.string.licencaIniciante))) {
                cor = R.color.cor_producao_licenca_iniciante;
            } else {
                cor = R.color.cor_producao_licenca_mestre;
            }
            tipo_licenca.setTextColor(ContextCompat.getColor(itemView.getContext(), cor));
        }

        private void configuraCorNomeTrabalhoProducao(TrabalhoProducao trabalhoProducao) {
            String raridade = trabalhoProducao.getRaridade();
            Context contextoView = itemView.getContext();
            int cor;
            if ("Melhorado".equals(raridade)) {
                cor = ContextCompat.getColor(contextoView, R.color.cor_producao_raridade_melhorado);
            } else if ("Raro".equals(raridade)) {
                cor = ContextCompat.getColor(contextoView, R.color.cor_producao_raridade_raro);
            } else if ("Especial".equals(raridade)) {
                cor = ContextCompat.getColor(contextoView, R.color.cor_producao_raridade_especial);
            } else {
                cor = MaterialColors.getColor(nome_trabalho, com.google.android.material.R.attr.colorOnSurface);
            }
            nome_trabalho.setTextColor(cor);
        }
    }
}
