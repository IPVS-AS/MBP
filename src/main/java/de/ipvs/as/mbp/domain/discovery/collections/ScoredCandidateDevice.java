package de.ipvs.as.mbp.domain.discovery.collections;


import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;

/**
 * Objects of this class represent candidate devices, given as {@link DeviceDescription}s, that were associated
 * with a certain score value.
 * Technically, this class just extends {@link DeviceDescription} for a field that captures the score value.
 */
public class ScoredCandidateDevice extends DeviceDescription {

    //Score of the candidate device
    private double score;

    /**
     * Creates a new scored candidate device from a given {@link DeviceDescription} and a score value that is
     * associated with the candidate device.
     *
     * @param candidateDevice The {@link DeviceDescription} of the candidate device
     * @param score           The score value that is associated with the candidate device
     */
    protected ScoredCandidateDevice(DeviceDescription candidateDevice, double score) {
        super();

        //Sanity check
        if (candidateDevice == null) {
            throw new IllegalArgumentException("The candidate device must not be null.");
        }

        //Copy fields from the given device description
        this.setName(candidateDevice.getName());
        this.setDescription(candidateDevice.getDescription());
        this.setKeywords(candidateDevice.getKeywords());
        this.setLocation(candidateDevice.getLocation());
        this.setIdentifiers(candidateDevice.getIdentifiers());
        this.setCapabilities(candidateDevice.getCapabilities());
        this.setAttachments(candidateDevice.getAttachments());
        this.setSshDetails(candidateDevice.getSshDetails());
        this.setLastUpdateTimestamp(candidateDevice.getLastUpdateTimestamp());

        //Set score
        setScore(score);
    }

    /**
     * Returns the score that is associated with the candidate device.
     *
     * @return The score
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the score that is associated with the candidate device.
     *
     * @param score The score to set
     * @return The scored candidate device
     */
    protected ScoredCandidateDevice setScore(double score) {
        //Sanity check
        if (score < 0) {
            throw new IllegalArgumentException("The score must be greater than or equal to zero.");
        }

        //Set score
        this.score = score;
        return this;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see java.util.HashMap
     */
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link java.util.HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     *     method, then calling the {@code hashCode} method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link java.lang.Object#equals(java.lang.Object)}
     *     method, then calling the {@code hashCode} method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hash tables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined
     * by class {@code Object} does return distinct integers for
     * distinct objects. (The hashCode may or may not be implemented
     * as some function of an object's memory address at some point
     * in time.)
     *
     * @return a hash code value for this object.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.lang.System#identityHashCode
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
