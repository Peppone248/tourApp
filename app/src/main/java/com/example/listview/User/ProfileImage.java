package com.example.listview.User;

public class ProfileImage {
    private String url;
    private String key;

    public ProfileImage(){

    }

    public ProfileImage(String url){
        this.url=url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
