# workflow-service_lib

## 依赖说明

<table border="1" cellspacing="0" cellpadding="6">
  <thead>
    <tr>
      <th>项目名称</th>
      <th>依赖/插件坐标</th>
      <th>显式版本</th>
      <th>实际版本来源</th>
      <th>Scope</th>
      <th>引入原因</th>
      <th>对象依赖说明</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>workflow-service</td>
      <td><code>com.zhou6:common-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>复用公共响应体和业务异常</td>
      <td>Controller 统一返回 <code>R</code>，异常统一使用 <code>BizException</code> 与错误码</td>
    </tr>
    <tr>
      <td>workflow-service</td>
      <td><code>org.projectlombok:lombok</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile optional</td>
      <td>DTO/VO 使用 Lombok 普通类</td>
      <td>为工作流请求参数和视图对象生成 getter、setter 和构造方法</td>
    </tr>
    <tr>
      <td>workflow-service</td>
      <td><code>org.springframework.boot:spring-boot-starter-web</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>提供流程工作台和内部 RPC REST 接口</td>
      <td>暴露 <code>/api/v1/workflow-api/process/todo</code>、<code>/done</code>、<code>/inner/start</code>、<code>/inner/complete</code>、<code>/inner/terminate</code></td>
    </tr>
    <tr>
      <td>workflow-service</td>
      <td><code>org.flowable:flowable-spring-boot-starter</code></td>
      <td><code>7.0.1</code></td>
      <td>父工程 <code>flowable.version</code></td>
      <td>compile</td>
      <td>接入 Flowable 流程引擎</td>
      <td>提供 <code>RuntimeService</code>、<code>TaskService</code>、<code>HistoryService</code>，并将引擎元数据表写入 PostgreSQL <code>zhou6</code> Schema</td>
    </tr>
    <tr>
      <td>workflow-service</td>
      <td><code>org.springframework.boot:spring-boot-starter-amqp</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>发布工作流状态变更事件</td>
      <td>流程终止后向 <code>zhou6.workflow.event.exchange</code> 发送 JSON 状态消息，生产端开启 mandatory，业务服务可按需绑定队列执行补偿</td>
    </tr>
    <tr>
      <td>workflow-service</td>
      <td><code>org.springframework.boot:spring-boot-starter-data-redis</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>恢复当前登录用户上下文</td>
      <td>首页统计接口从请求头 JWT 和 Redis 当前会话中恢复用户 ID，避免前端传递 <code>userId</code></td>
    </tr>
    <tr>
      <td>workflow-service</td>
      <td><code>org.apache.commons:commons-pool2</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>启用 Redis Lettuce 连接池配置</td>
      <td>让 <code>common-shared.yml</code> 中的 <code>spring.data.redis.lettuce.pool</code> 配置生效</td>
    </tr>
    <tr>
      <td>workflow-service</td>
      <td><code>org.postgresql:postgresql</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>runtime</td>
      <td>Flowable 元数据持久化</td>
      <td>连接 <code>jdbc:postgresql://81.70.186.59:5432/postgres?currentSchema=zhou6&amp;stringtype=unspecified</code></td>
    </tr>
    <tr>
      <td>workflow-service</td>
      <td><code>org.springdoc:springdoc-openapi-starter-webmvc-ui</code></td>
      <td>未显式声明</td>
      <td>父工程 <code>springdoc-openapi.version</code></td>
      <td>compile</td>
      <td>提供工作流服务接口文档</td>
      <td>生成 <code>/workflow/v3/api-docs</code>，供网关 Swagger 聚合读取</td>
    </tr>
  </tbody>
</table>
