package com.example.entity;

public class RoadCondition {
    private Integer id;
    private Integer roadId;     //用于记录道路的id
    private String plate;   //用于记录通过这个道路的车辆的车牌号
    private String time;    //用于记录车辆路过这条道路时的时间

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRoadId() {
        return roadId;
    }

    public void setRoadId(Integer roadId) {
        this.roadId = roadId;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
