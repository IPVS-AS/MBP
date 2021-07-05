package de.ipvs.as.mbp.service.discovery.processing.steps;

import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionRanking;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

import java.util.function.Supplier;

/**
 * Collectors of this class collect several {@link DeviceDescription}s and deposits them in a common
 * {@link DeviceDescriptionRanking}, where they are scored and ranked with respect to a certain {@link DeviceTemplate}.
 */
public class DeviceDescriptionRankingCollector implements Supplier<DeviceDescriptionRanking> {

    //Device template to use for scoring the device descriptions
    private DeviceTemplate deviceTemplate;

    /**
     * Creates a new collector that deposits device descriptions in a common {@link DeviceDescriptionRanking}, where
     * they are scored and ranked with respect to given {@link DeviceTemplate}.
     *
     * @param deviceTemplate The device template to use for scoring the device descriptions
     */
    public DeviceDescriptionRankingCollector(DeviceTemplate deviceTemplate) {
        //Set device template
        setDeviceTemplate(deviceTemplate);
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public DeviceDescriptionRanking get() {
        //Create new ranking from the provided device template
        return new DeviceDescriptionRanking(this.deviceTemplate);
    }

    /**
     * Returns the device template that is supposed to be used for scoring the device descriptions.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return deviceTemplate;
    }

    /**
     * Sets the device template that is supposed to be used for scoring the device descriptions.
     *
     * @param deviceTemplate The device template to set
     */
    public void setDeviceTemplate(DeviceTemplate deviceTemplate) {
        //Sanity check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Set device template
        this.deviceTemplate = deviceTemplate;
    }
}
