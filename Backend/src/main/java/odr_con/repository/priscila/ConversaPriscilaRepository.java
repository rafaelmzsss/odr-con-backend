package odr_con.repository.priscila;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import odr_con.entity.priscila.ConversaPriscila;

public interface ConversaPriscilaRepository extends JpaRepository<ConversaPriscila, String> {

	Optional<ConversaPriscila> findByIdAndUsuarioId(String id, Long usuarioId);

	Optional<ConversaPriscila> findByIdAndUsuarioIdIsNull(String id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from ConversaPriscila c where c.id = :id and c.usuarioId = :usuarioId")
	Optional<ConversaPriscila> findByIdAndUsuarioIdForUpdate(
			@Param("id") String id, @Param("usuarioId") Long usuarioId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from ConversaPriscila c where c.id = :id and c.usuarioId is null")
	Optional<ConversaPriscila> findAnonymousByIdForUpdate(@Param("id") String id);
}
