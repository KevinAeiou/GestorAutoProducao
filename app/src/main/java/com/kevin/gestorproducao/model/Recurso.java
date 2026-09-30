package com.kevin.gestorproducao.model;

public enum Recurso {

    MOLDE_DO_APRENDIZ("moldedoaprendiz"), PEDRA_DE_SOMBRAS("pedradesombras"), PEPITA_DE_COBRE("pepitadecobre"),
    MOLDE_DO_PRINCIPIANTE("moldedoprincipiante"), PEDRA_DA_LUZ("pedradaluz"), PEPITA_DE_PRATA("pepitadeprata"),
    PINCA_DO_APRENDIZ("pincadoaprendiz"), JADE_BRUTA("jadebruta"), ENERGIA_INICIAL("energiainicial"),
    PINCA_DO_PRINCIPIANTE("pincadoprincipiante"), ETER_INICIAL("eterinicial"), ONIX_EXTRAORDINARIA("onixextraordinaria"),
    FIBRA_DE_BRONZE("fibradebronze"), PRATA("prata"), PIN_DE_ESTUDANTE("pindeestudante"),
    FIBRA_DE_PLATINA("fibradeplatina"), PINO_DO_APRENDIZ("pinodoaprendiz"), AMBARITO("ambarito"),
    TECIDO_DELICADO("tecidodelicado"), SUBSTANCIA_INSTAVEL("substanciainstavel"), FURADOR_DO_APRENDIZ("furadordoaprendiz"),
    FURADOR_DO_PRINCIPIANTE("furadordoprincipiante"), SUBSTANCIA_ESTAVEL("substanciaestavel"), TECIDO_ESPESSO("tecidoespesso"),
    FIO_GROSSEIRO("fiogrosseiro"), TECIDO_DE_LINHO("tecidodelinho"), TESOURA_DO_APRENDIZ("tesouradoaprendiz"),
    FIO_GROSSO("fiogrosso"), TECIDO_DE_CETIM("tecidodecetim"), TESOURA_DO_PRINCIPIANTE("tesouradoprincipiante"),
    MARRETAO_DO_APRENDIZ("marretaodoaprendiz"), PLACAS_DE_COBRE("placasdecobre"), ANEIS_DE_BRONZE("aneisdebronze"),
    ANEIS_DE_ACO("aneisdeaco"), MARRETAO_DO_PRINCIPIANTE("marretaodoprincipiante"), PLACAS_DE_FERRO("placasdeferro"),
    FACA_DO_APRENDIZ("facadoaprendiz"), COURO_RESISTENTE("couroresistente"), ESCAMAS_DA_SERPENTE("escamasdaserpente"),
    COURO_GROSSO("courogrosso"), ESCAMAS_DO_LAGARTO("escamasdolagarto"), FACA_DO_PRINCIPIANTE("facadoprincipiante"),
    LASCAS("lascas"), MINERIO_DE_COBRE("mineriodecobre"), MO_DO_APRENDIZ("modoaprendiz"),
    LASCAS_DE_QUARTZO("lascasdequartzo"), MINERIO_DE_FERRO("mineriodeferro"), MO_DO_PRINCIPIANTE("modoprincipiante"),
    ESFERA_DO_APRENDIZ("esferadoaprendiz"), VARINHA_DE_MADEIRA("varinhademadeira"), CABECA_DO_CAJADO_DE_JADE("cabecadocajadodejade"),
    CABECA_DO_CAJADO_DE_ONIX("cabecadocajadodeonix"), ESFERA_DO_NEOFITO("esferadoneofito"), VARINHA_DE_ACO("varinhadeaco");

    private final String key;

    Recurso(String key) {
        this.key = key;
    }

    public static Recurso fromKey(String key) {
        if (key == null) return null;

        for (Recurso r : values()) {
            if (r.key.equalsIgnoreCase(key)) {
                return r;
            }
        }
        return null;
    }

    public String getKey() {
        return key;
    }
}