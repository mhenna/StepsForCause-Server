package com.techdev.stepsforcause.controller;

import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.models.UserAttributes;
import com.techdev.stepsforcause.routes.Routes;
import com.techdev.stepsforcause.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private MongoTemplate mongoTemplate;

    private UserService service = new UserService();


    @RequestMapping(value = Routes.USERS, method = RequestMethod.GET)
    public ResponseEntity getUsers() {
        return service.getUsers(mongoTemplate);
    }

    @RequestMapping(value = Routes.USERS, method = RequestMethod.POST)
    public ResponseEntity registerUsers(@RequestBody Map<String, Object> body) {
        return service.addUser(body, mongoTemplate);
    }

    @RequestMapping(value = Routes.USERS + Routes.LOGIN, method = RequestMethod.POST)
    public Map<String, Object> login(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        res.put("hi", "hi");
        return res;
    }

    @RequestMapping(value = Routes.STEPCOUNT, method = RequestMethod.PUT)
    public ResponseEntity updateStepCount(@RequestBody Map<String, Object> body) {
        Map<String, Object> userAndQuery = service.getUserAndQuery(body, mongoTemplate);

        User user = (User) userAndQuery.get("user");
        Query q = (Query) userAndQuery.get("query");
        Integer stepCount = Integer.valueOf(String.valueOf(body.get(UserAttributes.STEPCOUNT)));

        return user.updateStepCount(mongoTemplate, stepCount, q);
    }

    @RequestMapping(value = Routes.VERIFICATIONCODE, method = RequestMethod.PUT)
    public ResponseEntity updateVerificationCode(@RequestBody Map<String, Object> body) {
        Map<String, Object> userAndQuery = service.getUserAndQuery(body, mongoTemplate);

        User user = (User) userAndQuery.get("user");
        Query q = (Query) userAndQuery.get("query");
        String verificationCode = String.valueOf(body.get(UserAttributes.VERIFICATIONCODE));

        return user.updateVerificationCode(mongoTemplate, verificationCode, q);
    }
}
