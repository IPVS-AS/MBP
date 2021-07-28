package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.operator.Operator;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import de.ipvs.as.mbp.repository.projection.OperatorExcerpt;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

//@RepositoryRestResource(collectionResourceRel = "adapters", path = "adapters", excerptProjection = AdapterExcerpt.class)
//@Api(tags = {"Adapter entities"}, description = "CRUD for adapter entities")
public interface OperatorRepository extends UserEntityRepository<Operator> {

    Optional<Operator> findFirstByName(@Param("name") String name);

    boolean existsByNameAndDefaultEntity(String name, boolean defaultEntity);

    @RestResource(exported = false)
    List<OperatorExcerpt> findAllByDataModelId(@Param("dataModel.id") String dataModelId);

}
