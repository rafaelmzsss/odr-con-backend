package odr_con.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import odr_con.exception.colaboradores.EmailOuChaveJaCadastradosException;
import odr_con.exception.reclamacoes.ReclamacaoNaoEncontradaException;
import odr_con.exception.reclamacoes.TokenAcessoNaoEncontradoException;
import odr_con.exception.reclamacoes.UsuarioDaReclamacaoNaoEncontradoException;
import odr_con.exception.usuario.EmailJaCadastradoException;

@RestControllerAdvice
public class ApiExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
	@Value("${app.errors.include-details:false}")
	private boolean includeErrorDetails;

	@ExceptionHandler(EmailJaCadastradoException.class)
	public ResponseEntity<ErroResponse> handleEmailJaCadastrado(
			EmailJaCadastradoException ex,
			HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(
				ErroResponse.of(
						HttpStatus.CONFLICT.value(),
						"Conflict",
						ex.getMessage(),
						request.getRequestURI()));
	}

	@ExceptionHandler(EmailOuChaveJaCadastradosException.class)
	public ResponseEntity<ErroResponse> handleColaboradorJaCadastrado(
			EmailOuChaveJaCadastradosException ex,
			HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(
				ErroResponse.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler({ReclamacaoNaoEncontradaException.class, UsuarioDaReclamacaoNaoEncontradoException.class,
			TokenAcessoNaoEncontradoException.class})
	public ResponseEntity<ErroResponse> handleRecursoReclamacaoNaoEncontrado(
			RuntimeException ex,
			HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
				ErroResponse.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> handleValidacao(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		List<ErroResponse.CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
				.map(erro -> new ErroResponse.CampoErro(erro.getField(), erro.getDefaultMessage()))
				.toList();

		return ResponseEntity.badRequest().body(
				ErroResponse.of(
						HttpStatus.BAD_REQUEST.value(),
						"Bad Request",
						"Dados inválidos",
						request.getRequestURI(),
						campos));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> handleGenerico(
			Exception ex,
			HttpServletRequest request) {
		logger.error("Erro ao processar requisição para {}", request.getRequestURI(), ex);
		String mensagem = includeErrorDetails
				? ex.getClass().getSimpleName() + ": " + ex.getMessage()
				: "Erro interno inesperado";
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				ErroResponse.of(
						HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"Internal Server Error",
						mensagem,
						request.getRequestURI()));
	}
}
