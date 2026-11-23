package odr_con.service.chat;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
	private static final String SYSTEM_PROMPT = """
			Você é o assistente de acolhimento de uma plataforma de resolução online de conflitos.
			Responda sempre em português do Brasil, com empatia, clareza e imparcialidade.
			Faça até três perguntas objetivas quando faltarem fatos essenciais. Considere o histórico e não repita perguntas respondidas.
			Ajude a organizar o relato e sugira, sem afirmar que já houve encaminhamento, o setor que talvez possa ajudar.
			Trate suspeitas de assédio, fraude, ameaça, violência ou exposição de dados como situações que merecem atenção humana prioritária.
			Não dê aconselhamento jurídico ou diagnóstico. Nunca peça CPF, senhas ou dados bancários; oriente a não compartilhar dados pessoais.
			Não afirme que um relato foi registrado, armazenado ou enviado a uma equipe: esta conversa não cria nem encaminha uma reclamação.
			""";
	private static final Pattern CPF = Pattern.compile("\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b");
	private static final Pattern EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
	private static final Pattern LONG_NUMBER = Pattern.compile("\\b\\d{12,16}\\b");
	private static final String[] CRITICAL_TERMS = {"assédio", "assedio", "fraude", "ameaça", "ameaca", "roubo", "suborno", "agressão", "agressao", "violência", "violencia"};

	private final ChatClient chatClient;

	public ChatService(ChatClient.Builder builder) {
		ChatMemory memory = MessageWindowChatMemory.builder().maxMessages(15).build();
		this.chatClient = builder.defaultSystem(SYSTEM_PROMPT)
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
				.build();
	}

	public TriagemResponse processarMensagem(String sessionId, String mensagemBruta) {
		String session = (sessionId == null || sessionId.isBlank())
				? "ODR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT)
				: sessionId.trim();
		boolean sensitiveOrCritical = containsCriticalTerm(mensagemBruta);
		boolean piiDetected = CPF.matcher(mensagemBruta).find() || EMAIL.matcher(mensagemBruta).find()
				|| LONG_NUMBER.matcher(mensagemBruta).find();
		boolean escalate = sensitiveOrCritical || CPF.matcher(mensagemBruta).find();
		String sanitized = EMAIL.matcher(CPF.matcher(mensagemBruta).replaceAll("[CPF_PROTEGIDO]"))
				.replaceAll("[EMAIL_PROTEGIDO]");
		sanitized = LONG_NUMBER.matcher(sanitized).replaceAll("[DADO_FINANCEIRO_PROTEGIDO]");
		try {
			String reply = chatClient.prompt().user(sanitized)
					.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, session))
					.call().content();
			if (reply == null || reply.isBlank()) {
				throw new IllegalStateException("O modelo retornou uma resposta vazia");
			}
			String severity = escalate ? "CRITICO" : inferirSeveridade(sanitized);
			return new TriagemResponse(session, reply, severity,
					"Em Triagem / Atendimento IA", piiDetected, false);
		} catch (Exception ex) {
			String severity = escalate ? "CRITICO" : inferirSeveridade(sanitized);
			return new TriagemResponse(session,
					"Recebi seu relato (protocolo " + session + "). O assistente está temporariamente indisponível; tente novamente mais tarde ou procure atendimento humano. Esta conversa não registra nem encaminha uma reclamação. Severidade sugerida: " + severity + ".",
					severity, "Pendente de Atendimento", piiDetected, true);
		}
	}

	private boolean containsCriticalTerm(String text) {
		String lower = text.toLowerCase(Locale.ROOT);
		for (String term : CRITICAL_TERMS) if (lower.contains(term)) return true;
		return false;
	}

	private String inferirSeveridade(String text) {
		String lower = text.toLowerCase(Locale.ROOT);
		if (containsCriticalTerm(text) || lower.contains("crime")) return "CRITICO";
		if (lower.contains("atraso") || lower.contains("conflito") || lower.contains("desacordo") || lower.contains("reclamação") || lower.contains("reclamacao")) return "MEDIO";
		return "LEVE";
	}

	public record TriagemResponse(String sessionId, String reply, String severidade, String statusCaso,
			boolean dadosMascarados, boolean fallbackAtivado) {}
}
