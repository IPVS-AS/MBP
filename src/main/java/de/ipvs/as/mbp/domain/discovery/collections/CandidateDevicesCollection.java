package de.ipvs.as.mbp.domain.discovery.collections;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;

import java.util.*;
import java.util.stream.Stream;

/**
 * Objects of this class represent collections of candidate devices, represented by their {@link DeviceDescription}s,
 * that that were received from a certain repository with an unique name.
 */
public class CandidateDevicesCollection  {
    //Name of the repository from which the collection origins
    private String repositoryName;

    //Descriptions of the individual candidate devices
    private List<DeviceDescription> candidateDevices;

    /**
     * Creates a new, empty candidate devices collection.
     */
    public CandidateDevicesCollection() {
        //Initialize collection of candidate devices
        this.candidateDevices = new ArrayList<>();
    }

    /**
     * Creates a new candidate devices collection from a given repository name.
     *
     * @param repositoryName The repository name to use
     */
    public CandidateDevicesCollection(String repositoryName) {
        //Call default constructor
        this();

        //Set fields
        setRepositoryName(repositoryName);
    }

    /**
     * Returns the name of the repository from which the collection of device descriptions were received.
     *
     * @return The repository name
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Sets the name of the repository from which the collection of candidate devices were received.
     *
     * @param repositoryName The name of the repository to set
     * @return The candidate devices collection
     */
    public CandidateDevicesCollection setRepositoryName(String repositoryName) {
        //Sanity check
        if ((repositoryName == null) || (repositoryName.isEmpty())) {
            throw new IllegalArgumentException("Repository name must nut be null or empty.");
        }

        //Set repository name
        this.repositoryName = repositoryName;
        return this;
    }

    /**
     * Returns the descriptions of the candidate devices that were received from the repository.
     *
     * @return The descriptions of the candidate devices
     */
    public List<DeviceDescription> getCandidateDevices() {
        return candidateDevices;
    }

    /**
     * Sets the descriptions of the candidate devices that were received from the repository.
     *
     * @param candidateDevices The descriptions of the candidate devices to set
     * @return The candidate devices collection
     */
    public CandidateDevicesCollection setCandidateDevices(Collection<DeviceDescription> candidateDevices) {
        this.candidateDevices = new ArrayList<>(candidateDevices);
        return this;
    }

    /**
     * Adds multiple candidate devices, given as {@link DeviceDescription}s, to the collection.
     *
     * @param candidateDevices The descriptions of the candidate devicesto add
     * @return The candidate devices collection
     */
    public CandidateDevicesCollection addCandidateDevices(Collection<DeviceDescription> candidateDevices) {
        //Add the device descriptions
        this.candidateDevices.addAll(candidateDevices);
        return this;
    }


    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this set (its cardinality)
     */
    public int size() {
        return this.candidateDevices.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements
     */
    public boolean isEmpty() {
        return this.candidateDevices.isEmpty();
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     * More formally, returns {@code true} if and only if this set
     * contains an element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this set
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              set does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    public boolean contains(Object o) {
        return this.candidateDevices.contains(o);
    }

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     *
     * @return an iterator over the elements in this set
     */
    public Iterator<DeviceDescription> iterator() {
        return this.candidateDevices.iterator();
    }

    /**
     * Adds the specified element to this set if it is not already present
     * (optional operation).  More formally, adds the specified element
     * {@code e} to this set if the set contains no element {@code e2}
     * such that
     * {@code Objects.equals(e, e2)}.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns {@code false}.  In combination with the
     * restriction on constructors, this ensures that sets never contain
     * duplicate elements.
     *
     * <p>The stipulation above does not imply that sets must accept all
     * elements; sets may refuse to add any particular element, including
     * {@code null}, and throw an exception, as described in the
     * specification for {@link Collection#add Collection.add}.
     * Individual set implementations should clearly document any
     * restrictions on the elements that they may contain.
     *
     * @param deviceDescription element to be added to this set
     * @return {@code true} if this set did not already contain the specified
     * element
     * @throws UnsupportedOperationException if the {@code add} operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of the specified element
     *                                       prevents it from being added to this set
     * @throws NullPointerException          if the specified element is null and this
     *                                       set does not permit null elements
     * @throws IllegalArgumentException      if some property of the specified element
     *                                       prevents it from being added to this set
     */
    public boolean add(DeviceDescription deviceDescription) {
        //Null check
        if (deviceDescription == null) {
            throw new IllegalArgumentException("The device description must not be null.");
        }

        return this.candidateDevices.add(deviceDescription);
    }

    /**
     * Removes the specified element from this set if it is present
     * (optional operation).  More formally, removes an element {@code e}
     * such that
     * {@code Objects.equals(o, e)}, if
     * this set contains such an element.  Returns {@code true} if this set
     * contained the element (or equivalently, if this set changed as a
     * result of the call).  (This set will not contain the element once the
     * call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if this set contained the specified element
     * @throws ClassCastException            if the type of the specified element
     *                                       is incompatible with this set
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified element is null and this
     *                                       set does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the {@code remove} operation
     *                                       is not supported by this set
     */
    public boolean remove(Object o) {
        return this.candidateDevices.remove(o);
    }

    /**
     * Returns {@code true} if this set contains all of the elements of the
     * specified collection.  If the specified collection is also a set, this
     * method returns {@code true} if it is a <i>subset</i> of this set.
     *
     * @param c collection to be checked for containment in this set
     * @return {@code true} if this set contains all of the elements of the
     * specified collection
     * @throws ClassCastException   if the types of one or more elements
     *                              in the specified collection are incompatible with this
     *                              set
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     *                              or more null elements and this set does not permit null
     *                              elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>),
     *                              or if the specified collection is null
     * @see #contains(Object)
     */
    public boolean containsAll(Collection<?> c) {
        return this.candidateDevices.containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation).  If the specified
     * collection is also a set, the {@code addAll} operation effectively
     * modifies this set so that its value is the <i>union</i> of the two
     * sets.  The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress.
     *
     * @param c collection containing elements to be added to this set
     * @return {@code true} if this set changed as a result of the call
     * @throws UnsupportedOperationException if the {@code addAll} operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of the
     *                                       specified collection prevents it from being added to this set
     * @throws NullPointerException          if the specified collection contains one
     *                                       or more null elements and this set does not permit null
     *                                       elements, or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this set
     */
    public boolean addAll(Collection<? extends DeviceDescription> c) {
        //Null check
        if ((c == null) || (c.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The device descriptions must not be null.");
        }

        return this.candidateDevices.addAll(c);
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this set all of its elements that are not contained in the
     * specified collection.  If the specified collection is also a set, this
     * operation effectively modifies this set so that its value is the
     * <i>intersection</i> of the two sets.
     *
     * @param c collection containing elements to be retained in this set
     * @return {@code true} if this set changed as a result of the call
     * @throws UnsupportedOperationException if the {@code retainAll} operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of this set
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this set contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     */
    public boolean retainAll(Collection<?> c) {
        return this.candidateDevices.retainAll(c);
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation).  If the specified
     * collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>asymmetric set difference</i> of
     * the two sets.
     *
     * @param c collection containing elements to be removed from this set
     * @return {@code true} if this set changed as a result of the call
     * @throws UnsupportedOperationException if the {@code removeAll} operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of this set
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this set contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        return this.candidateDevices.removeAll(c);
    }

    /**
     * Removes all of the elements from this set (optional operation).
     * The set will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} method
     *                                       is not supported by this set
     */
    public void clear() {
        this.candidateDevices.clear();
    }

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * <p>This method should be overridden when the {@link #spliterator()}
     * method cannot return a spliterator that is {@code IMMUTABLE},
     * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
     * for details.)
     *
     * @return a sequential {@code Stream} over the elements in this collection
     * @implSpec The default implementation creates a sequential {@code Stream} from the
     * collection's {@code Spliterator}.
     * @since 1.8
     */
    public Stream<DeviceDescription> stream() {
        return this.candidateDevices.stream();
    }

    /**
     * Checks and returns whether a given object equals the current one. For this, the repository names
     * of both {@link CandidateDevicesCollection}s are compared.
     *
     * @param o The object to check against the current one
     * @return True, if both objects can be considered equal; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandidateDevicesCollection)) return false;
        CandidateDevicesCollection that = (CandidateDevicesCollection) o;
        return Objects.equals(repositoryName, that.repositoryName);
    }

    /**
     * Calculates a hash code for the {@link CandidateDevicesCollection} from its repository name.
     *
     * @return The resulting hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(repositoryName);
    }
}
