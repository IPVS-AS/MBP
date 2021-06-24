package de.ipvs.as.mbp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.servlet.http.Cookie;

import com.jcabi.ssh.Shell;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.util.BaseDeviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

        Device deviceObj = objectMapper.readValue(result.getResponse().getContentAsString(), Device.class);

        result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/devices/" + deviceObj.getId() + "/state/")
                .header("X-MBP-Access-Request", "requesting-entity-firstname=admin;;requesting-entity-lastname=admin;;requesting-entity-username=admin")
                .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("SSH_AVAILABLE"))
                .andDo(print())
                .andReturn();

        Shell shell = device.openSshShell();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        shell.exec("sudo cat /var/log/auth.log", new ByteArrayInputStream(new byte[0]), stdout, stderr);

        String stdoutString = stdout.toString();

        System.out.println(stdoutString);

        // Check that the testing command has been called in the proper context
        assertThat(stdoutString).contains("mbp : PWD=/home/mbp ; USER=root ; COMMAND=/usr/bin/test 5 -gt 2");
    }
}
