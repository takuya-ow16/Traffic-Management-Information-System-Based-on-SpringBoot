package com.example.mapper;

import com.example.entity.Appealed;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AppealedMapper {

    /**
     * 新增申诉
     */
    void insert(Appealed appealed);

    /**
     * 删除申诉
     */
    void deleteById(Integer id);

    /**
     * 更新申诉
     */
    void updateById(Appealed appealed);

    /**
     * 根据ID查询
     */
    @Select("select *, id as violationId from appealed where id = #{id}")
    Appealed selectById(Integer id);

    /**
     * 根据违章ID查询
     */
    @Select("select *, id as violationId from appealed where id = #{violationId}")
    Appealed selectByViolationId(Integer violationId);

    /**
     * 查询所有申诉（支持条件查询）
     */
    List<Appealed> selectAll(Appealed appealed);
}
