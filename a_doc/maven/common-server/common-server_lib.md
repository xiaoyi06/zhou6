# common-server_lib

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
      <td>common-server</td>
      <td><code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code></td>
      <td>当前模块</td>
      <td><code>com.zhou6:common-server</code></td>
      <td><code>0.0.1-SNAPSHOT</code></td>
      <td>父工程版本</td>
      <td>compile</td>
      <td>沉淀跨服务公共契约</td>
      <td>当前提供统一响应体 <code>R</code>，并提供 <code>UserContextHolder</code> 保存当前操作人 ID</td>
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
      <td><code>a_doc/maven/common-server/common-server_2026年05月24日.md</code></td>
      <td>公共契约模块第一次初始化流水</td>
    </tr>
  </tbody>
</table>




