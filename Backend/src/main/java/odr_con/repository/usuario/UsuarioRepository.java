package odr_con.repository.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import odr_con.entity.usuario.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	boolean existsByEmail(String email);

	Optional<Usuario> findByEmail(String email);
}
