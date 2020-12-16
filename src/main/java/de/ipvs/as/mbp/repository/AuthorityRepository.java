package de.ipvs.as.mbp.repository;

import java.util.Optional;

import de.ipvs.as.mbp.domain.user.Authority;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data MongoDB repository for the Authority entity.
 * @author Imeri Amil
 */
public interface AuthorityRepository extends MongoRepository<Authority, String> {
	
	public Optional<Authority> findByName(@Param("name") String name);
}
