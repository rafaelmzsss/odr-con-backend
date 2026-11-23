package odr_con.service.reclamacoes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import odr_con.dto.reclamacoes.ConfirmarRelatoRequest;
import odr_con.entity.priscila.ConversaPriscila;
import odr_con.entity.reclamacoes.Reclamacao;
import odr_con.repository.priscila.ConversaPriscilaRepository;
import odr_con.repository.reclamacoes.ReclamacaoRepository;

class RegistroRelatoServiceTest {
	private final ConversaPriscilaRepository conversaRepository = org.mockito.Mockito.mock(ConversaPriscilaRepository.class);
	private final ReclamacaoRepository reclamacaoRepository = org.mockito.Mockito.mock(ReclamacaoRepository.class);
	private final String conversationId = "a3101a2c-6b16-47bb-9cff-9cc90e80cced";
	private RegistroRelatoService service;

	@BeforeEach
	void configurar() {
		service = new RegistroRelatoService(conversaRepository, reclamacaoRepository);
		when(conversaRepository.findByIdAndUsuarioIdForUpdate(conversationId, 10L))
				.thenReturn(Optional.of(ConversaPriscila.builder()
						.id(conversationId).usuarioId(10L).status("ready_for_review")
						.empresa("Loja Horizonte").categoria("Produto")
						.descricao("Produto apresentou defeito.")
						.solucaoSolicitada("Substituição do produto").build()));
		when(reclamacaoRepository.findByConversationId(conversationId)).thenReturn(Optional.empty());
	}

	private ConfirmarRelatoRequest request() {
		return new ConfirmarRelatoRequest(conversationId,
				new ConfirmarRelatoRequest.ResumoReclamacaoRequest("Loja Horizonte", "Produto",
						"Produto apresentou defeito.", "Substituição do produto"));
	}

	@Test
	void geraProtocoloNoServidorAoRegistrarResumoConfirmado() {
		when(reclamacaoRepository.saveAndFlush(any(Reclamacao.class)))
				.thenAnswer(invocation -> Reclamacao.builder().id(27L).build());
		when(reclamacaoRepository.save(any(Reclamacao.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var resposta = service.registrar(10L, request());

		assertEquals("ODR-" + Year.now().getValue() + "-000027", resposta.protocol());
		assertEquals("registered", resposta.status());
	}

	@Test
	void repeticaoDaMesmaConversaDevolveProtocoloExistente() {
		when(reclamacaoRepository.findByConversationId(conversationId))
				.thenReturn(Optional.of(Reclamacao.builder().id(27L).usuarioId(10L)
						.conversationId(conversationId)
						.protocolo("ODR-" + Year.now().getValue() + "-000027").build()));

		var resposta = service.registrar(10L, request());

		assertEquals("ODR-" + Year.now().getValue() + "-000027", resposta.protocol());
		verify(reclamacaoRepository, never()).saveAndFlush(any(Reclamacao.class));
	}

	@Test
	void registraResumoEditadoEConfirmadoPelaPessoa() {
		ConfirmarRelatoRequest alterado = new ConfirmarRelatoRequest(conversationId,
				new ConfirmarRelatoRequest.ResumoReclamacaoRequest("Outra loja", "Produto",
						"Produto apresentou defeito.", "Substituição do produto"));
		when(reclamacaoRepository.saveAndFlush(any(Reclamacao.class)))
				.thenAnswer(invocation -> Reclamacao.builder().id(28L).build());
		when(reclamacaoRepository.save(any(Reclamacao.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var resposta = service.registrar(10L, alterado);
		ArgumentCaptor<Reclamacao> captor = ArgumentCaptor.forClass(Reclamacao.class);
		verify(reclamacaoRepository).saveAndFlush(captor.capture());
		assertEquals("Outra loja", captor.getValue().getEmpresa());
		assertEquals("ODR-" + Year.now().getValue() + "-000028", resposta.protocol());
	}
}
