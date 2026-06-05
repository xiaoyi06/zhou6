# order-service_2026年06月06日

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
      <td>order-service</td>
      <td>新增订单微服务 Maven 模块</td>
      <td>父工程未包含订单服务模块</td>
      <td>父工程 <code>pom.xml</code> 新增 <code>order-service</code> 模块，并新增 <code>order-service/pom.xml</code></td>
      <td>按订单系统计划新增独立微服务，承载订单状态机、账户预冻结、事务后置 MQ 和退款/取消链路</td>
      <td><code>pom.xml</code><br><code>order-service/pom.xml</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>order-service</td>
      <td>初始化订单服务差异化依赖</td>
      <td>无订单服务依赖说明</td>
      <td>新增 <code>common-server</code>、<code>mybatis-server</code>、<code>spring-boot-starter-web</code>、<code>spring-cloud-starter-openfeign</code>、<code>spring-cloud-starter-loadbalancer</code>、<code>spring-boot-starter-amqp</code>、<code>postgresql</code>、<code>spring-boot-starter-data-redis</code>、<code>commons-pool2</code>、<code>springdoc-openapi-starter-webmvc-ui</code></td>
      <td>支撑 REST 接口、PostgreSQL 持久化、账户服务 Feign 调用、RabbitMQ 后置投递、当前用户上下文和接口文档</td>
      <td><code>order-service/pom.xml</code><br><code>a_doc/maven/order-service/order-service_lib.md</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>order-service</td>
      <td>初始化订单服务启动配置</td>
      <td>无订单服务本地 bootstrap 配置</td>
      <td>新增 <code>bootstrap.yml</code>，配置服务名、环境和 Nacos 连接信息</td>
      <td>保证订单服务可注册到 Nacos，并从配置中心加载端口、上下文路径、RabbitMQ、Feign 超时和超时取消参数</td>
      <td><code>order-service/src/main/resources/bootstrap.yml</code><br><code>a_doc/nacos/order-service-dev.yml</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>order-service</td>
      <td>初始化订单数据库脚本</td>
      <td>无订单主表和订单现金扣减凭证脚本</td>
      <td>新增 <code>oms_order</code> 和 <code>oms_order_pay_receipt</code> 建表脚本，包含审计字段、唯一索引和查询索引</td>
      <td>为订单状态机、支付凭证、取消补偿和退款对账提供持久化地基</td>
      <td><code>a_doc/database/2026_06_06_oms_order.sql</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>order-service</td>
      <td>初始化订单业务代码</td>
      <td>无订单服务源码</td>
      <td>新增启动类、Controller、Service、实体、Mapper、DTO、VO、状态枚举、账户 Feign 客户端、RabbitMQ 发布器和超时取消任务</td>
      <td>实现创建并支付、订单详情、超时取消、退款审批、账户预冻结、红字冲正、事务提交后结算 MQ 和回滚后解冻补偿 MQ</td>
      <td><code>order-service/src/main/java/com/zhou6/cloud/order</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>gateway-service</td>
      <td>网关 Swagger 聚合新增订单服务</td>
      <td>网关 Swagger 聚合未包含 order-service</td>
      <td>新增 order-service OpenAPI 地址、Swagger UI 切换项和文档白名单</td>
      <td>让订单服务接口文档可以通过网关统一 Swagger 入口查看</td>
      <td><code>a_doc/nacos/gateway-service-dev.yml</code><br><code>OpenApiProxyWebFilter.java</code><br><code>JwtAuthenticationWebFilter.java</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>order-service</td>
      <td>补强订单服务健壮性</td>
      <td>请求体空值、Feign 异常包装、预冻结后本地事务回滚补偿、RabbitMQ 队列声明参数一致性不完整</td>
      <td>新增创建请求体校验；Feign 异常统一包装；预冻结成功后注册事务回滚解冻补偿；RabbitMQ 队列声明补齐死信参数并读取 Nacos 配置；RabbitTemplate 开启 mandatory</td>
      <td>订单服务是交易主线，需要优先防止空指针、远程调用异常失控、订单回滚后现金长时间冻结和 RabbitMQ 队列参数冲突</td>
      <td><code>OrderServiceImpl.java</code><br><code>AccountMessagePublisher.java</code><br><code>RabbitConfig.java</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>order-service</td>
      <td>补齐代码注释和 POM 说明</td>
      <td>订单服务部分实体字段、接口方法、核心私有方法、POM 依赖和模块文档说明不足</td>
      <td>补齐实体、DTO、VO、Service、Controller、Feign、MQ、配置类、定时任务、状态枚举和 POM 注释；新增 Maven 依赖总览和初始化流水文档</td>
      <td>保持新服务与现有微服务文档和代码注释风格一致，降低后续维护成本</td>
      <td><code>order-service</code><br><code>a_doc/maven/order-service</code></td>
    </tr>
  </tbody>
</table>
