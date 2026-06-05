# account-service_2026年06月06日

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
      <td>2026年06月06日</td>
      <td>account-service</td>
      <td>新增账户微服务 Maven 模块</td>
      <td>父工程未包含账户服务模块</td>
      <td>父工程 <code>pom.xml</code> 新增 <code>account-service</code> 模块</td>
      <td>按现金账户系统计划新增独立微服务，承载账户快照、账户流水、Redis 原子变更和 RabbitMQ 消费链路</td>
      <td><code>pom.xml</code><br><code>account-service/pom.xml</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>account-service</td>
      <td>初始化账户服务差异化依赖</td>
      <td>无账户服务依赖说明</td>
      <td>新增 <code>common-server</code>、<code>mybatis-server</code>、<code>spring-boot-starter-web</code>、<code>spring-boot-starter-data-redis</code>、<code>spring-boot-starter-amqp</code>、<code>postgresql</code>、<code>springdoc-openapi-starter-webmvc-ui</code></td>
      <td>支撑 REST 接口、PostgreSQL 持久化、Redis Lua 原子变更、RabbitMQ 异步消费和接口文档</td>
      <td><code>account-service/pom.xml</code><br><code>a_doc/maven/account-service/account-service_lib.md</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>account-service</td>
      <td>初始化账户服务启动配置</td>
      <td>无账户服务本地 bootstrap 配置</td>
      <td>新增 <code>bootstrap.yml</code>，配置服务名、端口 <code>52050</code>、上下文路径 <code>/account</code>、Nacos 地址和 RabbitMQ 地址</td>
      <td>保证账户服务可注册到 Nacos，并具备连接 RabbitMQ 消费结算和解冻消息的基础配置</td>
      <td><code>account-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>account-service</td>
      <td>初始化账户数据库脚本</td>
      <td>无现金账户表和流水表脚本</td>
      <td>新增 <code>sys_account</code> 和 <code>sys_account_flow</code> 建表脚本，包含审计字段和唯一索引</td>
      <td>为现金账户快照和只增不改流水审计提供持久化地基</td>
      <td><code>a_doc/database/2026_06_06_sys_account.sql</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>gateway-service</td>
      <td>网关 Swagger 聚合新增账户服务</td>
      <td>网关 Swagger 聚合只包含 auth、user、file 三个服务</td>
      <td>新增 account-service OpenAPI 地址和 Swagger UI 切换项</td>
      <td>让账户服务接口文档可以通过网关统一 Swagger 入口查看</td>
      <td><code>gateway-service/src/main/resources/bootstrap.yml</code><br><code>OpenApiProxyWebFilter.java</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>account-service</td>
      <td>补强账户服务健壮性</td>
      <td>请求体空值、金额精度、业务号长度、Redis/DB 异常补偿和 MQ 失败重投策略不完整</td>
      <td>新增账户业务错误码；Controller 增加请求体空校验；Service 统一校验金额精度、金额总位数、业务号长度和操作人；Redis 金额读取按 4 位小数校验；DB 更新异常时刷新 Redis 快照；RabbitMQ 队列增加死信队列，失败消息首次重投后仍失败则进入 DLQ</td>
      <td>现金账户属于资金相关链路，需要优先防止空指针、金额精度污染、无限重投和缓存快照分叉</td>
      <td><code>AccountController.java</code><br><code>AccountServiceImpl.java</code><br><code>AccountCacheService.java</code><br><code>AccountMessageListener.java</code><br><code>RabbitConfig.java</code><br><code>bootstrap.yml</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>account-service</td>
      <td>迁移运行配置到 Nacos</td>
      <td><code>bootstrap.yml</code> 中包含虚拟线程等运行配置</td>
      <td><code>bootstrap.yml</code> 只保留应用名、环境和 Nacos 连接信息；账户服务端口、上下文路径、RabbitMQ、账户缓存和 MQ 参数迁移到 Nacos <code>account-service-dev.yml</code></td>
      <td>保持本地配置轻量，让运行参数统一由 Nacos 配置中心管理</td>
      <td><code>account-service/src/main/resources/bootstrap.yml</code><br><code>a_doc/nacos/account-service-dev.yml</code><br>Nacos <code>account-service-dev.yml</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>account-service</td>
      <td>补充账户服务自动化测试</td>
      <td>账户服务只有手工接口验证，没有自动化测试覆盖关键回归点</td>
      <td>新增 Controller 契约测试、账户服务单元测试和 MQ 消费 ACK/NACK 行为测试，共 10 个用例</td>
      <td>覆盖金额精度校验、余额不足、冻结 DB 失败缓存刷新、入账快照更新失败、冻结成功、重复 MQ ACK、首次失败重投和二次失败进入死信队列等关键路径</td>
      <td><code>AccountControllerApiContractTest.java</code><br><code>AccountServiceImplTest.java</code><br><code>AccountMessageListenerTest.java</code></td>
    </tr>
  </tbody>
</table>
