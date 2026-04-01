package io.github.luminion.helper.media.ffmpeg;

/**
 * ffmpeg 静态入口。
 * <p>
 * 只负责创建客户端实例，没有独立职责。
 * 直接使用 {@link FfmpegClient#of(String, String, String, String)} 即可。
 *
 * @author luminion
 * @deprecated 请直接使用 {@link FfmpegClient}
 */
@Deprecated
public abstract class FfmpegHelper {
    private FfmpegHelper() {}

    /**
     * 创建 ffmpeg 客户端。
     */
    public static FfmpegClient of(String ffplay, String ffmpeg, String ffprobe, String tempMediaPath) {
        return FfmpegClient.of(ffplay, ffmpeg, ffprobe, tempMediaPath);
    }
}
