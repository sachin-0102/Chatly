package com.sachin0102.projects.chatly.Models;

import android.net.Uri;

public class User {

    private String mUserId;
    private String mName;
    private String mPhoneNumber;
    private String mProfileImage;


    public User(){

    }

    public User(String mUserId, String mName, String mPhoneNumber, String mProfileImage) {
        this.mUserId = mUserId;
        this.mName = mName;
        this.mPhoneNumber = mPhoneNumber;
        this.mProfileImage = mProfileImage;
    }

    public void setUserId(String userId){
        mUserId= userId;
    }

    public void setName(String name){
        mName= name;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public void setmProfileImage(String profileImage) {
        mProfileImage = profileImage;
    }

    public String getUserId(){
        return mUserId;
    }

    public String getName(){
        return mName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public String getProfileImage(){
        return mProfileImage;
    }
}
