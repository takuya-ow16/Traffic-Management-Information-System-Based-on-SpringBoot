package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Road;
import com.example.exception.CustomException;
import com.example.mapper.RoadMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

import com.example.mapper.RoadConditionMapper;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadService {

    @Resource
    private RoadMapper roadMapper;

    @Resource
    private RoadConditionMapper roadConditionMapper;

    public List<Road> selectAll(Road road) {
        return roadMapper.selectAll(road);
    }

    public Road selectId(Integer id) {
        return roadMapper.selectId(id);
    }

    public void add(Road road) {
        roadMapper.insert(road);
    }

    public void update(Road road) {
        roadMapper.updateById(road);
    }

    @Transactional
    public void delete(Integer id) {
        // Delete related road conditions first
        roadConditionMapper.deleteByRoadId(id);
        // Delete the road
        roadMapper.deleteById(id);
    }
}
