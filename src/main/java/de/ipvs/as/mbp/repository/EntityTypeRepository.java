package de.ipvs.as.mbp.repository;

import java.util.Optional;

import de.ipvs.as.mbp.domain.entity_type.EntityType;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Super interface for repositories of entity types.
 *
 * @param <T> The entity type for which the repository is used for
 */
@NoRepositoryBean
@ApiIgnore("Not an actual repository, only used as super interface")
public interface EntityTypeRepository<T extends EntityType> extends UserEntityRepository<T> {
    @RestResource(exported = false)
    Optional<T> findByName(@Param("name") String name);
}