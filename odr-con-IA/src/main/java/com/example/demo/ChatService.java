package com.example.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    private static final String SYSTEM_PROMPT_ODR = """
            Você é o assistente virtual oficial de acolhimento e mediação de uma plataforma corporativa de ODR (Online Dispute Resolution).
            Seu papel é conduzir o atendimento inicial com empatia, imparcialidade, sigilo e foco na resolução extrajudicial de conflitos (em conformidade com os PLs 533/2019 e 3.395/2026).

            DIRETRIZES DE ATENDIMENTO E TRIAGEM:

            1. Acolhimento 24/7: Receba o relato do colaborador ou cidadão de forma humana e calma.

            2. Tratamento de Relatos Fragmentados (Ambiguidade):
            Se o relato for muito curto, confuso ou faltar informações essenciais (o que aconteceu, quando ocorreu e quem são as partes/setores envolvidos), NÃO faça a classificação final ainda.
            Faça até 3 perguntas objetivas e guiadas para esclarecer os fatos.
            Considere sempre o histórico da conversa para não repetir perguntas já respondidas.

            3. Triagem Preditiva de Risco e Severidade:
            Quando houver informações suficientes no histórico do atendimento, classifique internamente o caso em:

               - LEVE: Dúvidas operacionais, ruídos de comunicação pontuais ou solicitações simples.
               - MEDIO: Conflitos interpessoais recorrentes, descumprimento de acordos internos ou falhas de processo.
               - CRITICO: Assédio moral/sexual, fraude, ameaças, exposição de dados sensíveis, linguagem tóxica grave ou risco jurídico imediato (encaminhamento imediato ao Compliance).

            4. Comprovação de Exaurimento Administrativo:
            Ao finalizar a coleta de dados, apresente um breve resumo formal consolidando todo o relato registrado para comprovar a tentativa prévia de conciliação e informe o setor de destino (RH, Jurídico, Compliance ou Ouvidoria).

            5. Bem-estar (WHO-5):
            Se o usuário demonstrar esgotamento emocional ou sofrimento psíquico no relato, ofereça ao final o convite opcional para responder ao questionário anônimo de bem-estar WHO-5.

            REGRAS ESTRITAS:

            - Nunca solicite CPF, senhas ou dados bancários. Se o usuário enviar dados mascarados como [CPF_PROTEGIDO], ignore o dado sensível e siga o atendimento normalmente.
            - Responda sempre em português do Brasil, de forma clara, estruturada e acolhedora.
            """;

    public ChatService(ChatClient.Builder chatClientBuilder) {

        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(15)
                .build();

        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT_ODR)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(this.chatMemory).build()
                )
                .build();
    }

    public TriagemResponse processarMensagem(String sessionId, String mensagemBruta) {

        // Se o frontend não enviar uma chave única, gera um protocolo de sessão automaticamente
        String protocoloSessao = (sessionId == null || sessionId.isBlank())
                ? "ODR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : sessionId.trim();

        // Camada de Guardrails (Masking de PII e detecção de toxicidade)
        boolean continhaPiiOuToxicidade = detectarPiiOuToxicidade(mensagemBruta);
        String mensagemAnonimizada = aplicarMaskingPii(mensagemBruta);

        try {

            // Chamada ao modelo LLM mantendo o histórico isolado pelo protocoloSessao
            String respostaIa = chatClient.prompt()
                    .user(mensagemAnonimizada)
                    .advisors(advisor -> advisor
                            .param(ChatMemory.CONVERSATION_ID, protocoloSessao)
                    )
                    .call()
                    .content();

            String severidade = continhaPiiOuToxicidade
                    ? "CRITICO"
                    : inferirSeveridade(mensagemAnonimizada);

            String status = continhaPiiOuToxicidade
                    ? "Crítico / Encaminhamento Imediato ao Compliance"
                    : "Em Triagem / Atendimento IA";

            return new TriagemResponse(
                    protocoloSessao,
                    respostaIa,
                    severidade,
                    status,
                    continhaPiiOuToxicidade,
                    false
            );

        } catch (Exception e) {

            // Fallback em caso de timeout ou indisponibilidade da API
            return executarFallbackDeterministico(
                    protocoloSessao,
                    mensagemAnonimizada,
                    continhaPiiOuToxicidade
            );
        }
    }

    private String aplicarMaskingPii(String texto) {

        if (texto == null) return "";

        String sanitizado = texto.replaceAll(
                "\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b",
                "[CPF_PROTEGIDO]"
        );

        sanitizado = sanitizado.replaceAll(
                "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
                "[EMAIL_PROTEGIDO]"
        );

        sanitizado = sanitizado.replaceAll(
                "\\b\\d{12,16}\\b",
                "[DADO_FINANCEIRO_PROTEGIDO]"
        );

        return sanitizado;
    }

    private boolean detectarPiiOuToxicidade(String texto) {

        if (texto == null) return false;

        String lower = texto.toLowerCase();

        boolean temCpf = Pattern.compile(
                "\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b"
        ).matcher(texto).find();

        boolean temTermoCritico =
                lower.contains("assédio") ||
                lower.contains("fraude") ||
                lower.contains("ameaça") ||
                lower.contains("roubo") ||
                lower.contains("suborno") ||
                lower.contains("agressão");

        return temCpf || temTermoCritico;
    }

    private String inferirSeveridade(String texto) {

        String lower = texto.toLowerCase();

        if (lower.contains("assédio") ||
                lower.contains("fraude") ||
                lower.contains("ameaça") ||
                lower.contains("crime")) {

            return "CRITICO";
        }

        if (lower.contains("atraso") ||
                lower.contains("conflito") ||
                lower.contains("desacordo") ||
                lower.contains("reclamação")) {

            return "MEDIO";
        }

        return "LEVE";
    }

    private TriagemResponse executarFallbackDeterministico(
            String protocoloSessao,
            String mensagemAnonimizada,
            boolean criticoForcado) {

        String severidade = criticoForcado
                ? "CRITICO"
                : inferirSeveridade(mensagemAnonimizada);

        String respostaFallback =
                "Seu relato (Protocolo: " + protocoloSessao + ") foi recebido e protegido pelo nosso protocolo de sigilo. " +
                "No momento, nosso assistente inteligente está passando por uma instabilidade temporária, " +
                "mas sua manifestação já foi registrada sob severidade [" + severidade + "] e encaminhada para " +
                "Triagem Manual prioritária da nossa equipe de mediação, garantindo o registro formal de atendimento.";

        return new TriagemResponse(
                protocoloSessao,
                respostaFallback,
                severidade,
                "Pendente de Triagem Manual",
                criticoForcado,
                true
        );
    }

    public record TriagemResponse(
            String sessionId,
            String reply,
            String severidade,
            String statusCaso,
            boolean dadosMascarados,
            boolean fallbackAtivado
    ) {}
}