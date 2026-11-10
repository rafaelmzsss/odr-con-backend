package odr_con.dto.colaboradores;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroColaboradorRequest(
		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 255, message = "O nome deve ter no máximo 255 caracteres")
		String nome,

		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "Informe um e-mail válido")
		@Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres")
		String email,

		@NotBlank(message = "A chave de acesso é obrigatória")
		@Size(max = 255, message = "A chave de acesso deve ter no máximo 255 caracteres")
		String chaveAcesso
) {
}
