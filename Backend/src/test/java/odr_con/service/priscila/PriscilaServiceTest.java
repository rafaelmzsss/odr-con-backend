package odr_con.service.priscila;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import odr_con.dto.priscila.PriscilaMensagemRequest;
import odr_con.entity.priscila.ConversaPriscila;
import odr_con.repository.priscila.ConversaPriscilaRepository;

class PriscilaServiceTest {
	private final ChatClient.Builder builder = mock(ChatClient.Builder.class);
	private final ChatClient chatClient = mock(ChatClient.class);
	private final ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
	private final ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
	private final ConversaPriscilaRepository conversaRepository = mock(ConversaPriscilaRepository.class);
	private PriscilaService service;

	@BeforeEach
	void configurarCliente() {
		when(builder.defaultSystem(anyString())).thenReturn(builder);
		when(builder.build()).thenReturn(chatClient);
		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(conversaRepository.save(any(ConversaPriscila.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		service = new PriscilaService(builder, conversaRepository);
	}

	@Test
	void retornaEstadoCollectingQuandoAindaFaltamDados() {
		when(responseSpec.content()).thenReturn("""
				{"reply":"Qual foi o produto adquirido?","status":"collecting","summary":null}
				""");

		var resposta = service.enviar(10L,
				new PriscilaMensagemRequest(null, "Comprei um produto e ele apresentou defeito."));

		assertNotNull(resposta.conversationId());
		assertEquals("collecting", resposta.status());
		assertEquals("Qual foi o produto adquirido?", resposta.reply());
		assertEquals(null, resposta.summary());
	}

	@Test
	void retornaEstadoReadyForReviewComResumoCompleto() {
		when(responseSpec.content()).thenReturn("""
				{"reply":"Revise o resumo do seu relato.","status":"ready_for_review","summary":{"company":"Loja Horizonte","category":"Produto","description":"Produto apresentou defeito.","requestedSolution":"Substituição do produto"}}
				""");

		var resposta = service.enviar(10L,
				new PriscilaMensagemRequest(null, "A Loja Horizonte vendeu um produto com defeito; quero substituição."));

		assertEquals("ready_for_review", resposta.status());
		assertEquals("Loja Horizonte", resposta.summary().company());
		assertEquals("Produto", resposta.summary().category());
		assertEquals("Substituição do produto", resposta.summary().requestedSolution());
	}
}
