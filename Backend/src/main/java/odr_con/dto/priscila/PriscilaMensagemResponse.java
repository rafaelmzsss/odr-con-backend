package odr_con.dto.priscila;

public record PriscilaMensagemResponse(
		String conversationId,
		String reply,
		String status,
		ResumoReclamacao summary) {
}
