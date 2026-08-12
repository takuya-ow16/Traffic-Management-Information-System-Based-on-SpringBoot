package com.example.controller;//package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Car;
import com.example.entity.User;
import com.example.entity.Violation;
import com.example.service.CarService;
import com.example.service.UserService;
import com.example.service.ViolationService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private CarService carService;

    @Resource
    private ViolationService violationService;

    @GetMapping("/selectAll")
    public Result selectAll(User user) {
        List<User> list = userService.selectAll(user);
        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectId(@PathVariable Integer id) {
        User user = userService.selectId(id);
        return Result.success(user);
    }

    @GetMapping("/selectOne")
    public Result selectOne(@RequestParam Integer id, @RequestParam String no) {
        User user = userService.selectId(id);
        return Result.success(user);
    }

    // 分页查询
    // pageNum当前页码
    // pageSize每页个数
    @GetMapping("/selectPage")
    public Result selectPage(User user,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<User> pageInfo = userService.selectPage(user, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectCarPage")
    public Result selectCarPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String plate) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("401", "请登录");
        }
        Car car = new Car();
        car.setOwnerId(currentUser.getIdCard());
        car.setPlate(plate);
        PageInfo<Car> pageInfo = carService.selectPageByOwnerID(car, pageNum, pageSize);
        return Result.success(pageInfo);
    }



    @GetMapping("/selectViolationPage")
    public Result selectViolationPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String plate,
            @RequestParam(required = false) String status) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("401", "请登录");
        }
        Violation violation = new Violation();
        violation.setOwnerId(currentUser.getIdCard());
        violation.setPlate(plate);
        violation.setStatus(status);
        PageInfo<Violation> pageInfo = violationService.selectPageByOwnerID(violation, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id) {
        userService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }

}
