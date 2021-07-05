package de.ipvs.as.mbp.service.discovery.processing.steps;

import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionSet;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

import java.util.function.Predicate;

/**
 * Filters out and discards invalid {@link DeviceDescriptionSet}s, such that only the valid ones remain.
 */
public class DeviceDescriptionSetValidityFilter implements Predicate<DeviceDescriptionSet> {

    //Device template for which the sets were supposed to be retrieved
    private DeviceTemplate deviceTemplate;

    /**
     * Creates a new validity filter for {@link DeviceDescriptionSet}s from a given {@link DeviceTemplate} for which
     * the {@link DeviceDescriptionSet}s were supposed to be retrieved.
     *
     * @param deviceTemplate The device template to use
     */
    public DeviceDescriptionSetValidityFilter(DeviceTemplate deviceTemplate) {
        //Set device template
        setDeviceTemplate(deviceTemplate);
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param deviceDescriptionSet the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(DeviceDescriptionSet deviceDescriptionSet) {
        //Null check
        if (deviceDescriptionSet == null) {
            return false;
        }

        //Check whether the referenced device template matches the one of this object
        if (!deviceDescriptionSet.getDeviceTemplateId().equals(this.deviceTemplate.getId())) {
            return false;
        }

        //Everything fine
        return true;
    }

    /**
     * Sets the device template for which the {@link DeviceDescriptionSet}s were supposed to be retrieved.
     *
     * @param deviceTemplate The device template to set
     */
    private void setDeviceTemplate(DeviceTemplate deviceTemplate) {
        //Null check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Set device template
        this.deviceTemplate = deviceTemplate;
    }
}
