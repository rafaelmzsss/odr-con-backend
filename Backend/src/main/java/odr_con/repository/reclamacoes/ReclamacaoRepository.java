package odr_con.repository.reclamacoes;

import org.springframework.data.jpa.repository.JpaRepository;

import odr_con.entity.reclamacoes.Reclamacao;

public interface ReclamacaoRepository extends JpaRepository<Reclamacao, Long> {
}
