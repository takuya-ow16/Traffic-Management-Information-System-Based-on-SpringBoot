package com.example.controller;//package com.example.controller;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.entity.Police;
import com.example.mapper.PoliceMapper;
import com.example.mapper.UserMapper;
import com.example.service.AdminService;
import com.example.service.PoliceService;
import com.example.service.TodayRegisterServiece;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    @Resource
    private PoliceService policeService;

    @Resource
    private TodayRegisterServiece todayRegisterServiece;

    @GetMapping("/selectAll")
    public Result selectAll(Admin admin){
        List<Admin> list = adminService.selectAll(admin);
        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectId(@PathVariable Integer id){
        Admin admin = adminService.selectId(id);
        return Result.success(admin);
    }

    @GetMapping("/selectOne")
    public Result selectOne(@RequestParam Integer id, @RequestParam String no){
        Admin admin = adminService.selectId(id);
        return Result.success(admin);
    }

    //分页查询
    //pageNum当前页码
    //pageSize每页个数
    @GetMapping("/selectPage")
    public Result selectPage(Admin admin,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Admin> pageInfo = adminService.selectPage(admin, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Admin admin){
        adminService.add(admin);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Admin admin){
        adminService.update(admin);
        return Result.success();
    }

    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id){
        adminService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        adminService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/getTodayRegisterNumbuer")
    public Result getTodayRegisterNumbuer(){    //获取今日的注册人数
        return Result.success(todayRegisterServiece.getTodayRegisterNumbuer());
    }

    @GetMapping("/getYesterdayRegisterNumber")
    public Result getYesterdayRegisterNumber(){     //获取昨日的注册人数差
        return Result.success(todayRegisterServiece.getYesterdayRegisterNumber());
    }

    @GetMapping("/getYesterdayRegisterNumberDiff")
    public Result getYesterdayRegisterNumberDiff(){     //获取今天和昨日的注册人数差
        return Result.success(todayRegisterServiece.getYesterdayRegisterNumberDiff());
    }

    @GetMapping("/getUserCount")
    public Integer getUserCount(){
        return userService.selectAllCount();
    }

    @GetMapping("/getPoliceCount")
    public Integer getPoliceCount(){
        return policeService.selectAllCount();
    }

    @GetMapping("/getAdminCount")
    public Integer getAdminCount(){
        return adminService.selectAllCount();
    }

    @GetMapping("/getAllRoleCount")
    public Integer getAllRoleCount(){
        return userService.selectAllCount() + policeService.selectAllCount() +  adminService.selectAllCount();
    }
}
