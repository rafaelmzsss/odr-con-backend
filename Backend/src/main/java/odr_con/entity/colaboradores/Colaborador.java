package odr_con.entity.colaboradores;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "colaboradores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Colaborador {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "chave_acesso", nullable = false, unique = true)
	private String chaveAcesso;

	@Column(nullable = false)
	@Builder.Default
	private boolean ativo = true;

	@Column(name = "criado_em", nullable = false)
	private LocalDateTime criadoEm;

	@PrePersist
	void prePersist() {
		if (criadoEm == null) {
			criadoEm = LocalDateTime.now();
		}
	}
}
