# zhou6 file-service 文件上传微服务设计说明

## 1. 设计目标

`file-service` 用于统一承载文件上传、下载、删除和文件元数据管理能力，屏蔽底层对象存储平台差异。当前实现面向华为云 OBS、MinIO、阿里云 OSS、腾讯云 COS 等对象存储平台。

核心设计思想：

- 面向接口编程：业务层只依赖 `FileStorageService`，不直接依赖具体云厂商 SDK。
- 策略模式：每个平台对应一个存储策略实现，内部使用各自官方 SDK。
- 条件装配：通过 `storage.type` 和 `@ConditionalOnProperty` 在启动时选择唯一存储实现。
- 元数据隔离：业务表只保存文件 ID 或业务关联，不直接写死对象存储 URL。

## 2. 模块位置

新增 Maven 子模块：

```text
file-service
```

根工程 `pom.xml` 已加入：

```xml
<module>file-service</module>
```

主要目录：

```text
file-service/src/main/java/com/zhou6/cloud/file
├── config          # OpenAPI、存储配置、条件装配
├── controller      # 文件 REST 接口
├── dto             # 请求/响应对象
├── entity          # sys_file 元数据实体
├── mapper          # MyBatis-Plus Mapper
├── service         # 文件业务服务
└── storage         # 存储策略接口和实现
```

## 3. 核心接口

统一存储接口：

```java
public interface FileStorageService {

    StoredFile upload(InputStream inputStream, String fileName, String contentType, long fileSize);

    InputStream download(String objectKey);

    void delete(String objectKey);

    boolean exists(String objectKey);

    String getPlatform();
}
```

该接口屏蔽对象存储平台差异，业务服务只关心上传、下载、删除和平台标识。上传和下载均使用流式处理，避免大文件一次性进入 JVM 堆内存。

## 4. 存储策略实现

```text
FileStorageService
├── MinioStorageServiceImpl  # MinIO 官方 SDK
├── ObsStorageServiceImpl    # 华为云 OBS 官方 SDK
├── OssStorageServiceImpl    # 阿里云 OSS 官方 SDK
└── CosStorageServiceImpl    # 腾讯云 COS 官方 SDK
```

各平台上传方式不同，但外部入口统一为 `FileStorageService`。Controller 和业务层不关心底层 SDK 差异，只负责传入 `InputStream`、文件名、文件类型和文件大小。

## 5. 条件装配

`StorageConfiguration` 根据 `storage.type` 装配唯一存储 Bean：

```java
@Bean
@ConditionalOnProperty(name = "storage.type", havingValue = "minio", matchIfMissing = true)
public FileStorageService minioStorageService(StorageProperties properties) {
    return new MinioStorageServiceImpl(properties.getMinio());
}
```

支持值：

```text
minio
obs
oss
cos
```

## 6. 配置示例

配置文件位置：

```text
file-service/src/main/resources/bootstrap.yml
```

示例：

```yaml
server:
  port: 52049
  servlet:
    context-path: /file

storage:
  type: minio
  minio:
    endpoint: http://127.0.0.1:9000
    public-endpoint: http://127.0.0.1:9000
    region: us-east-1
    access-key: your-ak
    secret-key: your-sk
    bucket-name: my-bucket
    path-style-access: true
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `storage.type` | 当前启用的存储平台，支持 `minio`、`obs`、`oss`、`cos` |
| `endpoint` | SDK 访问对象存储的地址 |
| `public-endpoint` | 返回给前端或业务系统访问文件的公开地址 |
| `region` | 存储区域 |
| `access-key` | 访问密钥 ID |
| `secret-key` | 访问密钥 Secret |
| `bucket-name` | 存储桶名称 |
| `path-style-access` | 访问 URL 是否拼接 bucket，MinIO 通常为 `true`，公有云通常为 `false` |

## 7. REST 接口

服务上下文路径：

```text
/file
```

控制器路径：

```text
/fileManagement
```

因此直连 file-service 时完整接口如下：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/file/fileManagement/upload` | 上传文件，`multipart/form-data`，字段名为 `file` |
| `POST` | `/file/fileManagement/detail` | 查询文件元数据 |
| `POST` | `/file/fileManagement/download` | 下载文件，JSON Body 传 `id` |
| `POST` | `/file/fileManagement/delete` | 删除文件和元数据 |
| `POST` | `/file/fileManagement/exists` | 判断文件元数据对应的对象存储文件是否存在 |

上传返回示例：

```json
{
  "code": "000000",
  "message": "success",
  "data": {
    "id": "1234567890",
    "fileName": "report.pdf",
    "fileSize": 10240,
    "contentType": "application/pdf",
    "objectKey": "2026/05/31/uuid.pdf",
    "platform": "minio",
    "url": "http://127.0.0.1:9000/my-bucket/2026/05/31/uuid.pdf"
  }
}
```

## 8. 数据库设计

建表脚本：

```text
a_doc/database/2026_05_31_sys_file.sql
```

表名：

```text
sys_file
```

关键字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 文件元数据主键，雪花 ID |
| `file_name` | `VARCHAR(255)` | 原始文件名 |
| `file_size` | `BIGINT` | 文件大小，单位字节 |
| `content_type` | `VARCHAR(128)` | 文件 MIME 类型 |
| `object_key` | `VARCHAR(512)` | 对象存储中的唯一键 |
| `platform` | `VARCHAR(32)` | 存储平台标识 |
| `bucket_name` | `VARCHAR(128)` | 存储桶名称 |
| `url` | `VARCHAR(1024)` | 文件访问地址 |

业务系统建议只保存 `sys_file.id`，需要展示或下载时再到 `file-service` 查询文件元数据。

## 9. 扩展方式

### 9.1 新增官方 SDK 平台

如果后续需要新增其他对象存储平台：

1. 引入官方 SDK 依赖。
2. 新增实现类并实现 `FileStorageService`。
3. 在 `StorageConfiguration` 中按 `storage.type` 条件装配。

业务层和 Controller 不需要改变。

## 10. 当前验证情况

已完成：

- 新增 `file-service` 模块。
- 新增 MinIO、OBS、OSS、COS 官方 SDK 存储策略。
- 新增上传、详情、下载、删除接口。
- 新增 `sys_file` 元数据实体、Mapper 和建表脚本。
- 根 `pom.xml` 已加入模块和各平台官方 SDK 依赖管理。

未完成：

- Maven 编译未能完成。原因是当前环境无法解析 `repo.maven.apache.org`，导致 Spring Boot 父 POM 下载失败。
