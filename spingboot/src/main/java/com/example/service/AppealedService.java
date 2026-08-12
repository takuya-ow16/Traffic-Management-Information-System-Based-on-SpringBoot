package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Appealed;
import com.example.entity.Car;
import com.example.entity.User;
import com.example.entity.Violation;
import com.example.mapper.AppealedMapper;
import com.example.mapper.CarMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.ViolationMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppealedService {

    @Resource
    private AppealedMapper appealedMapper;

    @Resource
    private ViolationMapper violationMapper;

    @Resource
    private CarMapper carMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 驳回申诉
     */
    @Transactional
    public void reject(Appealed appealed) {
        // 更新申诉表回复内容
        appealedMapper.updateById(appealed);

        // 更新违章状态为未处理(UNT)
        Violation violation = violationMapper.selectId(appealed.getId()); // ID一致
        if (violation != null) {
            violation.setStatus("UNT");
            violationMapper.updateById(violation);

            // 驳回后，车辆和用户状态应确保为异常(SQU)
            Car car = carMapper.selectByPlate(violation.getPlate());
            if (car != null && !"SQU".equals(car.getStatus())) {
                car.setStatus("SQU");
                carMapper.updateById(car);
            }

            User user = userMapper.selectByIdCar(violation.getOwnerId());
            if (user != null && !"SQU".equals(user.getStatus()) && !"OUT".equals(user.getStatus())) {
                user.setStatus("SQU");
                userMapper.updateById(user);
            }
        }
    }

    /**
     * 同意申诉
     */
    @Transactional
    public void pass(Integer id) {
        // 更新违章状态为申诉成功(PAS)
        Violation violation = violationMapper.selectId(id);
        if (violation != null) {
            violation.setStatus("PAS");
            violationMapper.updateById(violation);

            // 检查车辆是否还有其他违章，如果没有则恢复正常状态
            if (violationMapper.selectByPlateCountSQU(violation.getPlate()) == 0) {
                Car car = carMapper.selectByPlate(violation.getPlate());
                if (car != null) {
                    car.setStatus("NOR");
                    carMapper.updateById(car);
                }
            }

            // 检查用户是否还有其他违章
            if (violationMapper.selectByOnweridCountSQU(violation.getOwnerId()) == 0) {
                User user = userMapper.selectByIdCar(violation.getOwnerId());
                if (user != null && !user.getStatus().equals("OUT")) { // 封禁状态不恢复
                    user.setStatus("NOR");
                    userMapper.updateById(user);
                }
            }
        }
    }

    //新增申诉
    @Transactional
    public void add(Appealed appealed) {
        // 先查询是否已存在该违章的申诉
        Appealed dbAppealed = appealedMapper.selectByViolationId(appealed.getViolationId());
        
        if (dbAppealed != null) {
            // 如果已存在，则更新
            appealed.setId(dbAppealed.getId());
            appealedMapper.updateById(appealed);
        } else {
            // 如果不存在，则插入
            appealedMapper.insert(appealed);
        }

        // 更新违章单状态为已申诉(AED)
        Violation violation = violationMapper.selectId(appealed.getViolationId());
        if (violation != null) {
            violation.setStatus("AED");
            violationMapper.updateById(violation);
        }
    }

    //更新申诉（包括审核）
    @Transactional
    public void update(Appealed appealed) {
        appealedMapper.updateById(appealed);

        // 如果传递了违章状态，同步更新违章单状态
        if (StrUtil.isNotBlank(appealed.getViolationStatus())) {
            Violation violation = violationMapper.selectId(appealed.getViolationId());
            if (violation != null) {
                violation.setStatus(appealed.getViolationStatus());
                violationMapper.updateById(violation);
            }
        }
    }

    public void deleteById(Integer id) {
        // 获取申诉信息以恢复违章单状态
        Appealed appealed = appealedMapper.selectById(id);
        if (appealed != null) {
            // 由于申诉表ID即为违章ID，直接使用id查询违章信息
            Violation violation = violationMapper.selectId(id);
            if (violation != null) {
                violation.setStatus("UNT");
                violationMapper.updateById(violation);
            }
            appealedMapper.deleteById(id);
        }
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            deleteById(id);
        }
    }

    public Appealed selectId(Integer id) {
        return appealedMapper.selectById(id);
    }

    public PageInfo<Appealed> selectPage(Appealed appealed, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Appealed> list = appealedMapper.selectAll(appealed);
        return PageInfo.of(list);
    }
}
