package de.ipvs.as.mbp.service.discovery.processing;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 * Objects of this class can be used to calculate scores for {@link DeviceDescription}s, based on the scoring criteria
 * that are part of a given {@link DeviceTemplate} and relative to a collection of related {@link DeviceDescription}s.
 * Each device scorer can be used as {@link Comparator}s for sorting lists and other collections of
 * {@link DeviceDescription}s. The resulting scores are guaranteed to be greater than or equal to zero.
 */
public class DeviceDescriptionScorer implements Comparator<DeviceDescription> {
    //Device template to use for calculating the scores
    private DeviceTemplate deviceTemplate;

    //Set of related device descriptions for calculating relative scores
    private Collection<DeviceDescription> relatedDeviceDescriptions;

    /**
     * Creates a new device scorer from a given {@link DeviceTemplate} and a collection of {@link DeviceDescription}s
     * to which the calculated scores are relative.
     *
     * @param deviceTemplate            The device template to use for calculating the scores
     * @param relatedDeviceDescriptions The collection of device descriptions to use
     */
    public DeviceDescriptionScorer(DeviceTemplate deviceTemplate, Collection<DeviceDescription> relatedDeviceDescriptions) {
        //Set fields
        setDeviceTemplate(deviceTemplate);
        setRelatedDeviceDescriptions(relatedDeviceDescriptions);
    }

    /**
     * Returns the device template that is used for calculating the scores of device descriptions.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return deviceTemplate;
    }

    /**
     * Sets the device template that is supposed to be used for calculating the scores of device descriptions.
     *
     * @param deviceTemplate The device template to set
     */
    private void setDeviceTemplate(DeviceTemplate deviceTemplate) {
        //Null check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        this.deviceTemplate = deviceTemplate;
    }

    /**
     * Returns the collection of {@link DeviceDescription} to which the calculated scores are relative.
     *
     * @return The collection of device descriptions
     */
    public Collection<DeviceDescription> getRelatedDeviceDescriptions() {
        return this.relatedDeviceDescriptions;
    }

    /**
     * Sets the collection of {@link DeviceDescription} to which the calculated scores are relative.
     *
     * @param relatedDeviceDescriptions The collection of device descriptions to set
     */
    private void setRelatedDeviceDescriptions(Collection<DeviceDescription> relatedDeviceDescriptions) {
        //Null checks
        if ((relatedDeviceDescriptions == null) || (relatedDeviceDescriptions.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The device descriptions must not be null.");
        }

        this.relatedDeviceDescriptions = relatedDeviceDescriptions;
    }

    /**
     * Calculates and returns the score for a given {@link DeviceDescription} with respect to the current
     * {@link DeviceTemplate} that contains the scoring criteria.
     *
     * @param deviceDescription The device description for which the score is supposed to be calculated
     * @return The resulting score of the device description
     */
    public double score(DeviceDescription deviceDescription) {
        //Sanity check
        if (deviceDescription == null) {
            throw new IllegalArgumentException("The device description must not be null.");
        }

        //Stream trough the scoring criteria of the device template and sum the scores
        return this.deviceTemplate.getScoringCriteria().stream()
                .filter(Objects::nonNull) //Eliminate null criteria
                .mapToDouble(c -> c.getScoreIncrement(deviceDescription, this)) //Apply criteria and retrieve their score
                .sum(); //Sum all score increments/decrements
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     * <p>
     * The implementor must ensure that {@code sgn(compare(x, y)) ==
     * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
     * implies that {@code compare(x, y)} must throw an exception if and only
     * if {@code compare(y, x)} throws an exception.)<p>
     * <p>
     * The implementor must also ensure that the relation is transitive:
     * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
     * {@code compare(x, z)>0}.<p>
     * <p>
     * Finally, the implementor must ensure that {@code compare(x, y)==0}
     * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
     * {@code z}.<p>
     * <p>
     * It is generally the case, but <i>not</i> strictly required that
     * {@code (compare(x, y)==0) == (x.equals(y))}.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."<p>
     * <p>
     * In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param d1 the first object to be compared.
     * @param d2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(DeviceDescription d1, DeviceDescription d2) {
        //Null checks
        if ((d1 == null) && (d2 == null)) {
            return 0;
        } else if (d1 == null) {
            return -1;
        } else if (d2 == null) {
            return 1;
        }

        //Calculate the scores of both device descriptions
        double d1Score = score(d1);
        double d2Score = score(d2);

        //Compare the scores
        if (d1Score == d2Score) {
            return 0;
        } else if (d1Score > d2Score) {
            return -1;
        } else {
            return 1;
        }
    }
}
