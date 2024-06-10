package com.example.fyp_ilikethatcoffee;

public class UserAccount {
    private String docKey;
    private String Email;
    private String name;
    private String password;
    private String phone;
    private String userType;
    private String UserName;
    private boolean isEnable;
    private String Image;
    private String DOB;
    private String BIO;

    public UserAccount() {
        // Empty constructor
    }

    // Constructor with email and name and image parameters
    public UserAccount(String Email, String UserName, String Image) {
        this.Email = Email;
        this.UserName = UserName;
        this.Image = Image;
    }

    public UserAccount(String docKey, String Email, String name, String password, String phone, String userType, String UserName, boolean isEnable, String Image, String DOB, String BIO) {
        this.docKey = docKey;
        this.Email = Email;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.userType = userType;
        this.UserName = UserName;
        this.isEnable = isEnable;
        this.Image = Image;
        this.DOB = DOB;
        this.BIO = BIO;
    }

    public UserAccount(String userName, String email, String name, String dob, String bio, String image) {
    }

    public String getDob() {
        return DOB;
    }

    public void setDob(String dob) {
        this.DOB = DOB;
    }

    public String getBio() {
        return BIO;
    }

    public void setBio(String bio) {
        this.BIO = BIO;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String Image) {
        this.Image = Image;
    }

    public String getDocKey() {
        return docKey;
    }


    public void setDocKey(String docKey) {
        this.docKey = docKey;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    // Create getters and setters
    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }
}