package com.kevin.gestorproducao.utilitario;

import android.util.Base64;

import com.kevin.gestorproducao.rules.exception.SenhaPersonagemException;
import com.kevin.gestorproducao.rules.exception.SenhaPersonagemException.Tipo;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.function.Supplier;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Codifica a senha do personagem gravada em {@code Personagens/<id>/senha} no formato
 * {@code enc:v1:} + base64 URL-safe de {@code nonce(12) + texto cifrado + etiqueta(16)}
 * (AES-256-GCM, sem AAD), idêntico ao lido pela automação WarspearVision. Valor sem o prefixo
 * é senha crua (dados antigos) e continua válido.
 */
public final class SenhaPersonagemUtil {

    public static final String PREFIXO = "enc:v1:";
    public static final int TAMANHO_CHAVE_BYTES = 32;

    private static final String TRANSFORMACAO = "AES/GCM/NoPadding";
    private static final int TAMANHO_NONCE_BYTES = 12;
    private static final int TAMANHO_ETIQUETA_BYTES = 16;
    private static final int TAMANHO_ETIQUETA_BITS = TAMANHO_ETIQUETA_BYTES * 8;
    private static final int FLAGS_BASE64 = Base64.URL_SAFE | Base64.NO_WRAP;
    private static final SecureRandom GERADOR_ALEATORIO = new SecureRandom();

    private SenhaPersonagemUtil() {}

    public static boolean estaCodificada(String valor) {
        return valor != null && valor.startsWith(PREFIXO);
    }

    /**
     * Codifica a senha com um nonce novo a cada chamada.
     */
    public static String codificar(String senha, byte[] chave) {
        byte[] nonce = new byte[TAMANHO_NONCE_BYTES];
        GERADOR_ALEATORIO.nextBytes(nonce);

        return codificar(senha, chave, nonce);
    }

    static String codificar(String senha, byte[] chave, byte[] nonce) {
        validaChave(chave);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMACAO);
            cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(chave, "AES"),
                new GCMParameterSpec(TAMANHO_ETIQUETA_BITS, nonce)
            );
            byte[] textoCifrado = cipher.doFinal(senha.getBytes(StandardCharsets.UTF_8));

            byte[] combinado = new byte[nonce.length + textoCifrado.length];
            System.arraycopy(nonce, 0, combinado, 0, nonce.length);
            System.arraycopy(textoCifrado, 0, combinado, nonce.length, textoCifrado.length);

            return PREFIXO + Base64.encodeToString(combinado, FLAGS_BASE64);
        } catch (GeneralSecurityException e) {
            throw new SenhaPersonagemException(Tipo.FALHA_CODIFICACAO);
        }
    }

    /**
     * Decodifica um valor {@code enc:v1:}. Chave errada e valor adulterado falham do mesmo jeito
     * ({@link Tipo#FALHA_AUTENTICACAO}); valor mal formado falha com {@link Tipo#VALOR_INVALIDO}.
     */
    public static String decodificar(String valor, byte[] chave) {
        validaChave(chave);

        if (!estaCodificada(valor)) {
            throw new SenhaPersonagemException(Tipo.VALOR_INVALIDO);
        }

        byte[] combinado;
        try {
            combinado = Base64.decode(valor.substring(PREFIXO.length()), FLAGS_BASE64);
        } catch (IllegalArgumentException e) {
            throw new SenhaPersonagemException(Tipo.VALOR_INVALIDO);
        }

        if (combinado.length < TAMANHO_NONCE_BYTES + TAMANHO_ETIQUETA_BYTES) {
            throw new SenhaPersonagemException(Tipo.VALOR_INVALIDO);
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMACAO);
            cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(chave, "AES"),
                new GCMParameterSpec(TAMANHO_ETIQUETA_BITS, combinado, 0, TAMANHO_NONCE_BYTES)
            );
            byte[] texto = cipher.doFinal(
                combinado,
                TAMANHO_NONCE_BYTES,
                combinado.length - TAMANHO_NONCE_BYTES
            );

            return new String(texto, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new SenhaPersonagemException(Tipo.FALHA_AUTENTICACAO);
        }
    }

    /**
     * Devolve a senha em texto puro, seja crua ou codificada. A chave só é pedida ao
     * {@code fornecedorChave} quando o valor está codificado.
     */
    public static String resolver(String valor, Supplier<byte[]> fornecedorChave) {
        if (!estaCodificada(valor)) return valor;

        return decodificar(valor, fornecedorChave.get());
    }

    /**
     * Devolve o valor pronto para gravar: já codificado passa direto (sem usar a chave); senha
     * crua é codificada.
     */
    public static String garantirCodificada(String valor, Supplier<byte[]> fornecedorChave) {
        if (valor == null || estaCodificada(valor)) return valor;

        return codificar(valor, fornecedorChave.get());
    }

    /**
     * Converte a chave em base64 URL-safe para os 32 bytes; falha se não tiver exatamente 32.
     */
    public static byte[] chaveDeBase64(String chaveBase64) {
        if (chaveBase64 == null) throw new SenhaPersonagemException(Tipo.CHAVE_INVALIDA);

        try {
            byte[] chave = Base64.decode(chaveBase64.trim(), FLAGS_BASE64);
            validaChave(chave);

            return chave;
        } catch (IllegalArgumentException e) {
            throw new SenhaPersonagemException(Tipo.CHAVE_INVALIDA);
        }
    }

    private static void validaChave(byte[] chave) {
        if (chave == null || chave.length != TAMANHO_CHAVE_BYTES) {
            throw new SenhaPersonagemException(Tipo.CHAVE_INVALIDA);
        }
    }
}
