package odr_con.entity.reclamacoes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import odr_con.entity.tokens_acesso.TokenAcesso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reclamacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reclamacao {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id")
	private Long usuarioId;

	@Column(name = "tipo_registro", nullable = false, length = 100)
	private String tipoRegistro;

	@Column(name = "texto_original", nullable = false, columnDefinition = "TEXT")
	private String textoOriginal;

	@Column(nullable = false, length = 50)
	@Builder.Default
	private String status = "PENDENTE";

	@Column(name = "criado_em", nullable = false)
	private LocalDateTime criadoEm;

	@OneToMany(mappedBy = "reclamacao", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<TokenAcesso> tokensAcesso = new ArrayList<>();

	@PrePersist
	void prePersist() {
		if (status == null || status.isBlank()) {
			status = "PENDENTE";
		}
		if (criadoEm == null) {
			criadoEm = LocalDateTime.now();
		}
	}
}
