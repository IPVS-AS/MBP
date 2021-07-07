package de.ipvs.as.mbp;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.KeyPairRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.base.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestDeviceControllerTest extends BaseIntegrationTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private KeyPairRepository keyPairRepository;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private UserService userService;

    private Cookie adminCookie;

    @BeforeEach
    void initializeLogins() throws Exception {
        adminCookie = this.getSessionCookieForAdmin();
    }

    @Test
    void retrieveAllDevices_returnOk() throws Exception {
        MvcResult result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/devices")
                .cookie(adminCookie)
                .contentType("application/json")
                .header("X-MBP-Access-Request", "requesting-entity-firstname=admin;;requesting-entity-lastname=admin;;requesting-entity-username=admin")
                .characterEncoding("utf-8"))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void createDevice_returnOk() throws Exception {
        DeviceDTO requestDto = new DeviceDTO();
        requestDto.setName("testDevice");
        requestDto.setUsername("admin");
        requestDto.setPassword("12345");
        requestDto.setIpAddress("127.0.0.1");
        requestDto.setComponentType("Computer");

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/devices")
                .cookie(adminCookie)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestDto))
                .characterEncoding("utf-8"))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        Device responseDevice = objectMapper.readValue(result.getResponse().getContentAsString(), Device.class);

        Device deviceFromDB = userEntityService.getForId(deviceRepository, responseDevice.getId());

        assertThat(responseDevice.getId()).isEqualTo(deviceFromDB.getId());
        assertThat(deviceFromDB.getName()).isEqualTo(requestDto.getName());
        assertThat(responseDevice.getName()).isEqualTo(requestDto.getName());
        assertThat(deviceFromDB.getUsername()).isEqualTo(requestDto.getUsername());
        assertThat(responseDevice.getUsername()).isEqualTo(requestDto.getUsername());
        assertThat(deviceFromDB.getPassword()).isEqualTo(requestDto.getPassword());
        assertThat(responseDevice.getPassword()).isNull();
        assertThat(deviceFromDB.getComponentType()).isEqualTo(requestDto.getComponentType());
        assertThat(responseDevice.getComponentType()).isEqualTo(requestDto.getComponentType());
        assertThat(deviceFromDB.getIpAddress()).isEqualTo(requestDto.getIpAddress());
        assertThat(responseDevice.getIpAddress()).isEqualTo(requestDto.getIpAddress());
    }

}
