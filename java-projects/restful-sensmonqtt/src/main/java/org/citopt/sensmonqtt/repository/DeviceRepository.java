package org.citopt.sensmonqtt.repository;

import org.citopt.sensmonqtt.domain.device.Device;
import org.citopt.sensmonqtt.repository.projection.DeviceListProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author rafaelkperes
 */
@RepositoryRestResource(excerptProjection = DeviceListProjection.class)
public interface DeviceRepository
        extends MongoRepository<Device, String> {
    
    public Device findByName(@Param("name") String name);
    
    public Device findByMacAddress(@Param("macAddress") String name);

}
