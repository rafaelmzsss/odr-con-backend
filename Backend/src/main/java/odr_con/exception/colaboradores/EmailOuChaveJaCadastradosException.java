package odr_con.exception.colaboradores;

public class EmailOuChaveJaCadastradosException extends RuntimeException {
	public EmailOuChaveJaCadastradosException() {
		super("Já existe um colaborador cadastrado com esse e-mail ou chave de acesso");
	}
}
