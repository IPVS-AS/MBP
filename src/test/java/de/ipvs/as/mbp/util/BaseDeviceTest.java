package de.ipvs.as.mbp.util;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseDeviceTest extends BaseIntegrationTest {

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

        Device deviceObj = objectMapper.readValue(result.getResponse().getContentAsString(), Device.class);
        return deviceObj;
    }
}
