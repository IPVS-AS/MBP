package de.ipvs.as.mbp;

import org.junit.jupiter.api.Test;

import static de.ipvs.as.mbp.util.TestEnvironmentUtils.isCI;
import static de.ipvs.as.mbp.util.TestEnvironmentUtils.isDockerAvailable;
import static de.ipvs.as.mbp.util.TestEnvironmentUtils.isMQTTAvailable;

/**
 * This Test checks if the tests are executed in a CI environment If so the tests will fail if Docker or Mosquitto are
 * not available
 * <p>
 * Othewise these tests always pass.
 * <p>
 * Checking, whether or not applications are executed in a CI environment is done by checking the TEST_ENV environment
 * variable, if it matches 'ci' the tests will fail. If the variable is not set or the value mismatches the tests will passs.
 */
public class ServiceAvailabilityTest {

    @Test
    void dockerAvailable() {
        if(isCI() && !isDockerAvailable()) {
            System.err.println("Docker Daemon is not Available!");
            throw new RuntimeException("Docker must be available in a CI environment. But it is not.");
        } else if (!isCI()) {
            System.err.println("Not Checking Docker Availability. Running in a non-CI environment");
            return;
        }
        System.err.println("Docker Daemon is Available!");
    }

    @Test
    void mosquittoAvailable() {
        if(isCI() && !isMQTTAvailable()) {
            System.err.println("Mosquitto is not Available!");
            throw new RuntimeException("Mosquitto must be available in a CI environment. But it is not.");
        } else if (!isCI()) {
            System.err.println("Not Checking Mosquitto Availability. Running in a non-CI environment");
            return;
        }
        System.err.println("Mosquitto is Available!");
    }
}
