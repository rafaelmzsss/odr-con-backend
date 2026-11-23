package odr_con.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import odr_con.config.JwtConfig;
import odr_con.config.SecurityConfig;
import odr_con.dto.auth.AuthResponse;
import odr_con.dto.auth.CadastroAuthRequest;
import odr_con.service.auth.AuthService;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtConfig.class})
@ActiveProfiles("test")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Test
	void cadastroDevolveContratoConsumidoPeloFrontend() throws Exception {
		when(authService.cadastrar(any(CadastroAuthRequest.class)))
				.thenReturn(new AuthResponse("jwt-teste",
						new AuthResponse.UsuarioAuth("pessoa", "Ana Silva", "ana@example.com")));

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"ana@example.com","password":"senha123","role":"pessoa","fullName":"Ana Silva"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").value("jwt-teste"))
				.andExpect(jsonPath("$.user.role").value("pessoa"))
				.andExpect(jsonPath("$.user.fullName").value("Ana Silva"))
				.andExpect(jsonPath("$.user.email").value("ana@example.com"))
				.andExpect(jsonPath("$.user.password").doesNotExist());
	}

	@Test
	void recuperacaoInformaQueAindaNaoEstaConfiguradaSemConsultarEmail() throws Exception {
		mockMvc.perform(post("/api/auth/password/forgot")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"nao-existe@example.com\"}"))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.status").value("not_configured"))
				.andExpect(jsonPath("$.message").value(
						"A recuperação de senha ainda não está configurada. Nenhum e-mail foi enviado."));
	}
}
