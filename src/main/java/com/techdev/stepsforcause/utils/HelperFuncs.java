package com.techdev.stepsforcause.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class HelperFuncs {

    @Autowired
    private JavaMailSender javaMailSender;

    private MimeMessage message;
    private MimeMessageHelper helper;

    public String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    public void sendEmail(String to, String from, String subject, String body) {
        message = javaMailSender.createMimeMessage();

        try {
            helper = new MimeMessageHelper(message, true);//true indicates multipart message
            helper.setFrom(from); // <--- THIS IS IMPORTANT
            helper.setSubject(subject);
            helper.setTo(to);
            helper.setText(body, true);//true indicates body is html
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public ResponseEntity unathorizedTemplate() {
        Map<String, Object> res = new HashMap<>();
        res.put("error", "Unauthorized");
        return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
    }

    public Boolean userAuthenticated(HttpServletRequest request, JwtToken jwt) {
        String token = jwt.resolveToken(request);
        Boolean authenticated = false;

        if (jwt.validateToken(token))
            authenticated = true;

        return authenticated;
    }
}
