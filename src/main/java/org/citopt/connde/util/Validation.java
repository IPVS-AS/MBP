package org.citopt.connde.util;

import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * This class provides methods for validating input data.
 *
 * Created by Jan on 10.12.2018.
 */
public class Validation {
    //Regular expression for valid formatted MAC addresses (including separators)
    private static final String REGEX_MAC_ADDRESS = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    //Regular expression for valid unformatted MAC addresses (without separators)
    private static final String REGEX_UNFORMATTED_MAC_ADDRESS = "^[0-9A-Fa-f]{12}$";
    //Regular expression for valid private RSA key strings
    private static final String REGEX_PRIVATE_RSA_KEY = "^-----BEGIN RSA PRIVATE KEY-----(?:[\\n\\r]{1,2}.{64})+[\\n\\r]{1,2}.{1,64}[\\n\\r]{1,2}-----END RSA PRIVATE KEY-----$";

    /**
     * Checks if a provided formatted MAC address (including separators) is valid.
     *
     * @param macAddress The MAC address to check
     * @return True, if the MAC address is valid; false otherwise
     */
    public static boolean isValidMACAddress(String macAddress) {
        //Check for null
        if (macAddress == null) {
            return false;
        }
        //Test with regular expression
        return macAddress.matches(REGEX_MAC_ADDRESS);
    }

    /**
     * Checks if a provided unformatted MAC address (without separators) is valid.
     *
     * @param macAddress The MAC address to check
     * @return True, if the MAC address is valid; false otherwise
     */
    public static boolean isValidUnformattedMACAddress(String macAddress) {
        //Check for null
        if (macAddress == null) {
            return false;
        }
        //Test with regular expression
        return macAddress.matches(REGEX_UNFORMATTED_MAC_ADDRESS);
    }

    /**
     * Checks if a provided IP address is valid, either for the IPV4 format or the IPV6 format.
     *
     * @param ipAddress The IP address to check
     * @return True, if the IP address is valid; false otherwise
     */
    public static boolean isValidIPAddress(String ipAddress) {
        //Check for null
        if (ipAddress == null) {
            return false;
        }

        //Create new apache validator
        InetAddressValidator validator = new InetAddressValidator();

        //Test for both IPV4 and IPV6
        return (validator.isValidInet4Address(ipAddress)) || (validator.isValidInet6Address(ipAddress));
    }

    /**
     * Checks if a provided private RSA key string is of a valid format.
     *
     * @param rsaKey The key string to check
     * @return True, if the private RSA key is of a valid format; false otherwise
     */
    public static boolean isValidPrivateRSAKey(String rsaKey) {
        //Check for null
        if (rsaKey == null) {
            return false;
        }

        //Test with regular expression
        return rsaKey.matches(REGEX_PRIVATE_RSA_KEY);
    }
}
