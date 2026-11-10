package odr_con.repository.colaboradores;

import org.springframework.data.jpa.repository.JpaRepository;

import odr_con.entity.colaboradores.Colaborador;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
	boolean existsByEmail(String email);

	boolean existsByChaveAcesso(String chaveAcesso);
}
