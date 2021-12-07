package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.operator.Operator;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import de.ipvs.as.mbp.repository.projection.OperatorExcerpt;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

public interface OperatorRepository extends UserEntityRepository<Operator> {

    Optional<Operator> findFirstByName(@Param("name") String name);

    boolean existsByNameAndDefaultEntity(String name, boolean defaultEntity);

    List<OperatorExcerpt> findAllByDataModelId(@Param("dataModel.id") String dataModelId);
}
