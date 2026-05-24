# user-service_2026年05月24日

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
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化用户服务 Maven 依赖</td>
      <td>无用户服务模块依赖</td>
      <td>新增 Web、Lombok、common-server、mybatis-server、PostgreSQL、Redis 等依赖</td>
      <td>支撑用户 REST 接口、数据库查询、公共响应体、雪花 ID、登录会话上下文读取</td>
      <td><code>user-service/pom.xml</code><br><code>a_doc/maven/user-service/user-service_lib.md</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化用户微服务依赖</td>
      <td>无用户微服务模块</td>
      <td><code>spring-boot-starter</code><br><code>spring-boot-starter-web</code><br><code>spring-cloud-starter-alibaba-nacos-discovery</code><br><code>spring-cloud-starter-alibaba-nacos-config</code><br><code>spring-cloud-starter-bootstrap</code></td>
      <td>提供用户 Mock REST 接口，注册到 Nacos，并预留后续测试能力</td>
      <td><code>user-service/pom.xml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内补充 Spring Boot 基础依赖</td>
      <td>Spring Boot 基础类由 Web starter 传递依赖提供</td>
      <td>由父工程统一声明 <code>spring-boot-starter</code>，版本由 Spring Boot parent 3.3.0 管理</td>
      <td>减少 IDEA 对传递依赖索引不稳定造成的注解爆红；同时将公共依赖统一上收到父工程</td>
      <td>父工程 <code>pom.xml</code> 和 <code>user-service/pom.xml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内上收公共依赖</td>
      <td>子模块声明 Nacos、测试依赖和 Spring Boot 打包插件</td>
      <td>由父工程统一声明 Nacos、测试依赖和 Spring Boot 打包插件</td>
      <td>减少微服务重复配置，子模块只保留自身差异化依赖</td>
      <td>父工程 <code>pom.xml</code> 和 <code>user-service/pom.xml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化依赖说明文档</td>
      <td>无依赖说明文档</td>
      <td><code>a_doc/maven/user-service/user-service_lib.md</code><br><code>user-service_2026年05月24日.md</code></td>
      <td>记录当前微服务依赖来源、引入原因和初始化流水，便于后续依赖变更追踪</td>
      <td><code>a_doc/user-service</code> 文件夹</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内调整父工程名称和服务端口</td>
      <td>父工程未统一为 <code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code><br>服务端口为 <code>8081</code></td>
      <td>父工程为 <code>com.zhou6:zhou6:0.0.1-SNAPSHOT</code><br>服务端口为 <code>52048</code></td>
      <td>同步父工程项目命名，并按指定端口规划用户服务</td>
      <td><code>user-service/pom.xml</code><br><code>user-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内补充 Nacos 配置中心和 bootstrap 配置</td>
      <td>仅继承 Nacos Discovery；Nacos 配置项混在旧本地配置文件中</td>
      <td>继承 <code>spring-cloud-starter-alibaba-nacos-config</code><br>继承 <code>spring-cloud-starter-bootstrap</code><br>新增 <code>bootstrap.yml</code><br>注册名调整为 <code>user-service</code></td>
      <td>保证启动时先加载 Nacos 地址、命名空间和 <code>common-shared.yml</code>，并匹配网关 <code>lb://user-service</code> 路由</td>
      <td>父工程 <code>pom.xml</code><br><code>user-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内调整用户接口公共路径</td>
      <td>校验接口为 <code>/inner/users/verify</code><br>业务接口为 <code>/users/mock-info</code></td>
      <td>类级公共路径为 <code>/userInfo</code><br>校验接口为 <code>/userInfo/verify</code><br>业务接口为 <code>/userInfo/mock-info</code></td>
      <td>按用户服务统一公共路径规划接口地址，并同步 Auth Feign 调用和 Gateway 路由</td>
      <td><code>UserController.java</code><br><code>UserClient.java</code><br><code>gateway-service/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内统一 Controller 返回 R 风格并接入 Redis 用户信息</td>
      <td>Controller 直接返回 <code>Map</code><br><code>mockInfo</code> 临时拼装模拟用户信息</td>
      <td>新增 <code>R</code> 响应体，包含 <code>code</code>、<code>msg</code>、<code>data</code>、<code>traceId</code><br>新增 Redis 依赖<br><code>mockInfo</code> 从 Redis 读取用户信息</td>
      <td>统一 API 返回结构，并让用户信息来源从临时 Mock 迁移到 Redis 缓存</td>
      <td><code>user-service/pom.xml</code><br><code>UserController.java</code><br><code>dto</code> 包</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内接入 PostgreSQL 与 MyBatis-Plus</td>
      <td>用户服务暂未接入数据库持久层，用户信息主要来自 Redis Mock 缓存</td>
      <td>新增 <code>mybatis-plus-spring-boot3-starter</code><br>新增 <code>postgresql</code> 运行时驱动<br>新增 <code>SysUser</code> 实体和 <code>SysUserMapper</code></td>
      <td>按当前数据库 <code>sys_user</code> 表结构，为后续用户真实持久化和查询能力打基础</td>
      <td><code>user-service/pom.xml</code><br><code>SysUser.java</code><br><code>SysUserMapper.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内实现四大审计字段自动注入</td>
      <td>新增或修改数据时需要手动维护 <code>createTime</code>、<code>createBy</code>、<code>updateTime</code>、<code>updateBy</code></td>
      <td><code>SysUser</code> 审计字段标记 <code>FieldFill</code><br>新增 <code>MybatisAuditHandler</code><br>新增用户上下文拦截器读取 <code>userId</code></td>
      <td>把审计字段维护收敛到 MyBatis-Plus 自动填充处理器中，减少业务代码重复 set 字段</td>
      <td><code>SysUser.java</code><br><code>MybatisAuditHandler.java</code><br><code>UserContextInterceptor.java</code><br><code>WebMvcConfig.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内移除用户服务模拟数据并改为数据库查询</td>
      <td><code>verify</code> 使用固定账号 <code>zhou6/zhou6</code><br><code>mockInfo</code> 从 Redis 读取临时用户信息<br>用户服务依赖 <code>spring-boot-starter-data-redis</code></td>
      <td><code>verify</code> 按 <code>username</code> 查询 <code>sys_user</code> 并校验密码<br><code>info</code> 按用户 ID 查询 <code>sys_user</code><br>移除用户服务 Redis 依赖</td>
      <td>用户服务进入真实数据库读取阶段，不再保留固定账号、Mock 命名和 Redis 临时用户信息来源</td>
      <td><code>user-service/pom.xml</code><br><code>UserController.java</code><br><code>UserInfoService.java</code><br><code>UserInfoServiceImpl.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内补充用户服务 PostgreSQL 数据源配置</td>
      <td>Nacos <code>user-service-dev.yml</code> 仅配置 <code>server.port=52048</code>，用户服务无法初始化数据库连接</td>
      <td>Nacos <code>user-service-dev.yml</code> 新增 <code>spring.datasource</code><br>连接 <code>jdbc:postgresql://81.70.186.59:5432/postgres?currentSchema=zhou6</code><br>用户名 <code>postgres</code></td>
      <td>用户服务已改为从 <code>sys_user</code> 表查询，必须在启动阶段获取 PostgreSQL 数据源配置</td>
      <td>Nacos <code>user-service-dev.yml</code><br>用户服务启动配置</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内将 DTO 从 record 改为 Lombok 普通类</td>
      <td><code>VerifyResponse</code> 和 <code>UserInfoResponse</code> 使用 Java <code>record</code></td>
      <td>新增 <code>lombok</code> 依赖<br>DTO 改为普通类，并使用 <code>@Data</code>、<code>@NoArgsConstructor</code>、<code>@AllArgsConstructor</code></td>
      <td>按项目 DTO 风格使用类属性和 getter/setter，避免 record 不符合后续扩展习惯</td>
      <td><code>user-service/pom.xml</code><br><code>dto</code> 包</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内将用户主键策略调整为雪花 ID</td>
      <td><code>SysUser.id</code> 使用 <code>IdType.AUTO</code>，依赖数据库自增主键</td>
      <td><code>SysUser.id</code> 使用 <code>IdType.ASSIGN_ID</code>，由 MyBatis-Plus 默认雪花算法生成 <code>Long</code> 主键</td>
      <td>雪花算法需要 64 位整数承载，代码继续使用 <code>Long</code>，数据库字段应保持 <code>BIGINT</code></td>
      <td><code>SysUser.java</code><br><code>sys_user.id</code> 字段设计</td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内归档 Maven 依赖说明</td>
      <td>用户服务依赖说明曾位于旧目录 <code>doc/user-service/user-service_lib.md</code></td>
      <td>用户服务依赖说明迁移到 <code>a_doc/maven/user-service/user-service_lib.md</code></td>
      <td>按新的文档结构集中维护 Maven 相关说明，流水仍保留在当前初始化流水中</td>
      <td><code>a_doc/maven/user-service/user-service_lib.md</code></td>
    </tr>
<tr>
      <th>记录日期</th>
      <th>微服务名称</th>
      <th>初始化内容</th>
      <th>初始化前</th>
      <th>初始化后</th>
      <th>原因说明</th>
      <th>影响范围</th>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内统一只使用 bootstrap 配置文件</td>
      <td>端口和虚拟线程曾放在旧本地配置文件中</td>
      <td>删除旧本地配置文件<br>端口 <code>52048</code> 和虚拟线程配置合并到 <code>bootstrap.yml</code></td>
      <td>按项目约定统一只使用 <code>bootstrap.yml</code>，避免配置文件混用</td>
      <td><code>user-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内调整 Nacos 服务地址</td>
      <td><code>81.70.186.59:18848</code></td>
      <td><code>81.70.186.59:58848</code></td>
      <td>Nacos 服务端访问地址调整，账号密码保持 <code>nacos</code></td>
      <td><code>user-service/src/main/resources/bootstrap.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内删除本地虚拟线程重复配置</td>
      <td>本地 <code>bootstrap.yml</code> 声明 <code>spring.threads.virtual.enabled</code></td>
      <td>改由 Nacos <code>common-shared.yml</code> 统一提供虚拟线程配置</td>
      <td>虚拟线程属于所有 Java 21 服务可共享的公共运行配置</td>
      <td><code>user-service/src/main/resources/bootstrap.yml</code><br>Nacos <code>common-shared.yml</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内调整 Controller 入参并补充 Service 两层结构</td>
      <td>Controller 方法接收请求对象或 Header，并直接处理 Redis 读写逻辑</td>
      <td>Controller 方法只接收 <code>username</code>、<code>password</code>、<code>userId</code> 基础属性<br>新增 <code>UserInfoService</code> 接口和 <code>UserInfoServiceImpl</code> 实现类</td>
      <td>提升 Controller 方法复用性，并把业务逻辑下沉到 Service 层</td>
      <td><code>UserController.java</code><br><code>UserInfoService.java</code><br><code>UserInfoServiceImpl.java</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内新增用户联系方式字段</td>
      <td><code>sys_user</code>、实体和响应 DTO 不包含邮箱、联系电话</td>
      <td>新增 <code>email</code> 和 <code>contactPhone</code><br>数据库脚本新增 <code>email</code>、<code>contact_phone</code> 字段</td>
      <td>用户基础信息需要补充邮箱和联系电话，供登录上下文和用户信息接口复用</td>
      <td><code>SysUser.java</code><br><code>VerifyResponse.java</code><br><code>UserInfoResponse.java</code><br><code>a_doc/database</code></td>
    </tr>
<tr>
      <td>2026年05月24日</td>
      <td>user-service</td>
      <td>初始化期内改为从上下文获取当前登录人</td>
      <td><code>/userInfo/info</code> 接收 <code>userId</code> 请求参数</td>
      <td><code>/userInfo/info</code> 不再接收用户参数<br>拦截器解析 <code>Authorization</code>，再查询 Redis 登录会话快照并写入 <code>UserContextHolder</code></td>
      <td>Controller 不再关心请求头或用户 ID 参数，同时避免信任可伪造的用户 Header</td>
      <td><code>UserController.java</code><br><code>UserContextInterceptor.java</code><br><code>UserInfoServiceImpl.java</code></td>
    </tr>
  </tbody>
</table>


