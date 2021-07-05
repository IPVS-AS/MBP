package de.ipvs.as.mbp.service.discovery.processing.steps;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;

import java.util.function.Predicate;

/**
 * Filters out and discards invalid {@link DeviceDescription}s, such that only the valid ones remain.
 */
public class DeviceDescriptionValidityFilter implements Predicate<DeviceDescription> {
    /**
     * Evaluates this predicate on the given argument.
     *
     * @param deviceDescription the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(DeviceDescription deviceDescription) {
        //Null check
        if (deviceDescription == null) {
            return false;
        }

        //Everything fine
        return true;
    }
}
