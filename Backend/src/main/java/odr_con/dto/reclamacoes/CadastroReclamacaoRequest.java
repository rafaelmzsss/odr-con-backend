package odr_con.dto.reclamacoes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CadastroReclamacaoRequest(
		@Positive(message = "O ID do usuário deve ser positivo")
		Long usuarioId,

		@NotBlank(message = "O tipo de registro é obrigatório")
		@Size(max = 100, message = "O tipo de registro deve ter no máximo 100 caracteres")
		String tipoRegistro,

		@NotBlank(message = "O texto original é obrigatório")
		String textoOriginal
) {
}
