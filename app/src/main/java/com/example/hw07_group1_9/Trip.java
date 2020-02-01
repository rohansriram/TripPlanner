package com.example.hw07_group1_9;

import java.util.ArrayList;


public class Trip {

    String tripID;
    String tripAdminID;
    String tripTitle;
    String imageURL;
    String latitude;
    String longitude;
    ArrayList<String> tripMembers = new ArrayList<>();


    public ArrayList<String> getTripMembers() {
        return tripMembers;
    }

    public void setTripMembers(ArrayList<String> tripMembers) {
        this.tripMembers = tripMembers;
    }


    public Trip() {
    }

    public Trip(String tripID, String tripAdminID, String tripTitle, String imageURL, String latitude, String longitude, ArrayList<String> tripMembers) {
        this.tripID = tripID;
        this.tripAdminID = tripAdminID;
        this.tripTitle = tripTitle;
        this.imageURL = imageURL;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tripMembers = tripMembers;
    }

    public String getTripAdminID() {
        return tripAdminID;
    }

    public void setTripAdminID(String tripAdminID) {
        this.tripAdminID = tripAdminID;
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public void setTripTitle(String tripTitle) {
        this.tripTitle = tripTitle;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }


    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }
}
