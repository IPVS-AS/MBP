package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import org.springframework.data.rest.core.config.Projection;

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
