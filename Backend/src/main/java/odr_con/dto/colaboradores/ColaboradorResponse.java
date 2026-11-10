package odr_con.dto.colaboradores;

import java.time.LocalDateTime;

import odr_con.entity.colaboradores.Colaborador;

public record ColaboradorResponse(Long id, String nome, String email, boolean ativo, LocalDateTime criadoEm) {
	public static ColaboradorResponse from(Colaborador colaborador) {
		return new ColaboradorResponse(colaborador.getId(), colaborador.getNome(), colaborador.getEmail(),
				colaborador.isAtivo(), colaborador.getCriadoEm());
	}
}
