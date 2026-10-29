package com.kevin.gestorproducao.rules.exception;

/**
 * Falha ao codificar ou decodificar a senha de um personagem. A mensagem é fixa de propósito:
 * nunca carrega senha, chave nem o valor codificado.
 */
public class SenhaPersonagemException extends RuntimeException {

    public enum Tipo {
        CHAVE_AUSENTE("Chave de senhas não configurada."),
        CHAVE_INVALIDA("Chave de senhas inválida."),
        FALHA_AUTENTICACAO("Chave de senhas incorreta ou senha adulterada."),
        VALOR_INVALIDO("Senha codificada em formato inválido."),
        FALHA_CODIFICACAO("Não foi possível codificar a senha.");

        private final String mensagem;

        Tipo(String mensagem) {
            this.mensagem = mensagem;
        }
    }

    private final Tipo tipo;

    public SenhaPersonagemException(Tipo tipo) {
        super(tipo.mensagem);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public boolean exigeConfigurarChave() {
        return tipo == Tipo.CHAVE_AUSENTE || tipo == Tipo.CHAVE_INVALIDA ||
            tipo == Tipo.FALHA_AUTENTICACAO;
    }
}
