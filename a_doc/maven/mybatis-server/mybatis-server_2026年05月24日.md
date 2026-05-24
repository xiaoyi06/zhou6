# mybatis-server_2026年05月24日

## Maven 初始化流水

<table border="1" cellspacing="0" cellpadding="6">
  <thead>
    <tr>
      <th>记录日期</th>
      <th>模块名称</th>
      <th>依赖改动内容</th>
      <th>改动前</th>
      <th>改动后</th>
      <th>原因说明</th>
      <th>影响范围</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>2026年05月24日</td>
      <td>mybatis-server</td>
      <td>初始化 MyBatis-Plus 公共配置依赖</td>
      <td>数据库服务各自直接维护 MyBatis-Plus 配置</td>
      <td>新增 <code>mybatis-plus-spring-boot3-starter</code>，并由父工程统一管理版本</td>
      <td>统一数据库服务的 MyBatis-Plus 与雪花 ID 配置，减少重复配置</td>
      <td><code>common-util/mybatis-server/pom.xml</code><br><code>a_doc/maven/mybatis-server/mybatis-server_lib.md</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>mybatis-server</td>
      <td>初始化 MyBatis-Plus 公共配置模块</td>
      <td>用户服务直接引入 MyBatis-Plus，雪花 ID 生成器使用框架默认配置</td>
      <td>新增 <code>mybatis-server</code> 模块<br>统一注册 <code>IdentifierGenerator</code><br>支持 <code>zhou6.id.worker-id</code> 和 <code>zhou6.id.datacenter-id</code></td>
      <td>让所有数据库服务按需复用同一套 MyBatis-Plus 和雪花 ID 生成配置，避免每个服务重复写配置</td>
      <td>父工程 <code>pom.xml</code><br><code>mybatis-server</code><br>用户服务</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>mybatis-server</td>
      <td>初始化期内归档 Maven 依赖说明</td>
      <td>MyBatis 公共模块依赖说明曾位于旧目录 <code>doc/mybatis-server_lib.md</code></td>
      <td>MyBatis 公共模块依赖说明迁移到 <code>a_doc/maven/mybatis-server/mybatis-server_lib.md</code></td>
      <td>按新的文档结构集中维护 Maven 相关说明，流水仍保留在当前初始化流水中</td>
      <td><code>a_doc/maven/mybatis-server/mybatis-server_lib.md</code></td>
    </tr>
  </tbody>
</table>

