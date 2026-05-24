# auth-service_lib

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
      <td>auth-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>com.zhou6:common-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>复用公共响应体</td>
      <td>认证接口和 Feign 调用统一使用 <code>R</code>，后续链路 ID 字段只需在公共模块维护</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>auth-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-starter-web</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>提供登录和刷新 REST API</td>
      <td>暴露 <code>/auth/login</code> 与 <code>/auth/refresh</code> 接口</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>auth-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.projectlombok:lombok</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile optional</td>
      <td>减少 DTO 样板代码</td>
      <td>为登录、刷新、令牌和用户校验 DTO 生成 getter、setter 和构造方法</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>auth-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.cloud:spring-cloud-starter-loadbalancer</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td>compile</td>
      <td>支持 Feign 服务名调用</td>
      <td>让 <code>@FeignClient(name = "user-service")</code> 可通过 Nacos 服务发现选择用户服务实例</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>auth-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.cloud:spring-cloud-starter-openfeign</code></td>
      <td>未显式声明</td>
      <td>Spring Cloud BOM 2023.0.1</td>
      <td>compile</td>
      <td>调用用户服务内部校验接口</td>
      <td>通过 <code>user-service</code> 调用 <code>/userInfo/verify</code></td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>auth-service</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-starter-data-redis</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>保存长效刷新令牌</td>
      <td>将 20 分钟有效的 <code>refreshToken</code> 写入 Redis，并在刷新时轮换删除旧值</td>
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
      <td><code>a_doc/maven/auth-service/auth-service_2026年05月24日.md</code></td>
      <td>认证微服务文件夹第一次初始化流水</td>
    </tr>
  </tbody>
</table>




