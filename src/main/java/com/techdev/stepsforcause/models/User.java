package com.techdev.stepsforcause.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

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

    public Boolean isVerified;

    public User() {}

    public User(String fn, String ln, String email, String password, String verificationCode) {
        this.firstName = fn;
        this.lastName = ln;
        this.email = email;
        this.password = password;
        this.verificationCode = verificationCode;
        this.stepCount = 0;
        this.isVerified = false;
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
