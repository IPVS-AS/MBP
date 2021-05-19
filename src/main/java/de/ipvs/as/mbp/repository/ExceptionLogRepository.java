package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.logs.ExceptionLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for storing exception logs.
 */
@Repository
public interface ExceptionLogRepository extends MongoRepository<ExceptionLog, String> {

}
