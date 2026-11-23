package odr_con.controller.priscila;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import odr_con.config.JwtConfig;
import odr_con.config.SecurityConfig;
import odr_con.dto.priscila.PriscilaMensagemRequest;
import odr_con.dto.priscila.PriscilaMensagemResponse;
import odr_con.service.priscila.PriscilaService;

@WebMvcTest(controllers = PriscilaController.class)
@Import({SecurityConfig.class, JwtConfig.class})
@ActiveProfiles("test")
class PriscilaSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PriscilaService priscilaService;

	@Test
	void exigeBearerTokenValidoEPermitePessoaAutenticada() throws Exception {
		when(priscilaService.enviar(eq(42L), any(PriscilaMensagemRequest.class)))
				.thenReturn(new PriscilaMensagemResponse("conv", "Qual empresa?", "collecting", null));

		mockMvc.perform(post("/api/priscila/messages")
				.with(jwt().jwt(jwt -> jwt.claim("uid", 42L))
						.authorities(new SimpleGrantedAuthority("ROLE_PESSOA")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"conversationId\":null,\"message\":\"Comprei um produto.\"}"))
				.andExpect(status().isOk());

		verify(priscilaService).enviar(eq(42L), any(PriscilaMensagemRequest.class));
	}

	@Test
	void rejeitaTokenInvalido() throws Exception {
		mockMvc.perform(post("/api/priscila/messages")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"conversationId\":null,\"message\":\"teste\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void perfilEmpresaNaoPodeUsarConversaDePessoa() throws Exception {
		mockMvc.perform(post("/api/priscila/messages")
				.with(jwt().jwt(jwt -> jwt.claim("uid", 42L))
						.authorities(new SimpleGrantedAuthority("ROLE_EMPRESA")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"conversationId\":null,\"message\":\"teste\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void preflightAceitaSomenteOrigemLocalConfigurada() throws Exception {
		mockMvc.perform(options("/api/priscila/messages")
				.header(HttpHeaders.ORIGIN, "http://localhost:5173")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type,accept"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
	}
}
