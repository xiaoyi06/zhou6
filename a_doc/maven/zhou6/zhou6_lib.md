# zhou6_lib

## 总依赖说明

<table border="1" cellspacing="0" cellpadding="6">
  <thead>
    <tr>
      <th>项目名称</th>
      <th>项目类型</th>
      <th>父工程/继承来源</th>
      <th>版本管理来源</th>
      <th>依赖/插件坐标</th>
      <th>显式版本</th>
      <th>实际版本来源</th>
      <th>Scope</th>
      <th>引入原因</th>
      <th>对象依赖说明</th>
      <th>对应明细文件</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td><code>org.springframework.boot:spring-boot-starter-parent:3.3.0</code></td>
      <td>父工程 <code>pom.xml</code></td>
      <td><code>org.springframework.cloud:spring-cloud-dependencies</code></td>
      <td><code>2023.0.1</code></td>
      <td>父工程 properties</td>
      <td>import</td>
      <td>统一 Spring Cloud 组件版本</td>
      <td>管理 Gateway、LoadBalancer 等 Spring Cloud 依赖版本，子模块不需要单独写版本</td>
      <td>本文件</td>
    </tr>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td>父工程 dependencyManagement 管理</td>
      <td>父工程 <code>mybatis-plus.version</code></td>
      <td><code>com.baomidou:mybatis-plus-spring-boot3-starter</code></td>
      <td><code>3.5.7</code></td>
      <td>父工程 properties</td>
      <td>版本管理</td>
      <td>统一 MyBatis-Plus 版本</td>
      <td>用户服务按需引入该依赖，不在子模块重复写版本</td>
      <td>本文件</td>
    </tr>
    <tr>
      <td>common-server</td>
      <td>公共契约模块</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td><code>com.zhou6:common-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>统一跨服务公共对象</td>
      <td>提供 <code>R</code> 响应体，供 user/auth 等服务复用</td>
      <td><code>a_doc/maven/common-server/common-server_lib.md</code></td>
    </tr>
    <tr>
      <td>mybatis-server</td>
      <td>MyBatis 公共配置模块</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程版本和 MyBatis-Plus 版本管理</td>
      <td><code>com.zhou6:mybatis-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>统一数据库服务的 MyBatis-Plus 配置</td>
      <td>自动注册基于 <code>zhou6.id.worker-id</code> 和 <code>zhou6.id.datacenter-id</code> 的雪花 ID 生成器</td>
      <td><code>a_doc/maven/mybatis-server/mybatis-server_lib.md</code></td>
    </tr>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td>父工程 dependencyManagement 管理</td>
      <td>父工程 <code>flowable.version</code></td>
      <td><code>org.flowable:flowable-spring-boot-starter</code></td>
      <td><code>7.0.1</code></td>
      <td>父工程 properties</td>
      <td>版本管理</td>
      <td>统一 Flowable 工作流引擎版本</td>
      <td>工作流服务按需引入该依赖，不在子模块重复写版本</td>
      <td><code>a_doc/maven/workflow-service/workflow-service_lib.md</code></td>
    </tr>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td>父工程直接声明，子模块继承</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td><code>spring-cloud-starter-alibaba-nacos-config</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td>compile</td>
      <td>所有微服务统一接入 Nacos 配置中心</td>
      <td>启动时拉取 <code>common-shared.yml</code> 和服务环境配置，支持公共配置统一管理</td>
      <td>本文件</td>
    </tr>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td>父工程直接声明，子模块继承</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td><code>org.springframework.cloud:spring-cloud-starter-bootstrap</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td>compile</td>
      <td>启用 <code>bootstrap.yml</code> 启动上下文</td>
      <td>适配 Spring Boot 3 / Spring Cloud 2023 默认不自动解析 <code>bootstrap.yml</code> 的行为</td>
      <td>本文件</td>
    </tr>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td><code>org.springframework.boot:spring-boot-starter-parent:3.3.0</code></td>
      <td>父工程 <code>pom.xml</code></td>
      <td><code>com.alibaba.cloud:spring-cloud-alibaba-dependencies</code></td>
      <td><code>2023.0.1.0</code></td>
      <td>父工程 properties</td>
      <td>import</td>
      <td>统一 Spring Cloud Alibaba 组件版本</td>
      <td>管理 Nacos Discovery 等 Alibaba Cloud 生态依赖版本，子模块不需要单独写版本</td>
      <td>本文件</td>
    </tr>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td>父工程直接声明，子模块继承</td>
      <td>Spring Boot parent 3.3.0</td>
      <td><code>org.springframework.boot:spring-boot-starter</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>所有微服务共享 Spring Boot 基础运行时</td>
      <td>直接提供 <code>SpringApplication</code>、自动配置基础能力，减少 IDE 对传递依赖索引不稳定的影响</td>
      <td>本文件</td>
    </tr>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td>父工程直接声明，子模块继承</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td><code>spring-cloud-starter-alibaba-nacos-discovery</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td>compile</td>
      <td>所有微服务统一接入 Nacos 注册发现</td>
      <td>让 user-service、gateway-service 统一注册到 Nacos，并支持服务发现</td>
      <td>本文件</td>
    </tr>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td>父工程直接声明，子模块继承</td>
      <td>Spring Boot parent 3.3.0</td>
      <td><code>org.springframework.boot:spring-boot-starter-test</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>test</td>
      <td>所有微服务共享测试基础能力</td>
      <td>提供 JUnit、Spring Test、AssertJ 等测试工具</td>
      <td>本文件</td>
    </tr>
    <tr>
      <td>zhou6</td>
      <td>Maven 聚合父工程</td>
      <td>父工程 build 插件，子模块继承</td>
      <td>Spring Boot parent 3.3.0</td>
      <td><code>org.springframework.boot:spring-boot-maven-plugin</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>build plugin</td>
      <td>所有微服务统一打包成可执行 jar</td>
      <td>为每个 Spring Boot 微服务提供可执行 jar 打包能力</td>
      <td>本文件</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>Spring Boot parent</td>
      <td><code>org.projectlombok:lombok</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile optional</td>
      <td>用户服务 DTO 使用 Lombok 普通类</td>
      <td>为用户校验和用户信息响应 DTO 生成 getter、setter 和构造方法</td>
      <td><code>a_doc/maven/user-service/user-service_lib.md</code></td>
    </tr>
    <tr>
      <td>user-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>继承父工程 parent</td>
      <td><code>org.springframework.boot:spring-boot-starter-web</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>用户服务专属 Web 能力</td>
      <td>引入 Spring MVC、嵌入式 Tomcat、Jackson，支撑 <code>UserController</code> 暴露 <code>/users/{id}</code></td>
      <td><code>a_doc/maven/user-service/user-service_lib.md</code></td>
    </tr>
    <tr>
      <td>user-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>com.zhou6:common-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>复用公共响应体和用户上下文</td>
      <td>Controller 返回 <code>R&lt;VerifyResponse&gt;</code> 和 <code>R&lt;UserInfoResponse&gt;</code>，审计填充读取 <code>UserContextHolder</code></td>
      <td><code>a_doc/maven/user-service/user-service_lib.md</code></td>
    </tr>
    <tr>
      <td>user-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程管理 MyBatis-Plus 版本，Spring Boot parent 管理 PostgreSQL 版本</td>
      <td><code>mybatis-server</code><br><code>postgresql</code></td>
      <td>未显式声明</td>
      <td>父工程 properties 与 Spring Boot parent</td>
      <td>compile/runtime</td>
      <td>用户服务接入 PostgreSQL 持久层</td>
      <td>映射 <code>postgres</code> 库 <code>zhou6</code> 模式下的 <code>sys_user</code> 表，主键使用统一雪花 ID 配置</td>
      <td><code>a_doc/maven/user-service/user-service_lib.md</code></td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>继承父工程 BOM</td>
      <td><code>org.springframework.cloud:spring-cloud-starter-gateway</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td>compile</td>
      <td>网关服务专属路由能力</td>
      <td>引入 Gateway、WebFlux、Reactor Netty，处理 <code>/userInfo/**</code> 路由</td>
      <td><code>a_doc/maven/gateway-service/gateway-service_lib.md</code></td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>继承父工程 parent</td>
      <td><code>org.springframework.boot:spring-boot-starter-security</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>网关使用 WebFlux 安全链鉴权</td>
      <td>通过 <code>SecurityWebFilterChain</code> 和 JWT 过滤器统一管理认证授权</td>
      <td><code>a_doc/maven/gateway-service/gateway-service_lib.md</code></td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>继承父工程 parent</td>
      <td><code>org.springframework.boot:spring-boot-starter-data-redis-reactive</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>网关校验 Redis 当前会话</td>
      <td>用于实现新登录踢旧登录，JWT 中的 <code>sessionId</code> 必须匹配 Redis 当前会话</td>
      <td><code>a_doc/maven/gateway-service/gateway-service_lib.md</code></td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>继承父工程 BOM</td>
      <td><code>org.springframework.cloud:spring-cloud-starter-loadbalancer</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td>compile</td>
      <td>网关服务专属负载均衡路由能力</td>
      <td>解析 <code>lb://user-service</code>，从服务发现结果中选择实例并转发 <code>/userInfo/**</code></td>
      <td><code>a_doc/maven/gateway-service/gateway-service_lib.md</code></td>
    </tr>
    <tr>
      <td>auth-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>Spring Boot parent</td>
      <td><code>org.projectlombok:lombok</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile optional</td>
      <td>认证服务 DTO 使用 Lombok 普通类</td>
      <td>为登录、刷新、令牌和校验响应 DTO 生成 getter、setter 和构造方法</td>
      <td><code>a_doc/maven/auth-service/auth-service_lib.md</code></td>
    </tr>
    <tr>
      <td>auth-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>继承父工程 parent/BOM</td>
      <td><code>spring-boot-starter-web</code><br><code>spring-cloud-starter-openfeign</code><br><code>spring-cloud-starter-loadbalancer</code><br><code>spring-boot-starter-data-redis</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 与 Spring Cloud BOM</td>
      <td>compile</td>
      <td>认证服务专属接口、远程调用和 Redis 能力</td>
      <td>登录时通过 Feign + LoadBalancer 调用 user-service 校验密码，成功后签发 15 秒 JWT 并保存 20 分钟刷新令牌</td>
      <td><code>a_doc/maven/auth-service/auth-service_lib.md</code></td>
    </tr>
    <tr>
      <td>auth-service</td>
      <td>微服务</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>com.zhou6:common-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>复用公共响应体</td>
      <td>认证接口和 Feign 调用统一使用公共 <code>R</code> 响应体</td>
      <td><code>a_doc/maven/auth-service/auth-service_lib.md</code></td>
    </tr>
  </tbody>
</table>

## 流水文件汇总

<table border="1" cellspacing="0" cellpadding="6">
  <thead>
    <tr>
      <th>项目名称</th>
      <th>流水文件</th>
      <th>记录范围</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>zhou6</td>
      <td><code>a_doc/maven/zhou6/zhou6_2026年05月24日.md</code></td>
      <td>当前文件夹第一次初始化流水</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td><code>a_doc/maven/user-service/user-service_2026年05月24日.md</code></td>
      <td>用户微服务文件夹第一次初始化流水</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>a_doc/maven/gateway-service/gateway-service_2026年05月24日.md</code></td>
      <td>网关微服务文件夹第一次初始化流水</td>
    </tr>
    <tr>
      <td>auth-service</td>
      <td><code>a_doc/maven/auth-service/auth-service_2026年05月24日.md</code></td>
      <td>认证微服务文件夹第一次初始化流水</td>
    </tr>
    <tr>
      <td>common-server</td>
      <td><code>a_doc/maven/common-server/common-server_2026年05月24日.md</code></td>
      <td>公共契约模块第一次初始化流水</td>
    </tr>
    <tr>
      <td>mybatis-server</td>
      <td><code>a_doc/maven/mybatis-server/mybatis-server_2026年05月24日.md</code></td>
      <td>MyBatis 公共配置模块第一次初始化流水</td>
    </tr>
    <tr>
      <td>workflow-service</td>
      <td><code>a_doc/maven/workflow-service/workflow-service_2026年06月06日.md</code></td>
      <td>工作流微服务第一次初始化流水</td>
    </tr>
  </tbody>
</table>




