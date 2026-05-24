# mybatis-server_lib

## 总依赖说明

<table border="1" cellspacing="0" cellpadding="6">
  <thead>
    <tr>
      <th>项目名称</th>
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
      <td>mybatis-server</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>com.baomidou:mybatis-plus-spring-boot3-starter</code></td>
      <td>未显式声明</td>
      <td>父工程 <code>mybatis-plus.version=3.5.7</code></td>
      <td>compile</td>
      <td>统一 MyBatis-Plus 扩展能力</td>
      <td>提供 Mapper、实体注解和 <code>IdentifierGenerator</code> 扩展点</td>
      <td>当前使用</td>
    </tr>
    <tr>
      <td>mybatis-server</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块声明</td>
      <td><code>org.springframework.boot:spring-boot-autoconfigure</code></td>
      <td>未显式声明</td>
      <td>Spring Boot parent 3.3.0</td>
      <td>compile</td>
      <td>启用自动配置</td>
      <td>通过 <code>AutoConfiguration.imports</code> 自动注册统一雪花 ID 生成器</td>
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
      <td><code>a_doc/maven/mybatis-server/mybatis-server_2026年05月24日.md</code></td>
      <td>MyBatis 公共模块第一次初始化流水</td>
    </tr>
  </tbody>
</table>




