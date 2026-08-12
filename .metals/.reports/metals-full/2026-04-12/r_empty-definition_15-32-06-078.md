error id: file:///E:/毕设/spingboot/src/main/java/com/example/controller/AppealedController.java:_empty_/AppealedService#selectId#
file:///E:/毕设/spingboot/src/main/java/com/example/controller/AppealedController.java
empty definition using pc, found symbol in pc: _empty_/AppealedService#selectId#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 570
uri: file:///E:/毕设/spingboot/src/main/java/com/example/controller/AppealedController.java
text:
```scala
package com.example.controller;

import com.example.common.Result;
import com.example.entity.Appealed;
import com.example.service.AppealedService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appealed")
public class AppealedController {
    @Resource
    private AppealedService appealedService;

    @GetMapping("/selectById/{id}")
    public Result selectId(@PathVariable Integer id){
        Appealed appealed = appealedService.@@selectId(id);
        return Result.success(appealed);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Appealed appealed){
        appealedService.add(appealed);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Appealed appealed){
        appealedService.update(appealed);
        return Result.success();
    }

    @PutMapping("/reject")
    public Result reject(@RequestBody Appealed appealed){
        appealedService.reject(appealed);
        return Result.success();
    }

    @PutMapping("/pass/{id}")
    public Result pass(@PathVariable Integer id){
        appealedService.pass(id);
        return Result.success();
    }

    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id){
        appealedService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        appealedService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) Integer violationId,
                             @RequestParam(required = false) String violationStatus) {
        Appealed appealed = new Appealed();
        appealed.setViolationId(violationId);
        appealed.setViolationStatus(violationStatus);
        PageInfo<Appealed> pageInfo = appealedService.selectPage(appealed, pageNum, pageSize);
        return Result.success(pageInfo);
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/AppealedService#selectId#