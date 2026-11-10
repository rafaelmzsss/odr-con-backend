package odr_con.controller.reclamacoes;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import odr_con.dto.reclamacoes.AtualizarReclamacaoRequest;
import odr_con.dto.reclamacoes.CadastroReclamacaoRequest;
import odr_con.dto.reclamacoes.CadastroReclamacaoResponse;
import odr_con.dto.reclamacoes.ReclamacaoResponse;
import odr_con.service.reclamacoes.ReclamacaoService;

@RestController
@RequestMapping("/api/reclamacoes")
public class ReclamacaoController {
	private final ReclamacaoService reclamacaoService;

	public ReclamacaoController(ReclamacaoService reclamacaoService) {
		this.reclamacaoService = reclamacaoService;
	}

	@PostMapping
	public ResponseEntity<CadastroReclamacaoResponse> cadastrar(@Valid @RequestBody CadastroReclamacaoRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(reclamacaoService.cadastrar(request));
	}

	@GetMapping("/acompanhar/{token}")
	public ReclamacaoResponse buscarPorToken(@PathVariable String token) {
		return reclamacaoService.buscarPorToken(token);
	}

	@GetMapping
	public List<ReclamacaoResponse> listar() {
		return reclamacaoService.listar();
	}

	@GetMapping("/{id}")
	public ReclamacaoResponse buscarPorId(@PathVariable Long id) {
		return reclamacaoService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	public ReclamacaoResponse atualizar(@PathVariable Long id,
			@Valid @RequestBody AtualizarReclamacaoRequest request) {
		return reclamacaoService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		reclamacaoService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
