package odr_con.dto.priscila;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResumoReclamacao(
		@NotBlank @Size(max = 255) String company,
		@NotBlank @Size(max = 100) String category,
		@NotBlank @Size(max = 8000) String description,
		@NotBlank @Size(max = 2000) String requestedSolution) {
}
