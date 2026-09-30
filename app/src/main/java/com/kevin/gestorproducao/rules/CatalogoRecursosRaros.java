package com.kevin.gestorproducao.rules;

import static com.kevin.gestorproducao.utilitario.Utilitario.limpaString;

import com.kevin.gestorproducao.model.Profissao;
import com.kevin.gestorproducao.model.Recurso;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CatalogoRecursosRaros {
    private static final Map<Profissao, Map<String, Map<Recurso, Integer>>> MAP = new HashMap<>();

    static {
        Map<Recurso, Integer> comuns0 = new HashMap<>();
        Map<Recurso, Integer> avancados0 = new HashMap<>();
        Map<String, Map<Recurso, Integer>> armas_corpo_a_corpo = new HashMap<>();

        comuns0.put(Recurso.LASCAS, 4);
        comuns0.put(Recurso.MINERIO_DE_COBRE, 3);
        comuns0.put(Recurso.MO_DO_APRENDIZ, 2);

        avancados0.put(Recurso.LASCAS_DE_QUARTZO, 5);
        avancados0.put(Recurso.MINERIO_DE_FERRO, 4);
        avancados0.put(Recurso.MO_DO_PRINCIPIANTE, 3);

        armas_corpo_a_corpo.put("grandecolecaoderecursoscomuns", comuns0);
        armas_corpo_a_corpo.put("grandecolecaoderecursosavancados", avancados0);

        MAP.put(Profissao.ARMAS_CORPO_A_CORPO, armas_corpo_a_corpo);

        Map<Recurso, Integer> comuns1 = new HashMap<>();
        Map<Recurso, Integer> avancados1 = new HashMap<>();
        Map<String, Map<Recurso, Integer>> armas_longo_alcance = new HashMap<>();

        comuns1.put(Recurso.ESFERA_DO_APRENDIZ, 4);
        comuns1.put(Recurso.VARINHA_DE_MADEIRA, 3);
        comuns1.put(Recurso.CABECA_DO_CAJADO_DE_JADE, 2);

        avancados1.put(Recurso.ESFERA_DO_NEOFITO, 5);
        avancados1.put(Recurso.VARINHA_DE_ACO, 4);
        avancados1.put(Recurso.CABECA_DO_CAJADO_DE_ONIX, 3);

        armas_longo_alcance.put("grandecolecaoderecursoscomuns", comuns1);
        armas_longo_alcance.put("grandecolecaoderecursosavancados", avancados1);

        MAP.put(Profissao.ARMAS_DE_LONGO_ALCANCE, armas_longo_alcance);

        Map<Recurso, Integer> comuns7 = new HashMap<>();
        Map<Recurso, Integer> avancados7 = new HashMap<>();
        Map<String, Map<Recurso, Integer>> armaduras_pesadas = new HashMap<>();

        comuns7.put(Recurso.MARRETAO_DO_APRENDIZ, 4);
        comuns7.put(Recurso.PLACAS_DE_COBRE, 3);
        comuns7.put(Recurso.ANEIS_DE_BRONZE, 2);

        avancados7.put(Recurso.MARRETAO_DO_PRINCIPIANTE, 5);
        avancados7.put(Recurso.PLACAS_DE_FERRO, 4);
        avancados7.put(Recurso.ANEIS_DE_ACO, 3);

        armaduras_pesadas.put("grandecolecaoderecursoscomuns", comuns7);
        armaduras_pesadas.put("coletaemmassaderecursosavancados", avancados7);

        MAP.put(Profissao.ARMADURAS_PESADAS, armaduras_pesadas);

        Map<Recurso, Integer> comuns8 = new HashMap<>();
        Map<Recurso, Integer> avancados8 = new HashMap<>();
        Map<String, Map<Recurso, Integer>> armaduras_leve = new HashMap<>();

        comuns8.put(Recurso.FACA_DO_APRENDIZ, 4);
        comuns8.put(Recurso.COURO_RESISTENTE, 3);
        comuns8.put(Recurso.ESCAMAS_DA_SERPENTE, 2);

        avancados8.put(Recurso.FACA_DO_PRINCIPIANTE, 5);
        avancados8.put(Recurso.COURO_GROSSO, 4);
        avancados8.put(Recurso.ESCAMAS_DO_LAGARTO, 3);

        armaduras_leve.put("grandecolecaoderecursoscomuns", comuns8);
        armaduras_leve.put("coletaemmassaderecursosavancados", avancados8);

        MAP.put(Profissao.ARMADURAS_LEVES, armaduras_leve);

        Map<Recurso, Integer> comuns2 = new HashMap<>();
        Map<Recurso, Integer> avancados2 = new HashMap<>();
        Map<String, Map<Recurso, Integer>> armaduras_tecido = new HashMap<>();

        comuns2.put(Recurso.TESOURA_DO_APRENDIZ, 4);
        comuns2.put(Recurso.FIO_GROSSEIRO, 3);
        comuns2.put(Recurso.TECIDO_DE_LINHO, 2);

        avancados2.put(Recurso.TESOURA_DO_PRINCIPIANTE, 5);
        avancados2.put(Recurso.FIO_GROSSO, 4);
        avancados2.put(Recurso.TECIDO_DE_CETIM, 3);

        armaduras_tecido.put("grandecolecaoderecursoscomuns", comuns2);
        armaduras_tecido.put("coletaemmassaderecursosavancados", avancados2);

        MAP.put(Profissao.ARMADURAS_DE_TECIDO, armaduras_tecido);

        Map<Recurso, Integer> comuns3 = new HashMap<>();
        Map<Recurso, Integer> avancados3 = new HashMap<>();
        Map<String, Map<Recurso, Integer>> braceletes = new HashMap<>();

        comuns3.put(Recurso.FIBRA_DE_BRONZE, 4);
        comuns3.put(Recurso.PRATA, 3);
        comuns3.put(Recurso.PIN_DE_ESTUDANTE, 2);

        avancados3.put(Recurso.FIBRA_DE_PLATINA, 5);
        avancados3.put(Recurso.AMBARITO, 4);
        avancados3.put(Recurso.PINO_DO_APRENDIZ, 3);

        braceletes.put("grandecolecaoderecursoscomuns", comuns3);
        braceletes.put("grandecolecaoderecursosavancados", avancados3);

        MAP.put(Profissao.BRACELETES, braceletes);

        Map<Recurso, Integer> comuns4 = new HashMap<>();
        Map<Recurso, Integer> avancados4 = new HashMap<>();
        Map<String, Map<Recurso, Integer>> capotes = new HashMap<>();

        comuns4.put(Recurso.FURADOR_DO_APRENDIZ, 4);
        comuns4.put(Recurso.TECIDO_DELICADO, 3);
        comuns4.put(Recurso.SUBSTANCIA_INSTAVEL, 2);

        avancados4.put(Recurso.FURADOR_DO_PRINCIPIANTE, 5);
        avancados4.put(Recurso.TECIDO_ESPESSO, 4);
        avancados4.put(Recurso.SUBSTANCIA_ESTAVEL, 3);

        capotes.put("grandecolecaoderecursoscomuns", comuns4);
        capotes.put("grandecolecaoderecursosavancados", avancados4);

        MAP.put(Profissao.CAPOTES, capotes);

        Map<Recurso, Integer> comuns5 = new HashMap<>();
        Map<Recurso, Integer> avancados5 = new HashMap<>();
        Map<String, Map<Recurso, Integer>> aneis = new HashMap<>();

        comuns5.put(Recurso.MOLDE_DO_APRENDIZ, 4);
        comuns5.put(Recurso.PEPITA_DE_COBRE, 3);
        comuns5.put(Recurso.PEDRA_DE_SOMBRAS, 2);

        avancados5.put(Recurso.MOLDE_DO_PRINCIPIANTE, 5);
        avancados5.put(Recurso.PEPITA_DE_PRATA, 4);
        avancados5.put(Recurso.PEDRA_DA_LUZ, 3);

        aneis.put("grandecolecaoderecursoscomuns", comuns5);
        aneis.put("grandecolecaoderecursosavancados", avancados5);

        MAP.put(Profissao.ANEIS, aneis);

        Map<String, Map<Recurso, Integer>> amuletos = new HashMap<>();
        Map<Recurso, Integer> comuns6 = new HashMap<>();
        Map<Recurso, Integer> avancados6 = new HashMap<>();

        comuns6.put(Recurso.PINCA_DO_APRENDIZ, 4);
        comuns6.put(Recurso.JADE_BRUTA, 3);
        comuns6.put(Recurso.ENERGIA_INICIAL, 2);

        avancados6.put(Recurso.PINCA_DO_PRINCIPIANTE, 5);
        avancados6.put(Recurso.ONIX_EXTRAORDINARIA, 4);
        avancados6.put(Recurso.ETER_INICIAL, 3);

        amuletos.put("grandecolecaoderecursoscomuns", comuns6);
        amuletos.put("grandecolecaoderecursosavancados", avancados6);

        MAP.put(Profissao.AMULETOS, amuletos);
    }

    public static Map<Recurso, Integer> getRecursos(String profissaoStr, String nomeStr) {
        Profissao profissao = Profissao.fromKey(limpaString(profissaoStr));

        if (profissao == null) return Collections.emptyMap();

        Map<String, Map<Recurso, Integer>> trabalhos = MAP.get(profissao);

        if (trabalhos == null) return Collections.emptyMap();

        return trabalhos.getOrDefault(limpaString(nomeStr), Collections.emptyMap());
    }
}
