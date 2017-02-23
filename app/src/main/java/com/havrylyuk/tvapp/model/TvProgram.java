package com.havrylyuk.tvapp.model;

import com.google.gson.annotations.SerializedName;

/*{
        "channel_id":1,
        "date":"18/01/1970",
        "time":"05:10",
        "title":"Агенти справедливості",
        "description":"Агенти справедливості"}*/
/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */

public class TvProgram {

    @SerializedName("channel_id")
    private long channelId;

    @SerializedName("date")
    private String date;

    @SerializedName("time")
    private String time;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    public TvProgram() {
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
}
