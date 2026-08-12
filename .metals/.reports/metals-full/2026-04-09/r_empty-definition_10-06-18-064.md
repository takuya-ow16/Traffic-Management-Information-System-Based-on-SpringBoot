error id: file:///E:/毕设/spingboot/src/main/java/com/example/service/ViolationIdService.java:_empty_/ViolationIdMapper#selectAllById#
file:///E:/毕设/spingboot/src/main/java/com/example/service/ViolationIdService.java
empty definition using pc, found symbol in pc: _empty_/ViolationIdMapper#selectAllById#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 524
uri: file:///E:/毕设/spingboot/src/main/java/com/example/service/ViolationIdService.java
text:
```scala
package com.example.service;

import com.example.entity.ViolationId;
import com.example.mapper.ViolationIdMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViolationIdService {
    @Resource
    private ViolationIdMapper violationIdMapper;

    public List<ViolationId> selectAll() {
        return violationIdMapper.selectAll();
    }

    public ViolationId selectById(String id) {
        return violationIdMapper.@@selectAllById(id);
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/ViolationIdMapper#selectAllById#