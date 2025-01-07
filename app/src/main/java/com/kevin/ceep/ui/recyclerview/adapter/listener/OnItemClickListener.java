package com.kevin.ceep.ui.recyclerview.adapter.listener;

import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEspecificoAdapter;

public interface OnItemClickListener {

    void onItemClick(Trabalho trabalho, int adapterPosition);
    void onItemClick(ListaTrabalhoEspecificoAdapter trabalhoEspecificoAdapter);
    void onItemClick(TrabalhoEstoque trabalhoEstoque, int adapterPosition, int botaoId);
}
