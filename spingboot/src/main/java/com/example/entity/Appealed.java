package com.example.entity;

public class Appealed {
    private Integer id;
    private Integer violationId; // 关联违章单ID
    private String content;      // 申诉内容
    private String reply;        // 回复内容

    // 临时字段，不存储到appealed表，用于传递违章单的状态变更
    private String violationStatus; 

    // 关联查询字段
    private String userName;
    private String violationPlate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getViolationId() {
        return violationId;
    }

    public void setViolationId(Integer violationId) {
        this.violationId = violationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getViolationStatus() {
        return violationStatus;
    }

    public void setViolationStatus(String violationStatus) {
        this.violationStatus = violationStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getViolationPlate() {
        return violationPlate;
    }

    public void setViolationPlate(String violationPlate) {
        this.violationPlate = violationPlate;
    }
}
