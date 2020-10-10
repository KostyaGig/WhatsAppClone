package ru.kostya.whatsapp.model;

public class ChatList {
    private String userId,userName,description,date,urlImage;

    public ChatList(){

    }

    public ChatList(String userId, String userName, String description, String date, String urlImage) {
        this.userId = userId;
        this.userName = userName;
        this.description = description;
        this.date = date;
        this.urlImage = urlImage;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
