package de.ipvs.as.mbp;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.KeyPairRepository;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.service.UserService;
import de.ipvs.as.mbp.util.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BasicTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser(username = "admin", password = "12345")
    void createDeviceTest() throws Exception {
        System.out.println("User List: " + userService.getAll(Pageable.unpaged()).toList());
        System.out.println("User at test start" + userService.getLoggedInUser());

        DeviceDTO requestDto = new DeviceDTO();
        requestDto.setName("testDevice");
        requestDto.setUsername("admin");
        requestDto.setPassword("12345");
        requestDto.setIpAddress("127.0.0.1");
        requestDto.setComponentType("Computer");

        // .header("Authorization", "Basic YWRtaW46MTIzNDU=")
        // .header("X-MBP-Access-Request", "requesting-entity-firstname=admin;;requesting-entity-lastname=admin;;requesting-entity-username=admin")

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/devices")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestDto))
                .characterEncoding("utf-8"))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        System.out.println(result.getResponse().getContentAsString());

        //Device device = objectMapper.convertValue(result.getResponse().getContentAsString(), Device.class);

        //Device deviceFromDB = userEntityService.getForId(deviceRepository, device.getId());
        //System.out.println(deviceFromDB);
    }
}
