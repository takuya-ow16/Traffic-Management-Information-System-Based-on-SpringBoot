package com.example.service;
import com.example.entity.Car;
import com.example.exception.CustomException;
import com.example.mapper.CarMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {

    @Resource
    private CarMapper carMapper;

    //查询所有车辆信息
    public List<Car> selectAll(Car car) {
        return carMapper.selectAll(car);
    }

    //根据车主身份证号分页查询车辆信息
    public PageInfo<Car> selectPageByOwnerID(Car car,
            Integer pageNum,
            Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Car> list = carMapper.selectByOwnerID(car);
        return PageInfo.of(list);
    }

    //根据车辆ID查询车辆信息
    public Car selectId(Integer id) {
        return carMapper.selectId(id);
    }

    //分页查询所有车辆信息
    public PageInfo<Car> selectPage(Car car,
            Integer pageNum,
            Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Car> list = carMapper.selectAll(car);
        return PageInfo.of(list);
    }

    //交警添加车辆
    public void addPol(Car car) {
        String plate = car.getPlate();
        Car dbCar = carMapper.selectByPlate(plate);
        if (dbCar != null) {
            throw new CustomException("500", "车辆已存在");
        }
        car.setStatus("NOR");   //交警增加则直接为正常，不需要确认
        car.setMarkTime(cn.hutool.core.date.DateUtil.today());
        carMapper.insert(car);
    }

    //用户申请添加车辆
    public void add(Car car) {
        String plate = car.getPlate();
        Car dbCar = carMapper.selectByPlate(plate);
        if (dbCar != null) {
            throw new CustomException("500", "车辆已存在");
        }
        car.setStatus("TBC"); // 状态设为待确认 (To Be Confirmed)
        car.setMarkTime(cn.hutool.core.date.DateUtil.today());
        carMapper.insert(car);
    }

    //更新车辆信息
    public void update(Car car) {
        carMapper.updateById(car);
    }

    //根据ID删除车辆
    public void deleteById(Integer id) {
        Car car = carMapper.selectId(id);
        if (car == null) {
            System.out.println("删除失败：ID " + id + " 在数据库中不存在。");
        } else {
            System.out.println("正在删除员工：ID=" + car.getId() + ", Name=" + car.getPlate());
        }
        carMapper.deleteById(id);
    }

    //批量删除车辆
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            carMapper.deleteById(id);
        }
    }

}
