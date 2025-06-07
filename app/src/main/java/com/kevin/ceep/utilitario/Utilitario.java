package com.kevin.ceep.utilitario;
import java.text.Normalizer;
import java.util.UUID;

public class Utilitario {
    public static String removeAcentos(String string) {
        if (string == null) {
            return "";
        }
        return Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }
    public static String limpaString(String string) {
        return removeAcentos(string).toLowerCase().replace(" ","");
    }
    public static boolean comparaString(String string1, String string2) {
        return limpaString(string1).equals(limpaString(string2));
    }
    public static boolean stringContemString(String string1, String string2) {
        return removeAcentos(string1).toLowerCase().replace(" ","").contains(removeAcentos(string2).toLowerCase().replace(" ",""));
    }

    public static String geraIdAleatorio() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
