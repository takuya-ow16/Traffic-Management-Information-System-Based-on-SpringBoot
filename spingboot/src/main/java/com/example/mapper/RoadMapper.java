package com.example.mapper;

import com.example.entity.Road;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoadMapper {
     List<Road> selectAll(Road road);

     @Select("select * from road where id = #{id}")
     Road selectId(Integer id);

     void insert(Road road);

     void updateById(Road road);

     @Delete("delete from road where id = #{id}")
     void deleteById(Integer id);
}
