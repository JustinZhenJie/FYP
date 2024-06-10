package com.example.fyp_ilikethatcoffee.Posts;

import com.google.firebase.Timestamp;

public class Post {
    private String PostInfo;
    private String UserAccountId;
    private Timestamp DateCreated;
    //private String storeName;
    private String PostId;
    private String Image;

    public Post(){

    }
    public Post(String postInfo, String userAccountId, Timestamp dateCreated, String Image){
        this.PostInfo = postInfo;
        this.UserAccountId = userAccountId;
        this.DateCreated = dateCreated;
        this.Image = Image;
    }

    public String getUserAccountId(){
        return UserAccountId;
    }

    /*
    public String getProfileUrl() {
        return profileUrl;
    }

     */

    public String getPostInfo() {
        return PostInfo;
    }

    public Timestamp getDateCreated() {
        return DateCreated;
    }

    /*
    public String getStoreName(){return storeName;}


     */
    public String getImage() {
        return Image;
    }


    public void setPostInfo(String postInfo) {
        this.PostInfo = postInfo;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.DateCreated = dateCreated;
    }

    /*
    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

     */

    public void setUserAccountId(String userAccountId) {
        this.UserAccountId = userAccountId;
    }

    /*
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

     */

    public void setImage(String image) {
        this.Image = image;
    }


}
