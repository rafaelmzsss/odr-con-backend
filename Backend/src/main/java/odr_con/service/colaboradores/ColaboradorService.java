package odr_con.service.colaboradores;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import odr_con.dto.colaboradores.CadastroColaboradorRequest;
import odr_con.dto.colaboradores.ColaboradorResponse;
import odr_con.entity.colaboradores.Colaborador;
import odr_con.exception.colaboradores.EmailOuChaveJaCadastradosException;
import odr_con.repository.colaboradores.ColaboradorRepository;

@Service
public class ColaboradorService {
	private final ColaboradorRepository colaboradorRepository;

	public ColaboradorService(ColaboradorRepository colaboradorRepository) {
		this.colaboradorRepository = colaboradorRepository;
	}

	@Transactional
	public ColaboradorResponse cadastrar(CadastroColaboradorRequest request) {
		String email = request.email().trim().toLowerCase();
		String chaveAcesso = request.chaveAcesso().trim();

		if (colaboradorRepository.existsByEmail(email) || colaboradorRepository.existsByChaveAcesso(chaveAcesso)) {
			throw new EmailOuChaveJaCadastradosException();
		}

		Colaborador colaborador = Colaborador.builder()
				.nome(request.nome().trim())
				.email(email)
				.chaveAcesso(chaveAcesso)
				.ativo(true)
				.build();

		return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
	}
}
