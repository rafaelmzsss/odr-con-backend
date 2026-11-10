package odr_con.controller.usuario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import odr_con.config.SecurityConfig;
import odr_con.dto.usuario.CadastroUsuarioRequest;
import odr_con.dto.usuario.UsuarioResponse;
import odr_con.exception.usuario.EmailJaCadastradoException;
import odr_con.service.usuario.UsuarioService;

@WebMvcTest(controllers = UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService usuarioService;

	@Test
	void deveCadastrarUsuarioComSucesso() throws Exception {
		UsuarioResponse response = new UsuarioResponse(
				1L,
				"Maria Silva",
				"maria@email.com",
				LocalDateTime.of(2026, 11, 4, 12, 0));

		when(usuarioService.cadastrar(any(CadastroUsuarioRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/usuarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "maria@email.com",
						  "senha": "senha123"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.nome").value("Maria Silva"))
				.andExpect(jsonPath("$.email").value("maria@email.com"))
				.andExpect(jsonPath("$.senha").doesNotExist());
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/usuarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "nome": "",
						  "email": "email-invalido",
						  "senha": "123"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos").isArray());
	}

	@Test
	void deveRetornarConflictQuandoEmailJaExiste() throws Exception {
		when(usuarioService.cadastrar(any(CadastroUsuarioRequest.class)))
				.thenThrow(new EmailJaCadastradoException("maria@email.com"));

		mockMvc.perform(post("/api/usuarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "maria@email.com",
						  "senha": "senha123"
						}
						"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensagem").value("Já existe um usuário cadastrado com o e-mail: maria@email.com"));
	}
}
