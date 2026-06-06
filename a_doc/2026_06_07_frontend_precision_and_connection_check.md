# 2026_06_07 前端长整型精度与连接稳定性修复记录

## 1. 前端长整型精度修复

前端 JavaScript 对超过安全整数范围的数字会发生精度丢失。本次只调整对前端返回的 VO，不修改 Entity、Mapper、DTO 入参和数据库字段。

已调整为 `String` 的 VO 字段：

| 模块 | VO | 字段 |
| --- | --- | --- |
| auth-service | `TokenResponse` | `expiresIn` |
| file-service | `FileUploadVO` | `fileSize` |
| order-service | `OrderVO` | `userId` |
| user-service | `PageResponse` | `total`、`pageNum`、`pageSize` |
| user-service | `OrgUserVO` | `userId` |
| workflow-service | `WorkflowHomeSummaryVO` | `todoCount`、`pendingReviewCount`、`reviewedCount`、`rejectedCount` |
| workflow-service | `WorkflowDailyCountVO` | `todoCount`、`pendingReviewCount`、`reviewedCount`、`rejectedCount` |

说明：

- `DownloadFile` 虽然在 `vo` 包下，但只用于文件下载二进制响应，不直接作为 JSON 返回；其 `fileSize` 保持 `Long`，用于 `ResponseEntity.contentLength`。
- DTO 入参中的 `Long` 暂不调整，避免影响已有前端提交参数和服务间调用。
- `PageResponse` 保留接收 `long` 的构造方法，内部统一转字符串，减少分页服务实现改动。

## 2. Redis 连接稳定性排查

现状：

- Redis 统一配置在 `a_doc/nacos/common-shared.yml`。
- 所有使用 Redis 的模块均已引入 `commons-pool2`，连接池配置可以生效。
- Redis 是远程地址 `81.70.186.59:6379`，跨公网或跨云网络时，空闲连接容易被中间网络设备回收。

本次调整：

- `spring.data.redis.lettuce.pool.time-between-eviction-runs` 设置为 `30s`，让连接池定期巡检空闲连接，降低拿到失效连接的概率。

后续如果仍出现掉线，建议继续确认：

1. Redis 服务端 `timeout`、`tcp-keepalive` 配置。
2. 云服务器安全组、防火墙、NAT 网关是否会回收空闲 TCP 连接。
3. 应用日志中是否是 `RedisCommandTimeoutException`、`Connection reset`、`Connection refused`，三者对应原因不同。

## 3. PostgreSQL 连接稳定性排查

现状：

- user、file、account、order、workflow 都通过 Nacos 配置连接 PostgreSQL。
- Hikari 已配置 `max-lifetime=1500000`、`keepalive-time=300000`，总体方向正确。
- PostgreSQL 也是远程地址 `81.70.186.59:5432`，跨公网空闲连接同样可能被中间网络设备回收。

本次调整：

- 所有 PostgreSQL JDBC URL 增加 `tcpKeepAlive=true`。
- workflow-service 保留 `stringtype=unspecified`，并追加 `tcpKeepAlive=true`。

后续如果仍出现掉线，建议继续确认：

1. PostgreSQL 服务端 `tcp_keepalives_idle`、`tcp_keepalives_interval`、`tcp_keepalives_count`。
2. 云服务器安全组、防火墙、NAT 网关的 TCP idle timeout。
3. 应用日志中是否出现 `Connection is not available`、`This connection has been closed`、`Connection reset`。
4. 数据库最大连接数是否小于所有微服务 Hikari 连接池上限总和。
