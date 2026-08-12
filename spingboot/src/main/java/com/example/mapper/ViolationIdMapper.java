package com.example.mapper;


import com.example.entity.TodayRegister;
import com.example.entity.ViolationId;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ViolationIdMapper {
     @Select("select * from violationid")       //查询查询到所有的违章代码信息
     public List<ViolationId> selectAll();

     @Select("select * from violationid where id = #{id}")       //通过违章代码查询到违章信息
     public ViolationId selectAllById(String id);
}
