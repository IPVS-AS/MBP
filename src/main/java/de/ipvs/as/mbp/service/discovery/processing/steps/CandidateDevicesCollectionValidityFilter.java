package de.ipvs.as.mbp.service.discovery.processing.steps;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.util.Validation;

import java.util.function.Predicate;

/**
 * Filters out and discards invalid {@link CandidateDevicesCollection}s, such that only the valid ones remain.
 */
public class CandidateDevicesCollectionValidityFilter implements Predicate<CandidateDevicesCollection> {

    /**
     * Creates a new validity filter for {@link CandidateDevicesCollection}s.
     */
    public CandidateDevicesCollectionValidityFilter() {

    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param candidateDevicesCollection the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(CandidateDevicesCollection candidateDevicesCollection) {
        //Null check
        if (candidateDevicesCollection == null) {
            return false;
        }

        //Check if repository name is available and possibly valid
        return !Validation.isNullOrEmpty(candidateDevicesCollection.getRepositoryName());
    }
}
