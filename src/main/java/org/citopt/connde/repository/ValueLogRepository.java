package org.citopt.connde.repository;

import org.citopt.connde.domain.valueLog.ValueLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author rafaelkperes
 */
@RepositoryRestResource
public interface ValueLogRepository
        extends MongoRepository<ValueLog, String> {
    
    public Page<ValueLog> findAllByIdref(@Param("idref") String idref, Pageable pageable);
    
    public Page<ValueLog> findAllByComponent(@Param("component") String component, Pageable pageable);
    
}
