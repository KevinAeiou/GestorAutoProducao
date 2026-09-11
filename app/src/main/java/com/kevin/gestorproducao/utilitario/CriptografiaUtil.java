package com.kevin.gestorproducao.utilitario;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Cifra/decifra strings com uma chave AES-256/GCM presa ao Android Keystore do aparelho.
 * A chave nunca sai do hardware seguro e não é exportável — por isso um valor cifrado aqui
 * só pode ser decifrado no mesmo aparelho onde foi gravado. Use apenas para dados que não
 * precisam acompanhar o usuário entre dispositivos (ex.: campos guardados só no SQLite local).
 */
public final class CriptografiaUtil {

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String ALIAS_CHAVE_DADOS_LOCAIS = "gestorproducao_dados_locais";
    private static final String TRANSFORMACAO = "AES/GCM/NoPadding";
    private static final int TAMANHO_TAG_GCM_BITS = 128;
    private static final int TAMANHO_IV_BYTES = 12;

    private CriptografiaUtil() {}

    public static String encriptar(String textoPuro) {
        if (textoPuro == null || textoPuro.isEmpty()) return textoPuro;

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMACAO);
            cipher.init(Cipher.ENCRYPT_MODE, recuperaOuCriaChave());

            byte[] iv = cipher.getIV();
            byte[] textoCifrado = cipher.doFinal(textoPuro.getBytes(StandardCharsets.UTF_8));

            byte[] combinado = new byte[iv.length + textoCifrado.length];
            System.arraycopy(iv, 0, combinado, 0, iv.length);
            System.arraycopy(textoCifrado, 0, combinado, iv.length, textoCifrado.length);

            return Base64.encodeToString(combinado, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criptografar dado local", e);
        }
    }

    /**
     * Decifra um valor gravado por {@link #encriptar}. Se o valor não foi cifrado por esta
     * classe — caso de registros gravados antes desta correção — devolve o próprio texto
     * original em vez de lançar exceção; o próximo salvamento passa a cifrá-lo normalmente.
     */
    public static String decriptar(String textoCifradoBase64) {
        if (textoCifradoBase64 == null || textoCifradoBase64.isEmpty()) return textoCifradoBase64;

        try {
            byte[] combinado = Base64.decode(textoCifradoBase64, Base64.NO_WRAP);
            if (combinado.length <= TAMANHO_IV_BYTES) return textoCifradoBase64;

            byte[] iv = new byte[TAMANHO_IV_BYTES];
            byte[] textoCifrado = new byte[combinado.length - TAMANHO_IV_BYTES];
            System.arraycopy(combinado, 0, iv, 0, TAMANHO_IV_BYTES);
            System.arraycopy(combinado, TAMANHO_IV_BYTES, textoCifrado, 0, textoCifrado.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMACAO);
            cipher.init(
                Cipher.DECRYPT_MODE,
                recuperaOuCriaChave(),
                new GCMParameterSpec(TAMANHO_TAG_GCM_BITS, iv)
            );

            return new String(cipher.doFinal(textoCifrado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return textoCifradoBase64;
        }
    }

    private static SecretKey recuperaOuCriaChave() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);

        if (keyStore.containsAlias(ALIAS_CHAVE_DADOS_LOCAIS)) {
            return (SecretKey) keyStore.getKey(ALIAS_CHAVE_DADOS_LOCAIS, null);
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        );

        keyGenerator.init(
            new KeyGenParameterSpec.Builder(
                ALIAS_CHAVE_DADOS_LOCAIS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        );

        return keyGenerator.generateKey();
    }
}
