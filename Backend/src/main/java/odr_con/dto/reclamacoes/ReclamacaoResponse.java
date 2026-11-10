package odr_con.dto.reclamacoes;

import java.time.LocalDateTime;

import odr_con.entity.reclamacoes.Reclamacao;

public record ReclamacaoResponse(
		Long id,
		Long usuarioId,
		String tipoRegistro,
		String textoOriginal,
		String status,
		LocalDateTime criadoEm
) {
	public static ReclamacaoResponse from(Reclamacao reclamacao) {
		return new ReclamacaoResponse(reclamacao.getId(), reclamacao.getUsuarioId(), reclamacao.getTipoRegistro(),
				reclamacao.getTextoOriginal(), reclamacao.getStatus(), reclamacao.getCriadoEm());
	}
}
