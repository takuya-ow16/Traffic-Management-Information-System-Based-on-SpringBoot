package com.example.common;

//
//
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

////拦截器
@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor()) // 配置JWT的拦截器规则
                .addPathPatterns("/**") // 拦截所有的请求路径
                .excludePathPatterns("/login") // 放行登录
                .excludePathPatterns("/files/**")
                .excludePathPatterns("/roadCondition/add")
                .excludePathPatterns("/road/selectAll") // 放行道路列表查询
                .excludePathPatterns("/roadCondition/getFlow15Min/**") // 放行流量查询接口
                .excludePathPatterns("/roadCondition/getRecords15Min/**") // 放行实时记录查询接口
                .excludePathPatterns("/roadCondition/getFlow24Hours/**"); // 放行24小时流量查询接口
        super.addInterceptors(registry);
    }

    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }
}
