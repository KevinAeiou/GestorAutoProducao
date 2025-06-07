package com.kevin.ceep.db;

import static com.kevin.ceep.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID;
import static com.kevin.ceep.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_PERSONAGEM;
import static com.kevin.ceep.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_ID_TRABALHO;
import static com.kevin.ceep.db.contracts.EstoqueDbContract.EstoqueEntry.COLUMN_NAME_QUANTIDADE;
import static com.kevin.ceep.db.contracts.EstoqueDbContract.EstoqueEntry.TABLE_ESTOQUE;
import static com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry.TABLE_PERSONAGENS;
import static com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry.TABLE_TRABALHOS;
import static com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry.TABLE_TRABALHOS_PRODUCAO;
import static com.kevin.ceep.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_DATA_VENDA;
import static com.kevin.ceep.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_DESCRICAO;
import static com.kevin.ceep.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.COLUMN_NAME_VALOR;
import static com.kevin.ceep.db.contracts.TrabalhoVendidoContract.TrabalhoVendidoEntry.TABLE_TRABALHOS_VENDIDOS;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kevin.ceep.db.contracts.PersoagemDbContract.PersonagemEntry;
import com.kevin.ceep.db.contracts.TrabalhoDbContract.TrabalhoEntry;
import com.kevin.ceep.db.contracts.TrabalhoProducaoContract.TrabalhoProducaoEntry;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 9;
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
                "CREATE TABLE " + TABLE_TRABALHOS + " (" +
                TrabalhoEntry.COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                TrabalhoEntry.COLUMN_NAME_NOME + " TEXT," +
                TrabalhoEntry.COLUMN_NAME_NOME_PRODUCAO + " TEXT," +
                TrabalhoEntry.COLUMN_NAME_EXPERIENCIA + " INTEGER," +
                TrabalhoEntry.COLUMN_NAME_NIVEL + " INTEGER," +
                TrabalhoEntry.COLUMN_NAME_PROFISSAO + " TEXT," +
                TrabalhoEntry.COLUMN_NAME_RARIDADE + " TEXT," +
                TrabalhoEntry.COLUMN_NAME_TRABALHO_NECESSARIO + " TEXT" +
                ")"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE " + TABLE_PERSONAGENS + " (" +
                PersonagemEntry.COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                PersonagemEntry.COLUMN_NAME_ID_USUARIO + " VARCHAR(30)," +
                PersonagemEntry.COLUMN_NAME_NOME + " TEXT," +
                PersonagemEntry.COLUMN_NAME_EMAIL + " TEXT," +
                PersonagemEntry.COLUMN_NAME_SENHA + " TEXT," +
                PersonagemEntry.COLUMN_NAME_ESTADO + " BOOLEAN," +
                PersonagemEntry.COLUMN_NAME_USO + " BOOLEAN," +
                PersonagemEntry.COLUMN_NAME_AUTO_PRODUCAO + " BOOLEAN," +
                PersonagemEntry.COLUMN_NAME_ESPACO_PRODUCAO + " INTEGER" +
                ")"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE " +
                TABLE_TRABALHOS_PRODUCAO +
                " (" +
                TrabalhoProducaoEntry.COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                TrabalhoProducaoEntry.COLUMN_NAME_ID_TRABALHO + " VARCHAR(30)," +
                TrabalhoProducaoEntry.COLUMN_NAME_ID_PERSONAGEM + " VARCHAR(30)," +
                TrabalhoProducaoEntry.COLUMN_NAME_LICENCA + " TEXT," +
                TrabalhoProducaoEntry.COLUMN_NAME_ESTADO + " INTEGER," +
                TrabalhoProducaoEntry.COLUMN_NAME_RECORRENCIA + " BOOLEAN" +
                ")"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE " +
                TABLE_ESTOQUE +
                " (" +
                COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                COLUMN_NAME_ID_TRABALHO + " VARCHAR(30)," +
                COLUMN_NAME_ID_PERSONAGEM + " VARCHAR(30)," +
                COLUMN_NAME_QUANTIDADE + " INTEGER" +
                ")"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE " +
                TABLE_TRABALHOS_VENDIDOS +
                " (" +
                COLUMN_NAME_ID + " VARCHAR(30) PRIMARY KEY," +
                COLUMN_NAME_ID_PERSONAGEM + " VARCHAR(30), " +
                COLUMN_NAME_ID_TRABALHO + " VARCHAR(30), " +
                COLUMN_NAME_DESCRICAO + " TEXT, " +
                COLUMN_NAME_DATA_VENDA + " VARCHAR(10), " +
                COLUMN_NAME_QUANTIDADE + " INTEGER, " +
                COLUMN_NAME_VALOR + " INTEGER" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_TRABALHOS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONAGENS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_TRABALHOS_PRODUCAO);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ESTOQUE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_TRABALHOS_VENDIDOS);
        onCreate(sqLiteDatabase);
    }
}
