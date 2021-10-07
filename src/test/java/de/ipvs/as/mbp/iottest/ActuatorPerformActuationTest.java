package de.ipvs.as.mbp.iottest;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.base.BaseIoTTest;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.util.testexecution.RequiresMQTT;
import org.junit.jupiter.api.Test;

@RequiresMQTT
public class ActuatorPerformActuationTest extends BaseIoTTest {

    @Test
    void actuatorShouldPerformActuation() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        Device deviceObj = this.createNewDevice(device, sessionCookie, "performactuation-mockdevice");

    }
}
