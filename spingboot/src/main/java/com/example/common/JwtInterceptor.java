package com.example.common;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.mapper.AdminMapper;
import com.example.mapper.PoliceMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.exception.CustomException;

//拦截规则
public class JwtInterceptor implements HandlerInterceptor {
    // 注入三个 Mapper，用于去数据库查找
    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminMapper adminMapper;
    @Resource
    private PoliceMapper policeMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果是OPTIONS请求，直接放行
        if (RequestMethod.OPTIONS.name().equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("token"); // 从header里面传过来的参数
        if (StrUtil.isBlank(token)) { // 校验token查看是否存在
            token = request.getParameter("token"); // 如果不存在则从url参数中获取 ?token=xxx
        }

        // 如果不是映射到方法直接通过
        if (handler instanceof HandlerMethod) {
            AuthAccess annotation = ((HandlerMethod) handler).getMethodAnnotation(AuthAccess.class);
            if (annotation != null) {
                return true;
            }
        }

        // 执行认证，如果两个都没获取到url参数则报错
        if (StrUtil.isBlank(token)) {
            throw new CustomException("401", "请登录");
        }

        // 获取token中的id
        String userId;
        String role;
        try {
            userId = JWT.decode(token).getAudience().get(0); // JWT.decode(token) 解码
            role = JWT.decode(token).getClaim("role").asString(); // 从 Token 中解码出 role (自定义 Claim)
        } catch (JWTDecodeException e) {
            throw new CustomException("401", "请登录");  // 如果 Token 格式不对（比如被篡改了），解码会失败
        }

        // 根据token中的userid查询数据库
        Account account = null;
        if ("ADM".equals(role)) {
            account = adminMapper.selectId(Integer.valueOf(userId));
        } else if ("POL".equals(role)) {
            account = policeMapper.selectId(Integer.valueOf(userId));
        } else {
            // 默认为 USER 或者 role 为 null
            account = userMapper.selectId(Integer.valueOf(userId));
        }

        if (account == null) {
            throw new CustomException("401", "请登录");
        }

        // 通过用户密码加密之后生成验证器
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();
        try {
            jwtVerifier.verify(token); // 验证token
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new CustomException("401", "Token已过期");
        } catch (com.auth0.jwt.exceptions.SignatureVerificationException e) {
            throw new CustomException("401", "Token签名错误");
        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            throw new CustomException("401", "Token验证失败");
        }
        return true;
    }
}
