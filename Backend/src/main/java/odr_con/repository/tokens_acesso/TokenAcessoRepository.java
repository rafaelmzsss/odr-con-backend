package odr_con.repository.tokens_acesso;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import odr_con.entity.tokens_acesso.TokenAcesso;

public interface TokenAcessoRepository extends JpaRepository<TokenAcesso, Long> {
	boolean existsByToken(String token);

	@EntityGraph(attributePaths = "reclamacao")
	Optional<TokenAcesso> findByTokenAndAtivoTrue(String token);
}
