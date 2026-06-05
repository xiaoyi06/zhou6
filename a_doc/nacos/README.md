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
      <td><code>gateway-service-dev.yml</code></td>
      <td><code>DEFAULT_GROUP</code></td>
      <td><code>zhou6</code></td>
      <td>网关端口、统一路径前缀和 Swagger 聚合配置</td>
    </tr>
  </tbody>
</table>
