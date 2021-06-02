package de.ipvs.as.mbp;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.KeyPairRepository;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.service.UserService;
import de.ipvs.as.mbp.util.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BasicTest extends BaseIntegrationTest {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private KeyPairRepository keyPairRepository;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private UserService userService;

    @Test
    void name() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void createDeviceTest() throws Exception {
        System.out.println("User List: " + userService.getAll(Pageable.unpaged()).toList());

//        UserAuthData userAuthData = new UserAuthData();
//        userAuthData.setUsername("admin");
//        userAuthData.setPassword("12345");
//
//        MvcResult userAuthResult = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users/authenticate")
//                .contentType("application/json")
//                .content(objectMapper.writeValueAsString(userAuthData))
//                .characterEncoding("utf-8"))
//                .andDo(print())
//                .andExpect(status().isOk()).andReturn();

        DeviceDTO requestDto = new DeviceDTO();
        requestDto.setName("testDevice");
        requestDto.setUsername("admin");
        requestDto.setPassword("12345");

        // .header("Authorization", "Basic YWRtaW46MTIzNDU=")
        // .header("X-MBP-Access-Request", "requesting-entity-firstname=admin;;requesting-entity-lastname=admin;;requesting-entity-username=admin")

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/devices")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestDto))
                .characterEncoding("utf-8")
                .header("Authorization", "Basic YWRtaW46MTIzNDU="))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        Device device = objectMapper.convertValue(result.getResponse().getContentAsString(), Device.class);

        Device deviceFromDB = userEntityService.getForId(deviceRepository, device.getId());
        System.out.println(deviceFromDB);
    }
}
