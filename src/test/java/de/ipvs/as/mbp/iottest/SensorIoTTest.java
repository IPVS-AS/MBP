package de.ipvs.as.mbp.iottest;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.base.BaseIoTTest;
import de.ipvs.as.mbp.util.CommandOutput;
import de.ipvs.as.mbp.util.IoTDeviceContainer;
import de.ipvs.as.mbp.util.RequiresMQTT;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class SensorIoTTest extends BaseIoTTest {

    private final static String testScript = "#!/bin/bash\n" +
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
    @RequiresMQTT
    void deviceRunStartScript() throws Exception {
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

    @Test
    @RequiresMQTT
    void deviceRunScriptExpectData() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        Device deviceObj = this.createNewDevice(device, sessionCookie, "connect-mockdevice");

        // Create Operator
        Operator opResponse = createOperator(
                sessionCookie,
                "TestOperator",
                "",
                this.getRoutineFromClasspath("mbp_client.py","text/plain","scripts/mbp_client/mbp_client.py"),
                this.getRoutineFromClasspath("docker_dummy.py","text/plain","scripts/test_sensor/docker_dummy.py"),
                this.getRoutineFromClasspath("entry-file-name","text/plain","scripts/test_sensor/entry-file-name"),
                this.getRoutineFromClasspath("start.sh","application/x-shellscript","scripts/mbp_client/start.sh"),
                this.getRoutineFromClasspath("stop.sh","application/x-shellscript","scripts/mbp_client/stop.sh")
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

        // Assert that Tmux Server is running with a session
        CommandOutput commandOutput = device.runCommand("ps aux");
        String stdoutString = commandOutput.getStdout();
        System.out.println("Process List (ps aux):");
        System.out.println(stdoutString);
        System.out.println();
        assertThat(stdoutString).contains("tmux new-session");

        commandOutput = device.runCommand("sudo tmux list-sessions");
        stdoutString = commandOutput.getStdout();
        System.out.println("tmux session list");
        System.out.println(stdoutString);
        assertThat(stdoutString.split("\n").length).isEqualTo(1);
        assertThat(stdoutString).contains("scriptSession");

        // Wait some time for data to be sent
        System.out.println("Waiting for data to be Produced");
        Thread.sleep(15000);

        // Validate that data has been collected
        Thread.sleep(3600_000);
    }
}
