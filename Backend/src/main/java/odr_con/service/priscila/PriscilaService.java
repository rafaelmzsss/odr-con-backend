package odr_con.service.priscila;

import java.util.UUID;
import java.util.regex.Pattern;

import com.google.genai.errors.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import odr_con.dto.priscila.PriscilaMensagemResponse;
import odr_con.dto.priscila.PriscilaMensagemRequest;
import odr_con.dto.priscila.ResumoReclamacao;
import odr_con.entity.priscila.ConversaPriscila;
import odr_con.repository.priscila.ConversaPriscilaRepository;

@Service
public class PriscilaService {
	private static final Logger logger = LoggerFactory.getLogger(PriscilaService.class);
	private static final String COLLECTING = "collecting";
	private static final String READY_FOR_REVIEW = "ready_for_review";
	private static final int MAX_HISTORY_CHARS = 18000;
	private static final Pattern CPF = Pattern.compile("\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b");
	private static final Pattern EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
	private static final Pattern LONG_NUMBER = Pattern.compile("\\b\\d{12,16}\\b");
	private static final String SYSTEM_PROMPT = """
			Você é Priscila, assistente de acolhimento da plataforma ODR-CON. Converse em português do Brasil, com empatia e objetividade.
			Colete, sem inventar, quatro informações: empresa, categoria, descrição do problema e solução solicitada.
			Faça uma pergunta curta por vez para obter o que estiver faltando. Quando os quatro dados estiverem presentes, apresente um resumo conciso para revisão.
			Nunca diga que o relato foi registrado. O registro só acontece depois da confirmação da pessoa.
			Responda somente com JSON válido, sem markdown, neste formato exato:
			{"reply":"texto para a pessoa","status":"collecting","summary":null}
			ou, quando completo:
			{"reply":"Revise o resumo do seu relato.","status":"ready_for_review","summary":{"company":"...","category":"...","description":"...","requestedSolution":"..."}}
			""";

	private final ChatClient chatClient;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ConversaPriscilaRepository conversaRepository;

	public PriscilaService(
			ChatClient.Builder builder,
			ConversaPriscilaRepository conversaRepository) {
		this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
		this.conversaRepository = conversaRepository;
	}

	public PriscilaMensagemResponse enviar(Long usuarioId, PriscilaMensagemRequest request) {
		if (usuarioId == null) throw new IllegalArgumentException("usuarioId é obrigatório nesta rota");
		return enviarNaConversa(usuarioId, request);
	}

	public PriscilaMensagemResponse enviarAnonimo(PriscilaMensagemRequest request) {
		return enviarNaConversa(null, request);
	}

	private PriscilaMensagemResponse enviarNaConversa(Long usuarioId, PriscilaMensagemRequest request) {
		ConversaPriscila conversa = obterOuCriarConversa(usuarioId, request.conversationId());
		if (READY_FOR_REVIEW.equals(conversa.getStatus())) {
			return respostaAtual(conversa, "Revise o resumo do seu relato.");
		}

		String mensagem = mascarar(request.message().trim());
		String historico = limitar(conversa.getHistorico() + "\nPessoa: " + mensagem + "\n");
		conversa.setHistorico(historico);
		conversaRepository.save(conversa);

		try {
			String raw = chatClient.prompt().user(historico).call().content();
			JsonNode resultado = objectMapper.readTree(limparJson(raw));
			String reply = texto(resultado, "reply");
			String status = texto(resultado, "status");
			JsonNode summaryNode = resultado.get("summary");
			ResumoReclamacao resumo = resumirSeCompleto(status, summaryNode);
			if (resumo == null) {
				boolean modeloPediuRevisao = READY_FOR_REVIEW.equals(status);
				status = COLLECTING;
				if (modeloPediuRevisao) {
					reply = "Ainda faltam informações para preparar o resumo. Pode completar o que falta?";
				}
				conversa.setEmpresa(null);
				conversa.setCategoria(null);
				conversa.setDescricao(null);
				conversa.setSolucaoSolicitada(null);
			} else {
				status = READY_FOR_REVIEW;
				conversa.setEmpresa(resumo.company());
				conversa.setCategoria(resumo.category());
				conversa.setDescricao(resumo.description());
				conversa.setSolucaoSolicitada(resumo.requestedSolution());
			}
			if (reply.isBlank()) {
				reply = resumo == null ? "Pode me contar mais um detalhe para eu organizar seu relato?"
						: "Revise o resumo do seu relato.";
			}
			conversa.setStatus(status);
			conversa.setHistorico(limitar(historico + "Priscila: " + reply + "\n"));
			conversaRepository.save(conversa);
			return new PriscilaMensagemResponse(conversa.getId(), reply, status, resumo);
		} catch (Exception ex) {
			logger.warn("Falha na conversa Priscila; cadeia de causas: {}", cadeiaCausasSegura(ex));
			conversa.setStatus(COLLECTING);
			conversaRepository.save(conversa);
			return new PriscilaMensagemResponse(conversa.getId(),
					"A Priscila está temporariamente indisponível. Tente enviar sua mensagem novamente.",
					COLLECTING, null);
		}
	}

	private String cadeiaCausasSegura(Throwable erro) {
		StringBuilder cadeia = new StringBuilder();
		Throwable atual = erro;
		int nivel = 0;
		while (atual != null && nivel < 5) {
			if (nivel > 0) cadeia.append(" -> ");
			cadeia.append(atual.getClass().getSimpleName());
			if (atual instanceof RestClientResponseException resposta) {
				cadeia.append(" (HTTP ").append(resposta.getStatusCode().value()).append(')');
			}
			if (atual instanceof ApiException resposta) {
				cadeia.append(" (HTTP ").append(resposta.code()).append(", ")
						.append(resposta.status()).append(')');
			}
			if (atual.getCause() == atual) break;
			atual = atual.getCause();
			nivel++;
		}
		return cadeia.toString();
	}

	private ConversaPriscila obterOuCriarConversa(Long usuarioId, String idInformado) {
		if (idInformado == null) {
			return conversaRepository.save(ConversaPriscila.builder()
					.id(UUID.randomUUID().toString())
					.usuarioId(usuarioId)
					.historico("")
					.status(COLLECTING)
					.build());
		}
		var conversa = usuarioId == null
				? conversaRepository.findByIdAndUsuarioIdIsNull(idInformado)
				: conversaRepository.findByIdAndUsuarioId(idInformado, usuarioId);
		return conversa
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Conversa não encontrada"));
	}

	private ResumoReclamacao resumirSeCompleto(String status, JsonNode node) {
		if (!READY_FOR_REVIEW.equals(status) || node == null || node.isNull()) return null;
		String company = texto(node, "company");
		String category = texto(node, "category");
		String description = texto(node, "description");
		String requestedSolution = texto(node, "requestedSolution");
		if (company.isBlank() || category.isBlank() || description.isBlank() || requestedSolution.isBlank()) {
			return null;
		}
		if (company.length() > 255 || category.length() > 100 || description.length() > 8000
				|| requestedSolution.length() > 2000) return null;
		return new ResumoReclamacao(company, category, description, requestedSolution);
	}

	private String texto(JsonNode node, String campo) {
		JsonNode value = node == null ? null : node.get(campo);
		return value == null || value.isNull() ? "" : value.asText("").trim();
	}

	private String limparJson(String raw) {
		if (raw == null) throw new IllegalArgumentException("Resposta vazia do modelo");
		String json = raw.trim();
		if (json.startsWith("```")) {
			json = json.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
		}
		return json;
	}

	private String limitar(String historico) {
		return historico.length() <= MAX_HISTORY_CHARS
				? historico
				: historico.substring(historico.length() - MAX_HISTORY_CHARS);
	}

	private String mascarar(String mensagem) {
		String segura = CPF.matcher(mensagem).replaceAll("[CPF_PROTEGIDO]");
		segura = EMAIL.matcher(segura).replaceAll("[EMAIL_PROTEGIDO]");
		return LONG_NUMBER.matcher(segura).replaceAll("[NUMERO_PROTEGIDO]");
	}

	private PriscilaMensagemResponse respostaAtual(ConversaPriscila conversa, String reply) {
		ResumoReclamacao resumo = new ResumoReclamacao(conversa.getEmpresa(), conversa.getCategoria(),
				conversa.getDescricao(), conversa.getSolucaoSolicitada());
		return new PriscilaMensagemResponse(conversa.getId(), reply, conversa.getStatus(), resumo);
	}
}
