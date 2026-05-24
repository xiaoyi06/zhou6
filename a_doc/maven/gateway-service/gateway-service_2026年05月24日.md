# gateway-service_2026年05月24日

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
      <td>gateway-service</td>
      <td>初始化网关服务 Maven 依赖</td>
      <td>无网关服务模块依赖</td>
      <td>新增 Gateway、Security、LoadBalancer、Reactive Redis、common-server 等依赖</td>
      <td>支撑统一路由、WebFlux 安全链、服务名负载均衡、Redis 会话校验和公共 JWT 对象复用</td>
      <td><code>gateway-service/pom.xml</code><br><code>a_doc/maven/gateway-service/gateway-service_lib.md</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化网关微服务依赖</td>
      <td>无网关微服务模块</td>
      <td><code>spring-boot-starter</code><br><code>spring-cloud-starter-gateway</code><br><code>spring-cloud-starter-alibaba-nacos-discovery</code><br><code>spring-cloud-starter-alibaba-nacos-config</code><br><code>spring-cloud-starter-bootstrap</code></td>
      <td>提供统一网关入口，通过 Nacos 发现 user-service，并支持 <code>lb://</code> 负载均衡路由</td>
      <td><code>gateway-service/pom.xml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内补充 Spring Boot 基础依赖</td>
      <td>Spring Boot 基础类由 Gateway 传递依赖提供</td>
      <td>由父工程统一声明 <code>spring-boot-starter</code>，版本由 Spring Boot parent 3.3.0 管理</td>
      <td>减少 IDEA 对传递依赖索引不稳定造成的注解爆红；同时将公共依赖统一上收到父工程</td>
      <td>父工程 <code>pom.xml</code> 和 <code>gateway-service/pom.xml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内上收公共依赖</td>
      <td>子模块声明 Nacos、测试依赖和 Spring Boot 打包插件</td>
      <td>由父工程统一声明 Nacos、测试依赖和 Spring Boot 打包插件</td>
      <td>减少微服务重复配置，子模块只保留自身差异化依赖</td>
      <td>父工程 <code>pom.xml</code> 和 <code>gateway-service/pom.xml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化依赖说明文档</td>
      <td>无依赖说明文档</td>
      <td><code>a_doc/maven/gateway-service/gateway-service_lib.md</code><br><code>gateway-service_2026年05月24日.md</code></td>
      <td>记录当前微服务依赖来源、引入原因和初始化流水，便于后续依赖变更追踪</td>
      <td><code>a_doc/gateway-service</code> 文件夹</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内调整父工程名称和服务端口</td>
      <td>父工程未统一为 <code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code><br>服务端口为 <code>8000</code></td>
      <td>父工程为 <code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code><br>服务端口为 <code>51024</code></td>
      <td>同步父工程项目命名，并按指定端口规划网关入口</td>
      <td><code>gateway-service/pom.xml</code><br><code>gateway-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内补充 Nacos 配置中心和 bootstrap 配置</td>
      <td>仅继承 Nacos Discovery；Nacos 配置项混在旧本地配置文件中</td>
      <td>继承 <code>spring-cloud-starter-alibaba-nacos-config</code><br>继承 <code>spring-cloud-starter-bootstrap</code><br>新增 <code>bootstrap.yml</code><br>注册名调整为 <code>gateway-service</code></td>
      <td>保证启动时先加载 Nacos 地址、命名空间和 <code>common-shared.yml</code>，再注册网关并转发到 <code>user-service</code></td>
      <td>父工程 <code>pom.xml</code><br><code>gateway-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内统一只使用 bootstrap 配置文件</td>
      <td>端口和网关路由曾放在旧本地配置文件中</td>
      <td>删除旧本地配置文件<br>端口 <code>51024</code> 和网关路由合并到 <code>bootstrap.yml</code></td>
      <td>按项目约定统一只使用 <code>bootstrap.yml</code>，避免配置文件混用</td>
      <td><code>gateway-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内调整 Nacos 服务地址</td>
      <td><code>81.70.186.59:18848</code></td>
      <td><code>81.70.186.59:58848</code></td>
      <td>Nacos 服务端访问地址调整，账号密码保持 <code>nacos</code></td>
      <td><code>gateway-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内删除本地 JWT 重复配置</td>
      <td>本地 <code>bootstrap.yml</code> 声明 <code>jwt.secret</code></td>
      <td>改由 Nacos <code>common-shared.yml</code> 统一提供 JWT 签名秘钥</td>
      <td>认证服务签发和网关校验必须使用同一份秘钥，集中管理更稳</td>
      <td><code>gateway-service/src/main/resources/bootstrap.yml</code><br>Nacos <code>common-shared.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内同步用户服务路由地址</td>
      <td>网关路由匹配 <code>/users/**</code></td>
      <td>网关路由匹配 <code>/userInfo/**</code></td>
      <td>匹配用户服务新的类级公共路径 <code>/userInfo</code></td>
      <td><code>gateway-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内调整用户身份透传方式</td>
      <td>网关校验 JWT 后向下游注入 <code>X-User-Id</code> Header</td>
      <td>网关校验 JWT 后向下游追加 <code>userId</code> 请求参数，并拦截 <code>/userInfo/verify</code></td>
      <td>避免用户服务 Controller 直接依赖 Header，提高方法复用性</td>
      <td><code>AuthGlobalFilter.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内切换为 Spring Security WebFlux 鉴权</td>
      <td>使用自定义 <code>AuthGlobalFilter</code> 完成白名单、JWT 校验和用户身份透传</td>
      <td>新增 <code>spring-boot-starter-security</code><br>新增 <code>SecurityConfig</code><br>新增 <code>JwtAuthenticationWebFilter</code><br>删除 <code>AuthGlobalFilter</code></td>
      <td>使用 Gateway 响应式安全链统一管理认证和授权，并为后续权限体系扩展打基础</td>
      <td><code>gateway-service/pom.xml</code><br><code>SecurityConfig.java</code><br><code>JwtAuthenticationWebFilter.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内补充网关安全链注释并优化注入方式</td>
      <td><code>SecurityConfig</code> 注释不足，安全配置对象通过 <code>@Bean</code> 方法参数注入</td>
      <td>补充 Spring Security WebFlux 安全链中文注释<br>显式开启 <code>@EnableWebFluxSecurity</code><br>改用 <code>ServerHttpSecurity.http()</code> 静态创建安全配置对象</td>
      <td>提升安全配置可读性，并减少 IDEA 对方法参数注入和链式配置的误报</td>
      <td><code>SecurityConfig.java</code><br><code>JwtAuthenticationWebFilter.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内补充 Redis 会话校验</td>
      <td>网关只校验 JWT 签名和过期时间，旧客户端在短令牌剩余有效期内仍可能继续访问</td>
      <td>新增 <code>spring-boot-starter-data-redis-reactive</code><br>JWT 增加 <code>sessionId</code><br>网关校验 Redis 当前会话，不匹配直接返回 401</td>
      <td>实现同账号新登录后立即踢掉旧登录，避免旧 accessToken 在剩余有效期内继续访问</td>
      <td><code>gateway-service/pom.xml</code><br><code>JwtAuthenticationWebFilter.java</code><br><code>JwtTokenSupport.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内改为 JWT + Redis 会话校验</td>
      <td>网关曾向下游追加 <code>userId</code> 请求参数或用户 Header</td>
      <td>网关只解析 JWT 中的用户 ID 和会话 ID，并查询 Redis 当前会话快照；不再透传用户详情 Header</td>
      <td>避免下游服务信任可伪造的用户 Header，用户详情由业务服务自行解析 token 并查询 Redis 获取</td>
      <td><code>JwtAuthenticationWebFilter.java</code><br><code>JwtTokenSupport.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>gateway-service</td>
      <td>初始化期内归档 Maven 依赖说明</td>
      <td>网关服务依赖说明曾位于旧目录 <code>doc/gateway-service/gateway-service_lib.md</code></td>
      <td>网关服务依赖说明迁移到 <code>a_doc/maven/gateway-service/gateway-service_lib.md</code></td>
      <td>按新的文档结构集中维护 Maven 相关说明，流水仍保留在当前初始化流水中</td>
      <td><code>a_doc/maven/gateway-service/gateway-service_lib.md</code></td>
    </tr>
  </tbody>
</table>

