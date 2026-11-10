package odr_con.exception.reclamacoes;

public class UsuarioDaReclamacaoNaoEncontradoException extends RuntimeException {
	public UsuarioDaReclamacaoNaoEncontradoException(Long id) {
		super("Usuário não encontrado com o ID: " + id);
	}
}
