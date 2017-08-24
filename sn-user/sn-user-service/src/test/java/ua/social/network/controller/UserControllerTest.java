package ua.social.network.controller;

import com.sun.security.auth.UserPrincipal;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import ua.social.network.UserServiceApplication;
import ua.social.network.dto.AddFriendRequest;
import ua.social.network.dto.CreateUserRequest;
import ua.social.network.entity.FriendRequest;
import ua.social.network.entity.Role;
import ua.social.network.entity.User;
import ua.social.network.repository.FriendRequestRepository;
import ua.social.network.repository.UserRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mykola Yashchenko
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@Sql(scripts = "classpath:user/users.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:truncate_tables.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private UserController accountController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    public void shouldCreateNewUser() throws Exception {

        CreateUserRequest user = new CreateUserRequest();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setName("Name Name");

        String json = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        User actualUser = userRepository.findByEmail(user.getEmail());
        assertThat(actualUser, notNullValue());
        assertThat(actualUser.getEmail(), equalTo(user.getEmail()));
        assertThat(actualUser.getName(), equalTo(user.getName()));
        assertThat(actualUser.getPassword(), notNullValue());
        assertThat(actualUser.getRole(), equalTo(Role.USER));
    }

    @Test
    public void shouldFailWhenUserIsNotValid() throws Exception {

        final CreateUserRequest user = new CreateUserRequest();
        user.setEmail("t");
        user.setPassword("p");
        user.setName("n");

        String json = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void shouldSendFriendRequest() throws Exception {
        String friendUUID = "2";

        AddFriendRequest addFriendRequest = new AddFriendRequest();
        addFriendRequest.setUserId(friendUUID);

        String json = MAPPER.writeValueAsString(addFriendRequest);

        mockMvc.perform(post("/users/add_friend")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new UserPrincipal("USER-1@EMAIL.COM"))
                .content(json))
                .andExpect(status().isOk());

        User from = userRepository.getOne("1");
        User to = userRepository.getOne(friendUUID);

        FriendRequest req = friendRequestRepository.findRequestByUsers(from, to);
        assertThat(req, notNullValue());
        assertThat(req.getFrom().getId(), equalTo(from.getId()));
        assertThat(req.getTo().getId(), equalTo(to.getId()));
    }
}