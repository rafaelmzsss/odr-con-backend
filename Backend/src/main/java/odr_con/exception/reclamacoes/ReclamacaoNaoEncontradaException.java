package odr_con.exception.reclamacoes;

public class ReclamacaoNaoEncontradaException extends RuntimeException {
	public ReclamacaoNaoEncontradaException(Long id) {
		super("Reclamação não encontrada com o ID: " + id);
	}
}
