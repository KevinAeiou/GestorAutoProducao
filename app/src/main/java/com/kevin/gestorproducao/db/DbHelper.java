package com.kevin.gestorproducao.db;

import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_PERSONAGEM;
import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_TRABALHO;
import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_QUANTIDADE;
import static com.kevin.gestorproducao.db.contracts.EstoqueDbContract.EstoqueEntry.TABLE_ESTOQUE;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_AUTO_PRODUCAO;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_EMAIL;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_ESPACO_PRODUCAO;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_SENHA;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.COLUMN_NAME_USO;
import static com.kevin.gestorproducao.db.contracts.PersoagemDbContract.PersonagemEntry.TABLE_PERSONAGENS;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoEntry.TABLE_PROFISSOES;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoPersonagemEntry.COLUMN_NAME_PRIORIDADE;
import static com.kevin.gestorproducao.db.contracts.ProfissaoDbContract.ProfissaoPersonagemEntry.TABLE_PROFISSOES_PERSONAGEM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_CRIADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_EXPERIENCIA;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_MODIFICADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NIVEL;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_NOME_PRODUCAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_PROFISSAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.COLUMN_NAME_RARIDADE;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoEntry.TABLE_TRABALHOS;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoNecessarioEntry.COLUMN_TRABALHO_NECESSARIO_ID;
import static com.kevin.gestorproducao.db.contracts.TrabalhoDbContract.TrabalhoNecessarioEntry.TABLE_TRABALHOS_NECESSARIOS;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_ESTADO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_FINALIZADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_INICIADO_EM;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_LICENCA;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.COLUMN_NAME_RECORRENCIA;
import static com.kevin.gestorproducao.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.TABLE_TRABALHOS_PRODUCAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_DESCRICAO;
import static com.kevin.gestorproducao.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_VALOR;
import static com.kevin.gestorproducao.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.TABLE_TRABALHOS_VENDIDOS;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 13;
    public static final String DATABASE_NAME = "autoProducao.db";
    private static DbHelper minhaInstancia = null;

    public static DbHelper getInstance(Context context) {
        if (minhaInstancia == null) {
            minhaInstancia = new DbHelper(context.getApplicationContext());
        }
        return minhaInstancia;
    }

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_TRABALHOS + " (" +
            COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
            COLUMN_NAME_NOME + " TEXT," +
            COLUMN_NAME_NOME_PRODUCAO + " TEXT," +
            COLUMN_NAME_EXPERIENCIA + " INTEGER," +
            COLUMN_NAME_NIVEL + " INTEGER," +
            COLUMN_NAME_PROFISSAO + " TEXT," +
            COLUMN_NAME_RARIDADE + " TEXT," +
            "FOREIGN KEY(" + COLUMN_NAME_PROFISSAO + ") " +
            "REFERENCES " + TABLE_PROFISSOES + "(" + COLUMN_NAME_ID + ")" +
            ")"
        );
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_TRABALHOS_NECESSARIOS + " (" +
                COLUMN_NAME_ID_TRABALHO + " VARCHAR(30)," +
                COLUMN_TRABALHO_NECESSARIO_ID + " VARCHAR(30)," +
                "PRIMARY KEY(" + COLUMN_NAME_ID_TRABALHO + ", " + COLUMN_TRABALHO_NECESSARIO_ID + ")," +
                "FOREIGN KEY(" + COLUMN_NAME_ID_TRABALHO + ")"  +
                "REFERENCES " + TABLE_TRABALHOS + "(" + COLUMN_NAME_ID + ")," +
                "FOREIGN KEY(" + COLUMN_TRABALHO_NECESSARIO_ID + ")"  +
                "REFERENCES " + TABLE_TRABALHOS + "(" + COLUMN_NAME_ID + ")" +
            ")"
        );
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_PERSONAGENS + " (" +
                COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                COLUMN_NAME_NOME + " TEXT," +
                COLUMN_NAME_EMAIL + " TEXT," +
                COLUMN_NAME_SENHA + " TEXT," +
                COLUMN_NAME_ESTADO + " BOOLEAN," +
                COLUMN_NAME_USO + " BOOLEAN," +
                COLUMN_NAME_AUTO_PRODUCAO + " BOOLEAN," +
                COLUMN_NAME_ESPACO_PRODUCAO + " INTEGER" +
            ")"
        );
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_TRABALHOS_PRODUCAO + " (" +
                COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                COLUMN_NAME_ID_TRABALHO + " VARCHAR(30)," +
                COLUMN_NAME_ID_PERSONAGEM + " VARCHAR(30)," +
                COLUMN_NAME_CRIADO_EM + " INTEGER, " +
                COLUMN_NAME_MODIFICADO_EM + " INTEGER, " +
                COLUMN_NAME_INICIADO_EM + " INTEGER, " +
                COLUMN_NAME_FINALIZADO_EM + " INTEGER, " +
                COLUMN_NAME_LICENCA + " TEXT," +
                COLUMN_NAME_ESTADO + " INTEGER," +
                COLUMN_NAME_RECORRENCIA + " BOOLEAN" +
            ")"
        );
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_ESTOQUE + " (" +
                COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                COLUMN_NAME_ID_TRABALHO + " VARCHAR(30)," +
                COLUMN_NAME_ID_PERSONAGEM + " VARCHAR(30)," +
                COLUMN_NAME_QUANTIDADE + " INTEGER" +
            ")"
        );
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_TRABALHOS_VENDIDOS + " (" +
                COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                COLUMN_NAME_ID_PERSONAGEM + " VARCHAR(30), " +
                COLUMN_NAME_ID_TRABALHO + " VARCHAR(30), " +
                COLUMN_NAME_DESCRICAO + " TEXT, " +
                COLUMN_NAME_CRIADO_EM + " INTEGER, " +
                COLUMN_NAME_MODIFICADO_EM + " INTEGER, " +
                COLUMN_NAME_QUANTIDADE + " INTEGER, " +
                COLUMN_NAME_VALOR + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_NAME_ID_TRABALHO + ")"  +
                "REFERENCES " + TABLE_TRABALHOS + "(" + COLUMN_NAME_ID + ") ON DELETE CASCADE," +
                "FOREIGN KEY(" + COLUMN_NAME_ID_PERSONAGEM + ")"  +
                "REFERENCES " + TABLE_PERSONAGENS + "(" + COLUMN_NAME_ID + ") ON DELETE CASCADE" +
            ")"
        );
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_PROFISSOES + " (" +
                COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                COLUMN_NAME_NOME + " TEXT" +
            ")"
        );
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_PROFISSOES_PERSONAGEM + " (" +
                COLUMN_NAME_ID + " VARCHAR(30) NOT NULL, " +
                COLUMN_NAME_ID_PERSONAGEM + " TEXT NOT NULL, " +
                COLUMN_NAME_EXPERIENCIA + " INTEGER, " +
                COLUMN_NAME_PRIORIDADE + " INTEGER, " +
                "PRIMARY KEY(" + COLUMN_NAME_ID + ", " + COLUMN_NAME_ID_PERSONAGEM + ")," +
                "FOREIGN KEY(" + COLUMN_NAME_ID + ")"  +
                "REFERENCES " + TABLE_PROFISSOES + "(" + COLUMN_NAME_ID + ") ON DELETE CASCADE," +
                "FOREIGN KEY(" + COLUMN_NAME_ID_PERSONAGEM + ")"  +
                "REFERENCES " + TABLE_PERSONAGENS + "(" + COLUMN_NAME_ID + ") ON DELETE CASCADE" +
            ")"
        );
    }

    // Upgrades não derrubam mais tabelas (isso apagava os dados do usuário a cada subida de
    // versão). onCreate agora usa CREATE TABLE IF NOT EXISTS, então repeti-lo já preserva
    // tudo até a versão 13. Uma mudança real de schema deve ganhar seu próprio passo aqui
    // (`if (oldVersion < N) { db.execSQL("ALTER TABLE ...") }`) — nunca um DROP TABLE.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 13) {
            onCreate(sqLiteDatabase);
        }
    }
}
