package odr_con.service.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import odr_con.dto.usuario.CadastroUsuarioRequest;
import odr_con.dto.usuario.UsuarioResponse;
import odr_con.entity.usuario.Usuario;
import odr_con.exception.usuario.EmailJaCadastradoException;
import odr_con.repository.usuario.UsuarioRepository;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UsuarioResponse cadastrar(CadastroUsuarioRequest request) {
		Usuario salvo = cadastrarConta(request.nome(), request.email(), request.senha(), "pessoa");
		return UsuarioResponse.from(salvo);
	}

	@Transactional
	public Usuario cadastrarConta(String nome, String emailInformado, String senha, String perfil) {
		String email = emailInformado.trim().toLowerCase();

		if (usuarioRepository.existsByEmail(email)) {
			throw new EmailJaCadastradoException(email);
		}

		Usuario usuario = Usuario.builder()
				.nome(nome.trim())
				.email(email)
				.senha(passwordEncoder.encode(senha))
				.perfil(perfil)
				.build();

		return usuarioRepository.save(usuario);
	}
}
