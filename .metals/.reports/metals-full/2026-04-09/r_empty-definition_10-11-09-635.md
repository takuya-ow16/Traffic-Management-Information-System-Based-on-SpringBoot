error id: file:///E:/毕设/spingboot/src/main/java/com/example/service/PoliceService.java:_empty_/Police#
file:///E:/毕设/spingboot/src/main/java/com/example/service/PoliceService.java
empty definition using pc, found symbol in pc: _empty_/Police#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1951
uri: file:///E:/毕设/spingboot/src/main/java/com/example/service/PoliceService.java
text:
```scala
package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Police;
import com.example.exception.CustomException;
import com.example.mapper.PoliceMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PoliceService {

    @Resource
    private PoliceMapper policeMapper;

    public List<Police> selectAll(Police police) {
        return policeMapper.selectAll(police);
    }

    public Police selectId(Integer id) {
        return policeMapper.selectId(id);
    }

    public PageInfo<Police> selectPage(Police police,
            Integer pageNum,
            Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Police> list = policeMapper.selectAll(police);
        return PageInfo.of(list);
    }

    public void add(Police police) {
        String userName = police.getUsername();
        Police dbPolice = policeMapper.selectByUsername(userName);
        if (dbPolice != null) {
            throw new CustomException("500", "账号已存在请更换别的账号");
        }
        police.setRole("POL");
        police.setStatus("NOR");
        policeMapper.insert(police);
    }

    public void update(Police police) {
        policeMapper.updateById(police);
    }

    public void deleteById(Integer id) {
        Police police = policeMapper.selectId(id);
        if (police == null) {
            System.out.println("删除失败：ID " + id + " 在数据库中不存在。");
        } else {
            System.out.println("正在删除：ID=" + police.getId() + ", Name=" + police.getName());
        }
        policeMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            policeMapper.deleteById(id);
        }
    }

    public @@Police login(Account account) {
        String policename = account.getUsername();
        Police dbPolice = policeMapper.selectByUsername(policename);
        if (dbPolice == null) {
            throw new CustomException("500", "账号不存在");
        }

        String password = account.getPassword();
        if (!password.equals(dbPolice.getPassword())) {
            throw new CustomException("500", "账号或密码错误");
        }

        if (!"NOR".equals(dbPolice.getStatus())) {       //判断当前状态是否正常
            throw new CustomException("500", "当前账号状态不正常");
        }
        // 生成Token
        String token = TokenUtils.createToken(dbPolice.getId().toString(),
                dbPolice.getPassword(), dbPolice.getRole());
        dbPolice.setToken(token);
        return dbPolice;
    }

    public void updatePassword(Account account) {
        Integer id = account.getId();
        Police police = this.selectId(id);
        if (!police.getPassword().equals(account.getPassword())) { // 对比页面密码和原密码
            throw new CustomException("500", "对不起原密码错误");
        }
        police.setPassword(account.getNewPassword());
        this.update(police);
    }

    public Integer selectAllCount() {
        return policeMapper.selectAllCount();
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Police#