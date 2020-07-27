package org.citopt.connde.repository;

import org.citopt.connde.domain.access_control.ACPolicy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for access-control {@link ACPolicy policies}.
 * 
 * @author Jakob Benz
 */
@Repository
public interface ACPolicyRepository extends MongoRepository<ACPolicy<?>, String> {}