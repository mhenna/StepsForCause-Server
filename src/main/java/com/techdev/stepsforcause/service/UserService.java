package com.techdev.stepsforcause.service;

import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.models.UserAttributes;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class UserService {

    public List<User> getUsers(MongoTemplate mongoTemplate) {
        return mongoTemplate.findAll(User.class);
    }

    public Map<String, Object> addUser(Map<String, Object> body, MongoTemplate mongoTemplate) {
        Map<String, Object> res = new HashMap<>();
        try {
            User u = mongoTemplate.save(new User(String.valueOf(body.get(UserAttributes.FIRSTNAME)),
                    String.valueOf(body.get(UserAttributes.LASTNAME)),
                    String.valueOf(body.get(UserAttributes.EMAIL)),
                    String.valueOf(body.get(UserAttributes.PASSWORD)),
                    ""));
            res.put("user", u);
        } catch (Exception e) {
            res.put("error", e.getMessage());
        }

        return res;
    }

    public Map<String, Object> updateStepCount(Map<String, Object> body, MongoTemplate mongoTemplate) {
        Map<String, Object> res;
        Integer stepCount = Integer.valueOf(String.valueOf(body.get(UserAttributes.STEPCOUNT)));
        String email = String.valueOf(body.get(UserAttributes.EMAIL));

        Query q = new Query(where("email").is(email));
        User user = mongoTemplate.findOne(q, User.class);

        assert user != null;
        res = user.updateStepCount(mongoTemplate, stepCount, q);

        return res;
    }

    public Map<String, Object> updateVerificationCode(Map<String, Object> body, MongoTemplate mongoTemplate) {
        Map<String, Object> res;
        String verificationCode = String.valueOf(body.get(UserAttributes.VERIFICATIONCODE));
        String email = String.valueOf(body.get(UserAttributes.EMAIL));

        Query q = new Query(where("email").is(email));
        User user = mongoTemplate.findOne(q, User.class);

        assert user != null;
        res = user.updateVerificationCode(mongoTemplate, verificationCode, q);
        return res;
    }
}
