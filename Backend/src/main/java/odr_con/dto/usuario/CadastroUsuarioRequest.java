package odr_con.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequest(
		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 255, message = "O nome deve ter no máximo 255 caracteres")
		String nome,

		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "Informe um e-mail válido")
		@Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres")
		String email,

		@NotBlank(message = "A senha é obrigatória")
		@Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
		String senha
) {
}
