package com.example.mapper;

import com.example.entity.Police;
import com.example.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PoliceMapper {
     List<Police> selectAll(Police police);

     @Select("select COUNT(*) from police")    //查找总共有多少的Police
     Integer selectAllCount();

     @Select("select * from police where id = #{id}")
     Police selectId(Integer id);

     void insert(Police police);

     void updateById(Police police);

     @Delete("delete from police where id = #{id}")
     void deleteById(Integer id);

     @Select("select * from police where username = #{username}")
     Police selectByUsername(String username);
}
