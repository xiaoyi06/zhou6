# gateway-service_lib

## 总依赖说明

<table border="1" cellspacing="0" cellpadding="6">
  <thead>
    <tr>
      <th>微服务名称</th>
      <th>父工程</th>
      <th>依赖来源</th>
      <th>依赖/插件坐标</th>
      <th>显式版本</th>
      <th>实际版本来源</th>
      <th>Scope</th>
      <th>引入原因</th>
      <th>对象依赖说明</th>
      <th>当前状态</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.boot:spring-boot-starter</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>共享 Spring Boot 基础运行时</td>
      <td>直接提供 <code>SpringApplication</code>、自动配置基础能力</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-starter-security</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>使用 Spring Security WebFlux 安全链</td>
      <td>通过 <code>SecurityWebFilterChain</code> 管理白名单、禁止路径和 JWT 认证过滤器</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>com.zhou6:common-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>复用公共 JWT 与登录会话对象</td>
      <td>网关使用公共 <code>JwtTokenSupport</code> 解析 JWT，并读取 <code>LoginSession</code> 校验 Redis 当前会话</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-starter-data-redis-reactive</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>校验当前有效登录会话</td>
      <td>网关校验 JWT 后读取 Redis 中的 <code>zhou6:auth:session:{userId}</code>，旧登录会话被覆盖后立即返回 401</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>spring-cloud-starter-alibaba-nacos-config</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td>compile</td>
      <td>读取 Nacos 配置中心配置</td>
      <td>启动时加载 <code>common-shared.yml</code>，并按 <code>gateway-service-dev.yml</code> 读取网关远端配置</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.cloud:spring-cloud-starter-bootstrap</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td>compile</td>
      <td>启用 <code>bootstrap.yml</code></td>
      <td>让 Nacos 地址、命名空间和公共配置在应用上下文创建前先加载</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.cloud:spring-cloud-starter-gateway</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td>compile</td>
      <td>提供 API 网关与路由能力</td>
      <td>引入 Gateway、WebFlux、Reactor Netty，处理 <code>/userInfo/**</code> 路由；不引入 Spring MVC Web 依赖，避免 WebFlux 冲突</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>spring-cloud-starter-alibaba-nacos-discovery</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td>compile</td>
      <td>服务注册与发现</td>
      <td>将 <code>gateway-service</code> 注册到 Nacos，并从 Nacos 查询 <code>user-service</code> 实例</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.cloud:spring-cloud-starter-loadbalancer</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td>compile</td>
      <td>支持服务名负载均衡</td>
      <td>解析 Gateway 路由中的 <code>lb://user-service</code>，从服务发现结果中选择实例</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.boot:spring-boot-starter-test</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>test</td>
      <td>测试基础能力</td>
      <td>引入 JUnit、Spring Test、AssertJ 等，供后续网关路由测试使用</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>gateway-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.boot:spring-boot-maven-plugin</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>build plugin</td>
      <td>打包可执行服务</td>
      <td>将 <code>gateway-service</code> 打包成可执行 jar，支持 <code>java -jar</code> 启动</td>
      <td>当前使用</td>
    </tr>
  </tbody>
</table>

## 修改流水文件

<table border="1" cellspacing="0" cellpadding="6">
  <thead>
    <tr>
      <th>流水文件</th>
      <th>说明</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>a_doc/maven/gateway-service/gateway-service_2026年05月24日.md</code></td>
      <td>当前文件夹第一次初始化流水</td>
    </tr>
  </tbody>
</table>




