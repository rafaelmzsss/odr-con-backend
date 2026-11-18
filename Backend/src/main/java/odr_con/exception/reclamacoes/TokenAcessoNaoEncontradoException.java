package odr_con.exception.reclamacoes;

public class TokenAcessoNaoEncontradoException extends RuntimeException {
	public TokenAcessoNaoEncontradoException() {
		super("Token de acompanhamento não encontrado");
	}
}
