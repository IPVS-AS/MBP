package de.ipvs.as.mbp.util;

import javax.servlet.http.Cookie;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.user.UserLoginData;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class})
@SpringBootTest()
@AutoConfigureMockMvc
@AutoConfigureWebMvc
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseIntegrationTest {

    @Container
    public static MongoDbContainer mongoDbContainer = MongoDbContainer.getInstance();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    public Cookie getSessionCookieForAdmin() throws Exception {
        return getSessionCookie("admin", "12345");
    }

    public Cookie getSessionCookie(String username, String password) throws Exception {
        UserLoginData adminLogin = new UserLoginData(username, password);

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk()).andReturn();

        return result.getResponse().getCookie("user_session");
    }
}
