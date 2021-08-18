package de.ipvs.as.mbp.iottest;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.base.BaseIoTTest;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.util.CommandOutput;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SensorUploadOperatorTest extends BaseIoTTest {

    @Test
    void sensorUploadOperator() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        Device deviceObj = this.createNewDevice(device, sessionCookie, "connect-mockdevice");

        // Create Operator
        Operator opResponse = createOperator(
                sessionCookie,
                "TestOperator",
                "",
                new OperatorRoutine("test.sh", testScript)
        );
        assertThat(opResponse.getId()).isNotNull();

        // Create sensor
        Sensor sensorResponse = createSensor(
                sessionCookie,
                "TestSensor",
                "Temperature",
                deviceObj.getId(),
                opResponse.getId()
        );

        // Ensure Sensor is Ready
        ensureSensorIsReady(sessionCookie, sensorResponse.getId());

        CommandOutput commandOutput = device.runCommand("sudo cat /var/log/auth.log | grep 'COMMAND=/usr/bin/'");
        String stdoutString = commandOutput.getStdout();
        System.out.println(stdoutString);

        assertThat(stdoutString).contains("USER=root ; COMMAND=/usr/bin/[ -d /home/mbp/scripts/mbp");

        deploySensor(sessionCookie, sensorResponse.getId());

        // Check if deployment files exist
        commandOutput = device.runCommand("ls /home/mbp/scripts/mbp" + sensorResponse.getId());
        stdoutString = commandOutput.getStdout();
        assertThat(stdoutString.split("\n").length).isEqualTo(2);
        assertThat(stdoutString).contains("test.sh");
        assertThat(stdoutString).contains("mbp.properties");

        // Check if the script has been transferred properly
        commandOutput = device.runCommand(String.format("cat /home/mbp/scripts/mbp%s/test.sh", sensorResponse.getId()));
        stdoutString = commandOutput.getStdout();
        System.out.println(stdoutString);
        assertThat(stdoutString).isEqualTo(testScript);
    }
}
