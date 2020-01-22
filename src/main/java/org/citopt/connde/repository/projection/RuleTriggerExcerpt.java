package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

/**
 * Excerpt projection for rule trigger entities.
 */
@Projection(name = "list", types = RuleTrigger.class)
public interface RuleTriggerExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();

    String getDescription();

    String getQuery();
}
