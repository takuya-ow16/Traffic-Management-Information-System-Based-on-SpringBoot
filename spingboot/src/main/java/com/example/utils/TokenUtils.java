package com.example.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.mapper.AdminMapper;
import com.example.mapper.PoliceMapper;
import com.example.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class TokenUtils {

    // 1. 定义静态变量
    private static UserMapper staticUserMapper;
    private static AdminMapper staticAdminMapper;
    private static PoliceMapper staticPoliceMapper;

    // 2. 注入 Spring 管理的 Mapper 实例
    @Resource
    UserMapper userMapper;

    @Resource
    AdminMapper adminMapper;

    @Resource
    PoliceMapper policeMapper;

    // 3. 初始化静态变量 (@PostConstruct)
    @PostConstruct
    public void init() {
        staticUserMapper = userMapper;
        staticAdminMapper = adminMapper;
        staticPoliceMapper = policeMapper;
    }

    // 生成token
    public static String createToken(String userId, String sign, String role) {
        return JWT.create().withAudience(userId) // 将userid保存到token中作为载荷
                .withClaim("role", role) // 将role保存到token中
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2)) // 2小时后过期
                .sign(Algorithm.HMAC256(sign)); // 以password 作为 token密钥
    }

    // 获取当前登录用户的信息
    public static Account getCurrentUser() {
        try {
            // 1. 获取当前的 HTTP 请求对象
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            // 2. 尝试从 Header 或 URL 参数中获取 Token
            String token = request.getHeader("token");
            if (StrUtil.isBlank(token)) {
                 token = request.getParameter("token");
            }
            // 如果没拿到 Token，说明没登录
            if (StrUtil.isBlank(token)) {
                return null;
            }

            // 3. 解析 Token
            // 从 Token 字符串里把 userId 和 role 抠出来
            String userId = JWT.decode(token).getAudience().get(0);
            String role = JWT.decode(token).getClaim("role").asString();

            // 4. 根据角色去查对应的数据库表
            if ("ADM".equals(role)) {
                return staticAdminMapper.selectId(Integer.valueOf(userId));
            } else if ("POL".equals(role)) {
                return staticPoliceMapper.selectId(Integer.valueOf(userId));
            } else {
                return staticUserMapper.selectId(Integer.valueOf(userId));
            }

        } catch (Exception e) {
            return null;
        }
    }
}
