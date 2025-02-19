package com.kevin.ceep.repository;

import static com.kevin.ceep.ui.activity.Constantes.CHAVE_LISTA_PERSONAGEM;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_LISTA_PROFISSAO;
import static com.kevin.ceep.ui.activity.Constantes.CHAVE_USUARIOS;
import static com.kevin.ceep.utilitario.Utilitario.comparaString;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
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
    private final DatabaseReference minhaReferencia;

    public ProfissaoRepository(String personagemID) {
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.minhaReferencia = FirebaseDatabase.getInstance().getReference(CHAVE_USUARIOS)
                .child(usuarioID).child(CHAVE_LISTA_PERSONAGEM).child(personagemID)
                .child(CHAVE_LISTA_PROFISSAO);
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
        minhaReferencia.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Profissao> profissoes = new ArrayList<>();
                for (DataSnapshot dn:dataSnapshot.getChildren()){
                    Profissao profissao = dn.getValue(Profissao.class);
                    assert profissao != null;
                    profissao.setId(dn.getKey());
                    profissoes.add(profissao);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    profissoes.sort(Comparator.comparing(Profissao::getExperiencia).reversed());
                }
                liveData.setValue(new Resource<>(profissoes, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                liveData.setValue(new Resource<>(null, databaseError.getMessage()));
            }
        });
        return liveData;
    }

    public LiveData<Resource<Void>> modificaProfissao(Profissao profissaoModificada) {
        MutableLiveData<Resource<Void>> liveData = new MutableLiveData<>();
        minhaReferencia.child(profissaoModificada.getId()).setValue(profissaoModificada).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                liveData.setValue(new Resource<>(null, null));
            } else if (task.isCanceled()) {
                liveData.setValue(new Resource<>(null, Objects.requireNonNull(task.getException()).getMessage()));
            }
        });
        return liveData;
    }
}
