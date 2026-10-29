package com.kevin.gestorproducao.utilitario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.kevin.gestorproducao.rules.exception.SenhaPersonagemException;
import com.kevin.gestorproducao.rules.exception.SenhaPersonagemException.Tipo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.function.Supplier;

@RunWith(RobolectricTestRunner.class)
public class SenhaPersonagemUtilTest {

    private static final String CHAVE_VETOR = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";
    private static final String VALOR_VETOR =
        "enc:v1:AAECAwQFBgcICQoLFGe4c6TIpn6gNfL4xYxVXLHlkFt8cBxyDC8wd_Bi7PdNOA==";
    private static final String SENHA_VETOR = "Senha-de-teste-123";

    private static byte[] chave(int semente) {
        byte[] chave = new byte[SenhaPersonagemUtil.TAMANHO_CHAVE_BYTES];
        for (int i = 0; i < chave.length; i++) chave[i] = (byte) (i + semente);

        return chave;
    }

    private static Tipo tipoDoErro(Runnable acao) {
        try {
            acao.run();
        } catch (SenhaPersonagemException e) {
            return e.getTipo();
        }
        fail("Era esperada SenhaPersonagemException");

        return null;
    }

    @Test
    public void deve_DecodificarVetorDaAutomacao() {
        byte[] chave = SenhaPersonagemUtil.chaveDeBase64(CHAVE_VETOR);

        assertEquals(SENHA_VETOR, SenhaPersonagemUtil.decodificar(VALOR_VETOR, chave));
    }

    @Test
    public void deve_GerarValorIdenticoAoVetor_ComNonceFixo() {
        byte[] chave = SenhaPersonagemUtil.chaveDeBase64(CHAVE_VETOR);
        byte[] nonce = new byte[12];
        for (int i = 0; i < nonce.length; i++) nonce[i] = (byte) i;

        assertEquals(VALOR_VETOR, SenhaPersonagemUtil.codificar(SENHA_VETOR, chave, nonce));
    }

    @Test
    public void deve_FazerIdaEVolta() {
        String codificada = SenhaPersonagemUtil.codificar("minhaSenha", chave(0));

        assertTrue(SenhaPersonagemUtil.estaCodificada(codificada));
        assertEquals("minhaSenha", SenhaPersonagemUtil.decodificar(codificada, chave(0)));
    }

    @Test
    public void deve_FazerIdaEVolta_ComAcentosESimbolos() {
        String senha = "Sénha!çã#2026 ✓";

        String codificada = SenhaPersonagemUtil.codificar(senha, chave(0));

        assertEquals(senha, SenhaPersonagemUtil.decodificar(codificada, chave(0)));
    }

    @Test
    public void deve_GerarValoresDiferentes_ParaMesmaSenha() {
        String primeiro = SenhaPersonagemUtil.codificar("igual", chave(0));
        String segundo = SenhaPersonagemUtil.codificar("igual", chave(0));

        assertNotEquals(primeiro, segundo);
    }

    @Test
    public void deve_Falhar_ComChaveErrada() {
        String codificada = SenhaPersonagemUtil.codificar("segredo", chave(0));

        assertEquals(
            Tipo.FALHA_AUTENTICACAO,
            tipoDoErro(() -> SenhaPersonagemUtil.decodificar(codificada, chave(1)))
        );
    }

    @Test
    public void deve_Falhar_ComValorAdulterado() {
        String codificada = SenhaPersonagemUtil.codificar("segredo", chave(0));
        char ultimo = codificada.charAt(codificada.length() - 3);
        String adulterado = codificada.substring(0, codificada.length() - 3)
            + (ultimo == 'A' ? 'B' : 'A')
            + codificada.substring(codificada.length() - 2);

        assertEquals(
            Tipo.FALHA_AUTENTICACAO,
            tipoDoErro(() -> SenhaPersonagemUtil.decodificar(adulterado, chave(0)))
        );
    }

    @Test
    public void deve_Falhar_ComValorMalformado() {
        assertEquals(
            Tipo.VALOR_INVALIDO,
            tipoDoErro(() -> SenhaPersonagemUtil.decodificar("enc:v1:@@@não-é-base64", chave(0)))
        );
        assertEquals(
            Tipo.VALOR_INVALIDO,
            tipoDoErro(() -> SenhaPersonagemUtil.decodificar("enc:v1:AAEC", chave(0)))
        );
        assertEquals(
            Tipo.VALOR_INVALIDO,
            tipoDoErro(() -> SenhaPersonagemUtil.decodificar("enc:v1:", chave(0)))
        );
    }

    @Test
    public void deve_Falhar_ComChaveDeTamanhoErrado() {
        assertEquals(
            Tipo.CHAVE_INVALIDA,
            tipoDoErro(() -> SenhaPersonagemUtil.codificar("x", new byte[16]))
        );
        assertEquals(
            Tipo.CHAVE_INVALIDA,
            tipoDoErro(() -> SenhaPersonagemUtil.chaveDeBase64("AAEC"))
        );
        assertEquals(
            Tipo.CHAVE_INVALIDA,
            tipoDoErro(() -> SenhaPersonagemUtil.chaveDeBase64("@@@"))
        );
    }

    @Test
    public void deve_ResolverSenhaCrua_SemUsarAChave() {
        Supplier<byte[]> chaveProibida = () -> {
            throw new AssertionError("A chave não deveria ser pedida");
        };

        assertEquals("crua123", SenhaPersonagemUtil.resolver("crua123", chaveProibida));
        assertNull(SenhaPersonagemUtil.resolver(null, chaveProibida));
    }

    @Test
    public void deve_ResolverSenhaCodificada_ComAChave() {
        assertEquals(
            SENHA_VETOR,
            SenhaPersonagemUtil.resolver(
                VALOR_VETOR,
                () -> SenhaPersonagemUtil.chaveDeBase64(CHAVE_VETOR)
            )
        );
    }

    @Test
    public void deve_PropagarChaveAusente_AoResolverSenhaCodificada() {
        assertEquals(
            Tipo.CHAVE_AUSENTE,
            tipoDoErro(() -> SenhaPersonagemUtil.resolver(VALOR_VETOR, () -> {
                throw new SenhaPersonagemException(Tipo.CHAVE_AUSENTE);
            }))
        );
    }

    @Test
    public void naoDeve_RegravarValorJaCodificado() {
        Supplier<byte[]> chaveProibida = () -> {
            throw new AssertionError("A chave não deveria ser pedida");
        };

        assertEquals(VALOR_VETOR, SenhaPersonagemUtil.garantirCodificada(VALOR_VETOR, chaveProibida));
    }

    @Test
    public void deve_CodificarSenhaCrua_AoGarantirCodificada() {
        String valor = SenhaPersonagemUtil.garantirCodificada("crua123", () -> chave(0));

        assertTrue(SenhaPersonagemUtil.estaCodificada(valor));
        assertEquals("crua123", SenhaPersonagemUtil.decodificar(valor, chave(0)));
    }

    @Test
    public void deve_IdentificarFormato() {
        assertTrue(SenhaPersonagemUtil.estaCodificada(VALOR_VETOR));
        assertFalse(SenhaPersonagemUtil.estaCodificada("senha-crua"));
        assertFalse(SenhaPersonagemUtil.estaCodificada(""));
        assertFalse(SenhaPersonagemUtil.estaCodificada(null));
    }

    @Test
    public void naoDeve_VazarSenhaChaveOuValorNaMensagemDeErro() {
        String codificada = SenhaPersonagemUtil.codificar("segredo-secreto", chave(0));

        try {
            SenhaPersonagemUtil.decodificar(codificada, chave(1));
            fail();
        } catch (SenhaPersonagemException e) {
            assertFalse(e.getMessage().contains("segredo-secreto"));
            assertFalse(e.getMessage().contains(codificada));
            assertNull(e.getCause());
        }
    }
}
