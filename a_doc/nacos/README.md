# Nacos 配置说明

本目录保存需要发布到 Nacos 配置中心的配置内容，便于本地仓库追踪配置变更。

## 当前配置

<table border="1" cellspacing="0" cellpadding="6">
  <thead>
    <tr>
      <th>Data ID</th>
      <th>Group</th>
      <th>Namespace</th>
      <th>说明</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>account-service-dev.yml</code></td>
      <td><code>DEFAULT_GROUP</code></td>
      <td><code>zhou6</code></td>
      <td>账户服务端口、上下文路径、RabbitMQ、账户缓存和 MQ 消费参数</td>
    </tr>
    <tr>
      <td><code>order-service-dev.yml</code></td>
      <td><code>DEFAULT_GROUP</code></td>
      <td><code>zhou6</code></td>
      <td>订单服务端口、上下文路径、RabbitMQ、账户 Feign 超时和超时取消参数</td>
    </tr>
    <tr>
      <td><code>workflow-service-dev.yml</code></td>
      <td><code>DEFAULT_GROUP</code></td>
      <td><code>zhou6</code></td>
      <td>工作流服务端口、上下文路径、PostgreSQL zhou6 Schema、Flowable 引擎和工作流事件 MQ 参数</td>
    </tr>
    <tr>
      <td><code>gateway-service-dev.yml</code></td>
      <td><code>DEFAULT_GROUP</code></td>
      <td><code>zhou6</code></td>
      <td>网关端口、统一路径前缀和 Swagger 聚合配置</td>
    </tr>
  </tbody>
</table>

## 连接稳定性补充

- PostgreSQL 连接 URL 已增加 <code>tcpKeepAlive=true</code>，用于降低跨公网空闲连接被中间网络设备回收后的影响。
- Redis Lettuce 连接池已增加 <code>time-between-eviction-runs: 30s</code>，用于定期巡检空闲连接。
- 若线上仍出现 Redis 或数据库掉线，需要结合服务端 TCP keepalive、云网络 idle timeout 和应用异常日志继续定位。
