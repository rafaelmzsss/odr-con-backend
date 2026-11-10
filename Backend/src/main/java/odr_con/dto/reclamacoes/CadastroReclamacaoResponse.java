package odr_con.dto.reclamacoes;

import java.time.LocalDateTime;

import odr_con.entity.reclamacoes.Reclamacao;

public record CadastroReclamacaoResponse(
		Long id,
		Long usuarioId,
		String tipoRegistro,
		String textoOriginal,
		String status,
		LocalDateTime criadoEm,
		String tokenAcesso
) {
	public static CadastroReclamacaoResponse from(Reclamacao reclamacao, String tokenAcesso) {
		return new CadastroReclamacaoResponse(reclamacao.getId(), reclamacao.getUsuarioId(),
				reclamacao.getTipoRegistro(), reclamacao.getTextoOriginal(), reclamacao.getStatus(),
				reclamacao.getCriadoEm(), tokenAcesso);
	}
}
