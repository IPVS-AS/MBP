package de.ipvs.as.mbp.iottest;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.base.BaseIoTTest;
import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.util.CommandOutput;
import de.ipvs.as.mbp.util.testexecution.RequiresMQTT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RequiresMQTT
public class ActuatorPerformActuationTest extends BaseIoTTest {

    @Test
    void actuatorShouldPerformActuation() throws Exception {
        printStageMessage("Requesting Session Cookie");
        Cookie sessionCookie = getSessionCookieForAdmin();

        printStageMessage("Creating Device");
        Device deviceObj = this.createNewDevice(device, sessionCookie, "performactuation-mockdevice");

        printStageMessage("Creating Operator");
        Operator opResponse = createOperator(
                sessionCookie,
                "TestActuationOperator",
                "",
                this.getRoutineFromClasspath("mbp_client.py", "text/plain", "scripts/mbp_client/mbp_client.py"),
                this.getRoutineFromClasspath("docker_dummy.py", "text/plain", "scripts/test_actuator/docker_dummy.py"),
                this.getRoutineFromClasspath("entry-file-name", "text/plain", "scripts/test_actuator/entry-file-name"),
                this.getRoutineFromClasspath("running.sh", "application/x-shellscript", "scripts/test_actuator/running.sh"),
                this.getRoutineFromClasspath("start.sh", "application/x-shellscript", "scripts/mbp_client/start.sh"),
                this.getRoutineFromClasspath("stop.sh", "application/x-shellscript", "scripts/mbp_client/stop.sh")
        );

        printStageMessage("Creating Actuator Object");
        Actuator actuatorResponse = createActuator(
                sessionCookie,
                "TestActuator",
                "Switch",
                deviceObj.getId(),
                opResponse.getId()
        );

        printStageMessage("Deploying and Starting Actuator");
        deployActuator(sessionCookie, actuatorResponse.getId());
        startActuator(sessionCookie, actuatorResponse.getId());

        printStageMessage("Creating Rule Action");
        String ruleActionId = createActuatorRuleAction(sessionCookie, actuatorResponse.getId(), "TestActuationRuleAction", "test");

        printStageMessage("Triggering Action 10 times");
        for (int i = 0; i < 10; i++) {
            System.out.println("Calling " + (i + 1) + "/10 with ID " + ruleActionId);
            assertThat(testRuleAction(sessionCookie, ruleActionId)).isTrue();
            Thread.sleep(250);
        }

        printStageMessage("Validating action trigger count");
        String filePath = "/home/mbp/scripts/mbp" + actuatorResponse.getId() + "/actions.log";
        CommandOutput output = this.device.runCommand("cat " + filePath);
        System.out.println(output.getStdout());
        assertThat(output.getStdout().split("\n").length).isEqualTo(10);
    }
}
