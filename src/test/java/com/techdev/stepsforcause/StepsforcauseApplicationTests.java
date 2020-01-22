package com.techdev.stepsforcause;

import com.mongodb.MongoClient;
import com.techdev.stepsforcause.controller.UserController;
import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.routes.Routes;
import com.techdev.stepsforcause.utils.HelperFuncs;
import com.techdev.stepsforcause.utils.JwtToken;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
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

    @Autowired
    private UserController userController;

    private User youssef = new User("youssef", "elhady", "youssef@emc.com", "hello", "hi");
    private User mostafa = new User("mostafa", "henna", "mostafa@emc.com", "hello", "");

    @BeforeEach
    void setupDB() {
        MockitoAnnotations.initMocks(this);
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.save(youssef);
        mongoTemplate.save(mostafa);
    }

    @Test
    void contextLoads() throws Exception {
        assertThat(userController).isNotNull();
    }

    /* This is the part where get all users endpoint is tested
        1. Get users correctly
        2. Get users without token to make sure it will return unauthorized
     */
    @Test
    public void testGetUsers() throws Exception {
        String token = jwtToken.generateToken(youssef);
        mockMvc.perform(get("/" + Routes.USERS)
                .header("authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.users", hasSize(2)))
                    .andExpect(jsonPath("$.users[0].firstName", is("youssef")))
                    .andExpect(jsonPath("$.users[0].lastName", is("elhady")))
                    .andExpect(jsonPath("$.users[0].email", is("youssef@emc.com")))
                    .andExpect(jsonPath("$.users[1].firstName", is("mostafa")))
                    .andExpect(jsonPath("$.users[1].lastName", is("henna")))
                    .andExpect(jsonPath("$.users[1].email", is("mostafa@emc.com")));

        mockMvc.perform(get("/" + Routes.USERS))
                .andExpect(status().isUnauthorized());
    }

    /* This is the part where verification along with login endpoints are tested
        1. Logging in before user verification is complete
        2. Entering incorrect verificationCode
        3. Entering the correct verificationCode
        4. Logging in with incorrect email
        5. Logging in with incorrect password
        6. Logging in correctly */
    @Test
    public void testCheckVerificationCode() throws Exception {
        //////////////////// Login before verification ///////////////////////////
        mockMvc.perform(post("/" + Routes.USERS + Routes.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
        ///////////////////////////////////////////////////////////////////////////

        //////////////////////////// Verification /////////////////////////////////
        mockMvc.perform(post("/" + Routes.VERIFICATIONCODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"verificationCode\": \"hiiii\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());

        mockMvc.perform(post("/" + Routes.VERIFICATIONCODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"verificationCode\": \"hi\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user", hasValue(youssef.firstName)))
                .andExpect(jsonPath("$.user", hasValue(youssef.lastName)))
                .andExpect(jsonPath("$.user", hasValue(youssef.email)))
                .andExpect(jsonPath("$.user", hasValue("hi")))
                .andExpect(jsonPath("$.user", hasValue(true)));
        ///////////////////////////////////////////////////////////////////////////

        ///////////////////////////// Login ///////////////////////////////////////
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

        mockMvc.perform(post("/" + Routes.USERS + Routes.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"youssef@emc.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        ///////////////////////////////////////////////////////////////////////////
    }

    /* This is the part where updating the step count endpoint is tested
        1. Update step count correctly
        2. Update step count without token to make sure it will return unauthorized
     */
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

    @AfterAll
    public static void postTests() {
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        mongoClient.getDatabase("steps-for-cause-test").getCollection("user").drop();
    }
}

@SpringBootTest(properties = {"spring.data.mongodb.database=steps-for-cause-test", "spring.data.mongodb.uri=mongodb://localhost:27017"})
@AutoConfigureMockMvc
class RegistrationTests {
    @MockBean
    private HelperFuncs helperFuncs;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    private User youssef = new User("youssef", "elhady", "youssef@emc.com", "hello", "hi");
    private User mostafa = new User("mostafa", "henna", "mostafa@emc.com", "hello", "");

    @BeforeEach
    void setupDB() {
        MockitoAnnotations.initMocks(this);
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.indexOps(User.class).ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
        mongoTemplate.save(youssef);
        mongoTemplate.save(mostafa);
    }

    /* This is the part where registration endpoint is tested with all scenarios
        1. Register correctly
        2. Register with no first name
        3. Register with no last name
        4. Register with no password
        5. Register with an incorrectly formatted email
        6. Register again to ensure duplicate key error returns
     */
    @Test
    public void testRegister() throws Exception {
        doNothing().when(helperFuncs).sendEmail(anyString(), anyString(), anyString(), anyString());

        User u = new User("karim", "mady", "karim@karim.com", "hello", "");
        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"mady\", \"email\":\"karim@karim.com\", \"password\": \"hello\"}")
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
        Assert.assertEquals("karim@karim.com", retrievedUser.email);

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"\", \"lastName\": \"mady\", \"email\":\"karim@karim.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"\", \"email\":\"karim@karim.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"mady\", \"email\":\"karim@karim.com\", \"password\": \"\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"mady\", \"email\":\"karimkarim.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/" + Routes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"karim\", \"lastName\": \"mady\", \"email\":\"karim@karim.com\", \"password\": \"hello\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mongoTemplate.remove(retrievedUser);
    }

    @AfterAll
    public static void postTests() {
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        mongoClient.getDatabase("steps-for-cause-test").getCollection("user").drop();
    }
}
