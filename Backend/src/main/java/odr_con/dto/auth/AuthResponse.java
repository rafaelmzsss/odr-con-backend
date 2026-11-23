package odr_con.dto.auth;

public record AuthResponse(String accessToken, UsuarioAuth user) {
	public record UsuarioAuth(String role, String fullName, String email) {
	}
}
