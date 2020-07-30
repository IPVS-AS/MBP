package org.citopt.connde.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Utility class for {@link Collection}s.
 * 
 * @author Jakob Benz
 */
public class C {
	
	// Not to be instantiated
	private C() {}
	
	/**
	 * Indicates whether a collection is {@code null}
	 * or empty.
	 * 
	 * @param list the {@link Collection} to check.
	 * @return {@code true} if the given collection is
	 * 		   {@code null} or empty, {@code false} otherwise.
	 */
	public static final boolean nullOrEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}
	
	/**
	 * Indicates whether a collection is <b>not</b> {@code null}.
	 * 
	 * @param list the {@link Collection} to check.
	 * @return {@code true} if the given collection is <b>not</b>
	 * 		   {@code null}, {@code false} otherwise.
	 */
	public static final boolean notNull(Collection<?> list) {
		return list != null;
	}
	
	/**
	 * Indicates whether a collection is 
	 * <b>not</b> {@code null} and <b>not</b> empty.
	 * 
	 * @param list the {@link Collection} to check.
	 * @return {@code true} if and only if the given collection is
	 * 		   <b>not</b> {@code null} and <b>not</b> empty,
	 * 		   {@code false} otherwise.
	 */
	public static final boolean notEmpty(Collection<?> list) {
		return list != null && !list.isEmpty();
	}
	
	/**
	 * Indicates whether a given list of values
	 * contains a particular value.
	 * 
	 * @param value the value to check.
	 * @param values the values to check.
	 * @return {@code true} if and only if the given list
	 * 		   of values contains the given value;
	 * 		   {@code false} otherwise.
	 */
	@SafeVarargs
	public static final <T> boolean contains(T value, T... values) {
		for (T t : values) {
			if (t.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Initializes a given list if it is {@code null}.
	 * 
	 * @param list the {@link List}.
	 * @return a new (empty) {@link ArrayList} if the given list
	 * 		   is {@code null}; the (unmodified) list otherwise.
	 */
	public static final <T> List<T> initIfNull(List<T> list) {
		return list == null ? new ArrayList<T>() : list;
	}
	
	/**
	 * Searches for an element in a list based on a given predicate.
	 * 
	 * @param <T>
	 * @param list the list to search in.
	 * @param filterPredicate the predicate to identify the searched element.
	 * @return the searched element wrapped in an {@link Optional} if it exists;
	 * 		   an empty {@link Optional} otherwise.
	 */
	public static final <T> Optional<T> find(List<T> list, Predicate<T> filterPredicate) {
		return list.stream().filter(filterPredicate).findFirst();
	}
	
	/**
	 * Creates a set of some given objects.
	 * 
	 * @param items the objects.
	 * @return an {@link HashSet}.
	 */
	@SafeVarargs
	public static final <T> Set<T> setOf(T... items) {
		return new HashSet<T>(listOf(items));
	}
	
	/**
	 * Creates a list of some given objects.
	 * 
	 * @param items the objects.
	 * @return an {@link ArrayList}.
	 */
	@SafeVarargs
	public static final <T> List<T> listOf(T... items) {
		return new ArrayList<T>(Arrays.asList(items));
	}
	
}
