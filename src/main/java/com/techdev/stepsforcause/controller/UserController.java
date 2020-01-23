package com.techdev.stepsforcause.controller;

import com.techdev.stepsforcause.routes.Routes;
import com.techdev.stepsforcause.service.UserService;
import com.techdev.stepsforcause.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private JwtToken jwtTokenUtil;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService service;

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
}
