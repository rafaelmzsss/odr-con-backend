package odr_con.entity.priscila;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conversas_priscila")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversaPriscila {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "usuario_id", nullable = false)
	private Long usuarioId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String historico;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(length = 255)
	private String empresa;

	@Column(length = 100)
	private String categoria;

	@Column(columnDefinition = "TEXT")
	private String descricao;

	@Column(name = "solucao_solicitada", columnDefinition = "TEXT")
	private String solucaoSolicitada;

	@Column(name = "criado_em", nullable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	@PrePersist
	void aoCriar() {
		LocalDateTime agora = LocalDateTime.now();
		if (criadoEm == null) criadoEm = agora;
		atualizadoEm = agora;
	}

	@PreUpdate
	void aoAtualizar() {
		atualizadoEm = LocalDateTime.now();
	}
}
