package odr_con.controller.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import odr_con.dto.usuario.CadastroUsuarioRequest;
import odr_con.dto.usuario.UsuarioResponse;
import odr_con.service.usuario.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@PostMapping
	public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
		UsuarioResponse response = usuarioService.cadastrar(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
