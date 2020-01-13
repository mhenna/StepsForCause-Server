package com.techdev.stepsforcause.controller;

import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.routes.Routes;
import com.techdev.stepsforcause.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private MongoTemplate mongoTemplate;

    private UserService service = new UserService();


    @RequestMapping(value = Routes.USERS, method = RequestMethod.GET)
    public List<User> getUsers() {
        return service.getUsers(mongoTemplate);
    }

    @RequestMapping(value = Routes.USERS, method = RequestMethod.POST)
    public Map<String, Object> registerUsers(@RequestBody Map<String, Object> body) {
        return service.addUser(body, mongoTemplate);
    }

    @RequestMapping(value = Routes.STEPCOUNT, method = RequestMethod.PUT)
    public Map<String, Object> updateStepCount(@RequestBody Map<String, Object> body) {
        return service.updateStepCount(body, mongoTemplate);
    }

    @RequestMapping(value = Routes.VERIFICATIONCODE, method = RequestMethod.PUT)
    public Map<String, Object> updateVerificationCode(@RequestBody Map<String, Object> body) {
        return service.updateVerificationCode(body, mongoTemplate);
    }
}
