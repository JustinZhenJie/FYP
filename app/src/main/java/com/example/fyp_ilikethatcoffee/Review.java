package com.example.fyp_ilikethatcoffee;

import java.util.ArrayList;
import java.util.List;

public class Review {
    private Long ReviewId; // Assuming the ID is stored as a Long in Firestore
    private String ReviewDesc;
    private float Rating;
    private String UserName;
    private String Image;
    private List<Comment> comments = new ArrayList<>();
    private String UserAccountId;
    private String UserProfilePic;
    private String StoreName;
    String StoreId;

    // No-argument constructor needed for Firestore deserialization
    public Review() {
    }

    public Review(Long reviewId, String reviewDesc, float rating, String userName, String image, List<Comment> comments, String userAccountId, String userProfilePic, String storeName, String storeId) {
        ReviewId = reviewId;
        ReviewDesc = reviewDesc;
        Rating = rating;
        UserName = userName;
        Image = image;
        this.comments = comments;
        UserAccountId = userAccountId;
        UserProfilePic = userProfilePic;
        StoreName = storeName;
        StoreId = storeId;
    }

    public String getStoreId() {
        return StoreId;
    }

    public void setStoreId(String storeId) {
        StoreId = storeId;
    }

    // Getters and setters
    public Long getReviewId() {
        return ReviewId;
    }

    public void setReviewId(Long ReviewId) {
        this.ReviewId = ReviewId;
    }

    public String getReviewDesc() {
        return ReviewDesc;
    }

    public void setReviewDesc(String ReviewDesc) {
        this.ReviewDesc = ReviewDesc;
    }

    public float getRating() {
        return Rating;
    }

    public void setRating(float Rating) {
        this.Rating = Rating;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String Image) {
        this.Image = Image;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getUserAccountId() {
        return UserAccountId;
    }

    public void setUserAccountId(String UserAccountId) {
        this.UserAccountId = UserAccountId;
    }


    public String getUserProfilePic() {
        return UserProfilePic;
    }

    public void setUserProfilePic(String userProfilePic) {
        this.UserProfilePic = userProfilePic;
    }

    public String getStoreName() {
        return StoreName;
    }

    public void setStoreName(String storeName) {
        StoreName = storeName;
    }

}
