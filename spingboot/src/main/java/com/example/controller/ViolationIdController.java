package com.example.controller;

import com.example.common.Result;
import com.example.service.ViolationIdService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/violationId")
public class ViolationIdController {
    @Resource
    private ViolationIdService violationIdService;

    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(violationIdService.selectAll());
    }

    public Result selectById(String id) {
        return Result.success(violationIdService.selectById(id));
    }
}
