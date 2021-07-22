package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

//@RepositoryRestResource(collectionResourceRel = "adapters", path = "adapters", excerptProjection = AdapterExcerpt.class)
//@Api(tags = {"Adapter entities"}, description = "CRUD for adapter entities")
public interface OperatorRepository extends UserEntityRepository<Operator> {


    Optional<Operator> findFirstByName(@Param("name") String name);
    boolean existsByNameAndDefaultEntity(String name, boolean defaultEntity);

}
