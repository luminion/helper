# 更新日志

本项目所有重要变更都记录在此文件中。

版本号遵循语义化版本(SemVer)。

## 1.2.1

### 新增 / Added
- `MinioHelper` 新增 `builder()` 构建方式,支持复用已有的 `MinioClient`,并通过 `publicUrl(...)` 适配 MinIO path-style、阿里云 OSS virtual-hosted 及自定义域名/CDN 等多种访问形态。
- `MinioHelper` 新增对象删除能力:`delete(String)`(幂等)与 `delete(List<String>)` 批量删除(返回删除失败的对象名)。
- `MinioHelper` 新增 `stat(String)` 获取对象元信息(size / etag / contentType / lastModified / userMetadata),返回 `ObjectStat`。
- `MinioHelper` 新增 `listObjects(String prefix)` 按前缀递归列出对象名,以及 `copy(src, target)` 同桶内对象复制。
- `MinioHelper` 新增预签名能力:`presignedUrl(objectName, expiry)` 生成临时访问 URL(私有桶取链),`presignedUploadUrl(objectName, expiry)` 生成预签名 PUT 直传 URL。
- `MinioHelper` 新增上传重载 `upload(objectName, stream, size, contentType)`,支持已知长度上传以降低内存占用,并可显式指定 Content-Type。
- `EnumHelper` 新增判断枚举中指定 key 是否存在的方法。

### 修复 / Fixed
- `TreeHelper` 修正递归收集子节点的终止逻辑。

### 调整 / Changed
- `MinioHelper` 上传时按对象名后缀自动推断并设置 Content-Type,使图片、PDF 等在浏览器中可内联预览而非强制下载。
- `MinioHelper` 不再在构造时自动创建桶(`ensureBucketExists` 仍保留为可显式调用),避免在无建桶权限的对象存储(如 OSS)上初始化失败。
- `DatetimeHelper` 旧日期格式化器标记为废弃,并更新相关代码引用。

> 兼容性说明:`MinioHelper` 原有的 `of(...)` 工厂方法全部保留(标记 `@Deprecated`,委托至新实现),URL 输出与此前保持一致;旧用法无需修改即可继续使用。
