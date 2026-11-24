package odr_con.service.reclamacoes;

import java.time.Year;
import java.util.Locale;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import odr_con.dto.reclamacoes.ConfirmarRelatoRequest;
import odr_con.dto.reclamacoes.ConfirmarRelatoResponse;
import odr_con.entity.priscila.ConversaPriscila;
import odr_con.entity.reclamacoes.Reclamacao;
import odr_con.repository.priscila.ConversaPriscilaRepository;
import odr_con.repository.reclamacoes.ReclamacaoRepository;

@Service
public class RegistroRelatoService {
	private final ConversaPriscilaRepository conversaRepository;
	private final ReclamacaoRepository reclamacaoRepository;

	public RegistroRelatoService(
			ConversaPriscilaRepository conversaRepository,
			ReclamacaoRepository reclamacaoRepository) {
		this.conversaRepository = conversaRepository;
		this.reclamacaoRepository = reclamacaoRepository;
	}

	@Transactional
	public ConfirmarRelatoResponse registrar(Long usuarioId, ConfirmarRelatoRequest request) {
		if (usuarioId == null) throw new IllegalArgumentException("usuarioId é obrigatório nesta rota");
		return registrarParaUsuario(usuarioId, request);
	}

	@Transactional
	public ConfirmarRelatoResponse registrarAnonimo(ConfirmarRelatoRequest request) {
		return registrarParaUsuario(null, request);
	}

	private ConfirmarRelatoResponse registrarParaUsuario(Long usuarioId, ConfirmarRelatoRequest request) {
		var conversaEncontrada = usuarioId == null
				? conversaRepository.findAnonymousByIdForUpdate(request.conversationId())
				: conversaRepository.findByIdAndUsuarioIdForUpdate(request.conversationId(), usuarioId);
		ConversaPriscila conversa = conversaEncontrada
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada"));
		if (!"ready_for_review".equals(conversa.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"O relato ainda não está pronto para confirmação");
		}

		var resumo = request.summary();
		var existente = reclamacaoRepository.findByConversationId(request.conversationId());
		if (existente.isPresent()) {
			if (!Objects.equals(usuarioId, existente.get().getUsuarioId())) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada");
			}
			return new ConfirmarRelatoResponse(existente.get().getProtocolo(), "registered");
		}

		Reclamacao reclamacao = Reclamacao.builder()
				.usuarioId(usuarioId)
				.tipoRegistro(resumo.category().trim())
				.textoOriginal(resumo.description().trim())
				.empresa(resumo.company().trim())
				.solucaoSolicitada(resumo.requestedSolution().trim())
				.conversationId(conversa.getId())
				.status("REGISTRADA")
				.build();
		Reclamacao salva = reclamacaoRepository.saveAndFlush(reclamacao);
		salva.setProtocolo(String.format(Locale.ROOT, "ODR-%d-%06d", Year.now().getValue(), salva.getId()));
		reclamacaoRepository.save(salva);
		return new ConfirmarRelatoResponse(salva.getProtocolo(), "registered");
	}
}
