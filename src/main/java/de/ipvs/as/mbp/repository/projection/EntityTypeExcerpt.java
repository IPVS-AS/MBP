package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.entity_type.EntityType;
import org.springframework.data.rest.core.config.Projection;

/**
 * Super list excerpt for entity types.
 */
@Projection(name = "list", types = {EntityType.class})
public interface EntityTypeExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();
}
