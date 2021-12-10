package de.ipvs.as.mbp.iottest;

import javax.print.attribute.standard.JobName;
import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.base.BaseIoTTest;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.util.CommandOutput;
import de.ipvs.as.mbp.util.testexecution.RequiresMQTT;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiresMQTT
public class SensorRunOperatorExpectingDataTest extends BaseIoTTest {

    @Test
    void sensorDeployAndRunScriptExpectData() throws Exception {
        printStageMessage("Requesting Session Cookie");
        Cookie sessionCookie = getSessionCookieForAdmin();

        printStageMessage("Creating Device");
        Device deviceObj = this.createNewDevice(device, sessionCookie, "expectdata-mockdevice");

        //Create data model
        printStageMessage("Creating Data Model");
        DataModel dataModel = createDataModel(sessionCookie, "expectdata-datamodel", "",
                new JSONArray()
                        .put(new JSONObject("{\"name\": \"value\", \"type\": \"double\", \"parent\": \"RootObj\", \"children\": [] }"))
                        .put(new JSONObject("{\"name\": \"RootObj\", \"description\": \"\", \"type\": \"object\", \"unit\": \"\", \"parent\": \"\", \"children\": [\"value\"]}")));

        // Create Operator
        printStageMessage("Creating Operator");
        Operator opResponse = createOperator(
                sessionCookie,
                dataModel.getId(),
                "TestSensorOperator",
                "",
                this.getRoutineFromClasspath("mbp_client.py", "text/plain", "scripts/mbp_client/mbp_client.py"),
                this.getRoutineFromClasspath("docker_dummy.py", "text/plain", "scripts/test_sensor/docker_dummy.py"),
                this.getRoutineFromClasspath("entry-file-name", "text/plain", "scripts/test_sensor/entry-file-name"),
                this.getRoutineFromClasspath("start.sh", "application/x-shellscript", "scripts/mbp_client/start.sh"),
                this.getRoutineFromClasspath("stop.sh", "application/x-shellscript", "scripts/mbp_client/stop.sh")
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

        printStageMessage("Ensuring that the deployed Script is running");

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

        printStageMessage("Validating that data is received");

        // Wait some time for data to be sent
        System.out.println("Waiting for data to be Produced");
        Thread.sleep(15000);

        String url = RestConfiguration.BASE_PATH + "/sensors/" + sensorResponse.getId() + "/valueLogs?sort=time,desc&size=200&startTime=-1&endTime=-1";
        MvcResult result = this.mockMvc.perform(get(url).cookie(sessionCookie).headers(getMBPAccessHeaderForAdmin()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        JSONObject bodyObj = new JSONObject(result.getResponse().getContentAsString());
        JSONArray contentArray = bodyObj.getJSONArray("content");

        assertThat(contentArray.length()).isGreaterThanOrEqualTo(15);
    }
}
