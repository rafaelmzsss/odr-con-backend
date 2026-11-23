package odr_con.service.reclamacoes;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import odr_con.dto.reclamacoes.AtualizarReclamacaoRequest;
import odr_con.dto.reclamacoes.CadastroReclamacaoRequest;
import odr_con.dto.reclamacoes.CadastroReclamacaoResponse;
import odr_con.dto.reclamacoes.ReclamacaoResponse;
import odr_con.entity.reclamacoes.Reclamacao;
import odr_con.exception.reclamacoes.ReclamacaoNaoEncontradaException;
import odr_con.exception.reclamacoes.TokenAcessoNaoEncontradoException;
import odr_con.exception.reclamacoes.UsuarioDaReclamacaoNaoEncontradoException;
import odr_con.repository.reclamacoes.ReclamacaoRepository;
import odr_con.repository.usuario.UsuarioRepository;

@Service
public class ReclamacaoService {
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final int TOKEN_LENGTH = 16;
	private final ReclamacaoRepository reclamacaoRepository;
	private final UsuarioRepository usuarioRepository;

	public ReclamacaoService(ReclamacaoRepository reclamacaoRepository, UsuarioRepository usuarioRepository) {
		this.reclamacaoRepository = reclamacaoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public CadastroReclamacaoResponse cadastrar(CadastroReclamacaoRequest request, Long usuarioAutenticadoId) {
		validarUsuario(usuarioAutenticadoId);
		if (request.usuarioId() != null && !request.usuarioId().equals(usuarioAutenticadoId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O usuário do token não corresponde ao relato");
		}
		String tokenAcesso = gerarTokenUnico();
		Reclamacao reclamacao = Reclamacao.builder()
				.usuarioId(usuarioAutenticadoId)
				.tipoRegistro(request.tipoRegistro().trim())
				.textoOriginal(request.textoOriginal())
				.status("PENDENTE")
				.tokenAcesso(tokenAcesso)
				.build();
		Reclamacao salva = reclamacaoRepository.save(reclamacao);
		return CadastroReclamacaoResponse.from(salva, tokenAcesso);
	}

	@Transactional(readOnly = true)
	public ReclamacaoResponse buscarPorToken(String token) {
		Reclamacao reclamacao = reclamacaoRepository.findByTokenAcesso(token)
				.orElseThrow(TokenAcessoNaoEncontradoException::new);
		return ReclamacaoResponse.from(reclamacao);
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
		String token;
		do {
			StringBuilder digitos = new StringBuilder(TOKEN_LENGTH);
			for (int i = 0; i < TOKEN_LENGTH; i++) {
				digitos.append(SECURE_RANDOM.nextInt(10));
			}
			token = digitos.toString();
		} while (reclamacaoRepository.existsByTokenAcesso(token));
		return token;
	}
}
