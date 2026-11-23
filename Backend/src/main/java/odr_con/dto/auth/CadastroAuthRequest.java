package odr_con.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroAuthRequest(
		@JsonAlias("nome")
		@NotBlank @Size(max = 255) String fullName,

		@NotBlank @Email @Size(max = 255) String email,

		@JsonAlias("senha")
		@NotBlank @Size(min = 6, max = 100) String password,

		@NotBlank @Pattern(regexp = "(?i)^(pessoa|empresa)$") String role) {
}
