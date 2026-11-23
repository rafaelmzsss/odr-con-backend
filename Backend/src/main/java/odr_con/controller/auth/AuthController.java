package odr_con.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import odr_con.dto.auth.AuthResponse;
import odr_con.dto.auth.CadastroAuthRequest;
import odr_con.dto.auth.EsqueciSenhaRequest;
import odr_con.dto.auth.LoginAuthRequest;
import odr_con.service.auth.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> cadastrar(@Valid @RequestBody CadastroAuthRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(request));
	}

	@PostMapping("/login")
	public AuthResponse entrar(@Valid @RequestBody LoginAuthRequest request) {
		return authService.entrar(request);
	}

	@PostMapping("/password/forgot")
	public ResponseEntity<RecuperacaoResposta> solicitarRecuperacao(
			@Valid @RequestBody EsqueciSenhaRequest request) {
		// Resposta constante para não confirmar se o e-mail está cadastrado.
		return ResponseEntity.accepted().body(new RecuperacaoResposta("not_configured",
				"A recuperação de senha ainda não está configurada. Nenhum e-mail foi enviado."));
	}

	public record RecuperacaoResposta(String status, String message) {
	}
}
