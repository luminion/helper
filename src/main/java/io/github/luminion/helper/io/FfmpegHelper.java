package io.github.luminion.helper.io;

/**
 * 兼容旧包路径，建议改用 {@link io.github.luminion.helper.media.ffmpeg.FfmpegHelper}。
 *
 * @author luminion
 * @deprecated 请使用 {@link io.github.luminion.helper.media.ffmpeg.FfmpegHelper}
 */
@Deprecated
public class FfmpegHelper extends io.github.luminion.helper.media.ffmpeg.FfmpegClient {

    public FfmpegHelper(String ffplay, String ffmpeg, String ffprobe, String tempMediaPath) {
        super(ffplay, ffmpeg, ffprobe, tempMediaPath);
    }

    public static FfmpegHelper of(String ffplay, String ffmpeg, String ffprobe, String tempMediaPath) {
        return new FfmpegHelper(ffplay, ffmpeg, ffprobe, tempMediaPath);
    }
}
