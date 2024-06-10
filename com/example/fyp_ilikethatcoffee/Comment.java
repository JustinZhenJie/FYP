package com.example.fyp_ilikethatcoffee;

public class Comment {
    private String UserName;
    private String CommentInfo;
    private String UserProfilePic;
    private String UserAccountId;
    private String UserType;

    public Comment() {
    }

    public Comment(String UserName, String CommentInfo, String UserProfilePic, String UserAccountId, String UserType) {
        this.UserName = UserName;
        this.CommentInfo = CommentInfo;
        this.UserProfilePic = UserProfilePic;
        this.UserAccountId = UserAccountId;
        this.UserType = UserType;

    }



    public String getUsername() {
        return UserName;
    }

    public void setUsername(String UserName) {
        this.UserName = UserName;
    }

    public String getCommentInfo() {
        return CommentInfo;
    }

    public void setCommentInfo(String CommentInfo) {
        this.CommentInfo = CommentInfo;
    }

    public String getUserProfilePic() {
        return UserProfilePic;
    }
    public void setUserProfilePic(String UserProfilePic) {
        this.UserProfilePic = UserProfilePic;
    }
    public String getUserAccountId() {
        return UserAccountId;
    }

    public void setUserAccountId(String UserAccountId) {
        this.UserAccountId = UserAccountId;
    }

    public String getUserType() {
        return UserType;
    }
    public void setUserType(String UserType) {
        this.UserType = UserType;
    }
}
