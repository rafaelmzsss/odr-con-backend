package odr_con.service.reclamacoes;

import java.util.List;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import odr_con.dto.reclamacoes.AtualizarReclamacaoRequest;
import odr_con.dto.reclamacoes.CadastroReclamacaoRequest;
import odr_con.dto.reclamacoes.CadastroReclamacaoResponse;
import odr_con.dto.reclamacoes.ReclamacaoResponse;
import odr_con.entity.reclamacoes.Reclamacao;
import odr_con.entity.tokens_acesso.TokenAcesso;
import odr_con.exception.reclamacoes.ReclamacaoNaoEncontradaException;
import odr_con.exception.reclamacoes.TokenAcessoNaoEncontradoException;
import odr_con.exception.reclamacoes.UsuarioDaReclamacaoNaoEncontradoException;
import odr_con.repository.reclamacoes.ReclamacaoRepository;
import odr_con.repository.tokens_acesso.TokenAcessoRepository;
import odr_con.repository.usuario.UsuarioRepository;

@Service
public class ReclamacaoService {
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private final ReclamacaoRepository reclamacaoRepository;
	private final TokenAcessoRepository tokenAcessoRepository;
	private final UsuarioRepository usuarioRepository;

	public ReclamacaoService(ReclamacaoRepository reclamacaoRepository,
			TokenAcessoRepository tokenAcessoRepository, UsuarioRepository usuarioRepository) {
		this.reclamacaoRepository = reclamacaoRepository;
		this.tokenAcessoRepository = tokenAcessoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public CadastroReclamacaoResponse cadastrar(CadastroReclamacaoRequest request) {
		validarUsuario(request.usuarioId());
		Reclamacao reclamacao = Reclamacao.builder()
				.usuarioId(request.usuarioId())
				.tipoRegistro(request.tipoRegistro().trim())
				.textoOriginal(request.textoOriginal())
				.status("PENDENTE")
				.build();
		Reclamacao salva = reclamacaoRepository.save(reclamacao);

		String tokenAcesso = null;
		if (request.usuarioId() == null) {
			tokenAcesso = gerarTokenUnico();
			TokenAcesso token = TokenAcesso.builder()
					.reclamacao(salva)
					.token(tokenAcesso)
					.ativo(true)
					.build();
			tokenAcessoRepository.save(token);
		}

		return CadastroReclamacaoResponse.from(salva, tokenAcesso);
	}

	@Transactional(readOnly = true)
	public ReclamacaoResponse buscarPorToken(String token) {
		TokenAcesso tokenAcesso = tokenAcessoRepository.findByTokenAndAtivoTrue(token)
				.orElseThrow(TokenAcessoNaoEncontradoException::new);
		if (tokenAcesso.getExpiracao() != null && !tokenAcesso.getExpiracao().isAfter(LocalDateTime.now())) {
			throw new TokenAcessoNaoEncontradoException();
		}
		return ReclamacaoResponse.from(tokenAcesso.getReclamacao());
	}

	@Transactional(readOnly = true)
	public List<ReclamacaoResponse> listar() {
		return reclamacaoRepository.findAll().stream().map(ReclamacaoResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public ReclamacaoResponse buscarPorId(Long id) {
		return ReclamacaoResponse.from(obterPorId(id));
	}

	@Transactional
	public ReclamacaoResponse atualizar(Long id, AtualizarReclamacaoRequest request) {
		validarUsuario(request.usuarioId());
		Reclamacao reclamacao = obterPorId(id);
		reclamacao.setUsuarioId(request.usuarioId());
		reclamacao.setTipoRegistro(request.tipoRegistro().trim());
		reclamacao.setTextoOriginal(request.textoOriginal());
		reclamacao.setStatus(request.status().trim());
		return ReclamacaoResponse.from(reclamacaoRepository.save(reclamacao));
	}

	@Transactional
	public void excluir(Long id) {
		Reclamacao reclamacao = obterPorId(id);
		reclamacaoRepository.delete(reclamacao);
	}

	private Reclamacao obterPorId(Long id) {
		return reclamacaoRepository.findById(id).orElseThrow(() -> new ReclamacaoNaoEncontradaException(id));
	}

	private void validarUsuario(Long usuarioId) {
		if (usuarioId != null && !usuarioRepository.existsById(usuarioId)) {
			throw new UsuarioDaReclamacaoNaoEncontradoException(usuarioId);
		}
	}

	private String gerarTokenUnico() {
		byte[] bytes = new byte[32];
		String token;
		do {
			SECURE_RANDOM.nextBytes(bytes);
			token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		} while (tokenAcessoRepository.existsByToken(token));
		return token;
	}
}
