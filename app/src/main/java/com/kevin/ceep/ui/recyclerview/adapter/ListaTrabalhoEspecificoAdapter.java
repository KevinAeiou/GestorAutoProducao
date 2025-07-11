package com.kevin.ceep.ui.recyclerview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.ceep.R;
import com.kevin.ceep.model.ProfissaoTrabalho;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListenerTrabalho;

import java.util.ArrayList;
import java.util.List;

public class ListaTrabalhoEspecificoAdapter extends RecyclerView.Adapter<ListaTrabalhoEspecificoAdapter.TrabalhoEspecificoViewHolder> {

    private final List<Trabalho> trabalhos;
    private final List<ProfissaoTrabalho> profissoesTrabalhos;
    private final Context context;
    private OnItemClickListenerTrabalho onItemClickListener;

    public void setOnItemClickListener(OnItemClickListenerTrabalho onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ListaTrabalhoEspecificoAdapter(Context context, List<Trabalho> trabalho, List<ProfissaoTrabalho> profissoesTrabalhos) {
        this.profissoesTrabalhos = profissoesTrabalhos;
        this.trabalhos = trabalho;
        this.context = context;
    }
    @NonNull
    @Override
    public TrabalhoEspecificoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_trabalho_especifico,parent,false);
        return new TrabalhoEspecificoViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabalhoEspecificoViewHolder holder, int position) {
        Trabalho trabalho = trabalhos.get(position);
        holder.vincula(trabalho);
    }

    @Override
    public int getItemCount() {
        if (trabalhos == null){
            return 0;
        }
        return trabalhos.size();
    }
    public class TrabalhoEspecificoViewHolder extends RecyclerView.ViewHolder {

        private final TextView nomeTrabalhoEspecifico;
        private final TextView profissaoTrabalhoEspecifico;
        private final TextView raridadeTrabalhoEspecifico;
        private final TextView trabalhoNecessarioTrabalhoEspecifico;
        private final TextView experienciaTrabalhoEspecifico;
        private final TextView nivelTrabalhoEspecifico;
        public TrabalhoEspecificoViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeTrabalhoEspecifico = itemView.findViewById(R.id.itemNomeTrabaloEspecifico);
            profissaoTrabalhoEspecifico = itemView.findViewById(R.id.itemProfissaoTrabalhoEspecifico);
            nivelTrabalhoEspecifico = itemView.findViewById(R.id.itemNivelTrabaloEspecifico);
            raridadeTrabalhoEspecifico = itemView.findViewById(R.id.itemRaridadeTrabalhoEspecifico);
            trabalhoNecessarioTrabalhoEspecifico = itemView.findViewById(R.id.itemTrabalhoNecessarioTrabalhoEspecifico);
            experienciaTrabalhoEspecifico = itemView.findViewById(R.id.itemExperienciaTrabaloEspecifico);
            itemView.setOnClickListener(v -> {
                ProfissaoTrabalho profissaoTrabalho = profissoesTrabalhos.get(ListaTodosTrabalhosAdapter.posicaoPai);
                ArrayList<Trabalho> trabalhos = profissaoTrabalho.getTrabalhos();
                onItemClickListener.onItemClick(itemView, trabalhos.get(getAdapterPosition()), getAdapterPosition());
            });
        }
        public void vincula(Trabalho trabalho){
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
            String nomeTrabalhoNecessario1 = "", nomeTrabalhoNecessario2 = "";
            for (Trabalho trabalhoEncontrado : trabalhos) {
                if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[0])) {
                    nomeTrabalhoNecessario1 = trabalhoEncontrado.getNome();
                    break;
                }
            }
            trabalhoNecessario = nomeTrabalhoNecessario1;
            if (idTrabalhosNecessarios.length > 1) {
                for (Trabalho trabalhoEncontrado : trabalhos) {
                    if (trabalhoEncontrado.getId().equals(idTrabalhosNecessarios[1])) {
                        nomeTrabalhoNecessario2 = trabalhoEncontrado.getNome();
                        break;
                    }
                }
                trabalhoNecessario = trabalhoNecessario + ", " + nomeTrabalhoNecessario2;

            }
            trabalhoNecessarioTrabalhoEspecifico.setText(trabalhoNecessario);
        }

        private void confiuraCorNomeTrabalho(Trabalho trabalho) {
            String raridade = trabalho.getRaridade();
            switch (raridade) {
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
}
