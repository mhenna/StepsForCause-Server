package com.techdev.stepsforcause.models;

import org.springframework.data.annotation.Id;

public class User {

    @Id
    public String id;

    public String firstName;
    public String lastName;
    public String email;
    public Integer stepCount;
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
