package com.example.lenovo.chatactivity;

import android.graphics.Bitmap;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * Created by Lenovo on 2017/12/3.
 */

public class MsgLog extends DataSupport {
    @Column(unique=true)
    private int id;
    private String sender;
    private String receiver;
    private int type;//类型
    private String time;//时间
    private String context;//保存字符串
    private Bitmap picture;//保存图片

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
