package com.techdev.stepsforcause.service;

import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.models.UserAttributes;
import com.techdev.stepsforcause.utils.HelperFuncs;
import com.techdev.stepsforcause.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class UserService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HelperFuncs helperFuncs;

    public HelperFuncs getHelperFuncs() {
        return helperFuncs;
    }

    public ResponseEntity getUsers() {
        Map<String, Object> res = new HashMap<>();
        List<User> users = mongoTemplate.findAll(User.class);
        res.put("users", users);
        return new ResponseEntity(res, HttpStatus.OK);
    }

    public ResponseEntity addUser(Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        HttpStatus status = null;
        try {
            String verificationCode = helperFuncs.getRandomNumberString();
            User u = mongoTemplate.save(new User(String.valueOf(body.get(UserAttributes.FIRSTNAME)),
                    String.valueOf(body.get(UserAttributes.LASTNAME)),
                    String.valueOf(body.get(UserAttributes.EMAIL)),
                    String.valueOf(body.get(UserAttributes.PASSWORD)),
                    verificationCode));


            String emailBody = "Please use this verification code to verify your email is correct in order to be " +
                    "able to log in to StepsForCause.\n" + verificationCode;

            helperFuncs.sendEmail(String.valueOf(body.get(UserAttributes.EMAIL)),
                    "StepsForCause@outlook.com", "Verify account", emailBody);

            status = HttpStatus.OK;
            res.put("user", u);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity(res, status);
    }

    public ResponseEntity authenticateUser(Map<String, Object> body, JwtToken jwt) {
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
                if (!u.isVerified) {
                    res.put("error", "This user is not verified yet");
                    status = HttpStatus.NOT_ACCEPTABLE;
                } else if (!(String.valueOf(body.get(UserAttributes.PASSWORD)).equals(u.password))) {
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

    public Map<String, Object> getUserAndQuery(Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        String email = String.valueOf(body.get(UserAttributes.EMAIL));

        Query q = new Query(where("email").is(email));
        User user = mongoTemplate.findOne(q, User.class);
        res.put("user",  user);
        res.put("query", q);
        return res;
    }

    public ResponseEntity checkVerificationCode(Map<String, Object> body) {
        Map<String, Object> userAndQuery = getUserAndQuery(body);

        User user = (User) userAndQuery.get("user");
        Query q = (Query) userAndQuery.get("query");
        String verificationCode = String.valueOf(body.get(UserAttributes.VERIFICATIONCODE));
        Map<String, Object> res = new HashMap<>();

        HttpStatus status = null;
        try {
            if (user.verificationCode.equals(verificationCode)) {
                Update update = new Update();
                update.set(UserAttributes.ISVERIFIED, true);
                User u = mongoTemplate.findAndModify(q, update, new FindAndModifyOptions().returnNew(true),
                        User.class);
                res.put("user", u);
                status = HttpStatus.OK;
            } else {
                res.put("error", "Verification code is incorrect");
                status = HttpStatus.NOT_ACCEPTABLE;
            }
        } catch (Exception e) {
            res.put("error", e.getMessage());
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity(res, status);
    }

    public ResponseEntity updateStepCount(Map<String, Object> body) {
        Map<String, Object> userAndQuery = getUserAndQuery(body);
        User user = (User) userAndQuery.get("user");
        Query q = (Query) userAndQuery.get("query");
        Integer stepCount = Integer.valueOf(String.valueOf(body.get(UserAttributes.STEPCOUNT)));
        Map<String, Object> res = new HashMap<>();
        HttpStatus status = null;
        try {
            Update update = new Update();
            update.set(UserAttributes.STEPCOUNT, stepCount);
            User u = mongoTemplate.findAndModify(q, update, new FindAndModifyOptions().returnNew(true), User.class);
            res.put("user", u);
            status = HttpStatus.OK;
        } catch (Exception e) {
            res.put("error", e.getMessage());
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity(res, status);
    }
}
