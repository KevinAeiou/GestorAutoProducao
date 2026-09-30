package com.kevin.gestorproducao.dao;

import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_PERSONAGEM;
import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_TRABALHO;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoEntry.TABLE_PROFISSOES;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_EXPERIENCIA;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NIVEL;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME_PRODUCAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_PROFISSAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_RARIDADE;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.TABLE_TRABALHOS;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoNecessarioEntry.COLUMN_TRABALHO_NECESSARIO_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoNecessarioEntry.TABLE_TRABALHOS_NECESSARIOS;
import static com.kevin.gestorproducao.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.TABLE_TRABALHOS_VENDIDOS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.kevin.gestorproducao.db.DbHelper;
import com.kevin.gestorproducao.model.Trabalho;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrabalhoDao extends BaseDao {
    public TrabalhoDao(Context context) {
        super(DbHelper.getInstance(context).getWritableDatabase());
    }

    public ArrayList<Trabalho> recuperaTrabalhos () {
        ArrayList<Trabalho> trabalhos = new ArrayList<>();

        String query =
            "SELECT t.*, " +
            "p." + COLUMN_NAME_NOME + " AS profissao_nome, " +
            "GROUP_CONCAT(tn." + COLUMN_TRABALHO_NECESSARIO_ID + ") AS trabalhos_necessarios " +
            "FROM " + TABLE_TRABALHOS + " t " +
            "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
            " = p." + COLUMN_NAME_ID + " " +
            "LEFT JOIN " + TABLE_TRABALHOS_NECESSARIOS + " tn ON t." + COLUMN_NAME_ID +
            " = tn." + COLUMN_NAME_ID_TRABALHO + " " +
            "GROUP BY t." + COLUMN_NAME_ID + " " +
            "ORDER BY " +
            "profissao_nome ASC, " +
            "t." + COLUMN_NAME_RARIDADE + " ASC, " +
            "t." + COLUMN_NAME_NIVEL + " ASC, " +
            "t." + COLUMN_NAME_EXPERIENCIA + " ASC, " +
            "t." + COLUMN_NAME_NOME + " ASC";

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                Trabalho trabalho = new Trabalho();

                trabalho.setId(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
                );
                trabalho.setNome(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME))
                );
                trabalho.setNomeProducao(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO))
                );
                trabalho.setExperiencia(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA))
                );
                trabalho.setNivel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL))
                );
                trabalho.setProfissao(
                    cursor.getString(cursor.getColumnIndexOrThrow("profissao_nome"))
                );
                trabalho.setRaridade(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE))
                );
                String necessariosStr = cursor.getString(
                        cursor.getColumnIndexOrThrow("trabalhos_necessarios")
                );

                Map<String, Boolean> necessariosMap = new HashMap<>();

                if (necessariosStr != null && !necessariosStr.isEmpty()) {
                    String[] ids = necessariosStr.split(",");

                    for (String id : ids) {
                        necessariosMap.put(id, true);
                    }
                }

                trabalho.setNecessarios(necessariosMap);

                trabalhos.add(trabalho);
            }
        }

        return trabalhos;
    }

    public void substituirTodos(ArrayList<Trabalho> trabalhosServidor) {
        executaEmTransacao(() -> {
            db.delete(TABLE_TRABALHOS, null, null);
            db.delete(TABLE_TRABALHOS_NECESSARIOS, null, null);

            for (Trabalho trabalho : trabalhosServidor) {
                ContentValues values = getValues(trabalho);

                db.insert(TABLE_TRABALHOS, null, values);
                if (trabalho.possuiTrabalhosNecessarios()) {
                    for (String idTrabalho : trabalho.getListaTrabalhosNecessarios()) {
                        ContentValues values1 = new ContentValues();
                        values1.put(COLUMN_NAME_ID_TRABALHO, trabalho.getId());
                        values1.put(COLUMN_TRABALHO_NECESSARIO_ID, idTrabalho);

                        db.insert(TABLE_TRABALHOS_NECESSARIOS, null, values1);
                    }
                }
            }
        });
    }

    @NonNull
    private static ContentValues getValues(Trabalho trabalho) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_ID, trabalho.getId());
        values.put(COLUMN_NAME_NOME, trabalho.getNome());
        values.put(COLUMN_NAME_NOME_PRODUCAO, trabalho.getNomeProducao());
        values.put(COLUMN_NAME_EXPERIENCIA, trabalho.getExperiencia());
        values.put(COLUMN_NAME_NIVEL, trabalho.getNivel());
        values.put(COLUMN_NAME_PROFISSAO, trabalho.getProfissao());
        values.put(COLUMN_NAME_RARIDADE, trabalho.getRaridade());

        return values;
    }

    public void removerTrabalho(Trabalho trabalho) {
        executaEmTransacao(() -> {
            String selection = COLUMN_NAME_ID + " = ?";
            String[] selectionArgs = {trabalho.getId()};
            db.delete(TABLE_TRABALHOS, selection, selectionArgs);

            db.delete(
                TABLE_TRABALHOS_NECESSARIOS,
                COLUMN_NAME_ID_TRABALHO + " = ?",
                selectionArgs
            );
        });
    }

    public ArrayList<Trabalho> recuperaTrabalhosNecessarios(Trabalho trabalho) {
        ArrayList<Trabalho> trabalhos = new ArrayList<>();

        String selection =
            "SELECT t.*, p.nome AS nome_profissao " +
                "FROM " + TABLE_TRABALHOS + " t " +
                "INNER JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
                " = p." + COLUMN_NAME_ID + " " +
                "WHERE p.nome = ? " +
                    "AND t." + COLUMN_NAME_NIVEL + " = ? " +
                    "AND t." + COLUMN_NAME_RARIDADE + " = ?";

        String[] selectionArgs = {trabalho.getProfissao(), String.valueOf(trabalho.getNivel()), trabalho.getRaridade()};

        try (Cursor cursor = db.rawQuery(selection, selectionArgs)) {
            while (cursor.moveToNext()) {
                Trabalho trabalhoEncontrado = new Trabalho();

                trabalhoEncontrado.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
                trabalhoEncontrado.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));

                trabalhos.add(trabalhoEncontrado);
            }
        }

        return trabalhos;
    }

    public Trabalho recuperaTrabalhoPorNome(String nome) {
        String selection =
            "SELECT t.*, p.nome AS nome_profissao " +
            "FROM " + TABLE_TRABALHOS + " t " +
            "INNER JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO + " = p." + COLUMN_NAME_ID + " " +
            "WHERE REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(t.nome), ' ', ''), 'ç', 'c'), 'ã', 'a'), 'ô', 'o'), 'é', 'e'), 'á', 'a'), 'â', 'a'), 'ó', 'o') = ?";
        String[] selectionArgs = {nome};

        try (Cursor cursor = db.rawQuery(selection, selectionArgs)) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            Trabalho trabalho = new Trabalho();
            trabalho.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            trabalho.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
            trabalho.setNomeProducao(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO)));
            trabalho.setExperiencia(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)));
            trabalho.setNivel(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL)));
            trabalho.setProfissao(cursor.getString(cursor.getColumnIndexOrThrow("nome_profissao")));
            trabalho.setRaridade(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE)));

            return trabalho;
        }
    }

    public String trabalhoJaExiste(Trabalho trabalho) {
        String selection =
            "SELECT t." + COLUMN_NAME_ID +
            " FROM " + TABLE_TRABALHOS + " t"+
            " INNER JOIN " + TABLE_PROFISSOES + " p" +
            " WHERE t." + COLUMN_NAME_NOME + " = ?" +
            " AND t." + COLUMN_NAME_NOME_PRODUCAO + " = ?" +
            " AND t." + COLUMN_NAME_NIVEL + " = ?" +
            " AND t." + COLUMN_NAME_EXPERIENCIA + " = ?" +
            " AND p." + COLUMN_NAME_NOME + " = ?" +
            " AND t." + COLUMN_NAME_RARIDADE + " = ?;";

        String[] selectionArgs = {
            trabalho.getNome(),
            trabalho.getNomeProducao(),
            String.valueOf(trabalho.getNivel()),
            String.valueOf(trabalho.getExperiencia()),
            trabalho.getProfissao(),
            trabalho.getRaridade()
        };

        try (Cursor cursor = db.rawQuery(selection, selectionArgs)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID));
            }
        }

        return null;
    }

    public Trabalho recuperaTrabalhoPorId(String idTrabalho) {
        String selection = "SELECT t.*, "+
            "p." + COLUMN_NAME_NOME + " AS profissao_nome, " +
            "GROUP_CONCAT(tn." + COLUMN_TRABALHO_NECESSARIO_ID + ") AS trabalhos_necessarios " +
            "FROM " + TABLE_TRABALHOS + " t " +
            "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
            " = p." + COLUMN_NAME_ID + " " +
            "LEFT JOIN " + TABLE_TRABALHOS_NECESSARIOS + " tn ON t." + COLUMN_NAME_ID +
            " = tn." + COLUMN_NAME_ID_TRABALHO + " " +
            "WHERE t." + COLUMN_NAME_ID + " = ? " +
            "LIMIT 1";
        String[] selectionArgs = {idTrabalho};

        try (Cursor cursor = db.rawQuery(selection, selectionArgs)) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            Trabalho trabalho = new Trabalho();
            trabalho.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            trabalho.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
            trabalho.setNomeProducao(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO)));
            trabalho.setExperiencia(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)));
            trabalho.setNivel(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL)));
            trabalho.setProfissao(cursor.getString(cursor.getColumnIndexOrThrow("profissao_nome")));
            trabalho.setRaridade(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE)));

            String necessariosStr = cursor.getString(
                    cursor.getColumnIndexOrThrow("trabalhos_necessarios")
            );

            Map<String, Boolean> necessariosMap = new HashMap<>();

            if (necessariosStr != null && !necessariosStr.isEmpty()) {
                String[] ids = necessariosStr.split(",");

                for (String id : ids) {
                    necessariosMap.put(id, true);
                }
            }

            trabalho.setNecessarios(necessariosMap);

            return trabalho;
        }
    }

    public Trabalho recuperaTrabalhoProducaoRecursos(Trabalho trabalhoBase) {
        String nivel = trabalhoBase.getNivel() > 14 ? "10" : "3";
        String experiencia = trabalhoBase.getNivel() > 14 ? "330" : "70";
        String profissao = trabalhoBase.getProfissao();

        String selection = "SELECT t.*, "+
            "p." + COLUMN_NAME_NOME + " AS profissao_nome " +
            "FROM " + TABLE_TRABALHOS + " t " +
            "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
            " = p." + COLUMN_NAME_ID + " " +
            "WHERE t." + COLUMN_NAME_NIVEL + " = ? " +
                "AND p.nome = ? " +
                "AND t." + COLUMN_NAME_EXPERIENCIA + " = ? " +
            "LIMIT 1";

        String[] selectionArgs = {nivel, profissao, experiencia};

        try (Cursor cursor = db.rawQuery(selection, selectionArgs)) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            Trabalho trabalho = new Trabalho();
            trabalho.setId(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
            );
            trabalho.setNome(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME))
            );
            trabalho.setNomeProducao(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO))
            );
            trabalho.setExperiencia(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA))
            );
            trabalho.setNivel(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL))
            );
            trabalho.setProfissao(
                cursor.getString(cursor.getColumnIndexOrThrow("profissao_nome"))
            );
            trabalho.setRaridade(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE))
            );

            return trabalho;
        }
    }

    public ArrayList<Trabalho> recuperaMaisVendidos(String idPersonagem) {
        ArrayList<Trabalho> trabalhos = new ArrayList<>();

        String query =
            "SELECT " +
                "t." + COLUMN_NAME_ID + ", " +
                "t." + COLUMN_NAME_NOME + ", " +
                "t." + COLUMN_NAME_NOME_PRODUCAO + ", " +
                "t." + COLUMN_NAME_NIVEL + ", " +
                "t." + COLUMN_NAME_EXPERIENCIA + ", " +
                "t." + COLUMN_NAME_RARIDADE + ", " +
                "p." + COLUMN_NAME_NOME + " AS profissao_nome, " +
                "COUNT(v." + COLUMN_NAME_ID + ") AS total_vendas, " +
                "GROUP_CONCAT(tn." + COLUMN_TRABALHO_NECESSARIO_ID + ") AS trabalhos_necessarios " +
                "FROM " + TABLE_TRABALHOS_VENDIDOS + " v " +
                "INNER JOIN " + TABLE_TRABALHOS + " t ON v." +
                COLUMN_NAME_ID_TRABALHO + " = t." + COLUMN_NAME_ID + " " +
                "LEFT JOIN " + TABLE_TRABALHOS_NECESSARIOS + " tn ON t." + COLUMN_NAME_ID +
                " = tn." + COLUMN_NAME_ID_TRABALHO + " " +
                "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
                " = p." + COLUMN_NAME_ID + " " +
                "WHERE v." + COLUMN_NAME_ID_PERSONAGEM + " = ? " +
                "AND t." + COLUMN_NAME_RARIDADE + " = ? " +
                "GROUP BY t." + COLUMN_NAME_ID + " " +
                "ORDER BY " +
                "total_vendas DESC, " +
                "t." + COLUMN_NAME_NOME + " ASC ";

        try (Cursor cursor = db.rawQuery(
            query,
            new String[]{idPersonagem, "Raro"}
        )) {
            while (cursor.moveToNext()) {
                Trabalho trabalho = new Trabalho();
                trabalho.setId(cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)
                ));
                trabalho.setNome(cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)
                ));
                trabalho.setNomeProducao(cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO)
                ));
                trabalho.setNivel(cursor.getInt(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL)
                ));
                trabalho.setExperiencia(cursor.getInt(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)
                ));
                trabalho.setRaridade(cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE)
                ));
                trabalho.setProfissao(cursor.getString(
                    cursor.getColumnIndexOrThrow("profissao_nome")
                ));
                String necessariosStr = cursor.getString(cursor.getColumnIndexOrThrow("trabalhos_necessarios"));
                Map<String, Boolean> necessariosMap = new HashMap<>();
                if (necessariosStr != null && !necessariosStr.isEmpty()) {
                    String[] ids = necessariosStr.split(",");

                    for (String id : ids) {
                        necessariosMap.put(id, true);
                    }
                }
                trabalho.setNecessarios(necessariosMap);
                if (trabalho.ehProducaoDeRecursos()) continue;

                trabalhos.add(trabalho);
            }
        }

        return trabalhos;
    }

    public ArrayList<Trabalho> recuperaTrabalhosComuns(int nivelProducao, String profissao) {
        ArrayList<Trabalho> trabalhos = new ArrayList<>();

        String query =
            "SELECT t.*, " + "p." + COLUMN_NAME_NOME + " AS profissao_nome, " +
            "GROUP_CONCAT(tn." + COLUMN_TRABALHO_NECESSARIO_ID + ") AS trabalhos_necessarios " +
            "FROM " + TABLE_TRABALHOS + " t " +
            "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
            " = p." + COLUMN_NAME_ID + " " +
            "LEFT JOIN " + TABLE_TRABALHOS_NECESSARIOS + " tn ON t." + COLUMN_NAME_ID +
            " = tn." + COLUMN_NAME_ID_TRABALHO + " " +
            "WHERE t." + COLUMN_NAME_NIVEL + " = ? " +
            "AND t." + COLUMN_NAME_RARIDADE + " = ? " +
            "AND p.nome = ? " +
            "GROUP BY t." + COLUMN_NAME_ID + " " +
            "ORDER BY " + "profissao_nome ASC, " +
            "t." + COLUMN_NAME_RARIDADE + " ASC, " +
            "t." + COLUMN_NAME_NIVEL + " ASC, " +
            "t." + COLUMN_NAME_EXPERIENCIA + " ASC, " +
            "t." + COLUMN_NAME_NOME + " ASC";

        String[] selectionArgs = {String.valueOf(nivelProducao), "Comum", profissao};

        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            while (cursor.moveToNext()) {
                Trabalho trabalho = new Trabalho();

                trabalho.setId(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
                );
                trabalho.setNome(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME))
                );
                trabalho.setNomeProducao(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO))
                );
                trabalho.setExperiencia(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA))
                );
                trabalho.setNivel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL))
                );
                trabalho.setProfissao(
                        cursor.getString(cursor.getColumnIndexOrThrow("profissao_nome"))
                );
                trabalho.setRaridade(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE))
                );
                String necessariosStr = cursor.getString(
                        cursor.getColumnIndexOrThrow("trabalhos_necessarios")
                );

                Map<String, Boolean> necessariosMap = new HashMap<>();

                if (necessariosStr != null && !necessariosStr.isEmpty()) {
                    String[] ids = necessariosStr.split(",");

                    for (String id : ids) {
                        necessariosMap.put(id, true);
                    }
                }

                trabalho.setNecessarios(necessariosMap);

                trabalhos.add(trabalho);
            }
        }

        return trabalhos;
    }

    public void insereTrabalho(Trabalho trabalho) {
        executaEmTransacao(() -> {
            ContentValues values = getValues(trabalho);

            db.insert(TABLE_TRABALHOS, null, values);
            if (trabalho.possuiTrabalhosNecessarios()) {
                for (String idTrabalho : trabalho.getListaTrabalhosNecessarios()) {
                    ContentValues values1 = new ContentValues();
                    values1.put(COLUMN_NAME_ID_TRABALHO, trabalho.getId());
                    values1.put(COLUMN_TRABALHO_NECESSARIO_ID, idTrabalho);

                    db.insert(TABLE_TRABALHOS_NECESSARIOS, null, values1);
                }
            }
        });
    }

    public void modificaTrabalho(Trabalho trabalho) {
        executaEmTransacao(() -> {
            ContentValues values = getValues(trabalho);

            String selection = COLUMN_NAME_ID + " = ?";
            String[] selectionArgs = {trabalho.getId()};

            db.update(
                TABLE_TRABALHOS,
                values,
                selection,
                selectionArgs
            );

            db.delete(
                TABLE_TRABALHOS_NECESSARIOS,
                COLUMN_NAME_ID_TRABALHO + " = ?",
                new String[]{trabalho.getId()}
            );

            if (trabalho.possuiTrabalhosNecessarios()) {
                for (String idTrabalho : trabalho.getListaTrabalhosNecessarios()) {

                    ContentValues values1 = new ContentValues();

                    values1.put(
                        COLUMN_NAME_ID_TRABALHO,
                        trabalho.getId()
                    );

                    values1.put(
                        COLUMN_TRABALHO_NECESSARIO_ID,
                        idTrabalho
                    );

                    db.insert(
                        TABLE_TRABALHOS_NECESSARIOS,
                        null,
                        values1
                    );
                }
            }
        });
    }

    public Trabalho recuperaTrabalhoPorIdTrabalhoNecessario(String idTrabalho) {
        String selection =
            "SELECT t.*, " +
                "p." + COLUMN_NAME_NOME + " AS profissao_nome, " +
                "GROUP_CONCAT(tn2." + COLUMN_TRABALHO_NECESSARIO_ID + ") AS trabalhos_necessarios " +
                "FROM " + TABLE_TRABALHOS + " t " +
                "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
                " = p." + COLUMN_NAME_ID + " " +
                "INNER JOIN " + TABLE_TRABALHOS_NECESSARIOS + " tn ON t." + COLUMN_NAME_ID +
                " = tn." + COLUMN_NAME_ID_TRABALHO + " " +
                "LEFT JOIN " + TABLE_TRABALHOS_NECESSARIOS + " tn2 ON t." + COLUMN_NAME_ID +
                " = tn2." + COLUMN_NAME_ID_TRABALHO + " " +
                "WHERE tn." + COLUMN_TRABALHO_NECESSARIO_ID + " = ? " +
                "GROUP BY t." + COLUMN_NAME_ID + " " +
                "LIMIT 1";

        String[] selectionArgs = {idTrabalho};

        try (Cursor cursor = db.rawQuery(selection, selectionArgs)) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            Trabalho trabalho = new Trabalho();
            trabalho.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            trabalho.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME)));
            trabalho.setNomeProducao(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO)));
            trabalho.setExperiencia(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA)));
            trabalho.setNivel(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL)));
            trabalho.setProfissao(cursor.getString(cursor.getColumnIndexOrThrow("profissao_nome")));
            trabalho.setRaridade(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE)));

            String necessariosStr = cursor.getString(
                cursor.getColumnIndexOrThrow("trabalhos_necessarios")
            );

            Map<String, Boolean> necessariosMap = new HashMap<>();

            if (necessariosStr != null && !necessariosStr.isEmpty()) {
                String[] ids = necessariosStr.split(",");

                for (String id : ids) {
                    necessariosMap.put(id, true);
                }
            }

            trabalho.setNecessarios(necessariosMap);

            return trabalho;
        }
    }

    public ArrayList<Trabalho> recuperaTrabalhosNecessariosPorId(List<String> trabalhosNecessarios) {
        ArrayList<Trabalho> trabalhos = new ArrayList<>();

        if (trabalhosNecessarios == null || trabalhosNecessarios.isEmpty()) {
            return trabalhos;
        }

        String query = getString(trabalhosNecessarios);

        String[] selectionArgs = trabalhosNecessarios.toArray(new String[0]);

        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            while (cursor.moveToNext()) {
                Trabalho trabalho = new Trabalho();

                trabalho.setId(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID))
                );

                trabalho.setNome(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME))
                );

                trabalho.setNomeProducao(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOME_PRODUCAO))
                );

                trabalho.setExperiencia(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXPERIENCIA))
                );

                trabalho.setNivel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_NIVEL))
                );

                trabalho.setProfissao(
                        cursor.getString(cursor.getColumnIndexOrThrow("profissao_nome"))
                );

                trabalho.setRaridade(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RARIDADE))
                );

                String necessariosStr = cursor.getString(
                        cursor.getColumnIndexOrThrow("trabalhos_necessarios")
                );

                Map<String, Boolean> necessariosMap = new HashMap<>();

                if (necessariosStr != null && !necessariosStr.isEmpty()) {
                    String[] ids = necessariosStr.split(",");

                    for (String id : ids) {
                        necessariosMap.put(id, true);
                    }
                }

                trabalho.setNecessarios(necessariosMap);

                trabalhos.add(trabalho);
            }
        }

        return trabalhos;
    }

    @NonNull
    private static String getString(List<String> trabalhosNecessarios) {
        StringBuilder placeholders = new StringBuilder();

        for (int i = 0; i < trabalhosNecessarios.size(); i++) {
            placeholders.append("?");

            if (i < trabalhosNecessarios.size() - 1) {
                placeholders.append(",");
            }
        }

        return "SELECT t.*, " +
            "p." + COLUMN_NAME_NOME + " AS profissao_nome, " +
            "GROUP_CONCAT(tn." + COLUMN_TRABALHO_NECESSARIO_ID + ") AS trabalhos_necessarios " +
            "FROM " + TABLE_TRABALHOS + " t " +
            "LEFT JOIN " + TABLE_PROFISSOES + " p ON t." + COLUMN_NAME_PROFISSAO +
            " = p." + COLUMN_NAME_ID + " " +
            "LEFT JOIN " + TABLE_TRABALHOS_NECESSARIOS + " tn ON t." + COLUMN_NAME_ID +
            " = tn." + COLUMN_NAME_ID_TRABALHO + " " +
            "WHERE t." + COLUMN_NAME_ID + " IN (" + placeholders + ") " +
            "GROUP BY t." + COLUMN_NAME_ID + " " +
            "ORDER BY t." + COLUMN_NAME_NOME + " ASC";
    }
}
