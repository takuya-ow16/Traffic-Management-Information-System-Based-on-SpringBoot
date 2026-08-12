package com.example.mapper;


import com.example.entity.TodayRegister;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TodayRegisterMapper {
     @Select("select * from todayregister order by date desc")       //查询到所有的数据
     public List<TodayRegister> selectAll();

     @Select("select * from todayregister where date = #{date}")     //根据日期查询到今日所注册人数的数据
     public TodayRegister selectBydate(String date);

     @Insert("insert into todayregister (date, number) values (#{date}, #{number})")      //插入数据，用于重新写入人数
     public void insert(TodayRegister todayRegister);

     @Insert("update todayregister set number = #{number} where date = #{date}")
     public void update(TodayRegister todayRegister);
}
