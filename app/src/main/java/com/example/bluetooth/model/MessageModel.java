package com.example.bluetooth.model;

public class MessageModel {
    String msg,msgType;

    public MessageModel(String msg, String msgType) {
        this.msg = msg;
        this.msgType = msgType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
}
