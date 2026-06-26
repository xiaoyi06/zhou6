# zhou6 接口契约清单

生成时间：2026-06-06

## 通用返回格式

普通 JSON 接口统一返回 `R<T>`：

```json
{
  "code": "000000",
  "message": "请求成功",
  "data": {},
  "traceId": null
}
```

文件下载和 Excel 导出接口返回二进制响应，不包裹 `R<T>`。

## 路径说明

本次改造后，所有 Controller 只保留 `/api/v1` 版本路径；旧路径已移除。

`/api/v1` 定义在 `common-server` 的 `ApiPathConstants`；各微服务接口根路径定义在各自服务内的 `*ApiPathConstants`：

`common-util/common-server/src/main/java/com/zhou6/cloud/common/constant/ApiPathConstants.java`

- `auth-service/src/main/java/com/zhou6/cloud/auth/constant/AuthApiPathConstants.java`
- `file-service/src/main/java/com/zhou6/cloud/file/constant/FileApiPathConstants.java`
- `account-service/src/main/java/com/zhou6/cloud/account/constant/AccountApiPathConstants.java`
- `order-service/src/main/java/com/zhou6/cloud/order/constant/OrderApiPathConstants.java`
- `workflow-service/src/main/java/com/zhou6/cloud/workflow/constant/WorkflowApiPathConstants.java`
- `message-service/src/main/java/com/zhou6/cloud/message/constant/MessageApiPathConstants.java`
- `community-service/src/main/java/com/zhou6/cloud/community/constant/CommunityApiPathConstants.java`
- `user-service/src/main/java/com/zhou6/cloud/user/constant/UserApiPathConstants.java`
- `sys-service/src/main/java/com/zhou6/cloud/sys/constant/SysApiPathConstants.java`

## sys-service

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 分页查询 IP 黑名单 | POST | `/api/v1/sys-api/ipBlacklist/page` | `IpBlacklistQueryDTO` | `R<PageResponse<IpBlacklistVO>>` |
| 新增 IP 黑名单 | POST | `/api/v1/sys-api/ipBlacklist/add` | `IpBlacklistDTO` | `R<Void>` |
| 修改 IP 黑名单 | POST | `/api/v1/sys-api/ipBlacklist/edit` | `IpBlacklistDTO` | `R<Void>` |
| 批量删除 IP 黑名单 | POST | `/api/v1/sys-api/ipBlacklist/delete` | `IpBlacklistDeleteDTO` | `R<Void>` |
| 新增手工待办 | POST | `/api/v1/sys-api/todo/add` | `TodoSaveDTO`；用户由网关 JWT 识别 | `R<Void>` |
| 修改手工待办 | POST | `/api/v1/sys-api/todo/edit` | `TodoSaveDTO`；用户由网关 JWT 识别 | `R<Void>` |
| 完成手工待办 | POST | `/api/v1/sys-api/todo/complete` | `IdDTO`；用户由网关 JWT 识别 | `R<Void>` |
| 取消手工待办 | POST | `/api/v1/sys-api/todo/cancel` | `IdDTO`；用户由网关 JWT 识别 | `R<Void>` |
| 分页查询我的待办 | POST | `/api/v1/sys-api/todo/page` | `TodoPageQueryDTO`；用户由网关 JWT 识别 | `R<PageResponse<TodoVO>>` |
| 日历查询我的待办 | POST | `/api/v1/sys-api/todo/calendar` | `TodoPageQueryDTO`；用户由网关 JWT 识别 | `R<List<TodoVO>>` |
| 查询未读提醒 | POST | `/api/v1/sys-api/todo/reminder/unread` | 当前登录用户 | `R<List<TodoVO>>` |
| 标记提醒已读 | POST | `/api/v1/sys-api/todo/reminder/read` | `TodoReminderReadDTO`；用户由网关 JWT 识别 | `R<Void>` |
| 查询未读提醒数量 | POST | `/api/v1/sys-api/todo/reminder/unreadCount` | 当前登录用户 | `R<String>` |
| 分页查询菜单访问汇总 | POST | `/api/v1/sys-api/apiUsage/menuPage` | `MenuUsageSummaryQueryDTO`（可选 `userId`，传入时仅统计该用户） | `R<PageResponse<MenuUsageSummaryVO>>` |
| 管理员分页查询指定菜单的访问人员 | POST | `/api/v1/sys-api/apiUsage/menuUserPage` | `MenuUsageUserQueryDTO` | `R<PageResponse<MenuUsageUserVO>>` |
| 立即同步菜单访问统计 | POST | `/api/v1/sys-api/apiUsage/sync` | 无请求体 | `R<Void>` |
| 清空菜单访问统计 | POST | `/api/v1/sys-api/apiUsage/clear` | 无请求体 | `R<Void>` |
| 分页查询流量统计 | POST | `/api/v1/sys-api/traffic/page` | `TrafficQueryDTO` | `R<PageResponse<SysTrafficStat>>` |
| 立即同步流量统计 | POST | `/api/v1/sys-api/traffic/sync` | 无请求体 | `R<Void>` |
| 清空流量统计 | POST | `/api/v1/sys-api/traffic/clear` | 无请求体 | `R<Void>` |

`IpBlacklistDTO` 字段：`id:String`（编辑时必填）、`ipAddress:String`、`isStatus:Integer`（`0` 禁用，`1` 启用）、`remark:String`。启用后网关会立即拒绝对应 IP 的所有请求并返回 HTTP 403；IP 支持 IPv4 和 IPv6，服务端会规范化保存。

`TodoSaveDTO` 字段：`id:String`（编辑时必填）、`title:String`、`content:String`（可空）、`remindTime:LocalDateTime`（格式 `yyyy-MM-dd HH:mm:ss`）。待办仅能由创建用户操作，`/delete` 是 `/cancel` 的兼容入口，实际保留取消历史。

`TodoPageQueryDTO` 字段：`status:String`（`TODO`、`DONE`、`CANCELLED`）、`timeType:String`（`REMIND` 提醒时间、`FINISH` 完成/取消时间、`CREATE` 创建时间，可空）、`beginTime:LocalDateTime`、`endTime:LocalDateTime`、`pageNum:Integer`、`pageSize:Integer`。日历查询必须传完整时间范围。`timeType` 不传时：`TODO` 默认按 `remindTime` 查，`DONE/CANCELLED` 默认按 `finishTime` 查。`TodoVO` 返回待办来源 `sourceType`（`MANUAL`、`WORKFLOW`）、来源标识 `sourceId`、完成方 `completeMode`（`USER`、`WORKFLOW`）、完成/取消时间 `finishTime`、提醒状态 `remindStatus`（`PENDING`、`SENT`、`READ`）和过期标识 `expired:Boolean`；流程待办在前端应只读。

`TodoReminderReadDTO` 字段：`ids:List<String>`。待办提醒前端统一走 `message-service` 消息中心 WebSocket；这些待办提醒接口保留为兼容兜底，不建议新页面继续轮询。到期扫描周期由 Nacos 配置 `zhou6.sys.todo.reminder-scan-delay-ms` 控制，默认 5 秒。

待办新增/编辑/流程 upsert 时，如果提醒时间已经到期，会在保存提交后立即尝试生成 `TODO` 类型站内消息；未立即命中的待办由定时扫描兜底。扫描 claim 成功后，会通过服务发现调用 `message-service` 内部发送接口生成消息；如运行环境不使用 Nacos 服务发现，可配置 `zhou6.message.base-url` 指向消息服务内网地址。

`/todo/page` 和 `/todo/calendar` 查询前也会触发一次到期扫描兜底；前端列表标红优先使用 `expired=true`，不要自行猜测字段名。当前页面收到消息中心 WebSocket 的 `messageType=TODO` 事件后，应刷新当前日期的 `/todo/calendar` 或当前分页 `/todo/page`。

以下内部接口不经网关暴露，供 `workflow-service` 等服务通过内网调用：`POST /api/v1/sys-api/internal/todo/upsertWorkflowTodo`（`WorkflowTodoUpsertDTO`）、`/completeBySource` 与 `/cancelBySource`（`WorkflowTodoStatusDTO`）。流程待办以 `userId + sourceId` 幂等；其中 `sourceId` 必须使用流程任务 ID，不能复用整条流程实例 ID。

## message-service

消息中心是独立微服务，负责站内/站外通知落库、查询、已读状态和 WebSocket 实时推送；消息只做通知，不承载业务处理。浏览器访问内部接口会被网关禁止。

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 分页查询我的消息 | POST | `/api/v1/message-api/message/page` | `MessagePageQueryDTO`；用户由网关 JWT 识别 | `R<PageResponse<MessageVO>>` |
| 查询未读消息数量 | POST | `/api/v1/message-api/message/unreadCount` | `MessageUnreadCountDTO`，可为空 | `R<String>` |
| 批量标记消息已读 | POST | `/api/v1/message-api/message/read` | `MessageReadDTO`；`ids` 为接收人消息 ID | `R<Void>` |
| 全部已读 | POST | `/api/v1/message-api/message/readAll` | `MessageReadAllDTO`，可为空 | `R<Void>` |
| 内部发送消息 | POST | `/api/v1/message-api/internal/message/send` | `InternalMessageSendDTO`；仅内网服务调用 | `R<Void>` |

`MessagePageQueryDTO` 字段：`channel:String`（`INTERNAL` 站内、`EXTERNAL` 站外，可空查全部）、`messageType:String`、`readStatus:String`（`UNREAD`、`READ`）、`beginTime:LocalDateTime`、`endTime:LocalDateTime`、`pageNum:Integer`、`pageSize:Integer`。

`MessageVO` 字段：`id:String`（接收人消息 ID，已读接口传它）、`messageId:String`（消息主体 ID）、`channel:String`、`messageType:String`、`messageTypeName:String`、`title:String`、`content:String`、`sourceType:String`、`sourceName:String`、`sourceId:String`、`businessType:String`、`businessId:String`、`readStatus:String`、`sendTime:LocalDateTime`、`readTime:LocalDateTime`、`linkType:String`、`linkUrl:String`、`linkParams:String`。

`InternalMessageSendDTO` 字段：`channel:String`（默认 `INTERNAL`）、`messageType:String`、`title:String`、`content:String`、`sourceType:String`、`sourceName:String`、`sourceId:String`、`businessType:String`、`businessId:String`、`receiverUserIds:List<String>`、`linkType:String`（`ROUTE`、`URL`、`NONE`）、`linkUrl:String`、`linkParams:String`。当 `channel + sourceType + sourceId` 相同时，消息主体幂等复用，接收人维度通过 `messageId + userId` 防重复。

WebSocket 连接地址：`/ws/message?access_token=<JWT>`；订阅地址：`/user/queue/messages`。后端推送事件格式：

```json
{
  "eventType": "MESSAGE_CREATED",
  "unreadCount": "7",
  "message": {
    "id": "10001",
    "channel": "INTERNAL",
    "messageType": "TODO",
    "messageTypeName": "待办",
    "title": "待办提醒：处理权限申请",
    "sourceName": "待办中心",
    "sendTime": "2026-06-25 10:15:00",
    "readStatus": "UNREAD"
  }
}
```

网关本地安全规则已禁止 `/api/v1/message-api/internal/**`。Nacos 配置样例已补充到 `a_doc/nacos/message-service-dev.yml` 和 `a_doc/nacos/gateway-service-dev.yml`；由于 `message-service` 使用 `/message` 上下文路径，网关 HTTP 与 WebSocket 路由都需要 `PrefixPath=/message`。

## community-service

交流社区是独立微服务，负责帖子、评论树、点赞收藏、用户关注/喜欢/拉黑、@ 候选、帖子热度、置顶/加精、删除恢复，以及审核和标签能力的预留。当前版本发帖直接发布并写入 `auditStatus=APPROVED`，后续接入审核时可扩展为 `PENDING/APPROVED/REJECTED`。

帖子图片、gif 等资源仍由 `file-service` 上传；社区发帖接口只接收上传成功后的 `fileId` 作为帖子资源绑定关系。

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 发布帖子 | POST | `/api/v1/community-api/post/add` | `PostSaveDTO` | `R<String>`，帖子 ID |
| 分页查询帖子 | POST | `/api/v1/community-api/post/page` | `PostPageQueryDTO` | `R<PageResponse<PostVO>>` |
| 查询热门帖子 | POST | `/api/v1/community-api/post/hot` | `PostPageQueryDTO` | `R<PageResponse<PostVO>>` |
| 查询帖子详情 | POST | `/api/v1/community-api/post/detail` | `IdDTO` | `R<PostVO>` |
| 删除帖子 | POST | `/api/v1/community-api/post/delete` | `IdDTO` | `R<Void>` |
| 恢复帖子 | POST | `/api/v1/community-api/post/restore` | `IdDTO` | `R<Void>` |
| 审核帖子 | POST | `/api/v1/community-api/post/audit` | `PostAuditDTO` | `R<Void>` |
| 置顶帖子 | POST | `/api/v1/community-api/post/top` | `IdDTO` | `R<Void>` |
| 取消置顶 | POST | `/api/v1/community-api/post/untop` | `IdDTO` | `R<Void>` |
| 加精帖子 | POST | `/api/v1/community-api/post/feature` | `IdDTO` | `R<Void>` |
| 取消加精 | POST | `/api/v1/community-api/post/unfeature` | `IdDTO` | `R<Void>` |
| 点赞帖子 | POST | `/api/v1/community-api/post/like` | `IdDTO` | `R<Void>` |
| 取消点赞帖子 | POST | `/api/v1/community-api/post/unlike` | `IdDTO` | `R<Void>` |
| 收藏帖子 | POST | `/api/v1/community-api/post/favorite` | `IdDTO` | `R<Void>` |
| 取消收藏帖子 | POST | `/api/v1/community-api/post/unfavorite` | `IdDTO` | `R<Void>` |
| 新增评论/回复 | POST | `/api/v1/community-api/comment/add` | `CommentSaveDTO` | `R<String>`，评论 ID |
| 查询评论树 | POST | `/api/v1/community-api/comment/tree` | `CommentTreeQueryDTO` | `R<List<CommentVO>>` |
| 删除评论 | POST | `/api/v1/community-api/comment/delete` | `IdDTO` | `R<Void>` |
| 点赞评论 | POST | `/api/v1/community-api/comment/like` | `IdDTO` | `R<Void>` |
| 取消点赞评论 | POST | `/api/v1/community-api/comment/unlike` | `IdDTO` | `R<Void>` |
| 关注用户 | POST | `/api/v1/community-api/relation/follow` | `UserIdDTO` | `R<Void>` |
| 取消关注用户 | POST | `/api/v1/community-api/relation/unfollow` | `UserIdDTO` | `R<Void>` |
| 喜欢用户 | POST | `/api/v1/community-api/relation/favoriteUser` | `UserIdDTO` | `R<Void>` |
| 取消喜欢用户 | POST | `/api/v1/community-api/relation/unfavoriteUser` | `UserIdDTO` | `R<Void>` |
| 拉黑用户 | POST | `/api/v1/community-api/relation/block` | `UserIdDTO` | `R<Void>` |
| 取消拉黑用户 | POST | `/api/v1/community-api/relation/unblock` | `UserIdDTO` | `R<Void>` |
| 我的关注列表 | POST | `/api/v1/community-api/relation/followPage` | `PageQueryDTO` | `R<PageResponse<RelationUserVO>>` |
| 我的喜欢用户列表 | POST | `/api/v1/community-api/relation/favoriteUserPage` | `PageQueryDTO` | `R<PageResponse<RelationUserVO>>` |
| 我的拉黑列表 | POST | `/api/v1/community-api/relation/blockPage` | `PageQueryDTO` | `R<PageResponse<RelationUserVO>>` |
| @ 用户候选 | POST | `/api/v1/community-api/mention/candidates` | `MentionCandidateQueryDTO` | `R<PageResponse<MentionCandidateVO>>` |
| 预留帖子标签关系 | POST | `/api/v1/community-api/tag/reservePostTags` | `TagReserveDTO` | `R<Void>` |

`PostSaveDTO` 字段：`title:String`、`content:String`、`resources:List<PostResourceDTO>`、`mentionUserIds:List<String>`、`tagIds:List<String>`。`PostResourceDTO` 字段：`fileId:String`、`resourceType:String`（`IMAGE`、`GIF` 等）、`sortOrder:Integer`。`mentionUserIds` 只记录 @ 关系，不调用 `message-service`。

`PostAuditDTO` 字段：`id:String`、`auditStatus:String`（`PENDING`、`APPROVED`、`REJECTED`）、`publishStatus:String`（`PUBLISHED`、`HIDDEN`）。

`PostPageQueryDTO` 字段：`scope:String`（`ALL` 全部、`FOLLOWING` 关注用户、`FAVORITE_USER` 喜欢用户、`MINE` 我的帖子、`FAVORITED_POST` 我收藏的帖子）、`keyword:String`、`auditStatus:String`（预留）、`publishStatus:String`（预留）、`tagIds:List<String>`、`beginTime:LocalDateTime`、`endTime:LocalDateTime`、`pageNum:Integer`、`pageSize:Integer`。当前查询默认只返回 `PUBLISHED + APPROVED + 未删除` 的帖子，并排除当前用户拉黑的人。

`PostVO` 字段：`id:String`、`authorUserId:String`、`title:String`、`content:String`、`auditStatus:String`、`publishStatus:String`、`top:Boolean`、`featured:Boolean`、`viewCount:String`、`likeCount:String`、`commentCount:String`、`favoriteCount:String`、`heatScore:BigDecimal`、`liked:Boolean`、`favorited:Boolean`、`resources:List<PostResourceVO>`、`tags:List<TagVO>`、`createTime:LocalDateTime`、`updateTime:LocalDateTime`。

`CommentSaveDTO` 字段：`postId:String`、`parentId:String`（可空；不为空时回复任意层评论）、`replyToUserId:String`、`content:String`、`mentionUserIds:List<String>`。评论表使用 `parentId + rootId` 支持多层评论树。

`CommentVO` 字段：`id:String`、`postId:String`、`parentId:String`、`rootId:String`、`authorUserId:String`、`replyToUserId:String`、`content:String`、`likeCount:String`、`liked:Boolean`、`createTime:LocalDateTime`、`children:List<CommentVO>`。

`MentionCandidateQueryDTO` 字段：`keyword:String`、`pageNum:Integer`、`pageSize:Integer`。`keyword` 为空时优先返回当前用户喜欢和关注的用户；有关键词时调用 `user-service` 用户分页接口按账号/昵称模糊查询，并将关注/喜欢用户排前。拉黑用户会从候选中过滤。

点赞、取消点赞、收藏、取消收藏、评论点赞、评论取消点赞和浏览量属于高频互动，接口先写 Redis 热层并返回，随后通过 RabbitMQ 异步落库到明细表。Redis Lua 脚本会原子完成状态变更、计数变更和 pending 事件写入；如果 MQ 即时投递失败，`community-service` 会通过 `zhou6:community:interaction:pending` 定时补偿投递。DB 明细表仍是最终持久化来源，消费者落库时按唯一键幂等处理，并按明细表重算对应计数。

Redis Key 约定：

- `zhou6:community:post:like:{postId}`：帖子点赞用户 Set
- `zhou6:community:post:like:count:{postId}`：帖子点赞数
- `zhou6:community:post:favorite:{postId}`：帖子收藏用户 Set
- `zhou6:community:post:favorite:count:{postId}`：帖子收藏数
- `zhou6:community:post:view:count:{postId}`：帖子浏览数
- `zhou6:community:comment:like:{commentId}`：评论点赞用户 Set
- `zhou6:community:comment:like:count:{commentId}`：评论点赞数
- `zhou6:community:interaction:pending`：待补偿互动事件 ZSet

热度由 `community-service` 定时刷新，默认周期由 Nacos 配置 `zhou6.community.heat-refresh-delay-ms=300000` 控制；当前公式会优先读取 Redis 热层中的浏览、点赞、收藏计数，并结合评论数、置顶、加精和发布时间衰减计算综合分。DDL 位于 `a_doc/database/2026_06_26_community.sql`。

## auth-service

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 登录 | POST | `/api/v1/auth-api/login` | `LoginRequest` | `R<TokenResponse>` |
| 刷新令牌 | POST | `/api/v1/auth-api/refresh` | `RefreshRequest` | `R<TokenResponse>` |
| 退出登录 | POST | `/api/v1/auth-api/logout` | `LogoutRequest`，可为空 | `R<Void>` |

## file-service

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 上传文件 | POST | `/api/v1/file-api/upload` | multipart `file` | `R<FileUploadVO>` |
| 查询文件详情 | POST | `/api/v1/file-api/detail` | `FileIdDTO` | `R<FileUploadVO>` |
| 删除文件 | POST | `/api/v1/file-api/delete` | `FileIdDTO` | `R<Void>` |
| 判断文件是否存在 | POST | `/api/v1/file-api/exists` | `FileIdDTO` | `R<Boolean>` |
| 下载文件 | POST | `/api/v1/file-api/download` | `FileIdDTO` | `ResponseEntity<InputStreamResource>` |

## account-service

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 查询首页现金看板 | POST | `/api/v1/account-api/summary` | `AccountUserDTO` | `R<AccountSummaryVO>` |
| 现金预冻结 | POST | `/api/v1/account-api/freeze` | `AccountAmountDTO` | `R<Boolean>` |
| 现金入账 | POST | `/api/v1/account-api/credit` | `AccountAmountDTO` | `R<Void>` |
| 现金出账 | POST | `/api/v1/account-api/debit` | `AccountAmountDTO` | `R<Void>` |
| 红字冲正 | POST | `/api/v1/account-api/reverse` | `AccountReverseDTO` | `R<Void>` |

## order-service

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 创建并支付订单 | POST | `/api/v1/order-api/createPay` | `OrderCreateDTO` | `R<OrderVO>` |
| 查询订单详情 | POST | `/api/v1/order-api/detail` | `OrderSnDTO` | `R<OrderVO>` |
| 同意退款 | POST | `/api/v1/order-api/refund/approve` | `OrderRefundDTO` | `R<Void>` |

## workflow-service

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 查询待办任务 | GET | `/api/v1/workflow-api/process/todo` | query `userId` | `R<List<TaskVO>>` |
| 查询已办任务 | GET | `/api/v1/workflow-api/process/done` | query `userId` | `R<List<HistoricTaskVO>>` |
| 查询首页流程统计 | GET | `/api/v1/workflow-api/process/home/summary` | 当前登录用户 | `R<WorkflowHomeSummaryVO>` |
| 启动流程实例 | POST | `/api/v1/workflow-api/inner/start` | `ProcessStartDTO` | `R<String>` |
| 办理流程任务 | POST | `/api/v1/workflow-api/inner/complete` | `TaskCompleteDTO` | `R<Void>` |
| 终止流程实例 | POST | `/api/v1/workflow-api/inner/terminate` | `ProcessTerminateDTO` | `R<Void>` |
| 分页查询流程配置 | POST | `/api/v1/workflow-api/config/model/page` | `ModelQueryDTO` | `R<PageResponse<ModelVO>>` |
| 查询流程配置详情 | POST | `/api/v1/workflow-api/config/model/getById` | `ModelIdDTO` | `R<ModelDetailVO>` |
| 保存流程配置 | POST | `/api/v1/workflow-api/config/model/save` | `ModelSaveDTO` | `R<String>` |
| 删除流程配置 | POST | `/api/v1/workflow-api/config/model/delete` | `ModelIdDTO` | `R<Void>` |
| 部署流程 | POST | `/api/v1/workflow-api/config/deploy` | `DeployDTO` | `R<DeployResultVO>` |
| 分页查询流程定义 | POST | `/api/v1/workflow-api/config/definition/page` | `DefinitionQueryDTO` | `R<PageResponse<DefinitionVO>>` |
| 挂起流程定义 | POST | `/api/v1/workflow-api/config/definition/suspend` | `DefinitionIdDTO` | `R<Void>` |
| 激活流程定义 | POST | `/api/v1/workflow-api/config/definition/activate` | `DefinitionIdDTO` | `R<Void>` |
| 删除部署 | POST | `/api/v1/workflow-api/config/deployment/delete` | `DeploymentIdDTO` | `R<Void>` |

## user-service

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 校验登录账号密码 | POST | `/api/v1/user-api/userInfo/verify` | `VerifyRequest` | `R<VerifyResponse>` |
| 查询当前用户信息 | POST | `/api/v1/user-api/userInfo/info` | 无 | `R<UserInfoResponse>` |
| 按登录账号查询当前用户信息 | POST | `/api/v1/user-api/userInfo/infoByUsername` | 无 | `R<UserInfoResponse>` |
| 修改当前用户头像 | POST | `/api/v1/user-api/userInfo/avatar` | `UserAvatarDTO` | `R<Void>` |
| 修改当前用户密码 | POST | `/api/v1/user-api/userInfo/changePassword` | `UserChangePasswordDTO` | `R<Void>` |
| 分页查询用户 | POST | `/api/v1/user-api/userManagement/page` | `UserQueryDTO` | `R<PageResponse<UserManageVO>>` |
| 查询用户已分配角色 | POST | `/api/v1/user-api/userManagement/roles` | `UserRoleQueryDTO` | `R<PageResponse<RoleVO>>` |
| 查询用户未分配角色 | POST | `/api/v1/user-api/userManagement/unassignedRoles` | `UserRoleQueryDTO` | `R<PageResponse<RoleVO>>` |
| 全量分配用户角色 | POST | `/api/v1/user-api/userManagement/assignRoles` | `UserAssignRolesDTO` | `R<Void>` |
| 新增用户 | POST | `/api/v1/user-api/userManagement/add` | `UserSaveDTO` | `R<Void>` |
| 修改用户 | POST | `/api/v1/user-api/userManagement/edit` | `UserSaveDTO` | `R<Void>` |
| 删除用户 | POST | `/api/v1/user-api/userManagement/delete` | `UserDeleteDTO` | `R<Void>` |
| 修改用户状态 | POST | `/api/v1/user-api/userManagement/changeStatus` | `UserChangeStatusDTO` | `R<Void>` |
| 重置用户密码 | POST | `/api/v1/user-api/userManagement/resetPassword` | `UserResetPasswordDTO` | `R<Void>` |
| 查询用户详情 | POST | `/api/v1/user-api/userManagement/getById` | `UserIdDTO` | `R<UserManageVO>` |
| 导出用户列表 | POST | `/api/v1/user-api/userManagement/export` | `UserQueryDTO` | `ResponseEntity<Void>`，Excel 写入响应 |
| 分页查询角色 | POST | `/api/v1/user-api/role/page` | `RoleQueryDTO` | `R<PageResponse<RoleVO>>` |
| 新增角色 | POST | `/api/v1/user-api/role/add` | `RoleSaveDTO` | `R<Void>` |
| 修改角色 | POST | `/api/v1/user-api/role/edit` | `RoleSaveDTO` | `R<Void>` |
| 删除角色 | POST | `/api/v1/user-api/role/delete` | `RoleIdDTO` | `R<Void>` |
| 修改角色状态 | POST | `/api/v1/user-api/role/changeStatus` | `RoleChangeStatusDTO` | `R<Void>` |
| 查询角色用户 | POST | `/api/v1/user-api/role/config/users` | `RoleUserQueryDTO` | `R<PageResponse<RoleUserVO>>` |
| 给角色分配用户 | POST | `/api/v1/user-api/role/config/assignUsers` | `RoleAssignUsersDTO` | `R<Void>` |
| 取消用户角色 | POST | `/api/v1/user-api/role/config/removeUser` | `RoleRemoveUserDTO` | `R<Void>` |
| 配置角色数据权限 | POST | `/api/v1/user-api/role/config/dataScope` | `RoleDataScopeDTO` | `R<Void>` |
| 批量新增角色菜单 | POST | `/api/v1/user-api/role/config/addMenus` | `MenuAssignDTO` | `R<Void>` |
| 查询角色已配置菜单 | POST | `/api/v1/user-api/role/config/menus` | `RoleMenuQueryDTO` | `R<List<MenuVO>>` |
| 分页查询岗位 | POST | `/api/v1/user-api/post/page` | `PostQueryDTO` | `R<PageResponse<PostVO>>` |
| 新增岗位 | POST | `/api/v1/user-api/post/add` | `PostSaveDTO` | `R<Void>` |
| 修改岗位 | POST | `/api/v1/user-api/post/edit` | `PostSaveDTO` | `R<Void>` |
| 删除岗位 | POST | `/api/v1/user-api/post/delete` | `PostIdDTO` | `R<Void>` |
| 修改岗位状态 | POST | `/api/v1/user-api/post/changeStatus` | `PostChangeStatusDTO` | `R<Void>` |
| 查询岗位人员 | POST | `/api/v1/user-api/post/config/users` | `PostUserQueryDTO` | `R<List<PostUserVO>>` |
| 分配用户岗位 | POST | `/api/v1/user-api/post/config/assignUsers` | `PostAssignDTO` | `R<Void>` |
| 取消用户岗位 | POST | `/api/v1/user-api/post/config/removeUser` | `PostRemoveUserDTO` | `R<Void>` |
| 新增组织机构 | POST | `/api/v1/user-api/organization/add` | `OrgAddDTO` | `R<Void>` |
| 修改组织机构 | POST | `/api/v1/user-api/organization/edit` | `OrgEditDTO` | `R<Void>` |
| 删除组织机构 | POST | `/api/v1/user-api/organization/delete` | `OrgIdDTO` | `R<Void>` |
| 修改组织机构状态 | POST | `/api/v1/user-api/organization/changeStatus` | `OrgChangeStatusDTO` | `R<Void>` |
| 查询组织机构详情 | POST | `/api/v1/user-api/organization/getById` | `OrgIdDTO` | `R<OrgDetailVO>` |
| 查询组织机构树 | POST | `/api/v1/user-api/organization/getTree` | `OrgTreeQueryDTO` | `R<List<OrgTreeVO>>` |
| 导出组织机构列表 | POST | `/api/v1/user-api/organization/export` | `OrgTreeQueryDTO` | `ResponseEntity<Void>`，Excel 写入响应 |
| 查询直接子级组织机构 | POST | `/api/v1/user-api/organization/getChildren` | `OrgChildrenQueryDTO` | `R<List<OrgDetailVO>>` |
| 查询所有子级组织机构 | POST | `/api/v1/user-api/organization/getDescendants` | `OrgIdDTO` | `R<List<OrgDetailVO>>` |
| 分页查询组织机构人员 | POST | `/api/v1/user-api/organization/user/page` | `OrgUserPageDTO` | `R<PageResponse<OrgUserVO>>` |
| 向组织机构添加人员 | POST | `/api/v1/user-api/organization/user/add` | `OrgUserAddDTO` | `R<Void>` |
| 从组织机构移除人员 | POST | `/api/v1/user-api/organization/user/remove` | `OrgUserRemoveDTO` | `R<Void>` |
| 设置用户主组织机构 | POST | `/api/v1/user-api/organization/user/setPrimary` | `OrgUserSetPrimaryDTO` | `R<Void>` |
| 查询菜单树 | POST | `/api/v1/user-api/menu/getTree` | `MenuQueryDTO` | `R<List<MenuVO>>` |
| 查询用户菜单权限 | POST | `/api/v1/user-api/menu/userMenus` | `UserMenuQueryDTO` | `R<List<MenuVO>>` |
| 新增菜单 | POST | `/api/v1/user-api/menu/add` | `MenuSaveDTO` | `R<Void>` |
| 修改菜单 | POST | `/api/v1/user-api/menu/edit` | `MenuSaveDTO` | `R<Void>` |
| 删除菜单 | POST | `/api/v1/user-api/menu/delete` | `MenuIdDTO` | `R<Void>` |
| 获取当前用户路由 | POST | `/api/v1/user-api/menu/getRouters` | 可为空 | `R<List<RouterVO>>` |
| 给角色分配菜单 | POST | `/api/v1/user-api/menu/assign` | `MenuAssignDTO` | `R<Void>` |
| 查询菜单已配置角色 | POST | `/api/v1/user-api/menu/roles` | `MenuRoleQueryDTO` | `R<PageResponse<RoleVO>>` |
| 给菜单新增角色配置 | POST | `/api/v1/user-api/menu/assignRoles` | `MenuAssignRolesDTO` | `R<Void>` |

## 请求和响应字段

### auth-service DTO/VO

- `LoginRequest`: `username:String`, `password:String`
- `RefreshRequest`: `refreshToken:String`
- `LogoutRequest`: `refreshToken:String`
- `TokenResponse`: `accessToken:String`, `refreshToken:String`, `tokenType:String`, `expiresIn:String`

### file-service DTO/VO

- `FileIdDTO`: `id:String`
- `FileUploadVO`: `id:String`, `fileName:String`, `fileSize:String`, `contentType:String`, `objectKey:String`, `platform:String`, `url:String`

### account-service DTO/VO

- `AccountUserDTO`: `userId:Long`
- `AccountAmountDTO`: `userId:Long`, `amount:BigDecimal`, `bizType:String`, `bizId:String`, `operatorId:Long`, `remark:String`
- `AccountReverseDTO`: `origBizType:String`, `origBizId:String`, `reverseBizType:String`, `reverseBizId:String`, `adminId:Long`
- `AccountSummaryVO`: `available:BigDecimal`, `frozen:BigDecimal`, `settling:BigDecimal`, `total:BigDecimal`

### order-service DTO/VO

- `OrderCreateDTO`: `totalAmount:BigDecimal`, `payAmount:BigDecimal`
- `OrderSnDTO`: `orderSn:String`
- `OrderRefundDTO`: `orderSn:String`
- `OrderVO`: `orderSn:String`, `userId:String`, `totalAmount:BigDecimal`, `payAmount:BigDecimal`, `orderStatus:Integer`, `createTime:LocalDateTime`

### workflow-service DTO/VO

- `ProcessStartDTO`: `processKey:String`, `businessKey:String`, `variables:Map<String,Object>`
- `TaskCompleteDTO`: `taskId:String`, `variables:Map<String,Object>`, `comment:String`
- `ProcessTerminateDTO`: `businessKey:String`, `reason:String`
- `WorkflowEventMessage`: `businessKey:String`, `status:String`, `reason:String`, `eventTime:LocalDateTime`
- `TaskVO`: `taskId:String`, `taskName:String`, `processInstanceId:String`, `businessKey:String`, `createTime:Date`
- `HistoricTaskVO`: `taskId:String`, `taskName:String`, `processInstanceId:String`, `businessKey:String`, `endTime:Date`
- `ModelQueryDTO`: `name:String`, `key:String`, `category:String`, `pageNum:Integer`, `pageSize:Integer`
- `ModelSaveDTO`: `id:String`, `name:String`, `key:String`, `category:String`, `bpmnXml:String`
- `ModelIdDTO`: `id:String`
- `DeployDTO`: `modelId:String`
- `DefinitionQueryDTO`: `processKey:String`, `category:String`, `pageNum:Integer`, `pageSize:Integer`
- `DefinitionIdDTO`: `id:String`
- `DeploymentIdDTO`: `id:String`
- `ModelVO`: `id:String`, `name:String`, `key:String`, `category:String`, `version:Integer`, `createTime:String`, `lastUpdateTime:String`, `deploymentId:String`
- `ModelDetailVO`: `id:String`, `name:String`, `key:String`, `category:String`, `version:Integer`, `bpmnXml:String`, `createTime:String`, `lastUpdateTime:String`, `deploymentId:String`
- `DefinitionVO`: `id:String`, `key:String`, `name:String`, `category:String`, `version:Integer`, `deploymentId:String`, `suspended:Boolean`, `tenantId:String`
- `DeployResultVO`: `deploymentId:String`, `definitionId:String`, `definitionKey:String`, `version:Integer`
- `WorkflowHomeSummaryVO`: `todoCount:String`, `pendingReviewCount:String`, `reviewedCount:String`, `rejectedCount:String`, `trend:List<WorkflowDailyCountVO>`
- `WorkflowDailyCountVO`: `date:String`, `todoCount:String`, `pendingReviewCount:String`, `reviewedCount:String`, `rejectedCount:String`

### user-service DTO/VO

- `VerifyRequest`: `username:String`, `password:String`
- `VerifyResponse`: `verified:boolean`, `userId:String`, `username:String`, `nickname:String`, `email:String`, `contactPhone:String`, `message:String`
- `UserInfoResponse`: `userId:String`, `username:String`, `nickname:String`, `email:String`, `contactPhone:String`, `avatarFileId:String`, `avatarUrl:String`
- `UserAvatarDTO`: `avatarFileId:String`
- `UserChangePasswordDTO`: `oldPassword:String`, `newPassword:String`
- `UserQueryDTO`: `primaryOrgId:String`, `username:String`, `nickname:String`, `contactPhone:String`, `email:String`, `lastLoginIp:String`, `status:Integer`, `pageNum:Integer`, `pageSize:Integer`
- `UnassignedRoleUserQueryDTO`: `excludeRoleId:String`, `username:String`, `nickname:String`, `pageNum:Integer`, `pageSize:Integer`
- `UserRoleQueryDTO`: `userId:String`, `roleName:String`, `roleCode:String`, `pageNum:Integer`, `pageSize:Integer`
- `UserAssignRolesDTO`: `userId:String`, `roleIds:List<String>`
- `UserSaveDTO`: `id:String`, `username:String`, `nickname:String`, `contactPhone:String`, `email:String`, `gender:Integer`, `password:String`, `primaryOrgId:String`, `avatarFileId:String`, `personalSignature:String`, `workStatus:String`, `status:Integer`
- `UserDeleteDTO`: `id:String`, `ids:List<String>`
- `UserChangeStatusDTO`: `id:String`, `status:Integer`
- `UserResetPasswordDTO`: `id:String`
- `UserIdDTO`: `id:String`
- `UserManageVO`: `id:String`, `username:String`, `nickname:String`, `contactPhone:String`, `email:String`, `gender:Integer`, `primaryOrgId:String`, `primaryOrgName:String`, `avatarFileId:String`, `personalSignature:String`, `workStatus:String`, `status:Integer`, `lastLoginIp:String`, `lastLoginTime:String`, `createTime:String`, `updateTime:String`
- `RoleQueryDTO`: `roleName:String`, `roleCode:String`, `systemId:String`, `status:Integer`, `pageNum:Integer`, `pageSize:Integer`
- `RoleSaveDTO`: `id:String`, `roleName:String`, `roleCode:String`, `systemId:String`, `dataScope:Integer`, `orgIds:List<String>`, `sortOrder:Integer`, `remark:String`
- `RoleIdDTO`: `id:String`
- `RoleChangeStatusDTO`: `id:String`, `status:Integer`
- `RoleVO`: `id:String`, `roleName:String`, `roleCode:String`, `systemId:String`, `systemName:String`, `dataScope:Integer`, `orgIds:List<String>`, `sortOrder:Integer`, `status:Integer`, `remark:String`
- `ExternalSystemQueryDTO`: `systemName:String`, `systemCode:String`, `status:Integer`, `pageNum:Integer`, `pageSize:Integer`
- `ExternalSystemSaveDTO`: `id:String`, `systemName:String`, `systemCode:String`, `systemUrl:String`, `sortOrder:Integer`, `remark:String`
- `ExternalSystemIdDTO`: `id:String`
- `ExternalSystemChangeStatusDTO`: `id:String`, `status:Integer`
- `ExternalSystemVO`: `id:String`, `systemName:String`, `systemCode:String`, `systemUrl:String`, `sortOrder:Integer`, `status:Integer`, `remark:String`
- `RoleUserQueryDTO`: `id:String`, `roleId:String`, `keyword:String`, `username:String`, `nickname:String`, `contactPhone:String`, `email:String`, `status:Integer`, `pageNum:Integer`, `pageSize:Integer`
- `RoleAssignUsersDTO`: `roleId:String`, `userIds:List<String>`
- `RoleRemoveUserDTO`: `roleId:String`, `userIds:List<String>`
- `RoleDataScopeDTO`: `roleId:String`, `dataScope:Integer`, `orgIds:List<String>`
- `RoleMenuQueryDTO`: `roleId:String`, `status:Integer`
- `RoleUserVO`: `userId:String`, `username:String`, `nickname:String`, `contactPhone:String`, `email:String`, `gender:Integer`, `primaryOrgId:String`, `primaryOrgName:String`, `avatarFileId:String`, `personalSignature:String`, `workStatus:String`, `status:Integer`, `lastLoginIp:String`, `lastLoginTime:String`, `createTime:String`, `updateTime:String`
- `PostQueryDTO`: `postCode:String`, `postName:String`, `status:Integer`, `pageNum:Integer`, `pageSize:Integer`
- `PostSaveDTO`: `id:String`, `postCode:String`, `postName:String`, `sortOrder:Integer`
- `PostIdDTO`: `id:String`
- `PostChangeStatusDTO`: `id:String`, `status:Integer`
- `PostVO`: `id:String`, `postCode:String`, `postName:String`, `sortOrder:Integer`, `status:Integer`
- `PostUserQueryDTO`: `postId:String`, `orgId:String`
- `PostAssignDTO`: `postId:String`, `orgId:String`, `userIds:List<String>`
- `PostRemoveUserDTO`: `postId:String`, `userIds:List<String>`, `orgId:String`
- `PostUserVO`: `userId:String`, `username:String`, `nickname:String`, `contactPhone:String`, `orgId:String`, `orgName:String`
- `OrgAddDTO`: `parentId:Long`, `orgName:String`, `orgType:Short`, `orgCode:String`, `leaderId:Long`, `status:Short`, `sortOrder:Integer`
- `OrgEditDTO`: `id:Long`, `parentId:Long`, `orgName:String`, `orgType:Short`, `orgCode:String`, `leaderId:Long`, `status:Short`, `sortOrder:Integer`
- `OrgIdDTO`: `id:Long`
- `OrgChangeStatusDTO`: `id:Long`, `status:Short`
- `OrgTreeQueryDTO`: `orgName:String`, `orgType:Short`, `status:Short`
- `OrgDetailVO`: `id:String`, `parentId:String`, `orgName:String`, `orgType:Short`, `orgCode:String`, `leaderId:String`, `leaderName:String`, `status:Short`, `treePath:String`, `treeLevel:Integer`, `sortOrder:Integer`, `isDeleted:Short`, `createTime:String`, `updateTime:String`
- `OrgTreeVO`: `id:String`, `parentId:String`, `orgName:String`, `orgType:Short`, `orgCode:String`, `leaderId:String`, `leaderName:String`, `status:Short`, `sortOrder:Integer`, `createTime:String`, `updateTime:String`, `children:List<OrgTreeVO>`
- `OrgChildrenQueryDTO`: `parentId:Long`, `orgName:String`, `orgType:Short`, `status:Short`
- `OrgUserPageDTO`: `orgId:Long`, `pageNum:Integer`, `pageSize:Integer`
- `OrgUserAddDTO`: `orgId:Long`, `userIds:List<Long>`, `isPrimary:Short`
- `OrgUserRemoveDTO`: `orgId:Long`, `userIds:List<Long>`
- `OrgUserSetPrimaryDTO`: `orgId:Long`, `userId:Long`
- `OrgUserVO`: `userId:String`, `username:String`, `nickname:String`, `email:String`, `contactPhone:String`, `isPrimary:Short`
- `MenuQueryDTO`: `status:Integer`
- `UserMenuQueryDTO`: `id:String`, `userId:String`, `status:Integer`
- `MenuSaveDTO`: `id:String`, `menuName:String`, `parentId:String`, `sortOrder:Integer`, `routePath:String`, `componentPath:String`, `menuType:String`, `perms:String`, `icon:String`, `visible:Integer`, `status:Integer`
- `MenuIdDTO`: `id:String`
- `MenuAssignDTO`: `roleId:String`, `menuIds:List<String>`
- `MenuRoleQueryDTO`: `id:String`, `menuId:String`, `roleName:String`, `roleCode:String`, `status:Integer`, `pageNum:Integer`, `pageSize:Integer`
- `MenuAssignRolesDTO`: `menuId:String`, `roleIds:List<String>`
- `MenuVO`: `id:String`, `menuName:String`, `parentId:String`, `sortOrder:Integer`, `routePath:String`, `componentPath:String`, `menuType:String`, `perms:String`, `icon:String`, `visible:Integer`, `status:Integer`, `children:List<MenuVO>`
- `RouterVO`: `id:String`, `parentId:String`, `name:String`, `path:String`, `component:String`, `icon:String`, `hidden:Boolean`, `perms:List<String>`, `sortOrder:Integer`, `children:List<RouterVO>`
- `PageResponse<T>`: `total:String`, `pageNum:String`, `pageSize:String`, `records:List<T>`
- `FileDetailVO`: `id:String`, `url:String`

