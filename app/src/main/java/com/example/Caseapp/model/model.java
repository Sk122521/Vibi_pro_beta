package com.example.Caseapp.model;

public class model {

    private String title;
    private String description;
    private String status;

    private String image;

    private String key ;

    public model(){

    }

    public model(String title, String description, String status, String image, String key) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.image = image;
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
