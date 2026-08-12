package com.example.mapper;

import com.example.entity.Car;
import com.example.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CarMapper {
     List<Car> selectAll(Car car);

     @Select("select * from car where id = #{id}")
     Car selectId(Integer id);

     void insert(Car car);

     void updateById(Car car);

     @Delete("delete from car where id = #{id}")
     void deleteById(Integer id);

     @Select("select * from car where plate = #{plate}")
     Car selectByPlate(String plate);

     List<Car> selectByOwnerID(Car car);
}
