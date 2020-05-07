package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.adapter.Adapter;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "list", types = Adapter.class)
public interface AdapterExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();

    String getDescription();

    String getUnit();

    List getParameters();
}
