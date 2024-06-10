package com.example.fyp_ilikethatcoffee;

import com.google.firebase.firestore.auth.User;

public class Store {
    private String StoreAddress;
    private String StoreDesc;
    private String StoreName;
    private String UserAccountId;
    private int reviewCount;
    private double averageRating;
    private String ImageUrl;

    // Default constructor needed for Firestore
    public Store() {}

    // Constructor without ImageUrl
    public Store(String StoreAddress, String StoreDesc, String StoreName, String UserAccountId) {
        this.StoreAddress = StoreAddress;
        this.StoreDesc = StoreDesc;
        this.StoreName = StoreName;
        this.UserAccountId = UserAccountId;
        this.reviewCount = 0;
        this.averageRating = 0.0;
    }

    // Constructor with all fields
    public Store(String StoreAddress, String StoreDesc, String StoreName, String ImageUrl, String UserAccountId) {
        this.StoreAddress = StoreAddress;
        this.StoreDesc = StoreDesc;
        this.StoreName = StoreName;
        this.UserAccountId = UserAccountId;
        this.reviewCount = 0;
        this.averageRating = 0.0;
        this.ImageUrl = ImageUrl;
    }

    // Getters and setters
    public String getStoreAddress() { return StoreAddress; }
    public void setStoreAddress(String storeAddress) { this.StoreAddress = storeAddress; }

    public String getStoreDesc() { return StoreDesc; }
    public void setStoreDesc(String storeDesc) { this.StoreDesc = storeDesc; }

    public String getStoreName() { return StoreName; }
    public void setStoreName(String storeName) { this.StoreName = storeName; }

    public String getUserAccountId() { return UserAccountId; }
    public void setUserAccountId(String userAccountId) { this.UserAccountId = userAccountId; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public String getImageUrl() { return ImageUrl; }
    public void setImageUrl(String imageUrl) { this.ImageUrl = imageUrl; }
}
