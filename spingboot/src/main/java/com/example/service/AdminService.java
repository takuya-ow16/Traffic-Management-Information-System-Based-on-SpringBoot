package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    public List<Admin> selectAll(Admin admin) {
        return adminMapper.selectAll(admin);
    }

    public Admin selectId(Integer id) {
        return adminMapper.selectId(id);
    }

    public PageInfo<Admin> selectPage(Admin admin,
            Integer pageNum,
            Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> list = adminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

    public void add(Admin admin) {
        String userName = admin.getUsername();
        Admin dbAdmin = adminMapper.selectByUsername(userName);
        if (StrUtil.isBlank(admin.getRole())) {
            admin.setRole("ADM"); // 默认为管理员
        }
        admin.setStatus("NOR");
        adminMapper.insert(admin);
    }

    public void update(Admin admin) {
        adminMapper.updateById(admin);
    }

    public void deleteById(Integer id) {
        Admin admin = adminMapper.selectId(id);
        if (admin == null) {
            System.out.println("删除失败：ID " + id + " 在数据库中不存在。");
        } else {
            System.out.println("正在删除：ID=" + admin.getId() + ", Name=" + admin.getName());
        }
        adminMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            adminMapper.deleteById(id);
        }
    }

    public Admin login(Account admin) {
        String adminname = admin.getUsername();
        Admin dbAdmin = adminMapper.selectByUsername(adminname);
        if (dbAdmin == null) {
            throw new CustomException("500", "账号不存在");
        }

        String password = admin.getPassword();
        if (!password.equals(dbAdmin.getPassword())) {
            throw new CustomException("500", "账号或密码错误");
        }

        if (!"NOR".equals(dbAdmin.getStatus())) {       //判断当前状态是否正常
            throw new CustomException("500", "当前账号状态不正常");
        }

        // 生成Token
        String token = TokenUtils.createToken(dbAdmin.getId().toString(),
                dbAdmin.getPassword(), dbAdmin.getRole());
        dbAdmin.setToken(token);
        return dbAdmin;
    }

    public void updatePassword(Account account) {
        Integer id = account.getId();
        Admin admin = this.selectId(id);
        if (!admin.getPassword().equals(account.getPassword())) { // 对比页面密码和原密码
            throw new CustomException("500", "对不起原密码错误");
        }
        admin.setPassword(account.getNewPassword());
        this.update(admin);
    }

    public Integer selectAllCount() {
        return adminMapper.selectAllCount();
    }
}
