package com.example.service;

import com.example.entity.*;
import com.example.mapper.CarMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.ViolationIdMapper;
import com.example.mapper.ViolationMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViolationService {

    @Resource
    private ViolationMapper violationMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ViolationIdMapper violationIdMapper;

    @Resource
    private CarMapper carMapper;

    //查询所有违章记录
    public List<Violation> selectAll(Violation violation) {
        return violationMapper.selectAll(violation);
    }

    //根据ID查询违章记录
    public Violation selectId(Integer id) {
        return violationMapper.selectId(id);
    }

//分页查询违章记录
    public PageInfo<Violation> selectPage(Violation violation,
            Integer pageNum,
            Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Violation> list = violationMapper.selectAll(violation);
        return PageInfo.of(list);
    }


    //交警添加违章单
    public void policeAdd(Violation violation) {//用于交警增加违章单
        //1.先获取用户数据，方便后续修改用户状态
        User user = userMapper.selectByIdCar(violation.getOwnerId());
        user.setStatus("SQU");  //将用户状态更改为为违章
        //1.1 获取车辆数据,并修改车辆状态
        Car car = carMapper.selectByPlate(violation.getPlate());
        car.setStatus("SQU");
        //1.2更新用户和车的状态
        carMapper.updateById(car);
        userMapper.updateById(user); //更新用户状态
        //2.添加违章单状态
        violation.setStatus("UNT"); //将违章单状态更新未处理，
        //3.添加违章单
        violationMapper.insert(violation);
    }

    //用户申报违章
    public void userAdd(Violation violation) {//用于用户增加违章单   //用户新增的违章单只有在交警确认后才会将状态更改未UNT
        //1.添加违章单状态
        violation.setStatus("UND"); //将违章单状态更新举报中，
        violation.setInfoid(TokenUtils.getCurrentUser().getIdCard());
        //2.添加违章单
        violationMapper.insert(violation);
    }

    //交警确认/处理违章单
    public void policeAffirm(Violation violation) { //交警确认违章单后使用
        if  (violation.getStatus().equals("HPD")) {
            violation.setStatus("PRO"); //将违章单状态更新为已处理，
            this.update(violation);

            // 检查车辆是否还有其他违章
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

        } else if (violation.getStatus().equals("UND")) {
            violation.setStatus("UNT"); //将违章单状态更新为待处理，
            User user = userMapper.selectByIdCar(violation.getOwnerId());
            user.setStatus("SQU");
            userMapper.updateById(user);
            Car car = carMapper.selectByPlate(violation.getPlate());
            car.setStatus("SQU");
            carMapper.updateById(car);
            this.update(violation);
        }
    }

    //用户处理违章（支付）
    public void userHandleViolation(Violation violation) {  //当用户支付完之后调用
        User user = userMapper.selectByIdCar(violation.getOwnerId());
        Car car = carMapper.selectByPlate(violation.getPlate());
        ViolationId violationId = violationIdMapper.selectAllById(violation.getViolationid());
        int points = user.getPoints() - violationId.getPoints();
        user.setPoints(Math.max(points, 0));
        if (points <= 0) {
            user.setStatus("OUT");
        } else {
            car.setStatus(violationMapper.selectByPlateCountSQU(violation.getPlate()) > 1 ? "SQU" : "NOR");
            if (violationMapper.selectByOnweridCountSQU(violation.getOwnerId()) == 1) {
                user.setStatus("NOR");
            }
        }
        userMapper.updateById(user);
        carMapper.updateById(car);
        violation.setStatus("HPD");
        this.update(violation);
    }


    //更新违章记录
    public void update(Violation violation) {
        violationMapper.updateById(violation);
    }

    //根据ID删除违章记录
    public void deleteById(Integer id) {
        Violation violation = violationMapper.selectId(id);
        if (violation == null) {
            System.out.println("删除失败：ID " + id + " 在数据库中不存在。");
        } else {
            System.out.println("正在删除：ID=" + violation.getId() + ", Name=" + violation.getId());
        }
        violationMapper.deleteById(id);
    }

    //批量删除违章记录
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            violationMapper.deleteById(id);
        }
    }
    
    //获取违章总数
    public Integer selectAllCount() {
        return violationMapper.selectAllCount();
    }

    //根据车主ID分页查询违章记录（用户端）
    public PageInfo<Violation> selectPageByOwnerID(Violation violation, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Violation> list = violationMapper.selectByOwnerID(violation);
        return PageInfo.of(list);
    }
}
