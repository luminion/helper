# 更新日志

本项目所有重要变更都记录在此文件中。

版本号遵循语义化版本(SemVer)。

## 1.2.2 (未发布)

### 修复 / Fixed
- 🔴 `TreeHelper.findAllChildrenById/findAllChildrenByNode` 修复只沿首个子分支递归的严重缺陷,现在正确返回所有层级的后代节点(深度优先前序遍历)
- 🔴 `HttpHelper.execute()` 修复异常路径连接泄漏问题,在异常时确保调用 `connection.disconnect()`
- 🔴 `HttpHelper.responseString()` 修复逐行读取改写原始换行符的问题,改用字符缓冲直接读取
- 🔴 `ObjectMapperHelper` 全局 `instance` 字段添加 `volatile` 修饰,修复多线程可见性问题
- 🔴 `FfmpegHelper` 构造函数添加临时目录创建失败检查,失败时抛出 `IllegalStateException`
- 🔴 `FfmpegHelper.commandStart()` 显式关闭 `process.getOutputStream()`,避免流泄漏
- 🔴 `FfmpegHelper.mergeVideosUnstable()` 在 finally 块清理临时 .ts 文件,修复磁盘空间泄漏
- 🔴 `FfmpegHelper.calculationEndTime()` 修复跨午夜时间计算错误,改为统一的秒级计算逻辑
- 🔴 `FfmpegHelper` 修复路径文件名提取硬编码反斜杠 `\` 的跨平台问题,改用同时兼容 `/` 与 `\` 的辅助方法
- 🔴 `BitHelper.getSetBits/isSingleBit` 修复最高位(符号位)被静默丢弃的问题,现在支持全部 32/64 个 bit 位
- 🔴 `EasyExcelHelper` 和 `FastExcelHelper` 反射注册转换器失败时抛出 `IllegalStateException` 而非静默记录 warn
- 🟡 `HttpHelper.responseStream()` 优化成功码判断,将 2xx 范围视为成功(之前只认 200)
- 🟡 `BeanHelper.objectToMap()` 修复 Map 输入时忽略 `ignoreNull` 参数的问题,现在正确复制并过滤
- 🟡 `BeanHelper.objectToMap()` 修复 null 输入返回 null 的问题,现在返回空 Map 避免 NPE

### 新增 / Added
- ✨ `FileUploadHelper.mergeFile(fileMD5, fileExt, chunkCount, chunkSize)` 新增带分片大小校验的合并重载,避免分片写入中途崩溃导致的"文件存在但不完整"被错误合并

### 改进 / Improved
- ✨ `FileHelper.copyFile()` 方法改为 public,允许用户直接复制单个文件
- 📚 `FfmpegHelper` 清理 `audioWaveform()` 中未使用的死代码
- 📚 `GeoHelper.gcj02ToWgs84/toWGS84` 补全坐标转换精度限制说明(约 1～2 米误差)
- 📚 `BitHelper` 完善 `getSetBits` 文档,说明支持全部 bit 位


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
