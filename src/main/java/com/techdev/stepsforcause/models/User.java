package com.techdev.stepsforcause.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "user")
public class User {

    @Id
    public String id;

    @NotEmpty(message = "First name cannot be empty")
    public String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    public String lastName;

    @Indexed(unique = true)
    @Email
    public String email;

    public Integer stepCount;

    @NotEmpty(message = "Password cannot be empty")
    public String password;

    public String verificationCode;

    public User() {}

    public User(String fn, String ln, String email, String password, String verificationCode) {
        this.firstName = fn;
        this.lastName = ln;
        this.email = email;
        this.password = password;
        this.verificationCode = verificationCode;
        this.stepCount = 0;
    }

    public ResponseEntity updateStepCount(MongoTemplate mongoTemplate, Integer stepCount, Query q) {
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

    public ResponseEntity updateVerificationCode(MongoTemplate mongoTemplate, String verificationCode, Query q) {
        Map<String, Object> res = new HashMap<>();
        HttpStatus status = null;
        try {
            Update update = new Update();
            update.set(UserAttributes.VERIFICATIONCODE, verificationCode);
            User u = mongoTemplate.findAndModify(q, update, new FindAndModifyOptions().returnNew(true), User.class);
            res.put("user", u);
            status = HttpStatus.OK;
        } catch (Exception e) {
            res.put("error", e.getMessage());
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity(res, status);
    }

    public String toString() {
        return String.format(
                "User: {\n" +
                        "\tid: %s,\n" +
                        "\tfirstName: %s,\n" +
                        "\tlastName: %s,\n" +
                        "\temail: %s\n" +
                        "}",
                id, firstName, lastName, email
        );
    }
}
