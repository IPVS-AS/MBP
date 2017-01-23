package org.citopt.sensmonqtt.repository;

import java.util.List;
import org.citopt.sensmonqtt.domain.valueLog.ValueLog;
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
    
    public List<ValueLog> findAllByIdrefOrderByDateDesc(@Param("idref") String idref);
    
}
