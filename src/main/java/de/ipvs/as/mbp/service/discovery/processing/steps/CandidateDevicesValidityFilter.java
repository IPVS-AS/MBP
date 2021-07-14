package de.ipvs.as.mbp.service.discovery.processing.steps;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;

import java.util.function.Predicate;

/**
 * Filters out and discards invalid candidate devices, given as {@link DeviceDescription}s,
 * such that only the valid ones remain.
 */
public class CandidateDevicesValidityFilter implements Predicate<DeviceDescription> {
    /**
     * Evaluates this predicate on the given argument.
     *
     * @param candidateDevice the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(DeviceDescription candidateDevice) {
        //Null check
        if (candidateDevice == null) {
            return false;
        }

        //Everything fine
        return true;
    }
}
