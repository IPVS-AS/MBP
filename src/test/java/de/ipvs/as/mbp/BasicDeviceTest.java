package de.ipvs.as.mbp;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.util.BaseDeviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BasicDeviceTest extends BaseDeviceTest {
    @Test
    void deviceConnect() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        DeviceDTO requestDto = new DeviceDTO();
        requestDto.setName("connect-mockdevice");
        requestDto.setUsername("mbp");
        requestDto.setPassword("password");
        requestDto.setIpAddress("127.0.0.1");
        requestDto.setPort(device.getSshPort());
        requestDto.setComponentType("Computer");

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/devices")
                .cookie(sessionCookie)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestDto))
                .characterEncoding("utf-8"))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
    }
}
