package com.havrylyuk.tvapp.model;

import com.google.gson.annotations.SerializedName;
        /*{"id":1,
        "title":"Общеукраинские",
        "picture":"http://1804.biz/sites/default/files/styles/large/public/tv.png,qitok=bJm78VaG.pagespeed.ce.wu3JNuzhRr.png"}*/
/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */

public class TvCategory {

    @SerializedName("id")
    private long id;
    @SerializedName("title")
    private String title;
    @SerializedName("picture")
    private String picture;

    public TvCategory() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
