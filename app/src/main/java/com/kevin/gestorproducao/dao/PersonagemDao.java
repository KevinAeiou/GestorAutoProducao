package com.kevin.gestorproducao.dao;

import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_AUTO_PRODUCAO;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_EMAIL;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ESPACO_PRODUCAO;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ESTADO;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_SENHA;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_USO;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.TABLE_PERSONAGENS;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.kevin.gestorproducao.db.DbHelper;
import com.kevin.gestorproducao.model.Personagem;
import com.kevin.gestorproducao.utilitario.CriptografiaUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PersonagemDao extends BaseDao {
    public PersonagemDao(Context context) {
        super(DbHelper.getInstance(context).getWritableDatabase());
    }

    public ArrayList<Personagem> recuperaPersonagens() {
        ArrayList<Personagem> personagens = new ArrayList<>();

        String query = "SELECT *  FROM " + TABLE_PERSONAGENS;

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                Personagem personagem = new Personagem();
                boolean estado = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ESTADO)) == 1;
                boolean uso = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_USO)) == 1;
                boolean autoProducao = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_AUTO_PRODUCAO)) == 1;

                personagem.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
                personagem.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
                personagem.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_EMAIL)));
                personagem.setSenha(
                    CriptografiaUtil.decriptar(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_SENHA)))
                );
                personagem.setEstado(estado);
                personagem.setUso(uso);
                personagem.setAutoProducao(autoProducao);
                personagem.setEspacoProducao(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ESPACO_PRODUCAO)));

                personagens.add(personagem);
            }
        }

        return personagens;
    }

    public void substituirTodos(ArrayList<Personagem> personagens) {
        executaEmTransacao(() -> {
            // A senha nunca vem do Firebase (Usuario.senha e @Exclude) — preserva a cifra ja
            // gravada localmente por id, senao esta substituicao completa apaga a senha salva
            // no aparelho a cada sincronizacao.
            Map<String, String> senhasCifradasPorId = recuperaSenhasCifradasPorId();

            db.delete(TABLE_PERSONAGENS, null, null);

            for (Personagem personagem : personagens) {
                ContentValues values = getValues(personagem);

                String senhaCifradaExistente = senhasCifradasPorId.get(personagem.getId());
                if (senhaCifradaExistente != null) {
                    values.put(COLUMN_NAME_SENHA, senhaCifradaExistente);
                }

                db.insert(TABLE_PERSONAGENS, null, values);
            }
        });
    }

    private Map<String, String> recuperaSenhasCifradasPorId() {
        Map<String, String> senhasCifradasPorId = new HashMap<>();

        String query = "SELECT " + COLUMN_NAME_ID + ", " + COLUMN_NAME_SENHA +
            " FROM " + TABLE_PERSONAGENS;

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                senhasCifradasPorId.put(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_SENHA))
                );
            }
        }

        return senhasCifradasPorId;
    }

    public void inserePersonagem(Personagem personagem) {
        ContentValues values = getValues(personagem);

        insereEmTransacao(TABLE_PERSONAGENS, values);
    }

    private ContentValues getValues(Personagem personagem) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID, personagem.getId());
        values.put(COLUMN_NAME_NOME, personagem.getNome());
        values.put(COLUMN_NAME_EMAIL, personagem.getEmail());
        // Cifrada com a chave do Android Keystore deste aparelho (achado C1) — por isso não
        // é sincronizada com o Firebase (ver @Exclude em Usuario.senha): uma chave presa ao
        // Keystore não seria decifrável em outro dispositivo.
        values.put(COLUMN_NAME_SENHA, CriptografiaUtil.encriptar(personagem.getSenha()));
        boolean estado = personagem.getEstado();
        values.put(COLUMN_NAME_ESTADO, estado);
        values.put(COLUMN_NAME_USO, personagem.getUso());
        values.put(COLUMN_NAME_AUTO_PRODUCAO, personagem.isAutoProducao());
        values.put(COLUMN_NAME_ESPACO_PRODUCAO, personagem.getEspacoProducao());

        return values;
    }

    public void removePersonagem(String idPersonagem) {
        String whereClause = COLUMN_NAME_ID + " = ?";
        String[] whereArgs = {idPersonagem};

        removeEmTransacao(TABLE_PERSONAGENS, whereClause, whereArgs);
    }

    public void modificaPersonagem(Personagem personagem) {
        ContentValues values = getValues(personagem);
        String[] whereArgs = {personagem.getId()};
        String whereClause = COLUMN_NAME_ID + " = ?";

        atualizaEmTransacao(TABLE_PERSONAGENS, values, whereClause, whereArgs);
    }
}
