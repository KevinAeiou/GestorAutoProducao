package com.kevin.gestorproducao.rules;

import static com.kevin.gestorproducao.utilitario.Utilitario.limpaString;
import com.kevin.gestorproducao.model.Profissao;
import com.kevin.gestorproducao.model.Recurso;
import java.util.LinkedHashMap;
import java.util.Map;

public class CatalogoRecursos {
    private static final LinkedHashMap<Profissao, Map<Recurso, Integer>> CATALOGO = new LinkedHashMap<>();

    static {
        Map<Recurso, Integer> aneis = new LinkedHashMap<>();
        aneis.put(Recurso.MOLDE_DO_PRINCIPIANTE, 2);
        aneis.put(Recurso.PEPITA_DE_COBRE, 1);
        aneis.put(Recurso.PEDRA_DE_SOMBRAS, 1);
        aneis.put(Recurso.MOLDE_DO_APRENDIZ, 2);
        aneis.put(Recurso.PEPITA_DE_PRATA, 2);
        aneis.put(Recurso.PEDRA_DA_LUZ, 2);
        CATALOGO.put(Profissao.ANEIS, aneis);

        Map<Recurso, Integer> amuletos = new LinkedHashMap<>();
        amuletos.put(Recurso.PINCA_DO_APRENDIZ, 2);
        amuletos.put(Recurso.JADE_BRUTA, 1);
        amuletos.put(Recurso.ENERGIA_INICIAL, 1);
        amuletos.put(Recurso.PINCA_DO_PRINCIPIANTE, 2);
        amuletos.put(Recurso.ETER_INICIAL, 2);
        amuletos.put(Recurso.ONIX_EXTRAORDINARIA, 2);
        CATALOGO.put(Profissao.AMULETOS, amuletos);

        Map<Recurso, Integer> braceletes = new LinkedHashMap<>();
        braceletes.put(Recurso.FIBRA_DE_BRONZE, 2);
        braceletes.put(Recurso.PRATA, 1);
        braceletes.put(Recurso.PIN_DE_ESTUDANTE, 1);
        braceletes.put(Recurso.FIBRA_DE_PLATINA, 2);
        braceletes.put(Recurso.AMBARITO, 2);
        braceletes.put(Recurso.PINO_DO_APRENDIZ, 2);
        CATALOGO.put(Profissao.BRACELETES, braceletes);

        Map<Recurso, Integer> capotes = new LinkedHashMap<>();
        capotes.put(Recurso.TECIDO_DELICADO, 1);
        capotes.put(Recurso.SUBSTANCIA_INSTAVEL, 1);
        capotes.put(Recurso.FURADOR_DO_APRENDIZ, 2);
        capotes.put(Recurso.FURADOR_DO_PRINCIPIANTE, 2);
        capotes.put(Recurso.SUBSTANCIA_ESTAVEL, 2);
        capotes.put(Recurso.TECIDO_ESPESSO, 2);
        CATALOGO.put(Profissao.CAPOTES, capotes);

        Map<Recurso, Integer> armadurasDeTecido = new LinkedHashMap<>();
        armadurasDeTecido.put(Recurso.FIO_GROSSEIRO, 1);
        armadurasDeTecido.put(Recurso.TECIDO_DE_LINHO, 1);
        armadurasDeTecido.put(Recurso.TESOURA_DO_APRENDIZ, 2);
        armadurasDeTecido.put(Recurso.FIO_GROSSO, 2);
        armadurasDeTecido.put(Recurso.TECIDO_DE_CETIM, 2);
        armadurasDeTecido.put(Recurso.TESOURA_DO_PRINCIPIANTE, 2);
        CATALOGO.put(Profissao.ARMADURAS_DE_TECIDO, armadurasDeTecido);

        Map<Recurso, Integer> armadurasPesadas = new LinkedHashMap<>();
        armadurasPesadas.put(Recurso.PLACAS_DE_COBRE, 1);
        armadurasPesadas.put(Recurso.ANEIS_DE_BRONZE, 1);
        armadurasPesadas.put(Recurso.MARRETAO_DO_APRENDIZ, 2);
        armadurasPesadas.put(Recurso.ANEIS_DE_ACO, 2);
        armadurasPesadas.put(Recurso.MARRETAO_DO_PRINCIPIANTE, 2);
        armadurasPesadas.put(Recurso.PLACAS_DE_FERRO, 2);
        CATALOGO.put(Profissao.ARMADURAS_PESADAS, armadurasPesadas);

        Map<Recurso, Integer> armadurasLeves = new LinkedHashMap<>();
        armadurasLeves.put(Recurso.COURO_RESISTENTE, 1);
        armadurasLeves.put(Recurso.ESCAMAS_DA_SERPENTE, 1);
        armadurasLeves.put(Recurso.FACA_DO_APRENDIZ, 2);
        armadurasLeves.put(Recurso.COURO_GROSSO, 2);
        armadurasLeves.put(Recurso.ESCAMAS_DO_LAGARTO, 2);
        armadurasLeves.put(Recurso.FACA_DO_PRINCIPIANTE, 2);
        CATALOGO.put(Profissao.ARMADURAS_LEVES, armadurasLeves);

        Map<Recurso, Integer> armasCorpoACorpo = new LinkedHashMap<>();
        armasCorpoACorpo.put(Recurso.MINERIO_DE_COBRE, 1);
        armasCorpoACorpo.put(Recurso.MO_DO_APRENDIZ, 1);
        armasCorpoACorpo.put(Recurso.LASCAS, 2);
        armasCorpoACorpo.put(Recurso.LASCAS_DE_QUARTZO, 2);
        armasCorpoACorpo.put(Recurso.MINERIO_DE_FERRO, 2);
        armasCorpoACorpo.put(Recurso.MO_DO_PRINCIPIANTE, 2);
        CATALOGO.put(Profissao.ARMAS_CORPO_A_CORPO, armasCorpoACorpo);

        Map<Recurso, Integer> armasDeLongoAlcance = new LinkedHashMap<>();
        armasDeLongoAlcance.put(Recurso.VARINHA_DE_MADEIRA, 1);
        armasDeLongoAlcance.put(Recurso.CABECA_DO_CAJADO_DE_JADE, 1);
        armasDeLongoAlcance.put(Recurso.ESFERA_DO_APRENDIZ, 2);
        armasDeLongoAlcance.put(Recurso.CABECA_DO_CAJADO_DE_ONIX, 2);
        armasDeLongoAlcance.put(Recurso.ESFERA_DO_NEOFITO, 2);
        armasDeLongoAlcance.put(Recurso.VARINHA_DE_ACO, 2);
        CATALOGO.put(Profissao.ARMAS_DE_LONGO_ALCANCE, armasDeLongoAlcance);
    }

    public static int getQuantidade(String profissaoStr, String recursoStr) {
        Profissao profissao = Profissao.fromKey(limpaString(profissaoStr));
        Recurso recurso = Recurso.fromKey(limpaString(recursoStr));

        if (profissao == null || recurso == null) return 1;

        Map<Recurso, Integer> recursos = CATALOGO.get(profissao);

        if (recursos == null) return 1;

        return recursos.getOrDefault(recurso, 1);
    }

    public static Map<Profissao, Map<Recurso, Integer>> getCatalogo() {
        return CATALOGO;
    }
}
