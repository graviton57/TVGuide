package com.havrylyuk.tvapp.model;

import com.google.gson.annotations.SerializedName;

/*
{       "id":1,
        "name":"Украина",
        "url":"http://oll.tv/tv-channels",
        "picture":"http://s1.ollcdn.net/i/61/04/95/610495_ukraine.png",
        "category_id":8
*/

/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */

public class TvChannel {

    @SerializedName("id")
    private long id;
    @SerializedName("name")
    private String name;
    @SerializedName("url")
    private String url;
    @SerializedName("picture")
    private String picture;
    @SerializedName("category_id")
    private String categoryId;

    public TvChannel() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
