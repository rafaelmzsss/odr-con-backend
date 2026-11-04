package odr_con.dto.usuario;

import java.time.LocalDateTime;

import odr_con.entity.usuario.Usuario;

public record UsuarioResponse(
		Long id,
		String nome,
		String email,
		LocalDateTime criadoEm
) {

	public static UsuarioResponse from(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getEmail(),
				usuario.getCriadoEm());
	}
}
