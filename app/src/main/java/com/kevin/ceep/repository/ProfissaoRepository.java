package com.kevin.ceep.repository;

import static com.kevin.ceep.utilitario.Utilitario.comparaString;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.TrabalhoProducao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class ProfissaoRepository {
    private static final String CHAVE_PROFISSOES = "Profissoes";
    private static final String CHAVE_LISTA_PROFISSOES = "Lista_profissoes";
    private final DatabaseReference referenciaProfissoes;
    private final DatabaseReference referenciaListaProfissoes;

    public ProfissaoRepository(String idPersonagem) {
        FirebaseDatabase meuBanco= FirebaseDatabase.getInstance();
        this.referenciaProfissoes = meuBanco.getReference(CHAVE_PROFISSOES).child(idPersonagem);
        this.referenciaListaProfissoes = meuBanco.getReference(CHAVE_LISTA_PROFISSOES);
    }

    public Profissao retornaProfissaoModificada(ArrayList<Profissao> profissoes, TrabalhoProducao trabalhoModificado) {
        for (Profissao profissao : profissoes) {
            if (comparaString(profissao.getNome(), trabalhoModificado.getProfissao())) {
                Log.d("profissao", "Profissao encontrada: "+ profissao.getNome());
                return profissao;
            }
        }
        return null;
    }

    public LiveData<Resource<ArrayList<Profissao>>> pegaTodasProfissoes() {
        MutableLiveData<Resource<ArrayList<Profissao>>> liveData = new MutableLiveData<>();
        referenciaProfissoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Profissao> profissoes = new ArrayList<>();
                for (DataSnapshot dn:dataSnapshot.getChildren()) {
                    Profissao profissao = dn.getValue(Profissao.class);
                    assert profissao != null;
                    profissoes.add(profissao);
                }
                if (profissoes.isEmpty()) {
                    insereNovasProfissoes();
                }
                for (Profissao profissao: profissoes) {
                    Log.d("profissao", "ID profissao: " + profissao.getId());
                    referenciaListaProfissoes.child(profissao.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Profissao valor= snapshot.getValue(Profissao.class);
                            assert valor != null;
                            profissao.setNome(valor.getNome());
                            if (profissoes.indexOf(profissao) + 1 == profissoes.size()) {
                                profissoes.sort(Comparator.comparing(Profissao::getNome));
                                profissoes.sort(Comparator.comparing(Profissao::getExperiencia).reversed());
                                liveData.setValue(new Resource<>(profissoes, null));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                liveData.setValue(new Resource<>(null, databaseError.getMessage()));
            }
        });
        return liveData;
    }

    private void insereNovasProfissoes() {
        referenciaListaProfissoes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn: snapshot.getChildren()) {
                    Profissao valor= dn.getValue(Profissao.class);
                    if (valor == null) continue;
                    Profissao profissao= new Profissao();
                    profissao.setId(valor.getId());
                    profissao.setExperiencia(0);
                    profissao.setPrioridade(false);
                    referenciaProfissoes.child(profissao.getId()).setValue(profissao);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<Resource<Void>> modificaProfissao(Profissao profissaoModificada) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        Profissao profissao= new Profissao();
        profissao.setId(profissaoModificada.getId());
        profissao.setExperiencia(profissaoModificada.getExperiencia());
        profissao.setPrioridade(profissaoModificada.isPrioridade());
        referenciaProfissoes.child(profissaoModificada.getId()).setValue(profissao).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                liveData.setValue(new Resource<>(null, null));
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).getMessage()));
            }
        });
        return liveData;
    }
}
