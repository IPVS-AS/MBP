package de.ipvs.as.mbp;

import javax.servlet.http.Cookie;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.user.UserLoginData;
import de.ipvs.as.mbp.repository.UserRepository;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.service.user.UserSessionService;
import de.ipvs.as.mbp.util.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestUserControllerTest  extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionService userSessionService;

    @Test
    void retrieveAllUsers_returnOk() throws Exception {
        UserLoginData adminLogin = new UserLoginData("admin", "12345");

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        Cookie adminCookie = result.getResponse().getCookie("user_session");

        result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/users")
                .contentType("application/json")
                .cookie(adminCookie))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void retrieveAllUsers_returnUnauthorized() throws Exception {
        MvcResult result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/users")
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void getUserEntityForUsername_returnOk() throws Exception {
        UserLoginData adminLogin = new UserLoginData("admin", "12345");

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        Cookie adminCookie = result.getResponse().getCookie("user_session");

        result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/users")
                .contentType("application/json")
                .queryParam("username", "admin")
                .cookie(adminCookie))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void create_returnCreated() throws Exception {
        String newUser = "{\"id\":null,\"username\":\"newuser\",\"password\":\"password\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"isSystemUser\":false,\"isLoginable\":true,\"loginable\":true,\"entityType\":\"REQUESTING_ENTITY\",\"isAdmin\":false}";

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users")
                .contentType("application/json")
                .content(newUser))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();
    }

    @Test
    void searchByUsername_returnOk() throws Exception {

    }

    @Test
    void login_returnOk() throws Exception {

    }

    @Test
    void delete_returnOk() throws Exception {

    }

    @Test
    void promote_returnOk() throws Exception {

    }

    @Test
    void degrade_returnOk() throws Exception {

    }

    @Test
    void changePassword_returnOk() throws Exception {

    }

}
