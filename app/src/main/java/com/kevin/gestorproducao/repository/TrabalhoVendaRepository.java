package com.kevin.gestorproducao.repository;

import static com.kevin.gestorproducao.ui.activity.Constantes.CHAVE_VENDAS;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.gestorproducao.dao.VendaDao;
import com.kevin.gestorproducao.model.TrabalhoChanceVenda;
import com.kevin.gestorproducao.model.TrabalhoVendido;
import com.kevin.gestorproducao.repository.helper.FirebaseTimeoutHelper;
import com.kevin.gestorproducao.service.AnaliseVendaService;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TrabalhoVendaRepository {
    private static volatile TrabalhoVendaRepository instancia;
    private final DatabaseReference referenciaVendasIdPersonagem;
    private final DatabaseReference referenciaVendas;
    private ValueEventListener ouvinteVenda, ouvinteVendaIdPersonagem;
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);
    private final VendaDao vendaDao;

    public TrabalhoVendaRepository(Context context) {
        FirebaseDatabase meuBanco = FirebaseDatabase.getInstance();

        this.referenciaVendas = meuBanco.getReference(CHAVE_VENDAS);
        this.referenciaVendasIdPersonagem = meuBanco.getReference(CHAVE_VENDAS);
        this.vendaDao = new VendaDao(context);
    }

    public static synchronized TrabalhoVendaRepository getInstance(Context context) {
        if (instancia == null) {
            instancia = new TrabalhoVendaRepository(context);
        }

        return instancia;
    }
    public LiveData<Resource<ArrayList<TrabalhoVendido>>> recuperaVendasPorTrabalho(
        String idPersonagem,
        String idTrabalho
    ) {
        MutableLiveData<Resource<ArrayList<TrabalhoVendido>>> liveData = new MutableLiveData<>();

        liveData.setValue(new Resource<>(null, null));

        backgroundExecutor.execute(() -> {
            try {
                ArrayList<TrabalhoVendido> vendas = new ArrayList<>(
                    vendaDao.recuperaVendasPorTrabalho(idPersonagem, idTrabalho)
                );

                liveData.postValue(new Resource<>(vendas, null));
            } catch (Exception e) {
                liveData.postValue(new Resource<>(null, e.getMessage()));
            }
        });

        return liveData;
    }

    public LiveData<Resource<TrabalhoVendido>> recuperaUltimaVenda(
        String idPersonagem,
        String idTrabalho
    ) {
        MutableLiveData<Resource<TrabalhoVendido>> liveData = new MutableLiveData<>();

        backgroundExecutor.execute(() -> {
            try {
                TrabalhoVendido venda = vendaDao.recuperaUltimaVenda(idPersonagem, idTrabalho);

                liveData.postValue(new Resource<>(venda, null));
            } catch (Exception e) {
                liveData.postValue(new Resource<>(null, e.getMessage()));
            }
        });

        return liveData;
    }

    public LiveData<Resource<Void>> removeTrabalho(
        TrabalhoVendido trabalho,
        String idPersonagem
    ) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        if (idVendaInvalida(trabalho)) {
            liveData.setValue(new Resource<>(null, "Venda está nula ou id vazio!"));
            return liveData;
        }
        return FirebaseTimeoutHelper.execute(callback -> referenciaVendasIdPersonagem
            .child(idPersonagem)
            .child(trabalho.getId())
            .removeValue()
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        vendaDao.removeTrabalho(trabalho);
                        callback.sucesso();
                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }
                Exception exception = task.getException();
                String erroEncontrado = recuperaErro(exception, "Erro desconhecido ao remover venda");
                callback.erro(erroEncontrado);
            })
        );
    }

    public LiveData<Resource<Void>> modificaVenda(
        TrabalhoVendido trabalho,
        String idPersonagem
    ) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        String erroValidacao = validarVenda(trabalho);
        if (erroValidacao != null) {
            liveData.setValue(new Resource<>(null, erroValidacao));
            return liveData;
        }

        return FirebaseTimeoutHelper.execute(callback -> referenciaVendasIdPersonagem
            .child(idPersonagem)
            .child(trabalho.getId())
            .setValue(trabalho)
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        vendaDao.modificaVenda(trabalho, idPersonagem);
                        callback.sucesso();
                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }

                Exception exception = task.getException();
                String erroRecuperado = recuperaErro(exception, "Erro desconhecido ao modificar venda");
                callback.erro(erroRecuperado);
            })
        );
    }

    private String validarVenda(TrabalhoVendido trabalho) {
        if (idVendaInvalida(trabalho)) {
            return "ID da venda inválido";
        }

        if (trabalho.getCriadoEm() == null) {
            return "Campo 'criadoEm' é obrigatório";
        }

        if (trabalho.getModificadoEm() == null) {
            return "Campo 'modificadoEm' é obrigatório";
        }

        if (trabalho.getDescricao() == null || trabalho.getDescricao().isEmpty()) {
            return "Campo 'descricao' é obrigatório";
        }

        return null;
    }

    public LiveData<Resource<ArrayList<TrabalhoChanceVenda>>> recuperaAnaliseChanceVenda(
        String idPersonagem,
        long dataInicio,
        long dataFim
    ) {
        MutableLiveData<Resource<ArrayList<TrabalhoChanceVenda>>> analise = new MutableLiveData<>();

        analise.setValue(new Resource<>(null, null));

        backgroundExecutor.execute(() -> {
            try {
                ArrayList<TrabalhoChanceVenda> trabalhos = vendaDao.recuperaAnaliseChanceVenda(
                    idPersonagem,
                    dataInicio,
                    dataFim
                );

                AnaliseVendaService.ordenarPorChanceVenda(trabalhos, System.currentTimeMillis());

                analise.postValue(new Resource<>(trabalhos, null));
            } catch (Exception e) {
                analise.postValue(new Resource<>(null, e.getMessage()));
            }
        });

        return analise;
    }

    public void removeReferenciaTrabalhoEspecfico(String idTrabalho) {
        ouvinteVenda = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot vendaSnapshot: snapshot.getChildren()) {
                    for (DataSnapshot registroSnapshot : vendaSnapshot.getChildren()) {

                        String idTrabalhoFirebase = registroSnapshot.child("idTrabalho").getValue(String.class);

                        if (idTrabalhoFirebase != null && idTrabalhoFirebase.equals(idTrabalho)) {
                            registroSnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("repositorioVendas", "Erro: " + error.getMessage());
            }
        };

        referenciaVendas.addListenerForSingleValueEvent(ouvinteVenda);
    }

    public LiveData<Resource<Void>> insereVenda(
        TrabalhoVendido trabalho,
        String idPersonagem
    ) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();

        String erroValidacao = validarVenda(trabalho);
        if (erroValidacao != null) {
            liveData.setValue(new Resource<>(null, erroValidacao));
            return liveData;
        }

        return FirebaseTimeoutHelper.execute(callback -> referenciaVendasIdPersonagem
            .child(idPersonagem)
            .child(trabalho.getId())
            .setValue(trabalho)
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        vendaDao.insereVenda(trabalho, idPersonagem);
                        callback.sucesso();

                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }
                Exception exception = task.getException();
                String erroRecuperado = recuperaErro(exception, "Erro desconhecido ao inserir venda");
                callback.erro(erroRecuperado);
            })
        );
    }

    private String recuperaErro(Exception exception, String erroPadrao) {
        return exception == null ? erroPadrao : exception.getMessage();
    }

    private static boolean idVendaInvalida(TrabalhoVendido trabalho) {
        return trabalho == null || trabalho.getId() == null || trabalho.getId().isEmpty();
    }

    public void removeOuvinte() {
        if (referenciaVendasIdPersonagem != null && ouvinteVendaIdPersonagem != null) {
            referenciaVendasIdPersonagem.removeEventListener(ouvinteVendaIdPersonagem);
        }
        if (referenciaVendas != null && ouvinteVenda != null) {
            referenciaVendas.removeEventListener(ouvinteVenda);
        }
    }

    public LiveData<Resource<Void>> sincronizaVendas(String idPersonagem) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        ArrayList<TrabalhoVendido> vendas = new ArrayList<>();

        ouvinteVendaIdPersonagem = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vendas.clear();

                for (DataSnapshot dn : snapshot.getChildren()) {
                    TrabalhoVendido trabalho = dn.getValue(TrabalhoVendido.class);

                    if (trabalho != null) {
                        vendas.add(trabalho);
                    }
                }

                backgroundExecutor.execute(() -> {
                    try {
                        vendaDao.substituirTodos(vendas, idPersonagem);

                        liveData.postValue(new Resource<>(null, null));
                    } catch (RuntimeException e) {
                        liveData.postValue(new Resource<>(null, e.getMessage()));
                    }
                });

                liveData.setValue(new Resource<>(null, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(new Resource<>(null, error.getMessage()));
            }
        };

        if (idPersonagem == null || idPersonagem.isEmpty()) return liveData;

        referenciaVendasIdPersonagem.child(idPersonagem).addListenerForSingleValueEvent(ouvinteVendaIdPersonagem);
        return liveData;
    }

    public LiveData<Resource<ArrayList<TrabalhoVendido>>> recuperaMaisVendidos(
        String idPersonagem,
        Long dataInicio,
        Long dataFim
    ) {
        MutableLiveData<Resource<ArrayList<TrabalhoVendido>>> maisVendidos = new MutableLiveData<>();

        maisVendidos.setValue(new Resource<>(null, null));

        backgroundExecutor.execute(() -> {
            try {
                ArrayList<TrabalhoVendido> vendas = new ArrayList<>(
                    vendaDao.recuperaMaisVendidos(idPersonagem, dataInicio, dataFim)
                );

                maisVendidos.postValue(new Resource<>(vendas, null));
            } catch (Exception e) {
                maisVendidos.postValue(new Resource<>(null, e.getMessage()));
            }
        });

        return maisVendidos;
    }

    public LiveData<Resource<Void>> removeVendas(String idPersoangem) {
        return FirebaseTimeoutHelper.execute(callback -> referenciaVendas
            .child(idPersoangem)
            .removeValue()
            .addOnCompleteListener(backgroundExecutor, task -> {
                if (task.isSuccessful()) {
                    try {
                        vendaDao.removeVendas(idPersoangem);
                        callback.sucesso();
                    } catch (RuntimeException e) {
                        callback.erro(e.getMessage());
                    }
                    return;
                }

                Exception exception = task.getException();
                String erro = recuperaErro(exception, "Erro desconhecido ao remover vendas");
                callback.erro(erro);
            })
        );
    }
}
