package com.kevin.gestorproducao.utilitario;

import android.content.Context;
import android.content.SharedPreferences;

import com.kevin.gestorproducao.rules.exception.SenhaPersonagemException;
import com.kevin.gestorproducao.rules.exception.SenhaPersonagemException.Tipo;

/**
 * Guarda a chave de senhas dos personagens só neste aparelho, cifrada pelo Android Keystore
 * ({@link CriptografiaUtil}). A chave é colada pelo usuário uma vez; nunca vai para o Firebase,
 * para o git nem para o código-fonte.
 */
public final class ChaveSenhaStore {

    private static final String PREFS_NOME = "seguranca_senhas";
    private static final String CHAVE_PREFS = "chave_senha_personagem";

    private ChaveSenhaStore() {}

    public static boolean possuiChave(Context context) {
        return prefs(context).getString(CHAVE_PREFS, null) != null;
    }

    /**
     * Valida e grava a chave informada (base64 URL-safe de 32 bytes).
     */
    public static void salvar(Context context, String chaveBase64) {
        SenhaPersonagemUtil.chaveDeBase64(chaveBase64);

        String cifrada;
        try {
            cifrada = CriptografiaUtil.encriptar(chaveBase64.trim());
        } catch (IllegalStateException e) {
            throw new SenhaPersonagemException(Tipo.FALHA_CODIFICACAO);
        }

        prefs(context).edit().putString(CHAVE_PREFS, cifrada).apply();
    }

    /**
     * Devolve a chave gravada. Se estiver ausente ou não puder mais ser lida (ex.: aparelho
     * restaurado, Keystore perdido), lança erro para o usuário informá-la de novo.
     */
    public static byte[] recuperar(Context context) {
        String cifrada = prefs(context).getString(CHAVE_PREFS, null);
        if (cifrada == null) throw new SenhaPersonagemException(Tipo.CHAVE_AUSENTE);

        return SenhaPersonagemUtil.chaveDeBase64(CriptografiaUtil.decriptar(cifrada));
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NOME, Context.MODE_PRIVATE);
    }
}
