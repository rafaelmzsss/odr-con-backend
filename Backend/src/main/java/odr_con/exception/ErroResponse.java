package odr_con.exception;

import java.time.Instant;
import java.util.List;

public record ErroResponse(
		Instant timestamp,
		int status,
		String erro,
		String mensagem,
		String caminho,
		List<CampoErro> campos
) {

	public record CampoErro(String campo, String mensagem) {
	}

	public static ErroResponse of(int status, String erro, String mensagem, String caminho) {
		return new ErroResponse(Instant.now(), status, erro, mensagem, caminho, List.of());
	}

	public static ErroResponse of(int status, String erro, String mensagem, String caminho, List<CampoErro> campos) {
		return new ErroResponse(Instant.now(), status, erro, mensagem, caminho, campos);
	}
}
