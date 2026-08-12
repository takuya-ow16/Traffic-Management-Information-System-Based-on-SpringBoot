package com.example.mapper;

import com.example.entity.Violation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ViolationMapper {
     List<Violation> selectAll(Violation violation);

     @Select("select COUNT(*) from violation")    //查找总共有多少的违章单
     Integer selectAllCount();

     @Select("select * from violation where id = #{id}")
     Violation selectId(Integer id);

     void insert(Violation violation);

     void updateById(Violation violation);

     @Delete("delete from violation where id = #{id}")
     void deleteById(Integer id);

     @Select("select count(*) from violation where plate = #{plate}")
     Integer selectByPlateCount(String plate);

     @Select("select count(*) from violation where plate = #{plate} and (status = 'UNT' or status = 'AED')")
     Integer selectByPlateCountSQU(String plate);

     @Select("select COUNT(*) from violation where ownerid = #{ownerId}")
     Integer selectByOnweridCount(String onwerId);

     @Select("select COUNT(*) from violation where ownerid = #{ownerId} and (status = 'UNT' or status = 'AED')")
     Integer selectByOnweridCountSQU(String onwerId);

     List<Violation> selectByOwnerID(Violation violation);
}
