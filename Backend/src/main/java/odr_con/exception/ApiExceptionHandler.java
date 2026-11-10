package odr_con.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import odr_con.exception.usuario.EmailJaCadastradoException;

@RestControllerAdvice
public class ApiExceptionHandler {

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
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				ErroResponse.of(
						HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"Internal Server Error",
						"Erro interno inesperado",
						request.getRequestURI()));
	}
}
