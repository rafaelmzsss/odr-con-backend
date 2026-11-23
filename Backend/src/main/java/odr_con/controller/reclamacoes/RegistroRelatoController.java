package odr_con.controller.reclamacoes;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import odr_con.dto.reclamacoes.ConfirmarRelatoRequest;
import odr_con.dto.reclamacoes.ConfirmarRelatoResponse;
import odr_con.service.reclamacoes.RegistroRelatoService;

@RestController
@RequestMapping("/api/reports")
public class RegistroRelatoController {
	private final RegistroRelatoService registroRelatoService;

	public RegistroRelatoController(RegistroRelatoService registroRelatoService) {
		this.registroRelatoService = registroRelatoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ConfirmarRelatoResponse registrar(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody ConfirmarRelatoRequest request) {
		Object uid = jwt.getClaim("uid");
		if (!(uid instanceof Number id)) {
			throw new org.springframework.web.server.ResponseStatusException(
					HttpStatus.UNAUTHORIZED, "Token sem identificador de usuário");
		}
		return registroRelatoService.registrar(id.longValue(), request);
	}
}
