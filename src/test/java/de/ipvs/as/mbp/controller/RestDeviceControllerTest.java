package de.ipvs.as.mbp.controller;

import java.util.Optional;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.base.BaseBackendTest;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.KeyPairRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestDeviceControllerTest extends BaseBackendTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserEntityService userEntityService;

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
                        .headers(getMBPAccessHeaderForAdmin()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void getDeviceById_returnOk() throws Exception {
        DeviceDTO requestDto = new DeviceDTO();
        requestDto.setName("testDevice");
        requestDto.setUsername("admin");
        requestDto.setPassword("12345");
        requestDto.setIpAddress("127.0.0.1");
        requestDto.setComponentType("Computer");

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/devices")
                        .cookie(adminCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        Device createdDevice = objectMapper.readValue(result.getResponse().getContentAsString(), Device.class);

        result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/devices/" + createdDevice.getId())
                        .cookie(adminCookie)
                        .contentType("application/json")
                        .headers(getMBPAccessHeaderForAdmin()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        Device responseDevice = objectMapper.readValue(result.getResponse().getContentAsString(), Device.class);

        assertThat(responseDevice.getId()).isEqualTo(createdDevice.getId());
        assertThat(createdDevice.getName()).isEqualTo(requestDto.getName());
        assertThat(responseDevice.getName()).isEqualTo(requestDto.getName());
        assertThat(createdDevice.getUsername()).isEqualTo(requestDto.getUsername());
        assertThat(responseDevice.getUsername()).isEqualTo(requestDto.getUsername());
        assertThat(responseDevice.getPassword()).isNull();
        assertThat(createdDevice.getComponentType()).isEqualTo(requestDto.getComponentType());
        assertThat(responseDevice.getComponentType()).isEqualTo(requestDto.getComponentType());
        assertThat(createdDevice.getIpAddress()).isEqualTo(requestDto.getIpAddress());
        assertThat(responseDevice.getIpAddress()).isEqualTo(requestDto.getIpAddress());
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
                        .content(objectMapper.writeValueAsString(requestDto)))
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

    @Test
    void delete_returnNoContent() throws Exception {
        DeviceDTO requestDto = new DeviceDTO();
        requestDto.setName("testDevice");
        requestDto.setUsername("admin");
        requestDto.setPassword("12345");
        requestDto.setIpAddress("127.0.0.1");
        requestDto.setComponentType("Computer");

        mockMvc.perform(post(RestConfiguration.BASE_PATH + "/devices")
                        .cookie(adminCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        Optional<Device> createdDevice = deviceRepository.findFirstByName(requestDto.getName());
        assertThat(createdDevice.isPresent()).isTrue();

        mockMvc.perform(delete(RestConfiguration.BASE_PATH + "/devices/" + createdDevice.get().getId())
                        .cookie(adminCookie)
                        .contentType("application/json")
                        .headers(getMBPAccessHeaderForAdmin()))
                .andDo(print())
                .andExpect(status().isNoContent()).andReturn();

        Optional<Device> deletedDevice = deviceRepository.findFirstByName(requestDto.getName());
        assertThat(deletedDevice.isPresent()).isFalse();
    }
}
