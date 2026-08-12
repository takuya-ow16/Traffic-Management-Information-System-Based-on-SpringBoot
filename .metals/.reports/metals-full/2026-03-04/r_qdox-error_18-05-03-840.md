error id: file:///E:/毕设/spingboot/src/main/java/com/example/common/JwtInterceptor.java
file:///E:/毕设/spingboot/src/main/java/com/example/common/JwtInterceptor.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[31,1]

error in qdox parser
file content:
```java
offset: 1008
uri: file:///E:/毕设/spingboot/src/main/java/com/example/common/JwtInterceptor.java
text:
```scala
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

i@@mport com.example.common.enums.ErrorCode;

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
            throw new CustomException(ErrorCode.AUTH_ERROR);
        }

        // 获取token中的id
        String userId;
        String role;
        try {
            userId = JWT.decode(token).getAudience().get(0); // JWT.decode(token) 解码
            role = JWT.decode(token).getClaim("role").asString(); // 从 Token 中解码出 role (自定义 Claim)
        } catch (JWTDecodeException e) {
            throw new CustomException(ErrorCode.AUTH_ERROR);  // 如果 Token 格式不对（比如被篡改了），解码会失败
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
            throw new CustomException(ErrorCode.USER_NOT_EXIST);
        }

        // 通过用户密码加密之后生成验证器
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();
        try {
            jwtVerifier.verify(token); // 验证token
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (com.auth0.jwt.exceptions.SignatureVerificationException e) {
            throw new CustomException(ErrorCode.TOKEN_CHECK_ERROR);
        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            throw new CustomException(ErrorCode.TOKEN_CHECK_ERROR);
        }
        return true;
    }
}

```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.mtags.MtagsIndexer.index(MtagsIndexer.scala:22)
	scala.meta.internal.mtags.MtagsIndexer.index$(MtagsIndexer.scala:21)
	scala.meta.internal.mtags.JavaMtags.index(JavaMtags.scala:39)
	scala.meta.internal.mtags.Mtags$.allToplevels(Mtags.scala:155)
	scala.meta.internal.metals.DefinitionProvider.fromMtags(DefinitionProvider.scala:372)
	scala.meta.internal.metals.DefinitionProvider.$anonfun$positionOccurrence$6(DefinitionProvider.scala:291)
	scala.Option.orElse(Option.scala:477)
	scala.meta.internal.metals.DefinitionProvider.$anonfun$positionOccurrence$1(DefinitionProvider.scala:291)
	scala.Option.flatMap(Option.scala:283)
	scala.meta.internal.metals.DefinitionProvider.positionOccurrence(DefinitionProvider.scala:276)
	scala.meta.internal.metals.MetalsLspService.$anonfun$definitionOrReferences$1(MetalsLspService.scala:1732)
	scala.Option.map(Option.scala:242)
	scala.meta.internal.metals.MetalsLspService.definitionOrReferences(MetalsLspService.scala:1728)
	scala.meta.internal.metals.MetalsLspService.$anonfun$definition$1(MetalsLspService.scala:961)
	scala.meta.internal.metals.CancelTokens$.future(CancelTokens.scala:38)
	scala.meta.internal.metals.MetalsLspService.definition(MetalsLspService.scala:960)
	scala.meta.internal.metals.WorkspaceLspService.definition(WorkspaceLspService.scala:511)
	scala.meta.metals.lsp.DelegatingScalaService.definition(DelegatingScalaService.scala:65)
	java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
	java.base/java.lang.reflect.Method.invoke(Method.java:580)
	org.eclipse.lsp4j.jsonrpc.services.GenericEndpoint.lambda$recursiveFindRpcMethods$0(GenericEndpoint.java:65)
	org.eclipse.lsp4j.jsonrpc.services.GenericEndpoint.request(GenericEndpoint.java:128)
	org.eclipse.lsp4j.jsonrpc.RemoteEndpoint.handleRequest(RemoteEndpoint.java:271)
	org.eclipse.lsp4j.jsonrpc.RemoteEndpoint.consume(RemoteEndpoint.java:201)
	org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer.handleMessage(StreamMessageProducer.java:185)
	org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer.listen(StreamMessageProducer.java:97)
	org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor.run(ConcurrentMessageProcessor.java:114)
	java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:572)
	java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1583)
```
#### Short summary: 

QDox parse error in file:///E:/毕设/spingboot/src/main/java/com/example/common/JwtInterceptor.java