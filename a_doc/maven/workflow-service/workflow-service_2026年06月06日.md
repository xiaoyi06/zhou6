# workflow-service_2026年06月06日

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
      <td>workflow-service</td>
      <td>新增工作流微服务 Maven 模块</td>
      <td>父工程未包含工作流服务模块</td>
      <td>父工程 <code>pom.xml</code> 新增 <code>workflow-service</code> 模块，并统一管理 <code>flowable.version=7.0.1</code></td>
      <td>按 Flowable 落地计划新增独立流程底座服务，承载流程启动、办理、终止、待办已办查询和业务状态事件通知</td>
      <td><code>pom.xml</code><br><code>workflow-service/pom.xml</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>workflow-service</td>
      <td>初始化工作流服务差异化依赖</td>
      <td>无工作流服务依赖说明</td>
      <td>新增 <code>common-server</code>、<code>spring-boot-starter-web</code>、<code>flowable-spring-boot-starter</code>、<code>spring-boot-starter-amqp</code>、<code>postgresql</code>、<code>springdoc-openapi-starter-webmvc-ui</code></td>
      <td>支撑 REST 接口、Flowable 流程引擎、PostgreSQL 元数据持久化、RabbitMQ 状态事件广播和接口文档</td>
      <td><code>workflow-service/pom.xml</code><br><code>a_doc/maven/workflow-service/workflow-service_lib.md</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>workflow-service</td>
      <td>初始化工作流服务启动配置</td>
      <td>无工作流服务本地 bootstrap 配置和 Nacos 配置</td>
      <td>新增 <code>bootstrap.yml</code>，配置服务名、环境和 Nacos 连接信息；新增 <code>workflow-service-dev.yml</code>，配置端口 <code>52052</code>、上下文路径 <code>/workflow</code>、PostgreSQL <code>currentSchema=zhou6</code>、Flowable Schema 和工作流事件 MQ 参数</td>
      <td>保证工作流服务可注册到 Nacos，并让 Flowable 元数据表落到 PostgreSQL 的 <code>zhou6</code> Schema</td>
      <td><code>workflow-service/src/main/resources/bootstrap.yml</code><br><code>a_doc/nacos/workflow-service-dev.yml</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>workflow-service</td>
      <td>初始化工作流业务接口</td>
      <td>无流程启动、任务办理、流程终止、待办已办查询和工作流状态事件接口</td>
      <td>新增工作流启动类、路径常量、OpenAPI 配置、错误码、DTO、VO、前端工作台 Controller、内部 RPC Controller 和 RabbitMQ 事件发布服务</td>
      <td>形成业务微服务通过内部接口驱动流程、前端通过工作台接口查看任务、流程终止后通过 MQ 通知业务补偿的第一版闭环</td>
      <td><code>workflow-service/src/main/java/com/zhou6/cloud/workflow</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>gateway-service</td>
      <td>网关接入工作流服务</td>
      <td>网关路由和 Swagger 聚合未包含 workflow-service</td>
      <td>新增 <code>/api/v1/workflow-api/**</code> 路由、workflow-service OpenAPI 地址、Swagger UI 切换项和文档白名单</td>
      <td>让前端和外部系统可通过网关访问工作流接口，并通过统一 Swagger 入口查看接口文档</td>
      <td><code>a_doc/nacos/gateway-service-dev.yml</code><br><code>OpenApiProxyWebFilter.java</code><br><code>JwtAuthenticationWebFilter.java</code></td>
    </tr>
    <tr>
      <td>2026年06月06日</td>
      <td>workflow-service</td>
      <td>补强工作流服务健壮性并收敛异常风格</td>
      <td>Controller 直接调用 Flowable 引擎；入参只做基础空值校验；待办已办查询无返回上限；Flowable 和 MQ 异常未做工作流语义包装；RabbitMQ 生产端未显式配置 JSON 转换和 mandatory</td>
      <td>新增 <code>WorkflowProcessService</code> 和实现类统一封装流程操作；流程 Key、业务 Key、任务 ID、审批意见和终止原因统一 trim 并限制长度；待办已办查询使用 <code>zhou6.workflow.query.max-size</code> 控制最大返回条数；任务不存在、流程不存在等可预期情况直接抛业务异常；Flowable 未知异常交给公共全局异常兜底；工作流事件 MQ 发送失败统一包装；RabbitTemplate 使用 JSON 转换并开启 mandatory</td>
      <td>降低 Controller 复杂度，防止接口一次性拉取过多任务、空白参数污染流程实例，并让流程服务异常处理风格与现有 account/order/user 服务保持一致</td>
      <td><code>WorkflowProcessService.java</code><br><code>WorkflowProcessServiceImpl.java</code><br><code>ProcessController.java</code><br><code>WorkflowInnerController.java</code><br><code>RabbitConfig.java</code><br><code>WorkflowEventServiceImpl.java</code><br><code>a_doc/nacos/workflow-service-dev.yml</code></td>
    </tr>
    <tr>
      <td>2026年06月07日</td>
      <td>workflow-service</td>
      <td>新增首页工作流统计接口</td>
      <td>工作流服务只有待办和已办列表查询，没有首页统计数量和折线图数据；流程启动时未写入 Flowable 发起人，无法按当前用户统计我发起的流程</td>
      <td>新增 <code>/api/v1/workflow-api/process/home/summary</code> 接口，返回当前用户待办、待审核、已审核、驳回/废除数量和最近 3 天折线图数据；新增 <code>UserContextInterceptor</code> 从 JWT 和 Redis 会话恢复当前用户；流程启动时设置 Flowable <code>authenticatedUserId</code>，支持 <code>startedBy</code> 统计；新增首页统计 VO</td>
      <td>支撑首页工作台卡片和折线图展示，同时保持接口不需要前端传入 <code>userId</code></td>
      <td><code>ProcessController.java</code><br><code>WorkflowProcessService.java</code><br><code>WorkflowProcessServiceImpl.java</code><br><code>WorkflowHomeSummaryVO.java</code><br><code>WorkflowDailyCountVO.java</code><br><code>UserContextInterceptor.java</code><br><code>WebMvcConfig.java</code><br><code>workflow-service/pom.xml</code><br><code>a_doc/2026_06_06_api_contract.md</code></td>
    </tr>
  </tbody>
</table>
