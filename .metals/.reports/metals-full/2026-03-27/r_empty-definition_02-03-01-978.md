error id: file:///E:/毕设/spingboot/src/main/java/com/example/controller/RoadConditionController.java:_empty_/RoadConditionService#getFlowWithin15Min#
file:///E:/毕设/spingboot/src/main/java/com/example/controller/RoadConditionController.java
empty definition using pc, found symbol in pc: _empty_/RoadConditionService#getFlowWithin15Min#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1427
uri: file:///E:/毕设/spingboot/src/main/java/com/example/controller/RoadConditionController.java
text:
```scala
package com.example.controller;//package com.example.controller;

import com.example.common.Result;
import com.example.entity.RoadCondition;
import com.example.service.RoadConditionService;
import com.example.service.RoadService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roadCondition")
public class RoadConditionController {
    @Resource
    private RoadConditionService roadConditionService;

    @GetMapping("/selectAll")
    public Result selectAll(RoadCondition roadCondition){
        List<RoadCondition> list = roadConditionService.selectAll(roadCondition);
        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectId(@PathVariable Integer id){
        RoadCondition roadCondition = roadConditionService.selectId(id);
        return Result.success(roadCondition);
    }

    @PostMapping("/add")
    public Result add(@RequestBody RoadCondition roadCondition){
        System.out.println("RoadCondition Add Request Received: RoadId=" + roadCondition.getRoadId() + ", Plate=" + roadCondition.getPlate());
        roadConditionService.add(roadCondition);
        return Result.success();
    }

    @GetMapping("/getFlow15Min/{roadId}")
    public Result getFlow15Min(@PathVariable Integer roadId) {
        Integer flow = roadConditionService.getF@@lowWithin15Min(roadId);
        return Result.success(flow);
    }

    @GetMapping("/getRecords15Min/{roadId}")
    public Result getRecords15Min(@PathVariable Integer roadId) {
        List<RoadCondition> list = roadConditionService.getRecordsWithin15Min(roadId);
        return Result.success(list);
    }

    @GetMapping("/getFlow24Hours/{roadId}")
    public Result getFlow24Hours(@PathVariable Integer roadId) {
        List<Map<String, Object>> list = roadConditionService.getFlowWithin24Hours(roadId);
        return Result.success(list);
    }

    @PutMapping("/update")
    public Result update(@RequestBody RoadCondition roadCondition){
        roadConditionService.update(roadCondition);
        return Result.success();
    }

//    @DeleteMapping("/deleteById/{id}")
//    public Result deleteById(@PathVariable Integer id){
//        roadService.deleteById(id);
//        return Result.success();
//    }


}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/RoadConditionService#getFlowWithin15Min#