package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.entity_type.EntityType;
import org.springframework.data.rest.core.config.Projection;

/**
 * Super list excerpt for entity types.
 */
@Projection(name = "list", types = {EntityType.class})
public interface EntityTypeExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();
}
