package odr_con.dto.priscila;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PriscilaMensagemRequest(
		@Size(max = 36) @Pattern(regexp = "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")
		String conversationId,
		@NotBlank @Size(max = 4000) String message) {
}
