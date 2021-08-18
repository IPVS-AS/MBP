package de.ipvs.as.mbp.iottest;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.base.BaseIoTTest;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.util.CommandOutput;
import de.ipvs.as.mbp.util.testexecution.RequiresMQTT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SensorRunScriptTest extends BaseIoTTest {

    @Test
    @RequiresMQTT
    void sensorRunStartScript() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        Device deviceObj = this.createNewDevice(device, sessionCookie, "connect-mockdevice");

        // Create Operator
        Operator opResponse = createOperator(
                sessionCookie,
                "TestOperator",
                "",
                new OperatorRoutine("start.sh", testScript)
        );

        // Create sensor
        Sensor sensorResponse = createSensor(
                sessionCookie,
                "TestSensor",
                "Temperature",
                deviceObj.getId(),
                opResponse.getId()
        );

        deploySensor(sessionCookie, sensorResponse.getId());
        startSensor(sessionCookie, sensorResponse.getId());

        CommandOutput commandOutput = device.runCommand("cat /home/mbp/calllog.log");
        assertThat(commandOutput.getStderr()).isEmpty();
        String stdoutString = commandOutput.getStdout();
        assertThat(stdoutString).contains("Test Script was called");
        System.out.println(stdoutString);
    }

}
