package com.techdev.stepsforcause.service;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.models.UserAttributes;
import com.techdev.stepsforcause.utils.JwtToken;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
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

    public ResponseEntity authenticateUser(Map<String, Object> body, MongoTemplate mongoTemplate, JwtToken jwt) {
        Map<String, Object> res = new HashMap<>();
        HttpStatus status = null;
        String token = null;
        try {
            Query query = new Query(where(UserAttributes.EMAIL).is(String.valueOf(body.get(UserAttributes.EMAIL))));
            User u = mongoTemplate.findOne(query, User.class);

            if (u == null) {
                res.put("error", "User with this email does not exist");
                status = HttpStatus.NOT_FOUND;
            } else {
                if (!(String.valueOf(body.get(UserAttributes.PASSWORD)).equals(u.password))) {
                    res.put("error", "Incorrect password");
                    status = HttpStatus.BAD_REQUEST;
                } else {
                    token = jwt.generateToken(u);
                    res.put("token", token);
                    status = HttpStatus.OK;
                }
            }
        } catch (Exception e) {
            res.put("error", e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
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

    public Boolean userAuthenticated(HttpServletRequest request, JwtToken jwt) {
        String token = jwt.resolveToken(request);
        Boolean authenticated = false;

        if (jwt.validateToken(token))
            authenticated = true;

        return authenticated;
    }

    public ResponseEntity unathorizedTemplate() {
        Map<String, Object> res = new HashMap<>();
        res.put("error", "Unauthorized");
        return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
    }
}
