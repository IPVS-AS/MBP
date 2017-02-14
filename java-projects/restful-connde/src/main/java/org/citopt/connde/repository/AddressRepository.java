package org.citopt.connde.repository;

import java.util.List;
import org.citopt.connde.address.Address;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author rafaelkperes
 */
@RepositoryRestResource
public interface AddressRepository
        extends MongoRepository<Address, String> {
    
    public Address findByMac(@Param("mac") String mac);
    
    public List<Address> findAllByMac(@Param("mac") String mac, Pageable pageable);
    
}
