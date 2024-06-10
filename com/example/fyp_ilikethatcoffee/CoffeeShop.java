package com.example.fyp_ilikethatcoffee;

public class CoffeeShop {
    private String name;
    private double latitude;
    private double longitude;
    private double rating;
    private String imageURL;
    private String address;

    public CoffeeShop(String name, double latitude, double longitude, double rating, String imageURL, String address) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.imageURL = imageURL;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRating() {
        return rating;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getAddress() {
        return address;
    }
}
