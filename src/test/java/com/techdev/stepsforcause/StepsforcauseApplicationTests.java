package com.techdev.stepsforcause;

import com.techdev.stepsforcause.controller.UserController;
import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.routes.Routes;
import com.techdev.stepsforcause.utils.JwtToken;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.data.mongodb.database=steps-for-cause-test", "spring.data.mongodb.uri=mongodb://localhost:27017"})
@AutoConfigureMockMvc
class StepsforcauseApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JwtToken jwtToken;

    @InjectMocks
    private UserController userController;

    private User youssef = new User("youssef", "elhady", "youssef@emc.com", "hello", "");
    private User mostafa = new User("mostafa", "henna", "mostafa@emc.com", "hello", "");

    @Test
    void contextLoads() throws Exception {
        assertThat(userController).isNotNull();

        mongoTemplate.dropCollection(User.class);
        mongoTemplate.save(youssef);
        mongoTemplate.save(mostafa);
    }

    @Test
    public void getUsersShouldReturnUsers() throws Exception {
        mockMvc.perform(get("/" + Routes.USERS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.users", hasSize(2)))
                .andExpect(jsonPath("$.users[0].firstName", is("youssef")))
                .andExpect(jsonPath("$.users[0].lastName", is("elhady")))
                .andExpect(jsonPath("$.users[0].email", is("youssef@emc.com")))
                .andExpect(jsonPath("$.users[1].firstName", is("mostafa")))
                .andExpect(jsonPath("$.users[1].lastName", is("henna")))
                .andExpect(jsonPath("$.users[1].email", is("mostafa@emc.com")));
    }

    @Test
    public void testRegister() throws Exception {
        User u = new User("karim", "mady", "karim@emc.com", "hello", "");
        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"mady\", \"email\":\"karim@emc.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user", hasValue(u.firstName)))
                    .andExpect(jsonPath("$.user", hasValue(u.lastName)))
                    .andExpect(jsonPath("$.user", hasValue(u.email)))
                    .andExpect(jsonPath("$.user", hasValue(u.password)));

        Query q = new Query(Criteria.where("email").is(u.email));
        User retrievedUser = mongoTemplate.findOne(q, User.class);

        Assert.assertNotNull(retrievedUser);
        Assert.assertEquals("karim", retrievedUser.firstName);
        Assert.assertEquals("mady", retrievedUser.lastName);
        Assert.assertEquals("karim@emc.com", retrievedUser.email);

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"mady\", \"email\":\"karim@emc.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mongoTemplate.remove(retrievedUser);

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"\", \"lastName\": \"mady\", \"email\":\"karim@emc.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"\", \"email\":\"karim@emc.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"mady\", \"email\":\"karim@emc.com\", \"password\": \"\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"mady\", \"email\":\"karimemc.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogin() throws Exception {
        mockMvc.perform(post("/" + Routes.USERS + Routes.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/" + Routes.USERS + Routes.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youef@emc.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/" + Routes.USERS + Routes.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"password\": \"heo\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateStepCount() throws Exception {
        String token = jwtToken.generateToken(youssef);
        mockMvc.perform(put("/" + Routes.STEPCOUNT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"stepCount\": \"2000\"}")
                .header("authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user", hasValue(youssef.firstName)))
                    .andExpect(jsonPath("$.user", hasValue(youssef.lastName)))
                    .andExpect(jsonPath("$.user", hasValue(youssef.email)))
                    .andExpect(jsonPath("$.user", hasValue(2000)));

        mockMvc.perform(put("/" + Routes.STEPCOUNT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"stepCount\": \"2000\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateVerificationCode() throws Exception {
        String token = jwtToken.generateToken(youssef);
        mockMvc.perform(put("/" + Routes.VERIFICATIONCODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"verificationCode\": \"updated code\"}")
                .header("authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user", hasValue(youssef.firstName)))
                    .andExpect(jsonPath("$.user", hasValue(youssef.lastName)))
                    .andExpect(jsonPath("$.user", hasValue(youssef.email)))
                    .andExpect(jsonPath("$.user", hasValue("updated code")));

        mockMvc.perform(put("/" + Routes.VERIFICATIONCODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"verificationCode\": \"updated code\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
