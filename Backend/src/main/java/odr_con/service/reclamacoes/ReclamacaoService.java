package odr_con.service.reclamacoes;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import odr_con.dto.reclamacoes.AtualizarReclamacaoRequest;
import odr_con.dto.reclamacoes.CadastroReclamacaoRequest;
import odr_con.dto.reclamacoes.ReclamacaoResponse;
import odr_con.entity.reclamacoes.Reclamacao;
import odr_con.exception.reclamacoes.ReclamacaoNaoEncontradaException;
import odr_con.exception.reclamacoes.UsuarioDaReclamacaoNaoEncontradoException;
import odr_con.repository.reclamacoes.ReclamacaoRepository;
import odr_con.repository.usuario.UsuarioRepository;

@Service
public class ReclamacaoService {
	private final ReclamacaoRepository reclamacaoRepository;
	private final UsuarioRepository usuarioRepository;

	public ReclamacaoService(ReclamacaoRepository reclamacaoRepository, UsuarioRepository usuarioRepository) {
		this.reclamacaoRepository = reclamacaoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public ReclamacaoResponse cadastrar(CadastroReclamacaoRequest request) {
		validarUsuario(request.usuarioId());
		Reclamacao reclamacao = Reclamacao.builder()
				.usuarioId(request.usuarioId())
				.tipoRegistro(request.tipoRegistro().trim())
				.textoOriginal(request.textoOriginal())
				.status("PENDENTE")
				.build();
		return ReclamacaoResponse.from(reclamacaoRepository.save(reclamacao));
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
}
