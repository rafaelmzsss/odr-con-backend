package odr_con.service.auth;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import odr_con.dto.auth.AuthResponse;
import odr_con.dto.auth.CadastroAuthRequest;
import odr_con.dto.auth.LoginAuthRequest;
import odr_con.entity.usuario.Usuario;
import odr_con.repository.usuario.UsuarioRepository;
import odr_con.service.usuario.UsuarioService;

@Service
public class AuthService {
	private final UsuarioRepository usuarioRepository;
	private final UsuarioService usuarioService;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final long tokenTtlSeconds;

	public AuthService(
			UsuarioRepository usuarioRepository,
			UsuarioService usuarioService,
			PasswordEncoder passwordEncoder,
			JwtEncoder jwtEncoder,
			@Value("${app.jwt.ttl-seconds:3600}") long tokenTtlSeconds) {
		this.usuarioRepository = usuarioRepository;
		this.usuarioService = usuarioService;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.tokenTtlSeconds = tokenTtlSeconds;
	}

	@Transactional
	public AuthResponse cadastrar(CadastroAuthRequest request) {
		String perfil = normalizarRole(request.role());
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		Usuario usuario = usuarioService.cadastrarConta(request.fullName(), email, request.password(), perfil);
		return emitirResposta(usuario);
	}

	@Transactional(readOnly = true)
	public AuthResponse entrar(LoginAuthRequest request) {
		String perfilInformado = normalizarRole(request.role());
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		Usuario usuario = usuarioRepository.findByEmail(email)
				.filter(candidato -> candidato.getPerfil().equals(perfilInformado))
				.filter(candidato -> passwordEncoder.matches(request.password(), candidato.getSenha()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"E-mail, senha ou perfil inválidos"));
		return emitirResposta(usuario);
	}

	private AuthResponse emitirResposta(Usuario usuario) {
		Instant agora = Instant.now();
		String role = usuario.getPerfil().toUpperCase(Locale.ROOT);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("odr-con-backend")
				.issuedAt(agora)
				.expiresAt(agora.plusSeconds(tokenTtlSeconds))
				.subject(usuario.getEmail())
				.claim("uid", usuario.getId())
				.claim("roles", List.of("ROLE_" + role))
				.build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
		return new AuthResponse(token,
				new AuthResponse.UsuarioAuth(usuario.getPerfil(), usuario.getNome(), usuario.getEmail()));
	}

	private String normalizarRole(String role) {
		String normalizada = role.trim().toLowerCase(Locale.ROOT);
		if (!normalizada.equals("pessoa") && !normalizada.equals("empresa")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil inválido");
		}
		return normalizada;
	}
}
