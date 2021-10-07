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

@RequiresMQTT
public class SensorRunScriptTest extends BaseIoTTest {

    @Test
    void sensorRunStartScript() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        printStageMessage("Creating Device");
        Device deviceObj = this.createNewDevice(device, sessionCookie, "startscript-mockdevice");

        // Create Operator
        printStageMessage("Creating Operator");
        Operator opResponse = createOperator(
                sessionCookie,
                "TestOperator",
                "",
                new OperatorRoutine("start.sh", testScript)
        );

        // Create sensor
        printStageMessage("Creating Sensor");
        Sensor sensorResponse = createSensor(
                sessionCookie,
                "TestSensor",
                "Temperature",
                deviceObj.getId(),
                opResponse.getId()
        );

        printStageMessage("Deploying and Starting Sensor");
        deploySensor(sessionCookie, sensorResponse.getId());
        startSensor(sessionCookie, sensorResponse.getId());

        printStageMessage("Ensuring that the deployed Script has been invoked");

        CommandOutput commandOutput = device.runCommand("cat /home/mbp/calllog.log");
        assertThat(commandOutput.getStderr()).isEmpty();
        String stdoutString = commandOutput.getStdout();
        assertThat(stdoutString).contains("Test Script was called");
        System.out.println(stdoutString);
    }

}
