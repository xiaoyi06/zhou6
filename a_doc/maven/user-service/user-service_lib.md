# user-service_lib

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
      <td>user-service</td>
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
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>com.zhou6:common-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>复用公共响应体</td>
      <td>统一返回 <code>R</code>，避免每个服务重复定义响应结构</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>com.zhou6:mybatis-server</code></td>
      <td>未显式声明</td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>接入统一 MyBatis-Plus 公共配置</td>
      <td>传递引入 MyBatis-Plus，并统一使用 <code>zhou6.id</code> 下的雪花 ID 生成配置</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.postgresql:postgresql</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>runtime</td>
      <td>连接 PostgreSQL 数据库</td>
      <td>用于访问 <code>81.70.186.59:5432/postgres</code> 库 <code>zhou6</code> 模式下的 <code>sys_user</code> 用户表</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-starter-data-redis</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>读取当前登录会话快照</td>
      <td>拦截器解析 <code>Authorization</code> 后读取 <code>zhou6:auth:session:{userId}</code>，校验会话并写入 <code>UserContextHolder</code></td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>spring-cloud-starter-alibaba-nacos-config</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td>compile</td>
      <td>读取 Nacos 配置中心配置</td>
      <td>启动时加载 <code>common-shared.yml</code>，并按 <code>user-service-dev.yml</code> 读取用户服务端口和数据源配置</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>user-service</td>
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
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.projectlombok:lombok</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile optional</td>
      <td>减少 DTO 样板代码</td>
      <td>为 <code>VerifyResponse</code>、<code>UserInfoResponse</code> 生成 getter、setter 和构造方法</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-starter-web</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>提供 REST API 能力</td>
      <td>引入 Spring MVC、嵌入式 Tomcat、Jackson，支撑 <code>UserController</code> 暴露 <code>/userInfo/verify</code> 和 <code>/userInfo/info</code> 接口</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>spring-cloud-starter-alibaba-nacos-discovery</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td>compile</td>
      <td>服务注册与发现</td>
      <td>将 <code>user-service</code> 注册到 Nacos <code>zhou6</code> 命名空间，供网关按 <code>lb://user-service</code> 发现</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.boot:spring-boot-starter-test</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>test</td>
      <td>测试基础能力</td>
      <td>引入 JUnit、Spring Test、AssertJ 等，供后续接口和上下文测试使用</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>user-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.boot:spring-boot-maven-plugin</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>build plugin</td>
      <td>打包可执行服务</td>
      <td>将 <code>user-service</code> 打包成可执行 jar，支持 <code>java -jar</code> 启动</td>
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
      <td><code>a_doc/maven/user-service/user-service_2026年05月24日.md</code></td>
      <td>当前文件夹第一次初始化流水</td>
    </tr>
  </tbody>
</table>




