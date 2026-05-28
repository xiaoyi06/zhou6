# common-server_2026年05月24日

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
      <td>common-server</td>
      <td>初始化公共契约模块 Maven 配置</td>
      <td>无公共契约模块</td>
      <td>新增 <code>com.zhou6:common-server</code>，跳过 Spring Boot 可执行 jar 重打包</td>
      <td>公共 jar 模块只提供共享类，不需要启动类和可执行包</td>
      <td><code>common-util/common-server/pom.xml</code><br><code>a_doc/maven/common-server/common-server_lib.md</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>common-server</td>
      <td>初始化公共契约模块</td>
      <td><code>R</code> 响应体分别存在于 user/auth 服务中</td>
      <td>新增 <code>common-server</code><br><code>R</code> 统一迁移到 <code>com.zhou6.cloud.common.dto</code></td>
      <td>公共响应契约需要统一维护，后续加入链路 ID、错误码、分页对象时避免多个服务重复修改</td>
      <td>父工程 <code>pom.xml</code><br><code>common-server</code><br>用户服务和认证服务</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>common-server</td>
      <td>初始化期内补充用户上下文工具</td>
      <td>审计字段自动填充没有统一位置获取当前操作人 ID</td>
      <td>新增 <code>UserContextHolder</code>，通过 <code>ThreadLocal</code> 保存当前用户 ID</td>
      <td>给 MyBatis-Plus 审计字段自动填充提供公共上下文能力，避免各服务重复维护工具类</td>
      <td><code>common-server</code><br>用户服务审计填充处理器</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>common-server</td>
      <td>初始化期内扩展当前登录人上下文</td>
      <td><code>UserContextHolder</code> 只保存当前用户 ID</td>
      <td>新增 <code>CurrentLoginUser</code><br><code>UserContextHolder</code> 保存用户 ID、账号、昵称、邮箱和联系电话</td>
      <td>让业务服务可以统一通过上下文获取当前调用人信息，避免 Controller 重复接收用户字段</td>
      <td><code>CurrentLoginUser.java</code><br><code>UserContextHolder.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>common-server</td>
      <td>初始化期内归档 Maven 依赖说明</td>
      <td>公共模块依赖说明曾位于旧目录 <code>doc/common-server_lib.md</code></td>
      <td>公共模块依赖说明迁移到 <code>a_doc/maven/common-server/common-server_lib.md</code></td>
      <td>按新的文档结构集中维护 Maven 相关说明，流水仍保留在当前初始化流水中</td>
      <td><code>a_doc/maven/common-server/common-server_lib.md</code></td>
    </tr>
  </tbody>
</table>

