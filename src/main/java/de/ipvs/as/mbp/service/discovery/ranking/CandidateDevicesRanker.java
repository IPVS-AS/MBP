package de.ipvs.as.mbp.service.discovery.ranking;

import de.ipvs.as.mbp.domain.discovery.collections.ScoredCandidateDevice;

import java.time.Instant;
import java.util.Comparator;

/**
 * Objects of this class behave as {@link Comparator}s for {@link ScoredCandidateDevice}s and thus enable
 * to create a ranking from a given collection of {@link ScoredCandidateDevice}s.
 */
public class CandidateDevicesRanker implements Comparator<ScoredCandidateDevice> {
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
    public int compare(ScoredCandidateDevice d1, ScoredCandidateDevice d2) {
        //Null check
        if ((d1 == null) && (d2 == null)) {
            return 0;
        } else if (d1 == null) {
            return -1;
        } else if (d2 == null) {
            return 1;
        }

        //Compare the scores
        int compared = Double.compare(d2.getScore(), d1.getScore());

        //Check if score is different
        if (compared != 0) {
            return compared;
        }

        //Score is equal, so compare the timestamps
        Instant t1 = d1.getLastUpdateTimestamp();
        Instant t2 = d2.getLastUpdateTimestamp();

        //Null check
        if ((t1 == null) && (t2 == null)) {
            return 0;
        } else if (t1 == null) {
            return -1;
        } else if (t2 == null) {
            return 1;
        }

        //Compare timestamps such that the more recent one comes first
        return t2.compareTo(t1);
    }
}
