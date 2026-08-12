error id: file:///E:/毕设/spingboot/src/main/java/com/example/service/RoadConditionService.java:_empty_/RoadConditionMapper#selectAll#
file:///E:/毕设/spingboot/src/main/java/com/example/service/RoadConditionService.java
empty definition using pc, found symbol in pc: _empty_/RoadConditionMapper#selectAll#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 558
uri: file:///E:/毕设/spingboot/src/main/java/com/example/service/RoadConditionService.java
text:
```scala
package com.example.service;

import com.example.entity.RoadCondition;
import com.example.mapper.RoadConditionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// 道路监控业务层：负责处理道路车流记录、实时统计和趋势统计等业务
@Service
public class RoadConditionService {

    // 注入数据库访问对象，用于操作道路监控记录表
    @Resource
    private RoadConditionMapper roadConditionMapper;

    // 查询全部道路监控记录，可按条件筛选
    public List<RoadCondition> selectAll(RoadCondition roadCondition) {
        return roadConditionMapper.@@selectAll(roadCondition);
    }

    // 根据主键查询单条道路监控记录
    public RoadCondition selectId(Integer id) {
        return roadConditionMapper.selectId(id);
    }

    // 新增道路监控记录
    public void add(RoadCondition roadCondition) {
        // 同一路段同一车牌在 15 分钟内重复出现时，不再重复插入，避免统计数据失真
        Integer count = roadConditionMapper.checkDuplicateWithin15Min(roadCondition.getPlate(), roadCondition.getRoadId());
        if (count > 0) {
            System.out.println("Duplicate plate detected within 15 minutes: " + roadCondition.getPlate());
            return;
        }
        // 未命中重复规则时，才真正写入数据库
        roadConditionMapper.insert(roadCondition);
    }

    // 统计指定道路 15 分钟内的车流量
    public Integer getFlowWithin15Min(Integer roadId) {
        return roadConditionMapper.countFlowWithin15Min(roadId);
    }

    // 查询指定道路 15 分钟内的过车记录明细
    public List<RoadCondition> getRecordsWithin15Min(Integer roadId) {
        return roadConditionMapper.selectRecordsWithin15Min(roadId);
    }

    // 统计指定道路 24 小时内每个时间段的流量，用于趋势图展示
    public List<Map<String, Object>> getFlowWithin24Hours(Integer roadId) {
        return roadConditionMapper.countFlowWithin24Hours(roadId);
    }

    // 修改已有的道路监控记录
    public void update(RoadCondition roadCondition) {
        roadConditionMapper.updateById(roadCondition);
    }

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/RoadConditionMapper#selectAll#