package de.ipvs.as.mbp.util;

import org.testcontainers.junit.jupiter.Container;

public class BaseDeviceTest extends BaseIntegrationTest {
    @Container
    public static IoTDeviceContainer device = new IoTDeviceContainer();
}
