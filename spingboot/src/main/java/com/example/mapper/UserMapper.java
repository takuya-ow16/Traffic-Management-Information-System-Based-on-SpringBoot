package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {
     List<User> selectAll(User user);

     @Select("select COUNT(*) from user")    //查找总共有多少的USER
     Integer selectAllCount();

     @Select("select * from user where id = #{id}")
     User selectId(Integer id);

     void insert(User user);

     void updateById(User user);

     @Delete("delete from user where id = #{id}")
     void deleteById(Integer id);

     @Select("select * from user where username = #{username}")
     User selectByUsername(String username);

     @Select("select * from user where id_card = #{idCard}")
     User selectByIdCar(String username);
}
