package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.domain.rules.RuleActionType;
import org.springframework.data.rest.core.config.Projection;

/**
 * Excerpt projection for rule action entities.
 */
@Projection(name = "list", types = RuleAction.class)
public interface RuleActionExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();

    RuleActionType getType();
}
