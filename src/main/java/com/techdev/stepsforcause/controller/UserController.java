package com.techdev.stepsforcause.controller;

import com.techdev.stepsforcause.models.User;
import com.techdev.stepsforcause.models.UserAttributes;
import com.techdev.stepsforcause.routes.Routes;
import com.techdev.stepsforcause.service.UserService;
import com.techdev.stepsforcause.utils.HelperFuncs;
import com.techdev.stepsforcause.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private JwtToken jwtTokenUtil;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService service;

    /*
    //////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// Sample code for email sending /////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    private JavaMailSender javaMailSender;
    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper;
    helper = new MimeMessageHelper(message, true);//true indicates multipart message
        helper.setFrom("StepsForCause@outlook.com"); // <--- THIS IS IMPORTANT
        helper.setSubject("subject");
        helper.setTo("mhenna@aucegypt.edu");
        helper.setText("TEST FROM SPRING", true);//true indicates body is html
        javaMailSender.send(message);
     */

    @RequestMapping(value = Routes.USERS, method = RequestMethod.GET)
    public ResponseEntity getUsers() {
        ResponseEntity responseEntity;
        if (service.getHelperFuncs().userAuthenticated(request, jwtTokenUtil))
            responseEntity = service.getUsers();
        else
            responseEntity = service.getHelperFuncs().unathorizedTemplate();

        return responseEntity;
    }

    @RequestMapping(value = Routes.USERS, method = RequestMethod.POST)
    public ResponseEntity registerUsers(@RequestBody Map<String, Object> body) {
        return service.addUser(body);
    }

    @RequestMapping(value = Routes.USERS + Routes.LOGIN, method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody Map<String, Object> body) {
        return service.authenticateUser(body, jwtTokenUtil);
    }

    @RequestMapping(value = Routes.STEPCOUNT, method = RequestMethod.PUT)
    public ResponseEntity updateStepCount(@RequestBody Map<String, Object> body) {
        ResponseEntity responseEntity;
        if (service.getHelperFuncs().userAuthenticated(request, jwtTokenUtil))
            responseEntity = service.updateStepCount(body);
        else
            responseEntity = service.getHelperFuncs().unathorizedTemplate();

        return responseEntity;
    }

    @RequestMapping(value = Routes.VERIFICATIONCODE, method = RequestMethod.POST)
    public ResponseEntity checkVerificationCode(@RequestBody Map<String, Object> body) {
        ResponseEntity responseEntity;
        responseEntity = service.checkVerificationCode(body);

        return responseEntity;
    }

    public UserService getService() {
        return service;
    }
}
