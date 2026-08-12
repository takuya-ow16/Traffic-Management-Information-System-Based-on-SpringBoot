package com.example.controller;//package com.example.controller;

import com.example.common.Result;
import com.example.entity.Road;
import com.example.service.RoadService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/road")
public class RoadController {
    @Resource
    private RoadService roadService;

    @GetMapping("/selectAll")
    public Result selectAll(Road road){
        List<Road> list = roadService.selectAll(road);
        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectId(@PathVariable Integer id){
        Road road = roadService.selectId(id);
        return Result.success(road);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Road road){
        roadService.add(road);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Road road){
        roadService.update(road);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        roadService.delete(id);
        return Result.success();
    }


}
