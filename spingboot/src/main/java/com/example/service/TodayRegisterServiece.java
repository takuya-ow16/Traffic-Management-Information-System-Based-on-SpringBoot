package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.TodayRegister;
import com.example.mapper.TodayRegisterMapper;
import jakarta.annotation.Resource;
import net.sf.jsqlparser.statement.insert.Insert;

import org.springframework.stereotype.Service;

@Service
public class TodayRegisterServiece {
    @Resource
    TodayRegisterMapper todayRegisterMapper;

    public Integer getTodayRegisterNumbuer() { //获取今日注册人数
        String date = DateUtil.today();
        if (todayRegisterMapper.selectBydate(date) == null) {
            todayRegisterMapper.insert(new TodayRegister(date, 0));
            return 0;
        }
        return todayRegisterMapper.selectBydate(date).getNumber();
    }

    public void addNumber() {   
        String date = DateUtil.today();
        TodayRegister todayRegister = todayRegisterMapper.selectBydate(date);

        if (todayRegister != null) {
            todayRegister.setNumber(todayRegister.getNumber() + 1);
            todayRegisterMapper.update(todayRegister);
        } else {
            todayRegisterMapper.insert(new TodayRegister(date, 1));
        }
    }

    public Integer getYesterdayRegisterNumber() { //获取昨日注册人数
        // 获取昨天的日期，格式为 yyyy-MM-dd
        String yesterday = DateUtil.yesterday().toDateStr();
        if (todayRegisterMapper.selectBydate(yesterday) == null) {
            todayRegisterMapper.insert(new TodayRegister(yesterday, 0));
            return 0;
        }
        return todayRegisterMapper.selectBydate(yesterday).getNumber();
    }

    public Integer getYesterdayRegisterNumberDiff() { //获取今日和昨日的人数差
        Integer todayNumber = getTodayRegisterNumbuer();
        Integer yesterdayNumber = getYesterdayRegisterNumber();
        return todayNumber - yesterdayNumber;
    }
}
