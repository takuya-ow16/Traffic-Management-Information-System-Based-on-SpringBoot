error id: file:///E:/毕设/spingboot/src/main/java/com/example/service/RoadConditionService.java:_empty_/RoadConditionMapper#countFlowWithin15Min#
file:///E:/毕设/spingboot/src/main/java/com/example/service/RoadConditionService.java
empty definition using pc, found symbol in pc: _empty_/RoadConditionMapper#countFlowWithin15Min#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1169
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

@Service
public class RoadConditionService {

    @Resource
    private RoadConditionMapper roadConditionMapper;

    public List<RoadCondition> selectAll(RoadCondition roadCondition) {
        return roadConditionMapper.selectAll(roadCondition);
    }

    public RoadCondition selectId(Integer id) {
        return roadConditionMapper.selectId(id);
    }

    public void add(RoadCondition roadCondition) {
        // Check for duplicates within 15 minutes for the same road
        Integer count = roadConditionMapper.checkDuplicateWithin15Min(roadCondition.getPlate(), roadCondition.getRoadId());
        if (count > 0) {
            System.out.println("Duplicate plate detected within 15 minutes: " + roadCondition.getPlate());
            return;
        }
        roadConditionMapper.insert(roadCondition);
    }

    public Integer getFlowWithin15Min(Integer roadId) {
        return roadConditionMapper.countFlow@@Within15Min(roadId);
    }

    public List<RoadCondition> getRecordsWithin15Min(Integer roadId) {
        return roadConditionMapper.selectRecordsWithin15Min(roadId);
    }

    public List<Map<String, Object>> getFlowWithin24Hours(Integer roadId) {
        return roadConditionMapper.countFlowWithin24Hours(roadId);
    }

    public void update(RoadCondition roadCondition) {
        roadConditionMapper.updateById(roadCondition);
    }

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/RoadConditionMapper#countFlowWithin15Min#