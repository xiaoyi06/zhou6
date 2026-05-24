# auth-service_2026年05月24日

## Maven 初始化流水

<table border="1" cellspacing="0" cellpadding="6">
  <thead>
    <tr>
      <th>记录日期</th>
      <th>微服务名称</th>
      <th>依赖改动内容</th>
      <th>改动前</th>
      <th>改动后</th>
      <th>原因说明</th>
      <th>影响范围</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化认证服务 Maven 依赖</td>
      <td>无认证服务模块依赖</td>
      <td>新增 Web、OpenFeign、LoadBalancer、Redis、Lombok、common-server 等依赖</td>
      <td>支撑登录刷新接口、调用用户服务校验密码、Redis 保存会话、公共响应体和 JWT 对象复用</td>
      <td><code>auth-service/pom.xml</code><br><code>a_doc/maven/auth-service/auth-service_lib.md</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内补充 Feign 负载均衡依赖</td>
      <td>仅引入 <code>spring-cloud-starter-openfeign</code>，Feign 客户端无法按服务名创建负载均衡调用对象</td>
      <td>新增 <code>spring-cloud-starter-loadbalancer</code></td>
      <td>修复 <code>UserClient</code> 创建失败，支持通过 Nacos 中的 <code>user-service</code> 实例调用内部校验接口</td>
      <td><code>auth-service/pom.xml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内将 DTO 从 record 改为 Lombok 普通类</td>
      <td>登录、刷新、令牌和校验响应 DTO 使用 Java <code>record</code>，业务代码通过 <code>username()</code> 等访问器取值</td>
      <td>新增 <code>lombok</code> 依赖<br>DTO 改为普通类，并使用 <code>@Data</code>、<code>@NoArgsConstructor</code>、<code>@AllArgsConstructor</code><br>业务代码改为 getter 取值</td>
      <td>统一 DTO 编写方式，保留属性和 getter/setter 形态，方便后续字段扩展和框架反序列化</td>
      <td><code>auth-service/pom.xml</code><br><code>dto</code> 包<br><code>AuthServiceImpl.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内调整令牌有效期</td>
      <td>小令牌有效期为 15 秒<br>大令牌有效期为 7 天</td>
      <td>小令牌有效期为 15 秒<br>大令牌有效期为 20 分钟</td>
      <td>保持 accessToken 极短有效期，同时缩短 refreshToken 持有窗口</td>
      <td><code>AuthServiceImpl.java</code><br><code>auth-service/pom.xml</code><br>依赖说明文档</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内归档 Maven 依赖说明</td>
      <td>认证服务依赖说明曾位于旧目录 <code>doc/auth-service/auth-service_lib.md</code></td>
      <td>认证服务依赖说明迁移到 <code>a_doc/maven/auth-service/auth-service_lib.md</code></td>
      <td>按新的文档结构集中维护 Maven 相关说明，流水仍保留在当前初始化流水中</td>
      <td><code>a_doc/maven/auth-service/auth-service_lib.md</code></td>
    </tr>
<tr>
      <th>记录日期</th>
      <th>微服务名称</th>
      <th>初始化内容</th>
      <th>初始化前</th>
      <th>初始化后</th>
      <th>原因说明</th>
      <th>影响范围</th>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化认证微服务</td>
      <td>无认证微服务模块</td>
      <td>新增 <code>/auth/login</code><br>新增 <code>/auth/refresh</code><br>接入 Feign、Redis、Nacos、Bootstrap</td>
      <td>提供 15 秒短 JWT 和 20 分钟 Redis 刷新令牌的签发与轮换能力</td>
      <td><code>auth-service</code> 模块</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内删除本地公共配置重复声明</td>
      <td>本地 <code>bootstrap.yml</code> 声明虚拟线程、Redis 和 JWT 配置</td>
      <td>改由 Nacos <code>common-shared.yml</code> 统一提供虚拟线程、Redis 和 JWT 配置</td>
      <td>认证服务继续使用公共 Redis/JWT，但配置来源集中到 Nacos</td>
      <td><code>auth-service/src/main/resources/bootstrap.yml</code><br>Nacos <code>common-shared.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内同步用户校验接口地址</td>
      <td>Feign 调用 <code>/inner/users/verify</code></td>
      <td>Feign 调用 <code>/userInfo/verify</code></td>
      <td>匹配用户服务 <code>UserController</code> 的类级公共路径 <code>/userInfo</code></td>
      <td><code>auth-service/src/main/java/com/zhou6/cloud/auth/client/UserClient.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内统一 Controller 返回 R 风格</td>
      <td>认证接口直接返回 <code>TokenResponse</code>，Feign 直接接收 <code>VerifyResponse</code></td>
      <td>认证接口返回 <code>R&lt;TokenResponse&gt;</code><br>Feign 接收 <code>R&lt;VerifyResponse&gt;</code><br>新增异常处理返回 <code>R</code></td>
      <td>统一 API 响应结构，预留后续链路 ID 等公共响应字段</td>
      <td><code>AuthController.java</code><br><code>UserClient.java</code><br><code>R.java</code><br><code>AuthExceptionHandler.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内规范 Service 两层结构</td>
      <td><code>AuthService</code> 同时承担业务接口和实现逻辑</td>
      <td><code>AuthService</code> 改为接口<br><code>AuthServiceImpl</code> 承担具体实现</td>
      <td>符合后续扩展、测试替身、事务边界和接口隔离的服务层结构要求</td>
      <td><code>service/AuthService.java</code><br><code>service/impl/AuthServiceImpl.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内补充接口层方法描述和实现层注释</td>
      <td>Service 接口方法和 Feign 接口方法缺少方法描述，实现层关键业务步骤缺少注释</td>
      <td>接口方法补充 Javadoc<br>实现层补充登录校验、刷新令牌、令牌签发和 Redis 存储说明</td>
      <td>提升接口契约可读性，方便后续多人维护和扩展鉴权逻辑</td>
      <td><code>AuthService.java</code><br><code>AuthServiceImpl.java</code><br><code>UserClient.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内同步用户校验接口入参</td>
      <td>Feign 调用用户校验接口时传递登录请求对象</td>
      <td>Feign 调用用户校验接口时传递 <code>username</code> 和 <code>password</code> 基础属性</td>
      <td>匹配用户服务 Controller 方法基础属性入参风格</td>
      <td><code>UserClient.java</code><br><code>AuthServiceImpl.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内新增单账号单 IP 登录约束</td>
      <td>登录和刷新只校验账号密码或刷新令牌，不限制账号登录 IP</td>
      <td>登录和刷新时读取网关透传的 <code>X-Client-Ip</code><br>Redis 记录 <code>zhou6:auth:login-ip:{userId}</code><br>同账号不同 IP 登录返回 403</td>
      <td>满足一个账号只能在一个 IP 上登录的安全要求</td>
      <td><code>AuthController.java</code><br><code>AuthService.java</code><br><code>AuthServiceImpl.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内将单 IP 拒绝登录调整为新登录踢旧登录</td>
      <td>同账号不同 IP 登录时直接返回 403，旧登录不会被主动失效</td>
      <td>新登录成功后生成新的 <code>sessionId</code><br>覆盖 Redis 当前会话 <code>zhou6:auth:session:{userId}</code><br>删除旧 refreshToken</td>
      <td>满足当前用户在其他地方重新登录时，旧登录立即失效并被网关拦截</td>
      <td><code>AuthServiceImpl.java</code><br><code>JwtTokenSupport.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>auth-service</td>
      <td>初始化期内扩展登录用户快照</td>
      <td>JWT 和刷新令牌只记录用户 ID 与会话 ID</td>
      <td>JWT 只保留用户 ID 和会话 ID<br>refreshToken 和当前会话对应 Redis 值保存登录用户快照 JSON</td>
      <td>保持 token 内容轻量，用户详情统一从 Redis 登录会话快照读取</td>
      <td><code>JwtTokenSupport.java</code><br><code>AuthServiceImpl.java</code><br><code>VerifyResponse.java</code></td>
    </tr>
  </tbody>
</table>


