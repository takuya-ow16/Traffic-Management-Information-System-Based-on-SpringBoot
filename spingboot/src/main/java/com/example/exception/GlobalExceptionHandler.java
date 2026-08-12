package com.example.exception;

import com.example.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice("com.example.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody // 通过标识返回jason
    public Result error(Exception e) {
        e.printStackTrace(); // 打印错误堆栈信息，方便排查问题
        return Result.error();
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody // 通过标识返回jason
    public Result error(CustomException e) {
        e.printStackTrace();
        return Result.error(e.getCode(), e.getMessage());
    }
}
