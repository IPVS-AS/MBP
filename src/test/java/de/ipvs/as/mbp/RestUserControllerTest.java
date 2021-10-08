package de.ipvs.as.mbp;

import java.util.List;

import javax.servlet.http.Cookie;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.base.BaseBackendTest;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.user.UserLoginData;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.UserRepository;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.service.user.UserSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestUserControllerTest extends BaseBackendTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionService userSessionService;

    private final String newUser = "{\"id\":null,\"username\":\"newuser\",\"password\":\"password\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"isSystemUser\":false,\"isLoginable\":true,\"loginable\":true,\"entityType\":\"REQUESTING_ENTITY\",\"isAdmin\":false}";

    @Test
    void retrieveAllUsers_returnOk() throws Exception {
        Cookie adminCookie = this.getSessionCookieForAdmin();

        MvcResult result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/users")
                        .contentType("application/json+hal")
                        .cookie(adminCookie))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void retrieveAllUsers_returnUnauthorized() throws Exception {
        mockMvc.perform(get(RestConfiguration.BASE_PATH + "/users")
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void getUserEntityForUsername_returnOk() throws Exception {
        Cookie adminCookie = this.getSessionCookieForAdmin();

        MvcResult result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/users/admin")
                        .contentType("application/json")
                        .cookie(adminCookie))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        User user = this.objectMapper.readValue(result.getResponse().getContentAsString(), User.class);

        assertThat(user.getId()).isEqualTo(userService.getForUsername("admin").getId());
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getFirstName()).isEqualTo("admin");
        assertThat(user.getLastName()).isEqualTo("admin");
    }

    @Test
    void create_returnCreated() throws Exception {
        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users")
                        .contentType("application/json")
                        .content(newUser))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();

        User user = this.objectMapper.readValue(result.getResponse().getContentAsString(), User.class);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("newuser");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
    }

    @Test
    void searchByUsername_returnOk() throws Exception {
        Cookie adminCookie = this.getSessionCookieForAdmin();

        MvcResult result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/users/searchByUsername")
                        .contentType("application/json")
                        .queryParam("query", "admin")
                        .cookie(adminCookie))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        List<User> users = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<User>>() {
        });

        assertThat(users.size()).isEqualTo(1);
        assertThat(users.get(0).getId()).isEqualTo(userService.getForUsername("admin").getId());
        assertThat(users.get(0).getUsername()).isEqualTo("admin");
    }

    @Test
    void searchByUsername_emptyQuery_returnOk() throws Exception {
        Cookie adminCookie = this.getSessionCookieForAdmin();

        MvcResult result = mockMvc.perform(get(RestConfiguration.BASE_PATH + "/users/searchByUsername")
                        .contentType("application/json")
                        .param("query", "")
                        .cookie(adminCookie))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        List<User> users = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<User>>() {
        });

        assertThat(users.size()).isEqualTo(0);
    }

    @Test
    void login_returnOk() throws Exception {
        UserLoginData adminLogin = new UserLoginData("admin", "12345");

        MvcResult result = mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk()).andReturn();

        User user = this.objectMapper.readValue(result.getResponse().getContentAsString(), User.class);

        assertThat(user.getId()).isEqualTo(userService.getForUsername("admin").getId());
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getFirstName()).isEqualTo("admin");
        assertThat(user.getLastName()).isEqualTo("admin");

        Cookie adminCookie = result.getResponse().getCookie("user_session");

        assertThat(adminCookie).isNotNull();
        assertThat(adminCookie.getName()).isEqualTo("user_session");
    }

    @Test
    void delete_returnOk() throws Exception {
        Cookie adminCookie = this.getSessionCookieForAdmin();

        mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users")
                        .contentType("application/json")
                        .content(newUser))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();

        User user = userService.getForUsername("newuser");

        mockMvc.perform(delete(RestConfiguration.BASE_PATH + "/users/" + user.getId())
                        .contentType("application/json")
                        .cookie(adminCookie))
                .andExpect(status().isNoContent()).andReturn();

        assertThrows(MBPException.class, () -> userService.getForUsername("newuser"));
    }

    @Test
    void delete_returnForbidden() throws Exception {
        Cookie adminCookie = this.getSessionCookieForAdmin();

        List<User> users = userService.getAll(Pageable.unpaged()).toList();

        mockMvc.perform(delete(RestConfiguration.BASE_PATH + "/users/" + users.get(1).getId())
                        .contentType("application/json")
                        .cookie(adminCookie))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void delete_returnNotFound() throws Exception {
        Cookie adminCookie = this.getSessionCookieForAdmin();

        mockMvc.perform(delete(RestConfiguration.BASE_PATH + "/users/userThatDoesNotExist")
                        .contentType("application/json")
                        .cookie(adminCookie))
                .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void promoteAndDegrade_returnOk() throws Exception {
        Cookie adminCookie = this.getSessionCookieForAdmin();
        String username = "newuser";

        mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users")
                        .contentType("application/json")
                        .content(newUser))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();

        User user = userService.getForUsername(username);

        assertThat(user.isAdmin()).isFalse();

        mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users/" + user.getId() + "/promote")
                        .contentType("application/json")
                        .cookie(adminCookie))
                .andExpect(status().isOk()).andReturn();

        User promotedUser = userService.getForUsername(username);

        assertThat(promotedUser.isAdmin()).isTrue();
        assertThat(promotedUser.getId()).isEqualTo(user.getId());
        assertThat(promotedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(promotedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(promotedUser.getLastName()).isEqualTo(user.getLastName());

        mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users/" + user.getId() + "/degrade")
                        .contentType("application/json")
                        .cookie(adminCookie))
                .andExpect(status().isOk()).andReturn();

        User degradedUser = userService.getForUsername(username);

        assertThat(degradedUser.isAdmin()).isFalse();
        assertThat(degradedUser.getId()).isEqualTo(user.getId());
        assertThat(degradedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(degradedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(degradedUser.getLastName()).isEqualTo(user.getLastName());
    }

    @Test
    void changePassword_returnOk() throws Exception {
        Cookie adminCookie = this.getSessionCookieForAdmin();
        String newChangedPasswordUser = "{\"id\":null,\"username\":\"newuser\",\"password\":\"changedPassword\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"isSystemUser\":false,\"isLoginable\":true,\"loginable\":true,\"entityType\":\"REQUESTING_ENTITY\",\"isAdmin\":false}";
        String username = "newuser";

        mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users")
                        .contentType("application/json")
                        .content(newUser))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();

        User user = userService.getForUsername(username);

        mockMvc.perform(post(RestConfiguration.BASE_PATH + "/users/" + user.getId() + "/change_password")
                        .contentType("application/json")
                        .content(newChangedPasswordUser)
                        .cookie(adminCookie))
                .andExpect(status().isOk()).andReturn();

        User changedPasswordUser = userService.getForUsername(username);

        assertThat(changedPasswordUser.getPassword()).isNotEqualTo("changedPassword");
        assertThat(changedPasswordUser.getPassword()).isNotEqualTo(user.getPassword());
    }
}
