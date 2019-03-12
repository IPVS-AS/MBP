package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.componentType.ComponentType;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection interface for component types.
 *
 * @author Jan
 */
@Projection(name = "list", types = ComponentType.class)
public interface ComponentTypeProjection {
    String getId();

    String getName();

    String getComponent();
}