package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    public User selectId(Integer id) {
        return userMapper.selectId(id);
    }

    public PageInfo<User> selectPage(User user,
            Integer pageNum,
            Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }

    public void add(User user) {
        String username = user.getUsername();
        User dbUser = userMapper.selectByUsername(username);
        if (dbUser != null) {
            throw new CustomException("500", "账号已存在请更换别的账号");
        }
        user.setRole("USER");
        user.setPoints(12);
        user.setStatus("NOR");
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.updateById(user);
    }

    public User login(Account admin) {
        String adminname = admin.getUsername();
        User dbUser = userMapper.selectByUsername(adminname);
        if (dbUser == null) {
            throw new CustomException("500", "账号不存在");
        }

        String password = admin.getPassword();
        if (!password.equals(dbUser.getPassword())) {
            throw new CustomException("500", "账号或密码错误");
        }

        String status = dbUser.getStatus();
        if (!"NOR".equals(status) && !"SQU".equals(status)) {       //判断当前状态是否正常
            throw new CustomException("500", "当前账号状态不正常");
        }

        // 生成Token
        String token = TokenUtils.createToken(dbUser.getId().toString(),
                dbUser.getPassword(), dbUser.getRole());
        dbUser.setToken(token);
        return dbUser;
    }

    public void deleteById(Integer id) {
        User user = userMapper.selectId(id);
        if (user == null) {
            System.out.println("删除失败：ID " + id + " 在数据库中不存在。");
        } else {
            System.out.println("正在删除：ID=" + user.getId() + ", Name=" + user.getName());
        }
        userMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            userMapper.deleteById(id);
        }
    }

    public void register(User user) {
        this.add(user);
    }

    public void updatePassword(Account account) {
        Integer id = account.getId();
        User user = this.selectId(id);
        if (!user.getPassword().equals(account.getPassword())) { // 对比页面密码和原密码
            throw new CustomException("500", "对不起原密码错误");
        }
        user.setPassword(account.getNewPassword());
        this.update(user);
    }

    public Integer selectAllCount() {
        return userMapper.selectAllCount();
    }

}
