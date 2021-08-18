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

public class SensorRunOperatorExpectingDataTest extends BaseIoTTest {

    @Test
    @RequiresMQTT
    void sensorDeployAndRunScriptExpectData() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        Device deviceObj = this.createNewDevice(device, sessionCookie, "connect-mockdevice");

        // Create Operator
        Operator opResponse = createOperator(
                sessionCookie,
                "TestOperator",
                "",
                this.getRoutineFromClasspath("mbp_client.py", "text/plain", "scripts/mbp_client/mbp_client.py"),
                this.getRoutineFromClasspath("docker_dummy.py", "text/plain", "scripts/test_sensor/docker_dummy.py"),
                this.getRoutineFromClasspath("entry-file-name", "text/plain", "scripts/test_sensor/entry-file-name"),
                this.getRoutineFromClasspath("start.sh", "application/x-shellscript", "scripts/mbp_client/start.sh"),
                this.getRoutineFromClasspath("stop.sh", "application/x-shellscript", "scripts/mbp_client/stop.sh")
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
        // Thread.sleep(3600_000);
    }
}
