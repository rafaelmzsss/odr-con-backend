package odr_con.dto.reclamacoes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ConfirmarRelatoRequest(
		@NotBlank @Pattern(regexp = "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")
		String conversationId,
		@NotNull @Valid ResumoReclamacaoRequest summary) {

	public record ResumoReclamacaoRequest(
			@NotBlank @jakarta.validation.constraints.Size(max = 255) String company,
			@NotBlank @jakarta.validation.constraints.Size(max = 100) String category,
			@NotBlank @jakarta.validation.constraints.Size(max = 8000) String description,
			@NotBlank @jakarta.validation.constraints.Size(max = 2000) String requestedSolution) {
	}
}
