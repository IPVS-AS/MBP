package de.ipvs.as.mbp;

import javax.servlet.http.Cookie;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.reactor.Command;
import de.ipvs.as.mbp.domain.component.ComponentDTO;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Code;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.util.BaseDeviceTest;
import de.ipvs.as.mbp.util.CommandOutput;
import de.ipvs.as.mbp.util.IoTDeviceContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BasicDeviceTest extends BaseDeviceTest {

    private static String testScript = "#!/bin/bash\n" +
            "echo  $(date): Test Script was called | tee -a /home/mbp/calllog.log";

    @Container
    public static IoTDeviceContainer device = new IoTDeviceContainer();

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

    @Test
    void deviceUploadOperator() throws Exception {
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

    @Test
    void deviceRunScript() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        Device deviceObj = this.createNewDevice(device, sessionCookie, "connect-mockdevice");

        // Create Operator
        Operator opResponse = createOperator(
                sessionCookie,
                "TestOperator",
                "",
                new OperatorRoutine("test.sh", testScript)
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
        System.out.println(stdoutString);

    }
}
