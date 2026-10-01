package com.kevin.gestorproducao.rules;

import static com.kevin.gestorproducao.utilitario.Utilitario.limpaString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Nomes que cada tipo de profissão dá ao trabalho "licença de produção do aprendiz"
// (ex.: armaduras e armas, acessórios). Quando um nome mudar ou surgir outro, basta editar
// NOMES: a comparação ignora acento, maiúscula, espaço e pontuação.
public class CatalogoLicencasProducao {
    private static final List<String> NOMES = List.of(
        "Licença de produção do aprendiz",
        "Licença de Artesanato de Aprendiz",
        "Melhorar licença comum",
        "Melhoria: Licença comum"
    );

    private static final Set<String> NOMES_NORMALIZADOS = new HashSet<>();

    static {
        for (String nome : NOMES) {
            NOMES_NORMALIZADOS.add(normaliza(nome));
        }
    }

    private CatalogoLicencasProducao() {}

    public static boolean ehLicencaProducao(String nome) {
        return nome != null && NOMES_NORMALIZADOS.contains(normaliza(nome));
    }

    private static String normaliza(String nome) {
        return limpaString(nome).replaceAll("[^a-z0-9]", "");
    }
}
