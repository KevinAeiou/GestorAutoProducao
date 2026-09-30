package com.kevin.gestorproducao.ui.recyclerview.adapter;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseListAdapter<T, VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH>
{

    protected final List<T> lista = new ArrayList<>();

    public void atualiza(List<T> novaLista) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
            getDiffCallback(lista, novaLista)
        );

        lista.clear();
        lista.addAll(novaLista);
        diffResult.dispatchUpdatesTo(this);
    }

    public void remove(int posicao) {
        if (posicao < 0 || posicao >= lista.size()) return;
        lista.remove(posicao);
        notifyItemRemoved(posicao);
    }

    public void adiciona(T item, int posicao) {
        if (posicao < 0 || posicao > lista.size()) return;
        lista.add(posicao, item);
        notifyItemInserted(posicao);
    }

    public T getItem(int posicao) {
        return lista.get(posicao);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    protected abstract DiffUtil.Callback getDiffCallback(List<T> antiga, List<T> nova);

    public void limpaLista() {
        atualiza(new ArrayList<>());
    }
}