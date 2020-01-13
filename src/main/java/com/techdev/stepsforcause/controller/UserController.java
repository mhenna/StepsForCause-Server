package com.techdev.stepsforcause.controller;

import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.routes.Routes;
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

    @RequestMapping(value = Routes.USERS, method = RequestMethod.GET)
    public List<User> getUsers() {
        return mongoTemplate.findAll(User.class);
    }

    @RequestMapping(value = Routes.USERS, method = RequestMethod.POST)
    public User registerUsers(@RequestBody Map<String, Object> body) {
        return mongoTemplate.save(new User(String.valueOf(body.get("firstName")),
                String.valueOf(body.get("lastName")),
                String.valueOf(body.get("email")),
                String.valueOf(body.get("password")),
                "kjwfhekjwfhek"));
    }
}
