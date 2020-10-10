package ru.kostya.whatsapp.model;

public class CallList {
    private String userId,userName,date,urlImage,callType;

    public CallList(){

    }

    public CallList(String userId, String userName, String date, String urlImage, String callType) {
        this.userId = userId;
        this.userName = userName;
        this.date = date;
        this.urlImage = urlImage;
        this.callType = callType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }
}
