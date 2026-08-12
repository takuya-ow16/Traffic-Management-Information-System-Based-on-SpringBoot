package com.example.service;

import com.example.entity.ViolationId;
import com.example.mapper.ViolationIdMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViolationIdService {
    @Resource
    private ViolationIdMapper violationIdMapper;

    public List<ViolationId> selectAll() {
        return violationIdMapper.selectAll();
    }

    public ViolationId selectById(String id) {
        return violationIdMapper.selectAllById(id);
    }
}
