package org.citopt.connde.util;

/**
 * Utility class for {@code String}s.
 * 
 * @author Jakob Benz
 */
public class S {
	
	/**
	 * Indicates whether a {@code String} is {@code null}
	 * or empty.
	 * 
	 * @param s the {@code String} to check.
	 * @return {@code true} if the given {@code String} is
	 * 		   {@code null} or empty, {@code false} otherwise.
	 */
	public static final boolean nullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
	/**
	 * Indicates whether a {@code String} is <b>not</b> {@code null}.
	 * 
	 * @param s the {@code String} to check.
	 * @return {@code true} if the given {@code String} is <b>not</b>
	 * 		   {@code null}, {@code false} otherwise.
	 */
	public static final boolean notNull(String s) {
		return s != null;
	}
	
	/**
	 * Indicates whether a {@code String} is 
	 * <b>not</b> {@code null} and <b>not</b> empty.
	 * 
	 * @param s the {@code String} to check.
	 * @return {@code true} if and only if the given {@code String} is
	 * 		   <b>not</b> {@code null} and <b>not</b> empty,
	 * 		   {@code false} otherwise.
	 */
	public static final boolean notEmpty(String s) {
		return s != null && !s.isEmpty();
	}
	
	/**
	 * Capitalizes the given string (i.e. every word in it
	 * if it contains more than one chunk separated by a space).
	 * Also removes multiple white spaces.
	 * 
	 * @return the capizalized {@code String}, e.g. TEST -> Test.
	 */
	public static String capitalize(String string) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String word : string.trim().split(" ")) {
			if (word.length() == 1)
				stringBuilder.append(Character.toUpperCase(word.charAt(0)));
			else if (word.length() > 1)
				stringBuilder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
			stringBuilder.append(" ");
		}
		return stringBuilder.toString().substring(0, string.trim().length());
	}

}
