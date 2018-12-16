package org.citopt.connde.web.rest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * Utility class for HTTP headers creation.
 * @author Imeri Amil
 */
public final class HeaderUtil {

    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);

    private HeaderUtil() {
    }

    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-MBP-alert", message);
        headers.add("X-MBP-params", param);
        return headers;
    }

    public static HttpHeaders createFailureAlert(String message, String param) {
        log.error("Entity creation failed, {}", message);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-MBP-error", message);
        headers.add("X-MBP-params", param);
        return headers;
    }
}
