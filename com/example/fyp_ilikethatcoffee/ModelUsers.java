package com.example.fyp_ilikethatcoffee;

public class ModelUsers {
    String Name;
    String Email;
    String Username;

    String useraccountid;

    public ModelUsers() {
    }

    public String getName() {

        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        this.Username = username;
    }


    public String getUid() {
        return useraccountid;
    }

    public void setUid(String useraccountid) {
        this.useraccountid = useraccountid;
    }

    public ModelUsers(String name, String email, String username, String useraccountid) {
        this.Name = name;
        this.Email = email;
        this.Username = username;
        this.useraccountid = useraccountid;

    }




}

