package odr_con.entity.tokens_acesso;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odr_con.entity.reclamacoes.Reclamacao;

@Entity
@Table(name = "tokens_acesso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenAcesso {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reclamacao_id", nullable = false)
	private Reclamacao reclamacao;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(name = "criado_em", nullable = false)
	private LocalDateTime criadoEm;

	private LocalDateTime expiracao;

	@Column(nullable = false)
	@Builder.Default
	private boolean ativo = true;

	@PrePersist
	void prePersist() {
		if (criadoEm == null) {
			criadoEm = LocalDateTime.now();
		}
	}
}
