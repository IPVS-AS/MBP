package org.citopt.connde.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This util class provides a collection of different crypto-related methods that may be useful in different contexts.
 */
public class CryptoUtils {
    /**
     * Generates a MD5 hash from a given input string.
     *
     * @param inputString The input string to hash
     * @return The generated MD5 hash string
     */
    public static String md5(String inputString) {
        return md5(inputString.getBytes());
    }

    /**
     * Generates a MD5 hash from given input data..
     *
     * @param inputData The input data to hash
     * @return The generated MD5 hash string
     */
    public static String md5(byte[] inputData) {
        try {
            //Generate message digest instance for MD5
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            //Prepare hashing
            messageDigest.update(inputData, 0, inputData.length);

            //Hash and store result as big integer
            BigInteger bigInt = new BigInteger(1, messageDigest.digest());

            //Return result as string
            return String.format("%1$032X", bigInt).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
