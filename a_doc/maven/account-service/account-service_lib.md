# account-service_lib

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
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.boot:spring-boot-starter</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>共享 Spring Boot 基础运行时</td>
      <td>提供 <code>SpringApplication</code>、自动配置和基础日志能力</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>com.zhou6:common-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>复用公共响应体和异常处理</td>
      <td>统一使用 <code>R</code>、<code>BizException</code> 和公共全局异常处理器</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>com.zhou6:mybatis-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>接入统一 MyBatis-Plus 公共配置</td>
      <td>用于 <code>sys_account</code>、<code>sys_account_flow</code> 持久化和雪花 ID 生成</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-starter-web</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>提供账户 REST API 能力</td>
      <td>支撑 <code>/account/api/account/summary</code>、冻结、入账、出账和冲正接口</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-starter-data-redis</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>缓存现金快照并执行 Lua 原子变更</td>
      <td>使用 <code>zhou6:account:{userId}</code> Hash 存储可用、冻结、待结算和累计现金</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.apache.commons:commons-pool2</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>启用 Redis Lettuce 连接池配置</td>
      <td>让 Nacos 中的 <code>spring.data.redis.lettuce.pool</code> 配置实际生效</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-starter-amqp</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>接入 RabbitMQ 异步消费链路</td>
      <td>消费 <code>zhou6.account.settle</code> 和 <code>zhou6.account.unfreeze</code> 队列，完成结算和解冻落库</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.postgresql:postgresql</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>runtime</td>
      <td>连接 PostgreSQL 数据库</td>
      <td>保存账户快照 <code>sys_account</code> 和只增不改流水 <code>sys_account_flow</code></td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springdoc:springdoc-openapi-starter-webmvc-ui</code></td>
      <td>未显式声明</td>
      <td>父工程 dependencyManagement</td>
      <td>compile</td>
      <td>提供账户服务接口文档</td>
      <td>生成 <code>/account/v3/api-docs</code>，供网关 Swagger 聚合读取</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>spring-cloud-starter-alibaba-nacos-discovery</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td>compile</td>
      <td>服务注册与发现</td>
      <td>将 <code>account-service</code> 注册到 Nacos <code>zhou6</code> 命名空间</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>spring-cloud-starter-alibaba-nacos-config</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud Alibaba BOM 2023.0.1.0</td>
      <td>compile</td>
      <td>读取 Nacos 配置中心配置</td>
      <td>启动时加载 <code>common-shared.yml</code> 和 <code>account-service-dev.yml</code></td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.cloud:spring-cloud-starter-bootstrap</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td>compile</td>
      <td>启用 <code>bootstrap.yml</code></td>
      <td>让 Nacos 地址、命名空间和公共配置在应用上下文创建前加载</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.boot:spring-boot-starter-test</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>test</td>
      <td>测试基础能力</td>
      <td>供后续账户服务接口测试、事务测试和缓存脚本测试使用</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>account-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>父工程统一继承</td>
      <td><code>org.springframework.boot:spring-boot-maven-plugin</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>build plugin</td>
      <td>打包可执行服务</td>
      <td>将 <code>account-service</code> 打包成可执行 jar</td>
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
      <td><code>a_doc/maven/account-service/account-service_2026年06月06日.md</code></td>
      <td>账户服务初始化流水</td>
    </tr>
  </tbody>
</table>
