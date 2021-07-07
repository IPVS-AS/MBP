package de.ipvs.as.mbp.base;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.component.ComponentDTO;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.util.IoTDeviceContainer;
import de.ipvs.as.mbp.util.RequiresMQTT;
import de.ipvs.as.mbp.util.RequiresMQTTExtension;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RequiresMQTTExtension.class})
public abstract class BaseIoTTest extends BaseIntegrationTest {

    public OperatorRoutine getRoutineFromClasspath(String name,String type, String path) {
        return null;
    }

    public Device createNewDevice(IoTDeviceContainer container, Cookie sessionCookie, String name) throws Exception {
        DeviceDTO requestDto = new DeviceDTO();
        requestDto.setName(name);
        requestDto.setUsername("mbp");
        requestDto.setPassword("password");
        requestDto.setIpAddress("127.0.0.1");
        requestDto.setPort(container.getSshPort());
        requestDto.setComponentType("Computer");

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/devices")
                .cookie(sessionCookie)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestDto))
                .characterEncoding("utf-8"))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Device.class);
    }

    public Operator createOperator(Cookie sessionCookie, String operatorName, String operatorUnit, OperatorRoutine... scripts) throws Exception {
        JSONArray routines = new JSONArray();

        for (OperatorRoutine script : scripts) {
            routines.put(script.toJSONObject());
        }

        JSONObject operatorObj = new JSONObject();
        operatorObj.put("parameters", new JSONArray());
        operatorObj.put("unit", operatorUnit);
        operatorObj.put("name", operatorName);
        operatorObj.put("errors", new JSONObject());
        operatorObj.put("routines", routines);

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/operators")
                .headers(getMBPAccessHeaderForAdmin())
                .cookie(sessionCookie)
                .contentType(REQUEST_CONTENT_TYPE)
                .content(operatorObj.toString())
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value("admin"))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Operator.class);
    }

    public void deploySensor(Cookie sessionCookie, String sensorId) throws Exception {
        mockMvc.perform(post(RestConfiguration.BASE_PATH + "/deploy/sensor/" + sensorId)
                .headers(getMBPAccessHeaderForAdmin())
                .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andDo(print());
    }

    public void startSensor(Cookie sessionCookie, String sensorId) throws Exception {
        mockMvc.perform(post(RestConfiguration.BASE_PATH + "/start/sensor/" + sensorId)
                .headers(getMBPAccessHeaderForAdmin())
                .cookie(sessionCookie)
                .contentType(REQUEST_CONTENT_TYPE)
                .content("[]"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    public Sensor createSensor(Cookie sessionCookie, String sensorName, String sensorType, String deviceId, String operatorId) throws Exception {
        ComponentDTO sensorReq = new ComponentDTO();
        sensorReq.setDeviceId(deviceId);
        sensorReq.setComponentType(sensorType);
        sensorReq.setOperatorId(operatorId);
        sensorReq.setName(sensorName);

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/sensors")
                .headers(getMBPAccessHeaderForAdmin())
                .cookie(sessionCookie)
                .contentType(REQUEST_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(sensorReq))
        ).andDo(print()).andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Sensor.class);
    }

    public void ensureDeviceHasSSH(Cookie sessionCookie, String deviceId) throws Exception {
        mockMvc.perform(get(RestConfiguration.BASE_PATH + "/devices/" + deviceId + "/state/")
                .headers(getMBPAccessHeaderForAdmin())
                .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("SSH_AVAILABLE"))
                .andDo(print());
    }

    public void ensureSensorIsReady(Cookie sessionCookie, String sensorId) throws Exception {
        mockMvc.perform(get(RestConfiguration.BASE_PATH + "/sensors/state/" + sensorId)
                .headers(getMBPAccessHeaderForAdmin())
                .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("READY"))
                .andDo(print());
    }

    public static class OperatorRoutine {
        public String name;
        public String content;
        public String contentType;

        public OperatorRoutine(String name, String contentType, String content) {
            this.name = name;
            this.content = content;
            this.contentType = contentType;
        }

        public OperatorRoutine(String name, String content) {
            this(name, "application/x-shellscript", content);
        }

        public JSONObject toJSONObject() throws Exception {
            JSONObject testScriptObj = new JSONObject();
            testScriptObj.put("name", name);
            testScriptObj.put("content",
                    String.format("data:%s;base64,%s", this.contentType,
                            Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8))));
            return testScriptObj;
        }
    }
}
