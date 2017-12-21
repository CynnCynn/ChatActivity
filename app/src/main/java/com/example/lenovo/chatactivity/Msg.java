package com.example.lenovo.chatactivity;

import android.graphics.Bitmap;

/**
 * Created by Lenovo on 2017/11/10.
 */

public class Msg {
    public static final int TYPE_RECEIVED=0;//接收
    public static final int TYPE_SEND=1;//发送
    public static final int TYPE_SEND_IMAGE=2;//发送图片
    public static final int TYPE_RECEIVED_IMAGE=3;//接收图片
    private String content;
    private Bitmap picture;
    private int type;
    public Msg(String content,int type){
        this.content=content;
        this.type=type;
    }
    public Msg(Bitmap picture,int type){
        this.picture=picture;
        this.type=type;
    }
    public String getContent(){
        return content;
    }
    public int getType(){
        return type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public void setType(int type) {
        this.type = type;
    }
    //    public static Msg createImageMessage(int type, String pathName){
//
//        return Msg;
//
////        this.content=content;
////        this.type=type;
////        this.picture=picture;
//    }
}
