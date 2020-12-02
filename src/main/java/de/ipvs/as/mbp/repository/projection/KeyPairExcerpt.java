package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "list", types = KeyPair.class)
public interface KeyPairExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();

    String getPublicKey();

    boolean hasPublicKey();

    boolean hasPrivateKey();

}
