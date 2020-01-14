package com.techdev.stepsforcause.service;

import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.models.UserAttributes;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class UserService {

    public ResponseEntity getUsers(MongoTemplate mongoTemplate) {
        Map<String, Object> res = new HashMap<>();
        List<User> users = mongoTemplate.findAll(User.class);
        res.put("users", users);
        return new ResponseEntity(res, HttpStatus.OK);
    }

    public ResponseEntity addUser(Map<String, Object> body, MongoTemplate mongoTemplate) {
        Map<String, Object> res = new HashMap<>();
        HttpStatus status = null;
        try {
            User u = mongoTemplate.save(new User(String.valueOf(body.get(UserAttributes.FIRSTNAME)),
                    String.valueOf(body.get(UserAttributes.LASTNAME)),
                    String.valueOf(body.get(UserAttributes.EMAIL)),
                    String.valueOf(body.get(UserAttributes.PASSWORD)),
                    ""));
            status = HttpStatus.OK;
            res.put("user", u);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity(res, status);
    }

    public Map<String, Object> getUserAndQuery(Map<String, Object> body, MongoTemplate mongoTemplate) {
        Map<String, Object> res = new HashMap<>();
        String email = String.valueOf(body.get(UserAttributes.EMAIL));

        Query q = new Query(where("email").is(email));
        User user = mongoTemplate.findOne(q, User.class);
        res.put("user",  user);
        res.put("query", q);
        return res;
    }
}
