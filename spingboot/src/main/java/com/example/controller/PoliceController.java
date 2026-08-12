package com.example.controller;//package com.example.controller;

import com.example.common.Result;
import com.example.entity.Police;
import com.example.service.PoliceService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/police")
public class PoliceController {
    @Resource
    private PoliceService policeService;

    @GetMapping("/selectAll")
    public Result selectAll(Police police){
        List<Police> list = policeService.selectAll(police);
        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectId(@PathVariable Integer id){
        Police police = policeService.selectId(id);
        return Result.success(police);
    }

    @GetMapping("/selectOne")
    public Result selectOne(@RequestParam Integer id, @RequestParam String no){
        Police police = policeService.selectId(id);
        return Result.success(police);
    }

    //分页查询
    //pageNum当前页码
    //pageSize每页个数
    @GetMapping("/selectPage")
    public Result selectPage(Police police,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Police> pageInfo = policeService.selectPage(police, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Police police){
        policeService.add(police);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Police police){
        policeService.update(police);
        return Result.success();
    }

    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id){
        policeService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        policeService.deleteBatch(ids);
        return Result.success();
    }
}
