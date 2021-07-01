package de.ipvs.as.mbp;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.util.BaseDeviceTest;
import de.ipvs.as.mbp.util.CommandOutput;
import de.ipvs.as.mbp.util.IoTDeviceContainer;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BasicDeviceTest extends BaseDeviceTest {

    @Container
    public static IoTDeviceContainer device = new IoTDeviceContainer();

    @Test
    void deviceConnect() throws Exception {
        Cookie sessionCookie = getSessionCookieForAdmin();

        Device deviceObj = this.createNewDevice(device,sessionCookie,"connect-mockdevice");

        mockMvc.perform(get(RestConfiguration.BASE_PATH + "/devices/" + deviceObj.getId() + "/state/")
                .header("X-MBP-Access-Request", "requesting-entity-firstname=admin;;requesting-entity-lastname=admin;;requesting-entity-username=admin")
                .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("SSH_AVAILABLE"))
                .andDo(print())
                .andReturn();

        CommandOutput commandOutput =  device.runCommand("sudo cat /var/log/auth.log");
        String stdoutString = commandOutput.getStdout();

        System.out.println(stdoutString);

        // Check that the testing command has been called in the proper context
        assertThat(stdoutString).contains("mbp : PWD=/home/mbp ; USER=root ; COMMAND=/usr/bin/test 5 -gt 2");
    }

    @Test
    void deviceFileUpload() throws Exception{
        Cookie sessionCookie = getSessionCookieForAdmin();

        Device deviceObj = this.createNewDevice(device,sessionCookie,"connect-mockdevice");

    }
}
