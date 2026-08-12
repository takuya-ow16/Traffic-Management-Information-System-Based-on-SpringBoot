package com.example.entity;

public class Violation {    //违章单
    Integer id;     //违章单id
    String plate;   //违章的车牌号
    String ownerId; //车主身份证
    String time;    //违章时间
    String address; //违章地点
    String violationid; //违章代码
    String image; //违章图片
    String status;  //违章单的状态 举报中（UND） 未处理（UNT） 已支付（HPD） 已处理（PRO） 已经申诉(AED) 申诉成功(PAS)
    String infoid; // 举报人身份证

    private String ownerName;
    private String violationName;
    private Integer violationMoney;
    private Integer violationPoints;

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getViolationName() {
        return violationName;
    }

    public void setViolationName(String violationName) {
        this.violationName = violationName;
    }

    public Integer getViolationMoney() {
        return violationMoney;
    }

    public void setViolationMoney(Integer violationMoney) {
        this.violationMoney = violationMoney;
    }

    public Integer getViolationPoints() {
        return violationPoints;
    }

    public void setViolationPoints(Integer violationPoints) {
        this.violationPoints = violationPoints;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getViolationid() {
        return violationid;
    }

    public void setViolationid(String violationid) {
        this.violationid = violationid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInfoid() {
        return infoid;
    }

    public void setInfoid(String infoid) {
        this.infoid = infoid;
    }
}
