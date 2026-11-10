package odr_con.controller.colaboradores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import odr_con.dto.colaboradores.CadastroColaboradorRequest;
import odr_con.dto.colaboradores.ColaboradorResponse;
import odr_con.service.colaboradores.ColaboradorService;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {
	private final ColaboradorService colaboradorService;

	public ColaboradorController(ColaboradorService colaboradorService) {
		this.colaboradorService = colaboradorService;
	}

	@PostMapping
	public ResponseEntity<ColaboradorResponse> cadastrar(@Valid @RequestBody CadastroColaboradorRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(colaboradorService.cadastrar(request));
	}
}
