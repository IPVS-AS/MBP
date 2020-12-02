package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.operator.Operator;

//@RepositoryRestResource(collectionResourceRel = "adapters", path = "adapters", excerptProjection = AdapterExcerpt.class)
//@Api(tags = {"Adapter entities"}, description = "CRUD for adapter entities")
public interface OperatorRepository extends UserEntityRepository<Operator> {

    boolean existsByNameAndDefaultEntity(String name, boolean defaultEntity);

}
