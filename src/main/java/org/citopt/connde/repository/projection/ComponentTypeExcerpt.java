package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.entity_type.ComponentType;
import org.springframework.data.rest.core.config.Projection;

/**
 * Excerpt projection interface for component types that is e.g. suitable for lists.
 *
 * @author Jan
 */
@Projection(name = "list", types = ComponentType.class)
public interface ComponentTypeExcerpt {
    String getId();

    String getName();

    String getComponent();
}