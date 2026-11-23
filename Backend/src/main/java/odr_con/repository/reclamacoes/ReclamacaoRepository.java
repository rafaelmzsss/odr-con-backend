package odr_con.repository.reclamacoes;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import odr_con.entity.reclamacoes.Reclamacao;

public interface ReclamacaoRepository extends JpaRepository<Reclamacao, Long> {
	boolean existsByTokenAcesso(String tokenAcesso);

	Optional<Reclamacao> findByTokenAcesso(String tokenAcesso);

	Optional<Reclamacao> findByConversationId(String conversationId);
}
