package com.example.hw07_group1_9;

import java.io.Serializable;

public class UserProfile implements Serializable {

    String firstname;
    String lastname;
    String userID;
    String gender;
    String image;

    public UserProfile() {
    }

    public UserProfile(String firstname, String lastname, String userID, String gender, String image) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.userID = userID;
        this.gender = gender;
        this.image = image;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
