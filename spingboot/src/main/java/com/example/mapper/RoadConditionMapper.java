package com.example.mapper;

import com.example.entity.RoadCondition;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface RoadConditionMapper {
     List<RoadCondition> selectAll(RoadCondition roadCondition);

     @Select("select * from roadCondition where id = #{id}")
     RoadCondition selectId(Integer id);

     @Select("select count(*) from roadcondition where plate = #{plate} and roadid = #{roadId} and time > DATE_SUB(NOW(), INTERVAL 15 MINUTE)")
     Integer checkDuplicateWithin15Min(@Param("plate") String plate, @Param("roadId") Integer roadId);

     @Select("select count(*) from roadcondition where roadid = #{roadId} and time > DATE_SUB(NOW(), INTERVAL 15 MINUTE)")
     Integer countFlowWithin15Min(Integer roadId);

     @Select("select * from roadcondition where roadid = #{roadId} and time > DATE_SUB(NOW(), INTERVAL 15 MINUTE) order by time desc")
     List<RoadCondition> selectRecordsWithin15Min(Integer roadId);

     @Select("SELECT DATE_FORMAT(time, '%H') as hour, COUNT(*) as count FROM roadcondition WHERE roadid = #{roadId} AND time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) GROUP BY DATE_FORMAT(time, '%H')")
     List<Map<String, Object>> countFlowWithin24Hours(Integer roadId);

     void insert(RoadCondition roadCondition);

     void updateById(RoadCondition roadCondition);

     @Delete("delete from roadcondition where id = #{id}")
     void deleteById(Integer id);

     @Delete("delete from roadcondition where roadid = #{roadId}")
     void deleteByRoadId(Integer roadId);
}
