package odr_con.controller.priscila;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import odr_con.dto.priscila.PriscilaMensagemRequest;
import odr_con.dto.priscila.PriscilaMensagemResponse;
import odr_con.service.priscila.PriscilaService;

@RestController
@RequestMapping("/api/priscila/messages")
public class PriscilaController {
	private final PriscilaService priscilaService;

	public PriscilaController(PriscilaService priscilaService) {
		this.priscilaService = priscilaService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	public PriscilaMensagemResponse enviar(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody PriscilaMensagemRequest request) {
		Object uid = jwt.getClaim("uid");
		if (!(uid instanceof Number id)) {
			throw new org.springframework.web.server.ResponseStatusException(
					HttpStatus.UNAUTHORIZED, "Token sem identificador de usuário");
		}
		return priscilaService.enviar(id.longValue(), request);
	}
}
