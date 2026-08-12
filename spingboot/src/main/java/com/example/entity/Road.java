package com.example.entity;

public class Road {
    private Integer id; //道路的id号 具有唯一性
    private String name; //道路名
    private String linkAddress; //用于存储上传视频或图片的链接

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinkaddress() {
        return linkAddress;
    }

    public void setLinkaddress(String linkAddress) {
        this.linkAddress = linkAddress;
    }
}
