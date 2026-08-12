package com.example.mapper;

import com.example.entity.Admin;
import com.example.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AdminMapper {
     List<Admin> selectAll(Admin admin);

     @Select("select COUNT(*) from admin")    //查找总共有多少的USER
     Integer selectAllCount();

     @Select("select * from admin where id = #{id}")
     Admin selectId(Integer id);

     void insert(Admin admin);

     void updateById(Admin admin);

     @Delete("delete from admin where id = #{id}")
     void deleteById(Integer id);

     @Select("select * from admin where username = #{username}")
     Admin selectByUsername(String username);
}
