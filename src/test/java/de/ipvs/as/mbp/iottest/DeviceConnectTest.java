package de.ipvs.as.mbp.iottest;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.base.BaseIoTTest;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.util.CommandOutput;
import de.ipvs.as.mbp.util.IoTDeviceContainer;
import de.ipvs.as.mbp.util.testexecution.RequiresMQTT;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DeviceConnectTest extends BaseIoTTest {

    @Test
    void deviceConnect() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        Device deviceObj = this.createNewDevice(device, sessionCookie, "connect-mockdevice");

        ensureDeviceHasSSH(sessionCookie, deviceObj.getId());

        CommandOutput commandOutput = device.runCommand("sudo cat /var/log/auth.log");
        String stdoutString = commandOutput.getStdout();

        System.out.println(stdoutString);

        // Check that the testing command has been called in the proper context
        assertThat(stdoutString).contains("mbp : PWD=/home/mbp ; USER=root ; COMMAND=/usr/bin/test 5 -gt 2");
    }

}
