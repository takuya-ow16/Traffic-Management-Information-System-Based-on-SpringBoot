package com.example.controller;//package com.example.controller;

import com.example.common.Result;
import com.example.entity.Car;
import com.example.service.CarService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/car")
public class CarController {
    @Resource
    private CarService carService;

    @GetMapping("/selectAll")
    public Result selectAll(Car car){
        List<Car> list = carService.selectAll(car);
        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectId(@PathVariable Integer id){
        Car car = carService.selectId(id);
        return Result.success(car);
    }

    @GetMapping("/selectOne")
    public Result selectOne(@RequestParam Integer id, @RequestParam String no){
        Car car = carService.selectId(id);
        return Result.success(car);
    }

    //分页查询
    //pageNum当前页码
    //pageSize每页个数
    @GetMapping("/selectPage")
    public Result selectPage(Car car,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Car> pageInfo = carService.selectPage(car, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @PostMapping("/addPol")
    public Result addPol(@RequestBody Car car){
        carService.addPol(car);
        return Result.success();
    }


    @PostMapping("/add")
    public Result add(@RequestBody Car car){
        carService.add(car);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Car car){
        carService.update(car);
        return Result.success();
    }

    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id){
        carService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        carService.deleteBatch(ids);
        return Result.success();
    }
}
