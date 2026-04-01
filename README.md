# helper

一个面向 Java 日常开发的工具库，提供常用的静态工具、值对象、客户端封装与快捷入口。

## 当前版本

当前版本：`1.2.0`

这个版本完成了包结构整理和命名收敛：

- 新包结构已经生效，推荐新代码直接使用新包
- 旧公开入口仍然保留，用于兼容已有项目
- 旧入口已标记为 `@Deprecated`，后续会在合适的 major 版本中移除

## 当前包结构

```text
io.github.luminion.helper
├── base                基础工具
├── core                核心接口
├── collection          旧兼容入口
├── datetime            旧兼容入口
├── enums               枚举解析
├── excel               Excel 辅助
├── geo                 地理坐标
├── geographical        旧兼容入口
├── http                HTTP 请求
├── io                  文件 IO 与旧兼容入口
├── json                JSON / Jackson
├── media.ffmpeg        ffmpeg 能力
├── reflect             反射工具
├── spring              Spring 辅助
├── storage.minio       MinIO 存储
├── time                时间日期
├── tree                树结构处理
└── upload              分片上传
```

## 设计约定

- `*Helper`：只负责静态入口、快捷方法或纯静态工具方法
- `*Client`：带上下文的执行对象，例如 MinIO、ffmpeg
- `*Value`：不可变值对象，例如时间值、坐标点
- `*Resolver`：带索引或上下文的查询对象
- `*Builder`：负责分步骤配置和执行构建

## 推荐入口

时间：

```java
import io.github.luminion.helper.time.DateTimeHelper;
import io.github.luminion.helper.time.DateTimeValue;

DateTimeValue now = DateTimeHelper.now();
```

HTTP：

```java
import io.github.luminion.helper.http.HttpHelper;
import io.github.luminion.helper.http.HttpResponse;

HttpResponse response = HttpHelper.get("https://example.com").response();
```

JSON：

```java
import io.github.luminion.helper.json.JacksonHelper;

String json = JacksonHelper.toJson(data);
```

Geo：

```java
import io.github.luminion.helper.geo.GeoHelper;
import io.github.luminion.helper.geo.GeoPoint;

GeoPoint point = GeoHelper.ofWGS84(116.397128, 39.916527);
```

MinIO：

```java
import io.github.luminion.helper.storage.minio.MinioHelper;
import io.github.luminion.helper.storage.minio.MinioStorageClient;

MinioStorageClient client = MinioHelper.create(endpoint, accessKey, secretKey);
```

ffmpeg：

```java
import io.github.luminion.helper.media.ffmpeg.FfmpegHelper;
import io.github.luminion.helper.media.ffmpeg.FfmpegClient;

FfmpegClient client = FfmpegHelper.create(ffplay, ffmpeg, ffprobe, tempMediaPath);
```

树结构：

```java
import io.github.luminion.helper.tree.TreeHelper;
import io.github.luminion.helper.tree.TreeBuilder;

TreeBuilder<Node, Long> builder = TreeHelper.create(Node::getId, Node::getParentId, Node::setChildren);
```

## 迁移说明

本次整理后的主要映射关系如下。

- `io.github.luminion.helper.datetime.*` -> `io.github.luminion.helper.time.*`
- `io.github.luminion.helper.geographical.*` -> `io.github.luminion.helper.geo.*`
- `io.github.luminion.helper.collection.Enum*` -> `io.github.luminion.helper.enums.*`
- `io.github.luminion.helper.collection.Tree*` -> `io.github.luminion.helper.tree.*`
- `io.github.luminion.helper.io.FileUpload*` -> `io.github.luminion.helper.upload.*`
- `io.github.luminion.helper.io.Ffmpeg*` -> `io.github.luminion.helper.media.ffmpeg.*`
- `io.github.luminion.helper.io.Minio*` -> `io.github.luminion.helper.storage.minio.*`

JSON 模块当前建议优先使用：

- `JacksonHelper`
- `JacksonWrapper`

旧的 `ObjectMapperHelper` 仍然保留，用于兼容已有代码。

## 兼容策略

- 已发布过的旧公开入口会继续保留一段时间
- 新增功能优先只放在新包和新结构中
- 旧入口以兼容转发为主，不再作为主要维护入口

## 构建

```bash
mvn -q -DskipTests compile
```
