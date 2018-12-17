package org.citopt.connde.repository;

import org.citopt.connde.domain.user.Authority;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Authority entity.
 * @author Imeri Amil
 */
public interface AuthorityRepository extends MongoRepository<Authority, String> {
}
