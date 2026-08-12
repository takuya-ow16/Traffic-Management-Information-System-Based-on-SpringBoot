package com.example.controller;

import cn.hutool.core.util.StrUtil;
import com.example.common.AuthAccess;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomException;

import com.example.mapper.UserMapper;
import com.example.service.AdminService;
import com.example.service.PoliceService;
import com.example.service.TodayRegisterServiece;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

 @RestController
 public class WebController {
     @Resource
    private PoliceService policeService;

     @Resource
     private AdminService adminService;
     @Resource
     private UserService userService;

     @Resource
     private TodayRegisterServiece todayRegisterServiece;

     @GetMapping("/")
     public Result hello() {
      return Result.success("success");
     }

     @PostMapping("/login")
     public Result login(@RequestBody Account account) {
       if (StrUtil.isBlank(account.getUsername()) ||
        StrUtil.isBlank(account.getPassword())) {
        throw new CustomException("500", "账号或密码错误");
       }

       if ("ADM".equals(account.getRole())) {
           account = adminService.login(account);
           return Result.success(account);
       } else if  ("POL".equals(account.getRole())) {
           account = policeService.login(account);
           return Result.success(account);
       } else if ("USER".equals(account.getRole())) {
           account = userService.login(account);
           return Result.success(account);
       } else {
         throw new CustomException("500", "非法输入");
       }
     }

     @AuthAccess
     @PostMapping("/register")
     public Result register(@RequestBody User user) {
       userService.register(user);
       todayRegisterServiece.addNumber();
       return Result.success();
     }

     @PutMapping("/updatePassword")
     public Result updatePassword(@RequestBody Account account) {
         Account result = null;
         if ("ADM".equals(account.getRole())) {
             adminService.updatePassword(account);
         } else if ("POL".equals(account.getRole())) {
             policeService.updatePassword(account);
         } else if ("USER".equals(account.getRole())) {
             userService.updatePassword(account);
         } else {
             throw new CustomException("500", "非法输入");
         }
         return Result.success(result);
     }
}
