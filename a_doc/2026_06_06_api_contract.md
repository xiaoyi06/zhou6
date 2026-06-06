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
- `user-service/src/main/java/com/zhou6/cloud/user/constant/UserApiPathConstants.java`

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

## user-service

| 接口名 | 方法 | 路径 | 请求参数 | 返回数据 |
| --- | --- | --- | --- | --- |
| 校验登录账号密码 | POST | `/api/v1/user-api/userInfo/verify` | `VerifyRequest` | `R<VerifyResponse>` |
| 查询当前用户信息 | POST | `/api/v1/user-api/userInfo/info` | 无 | `R<UserInfoResponse>` |
| 按登录账号查询当前用户信息 | POST | `/api/v1/user-api/userInfo/infoByUsername` | 无 | `R<UserInfoResponse>` |
| 修改当前用户头像 | POST | `/api/v1/user-api/userInfo/avatar` | `UserAvatarDTO` | `R<Void>` |
| 修改当前用户密码 | POST | `/api/v1/user-api/userInfo/changePassword` | `UserChangePasswordDTO` | `R<Void>` |
| 分页查询用户 | POST | `/api/v1/user-api/userManagement/page` | `UserQueryDTO` | `R<PageResponse<UserManageVO>>` |
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
- `WorkflowHomeSummaryVO`: `todoCount:String`, `pendingReviewCount:String`, `reviewedCount:String`, `rejectedCount:String`, `trend:List<WorkflowDailyCountVO>`
- `WorkflowDailyCountVO`: `date:String`, `todoCount:String`, `pendingReviewCount:String`, `reviewedCount:String`, `rejectedCount:String`

### user-service DTO/VO

- `VerifyRequest`: `username:String`, `password:String`
- `VerifyResponse`: `verified:boolean`, `userId:String`, `username:String`, `nickname:String`, `email:String`, `contactPhone:String`, `message:String`
- `UserInfoResponse`: `userId:String`, `username:String`, `nickname:String`, `email:String`, `contactPhone:String`, `avatarFileId:String`, `avatarUrl:String`
- `UserAvatarDTO`: `avatarFileId:String`
- `UserChangePasswordDTO`: `oldPassword:String`, `newPassword:String`
- `UserQueryDTO`: `primaryOrgId:String`, `username:String`, `nickname:String`, `contactPhone:String`, `email:String`, `lastLoginIp:String`, `status:Integer`, `pageNum:Integer`, `pageSize:Integer`
- `UserSaveDTO`: `id:String`, `username:String`, `nickname:String`, `contactPhone:String`, `email:String`, `gender:Integer`, `password:String`, `primaryOrgId:String`, `avatarFileId:String`, `personalSignature:String`, `workStatus:String`, `status:Integer`
- `UserDeleteDTO`: `id:String`, `ids:List<String>`
- `UserChangeStatusDTO`: `id:String`, `status:Integer`
- `UserResetPasswordDTO`: `id:String`
- `UserIdDTO`: `id:String`
- `UserManageVO`: `id:String`, `username:String`, `nickname:String`, `contactPhone:String`, `email:String`, `gender:Integer`, `primaryOrgId:String`, `primaryOrgName:String`, `avatarFileId:String`, `personalSignature:String`, `workStatus:String`, `status:Integer`, `lastLoginIp:String`, `lastLoginTime:String`, `createTime:String`, `updateTime:String`
- `RoleQueryDTO`: `roleName:String`, `roleCode:String`, `status:Integer`, `pageNum:Integer`, `pageSize:Integer`
- `RoleSaveDTO`: `id:String`, `roleName:String`, `roleCode:String`, `dataScope:Integer`, `sortOrder:Integer`, `remark:String`
- `RoleIdDTO`: `id:String`
- `RoleChangeStatusDTO`: `id:String`, `status:Integer`
- `RoleVO`: `id:String`, `roleName:String`, `roleCode:String`, `dataScope:Integer`, `sortOrder:Integer`, `status:Integer`, `remark:String`
- `RoleUserQueryDTO`: `id:String`, `roleId:String`, `keyword:String`, `username:String`, `nickname:String`, `contactPhone:String`, `email:String`, `status:Integer`, `pageNum:Integer`, `pageSize:Integer`
- `RoleAssignUsersDTO`: `roleId:String`, `userIds:List<String>`
- `RoleRemoveUserDTO`: `roleId:String`, `userId:String`
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
- `PostRemoveUserDTO`: `postId:String`, `userId:String`, `orgId:String`
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
- `OrgUserRemoveDTO`: `orgId:Long`, `userId:Long`
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

