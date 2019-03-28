package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.adapter.Adapter;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

/**
 *
 * @author rafaelkperes
 */
@Projection(name = "list", types = Adapter.class)
public interface AdapterListProjection {

    String getId();

    String getName();

    String getDescription();

    String getUnit();

    List getParameters();
}
