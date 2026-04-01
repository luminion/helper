package io.github.luminion.helper.media.ffmpeg;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.*;

/**
 * ffmpeg 客户端。
 * <p>
 * 负责命令拼装、进程执行以及常见音视频处理能力封装。
 *
 * @author luminion
 */
@Slf4j
public class FfmpegClient {
    private final String ffplay;
    private final String ffmpeg;
    private final String ffprobe;
    private final String tempMediaPath;
    private final File tempMediaDir;

    public FfmpegClient(String ffplay, String ffmpeg, String ffprobe, String tempMediaPath) {
        this.ffplay = ffplay;
        this.ffmpeg = ffmpeg;
        this.ffprobe = ffprobe;
        this.tempMediaPath = tempMediaPath;
        this.tempMediaDir = new File(tempMediaPath);
        if (!tempMediaDir.exists()) {
            tempMediaDir.mkdirs();
        }
    }

    /**
     * ffmpeg助手
     *
     * @param ffplay        ffplay可执行文件路径
     * @param ffmpeg        ffmpeg可执行文件路径
     * @param ffprobe       ffprobe可执行文件路径
     * @param tempMediaPath 临时文件保存路径
     */
    public static FfmpegClient of(String ffplay, String ffmpeg, String ffprobe, String tempMediaPath) {
        return new FfmpegClient(ffplay, ffmpeg, ffprobe, tempMediaPath);
    }

    /**
     * 调用命令行执行
     *
     * @param command 命令行参数
     * @return {@link String } 执行结果
     */
    public String commandStart(List<String> command) {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("command must not be empty");
        }
        String join = String.join(" ", command);
        log.info("command : {}", join);
        ProcessBuilder builder = new ProcessBuilder();
        // 合并标准输出和错误输出，避免调用方需要维护两套日志读取逻辑。
        builder.redirectErrorStream(true);
        builder.command(command);
        Process process = null;
        StringBuilder result = new StringBuilder();
        try {
            process = builder.start();
            // ffmpeg/ffprobe 的核心信息都从控制台输出，这里统一收集成文本，便于调试和后续自行解析。
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.info(line);
                    result.append(line);
                    result.append("\n");
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("command exit code: {}, command: {}", exitCode, join);
            }
        } catch (IOException e) {
            log.error("execute command error ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("execute command interrupted", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return result.toString();
    }

    private void removeExisted(String filePath) {
        File file = new File(filePath);
        // 输出文件统一先清理，避免 ffmpeg 在同名文件存在时进入交互确认或直接失败。
        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) {
                throw new RuntimeException("delete existed file failed : " + filePath);
            }
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean mkdirs = parent.mkdirs();
            if (!mkdirs && !parent.exists()) {
                throw new IllegalStateException("create parent directory failed: " + parent.getAbsolutePath());
            }
        }
    }

    private void validateInputList(List<String> paths, String name) {
        // 多输入命令一旦混入空路径，最终的 ffmpeg 报错通常很难定位，这里提前拦截。
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) {
                throw new IllegalArgumentException(name + " contains blank path");
            }
        }
    }

    /**
     * 播放音频和视频
     *
     * @param resourcesPath 文件的路径
     */
    public void playVideoAudio(String resourcesPath) {
        List<String> command = new ArrayList<>();
        command.add(ffplay);
        command.add("-window_title");
        String fileName = resourcesPath.substring(resourcesPath.lastIndexOf(File.separator) + 1);
        command.add(fileName);
        command.add(resourcesPath);
        // 播放完后自动退出
        // command.add("-autoexit");
        commandStart(command);
    }

    /**
     * 播放音频和视频并指定循环次数
     *
     * @param resourcesPath 文件的路径
     * @param loop          循环播放次数
     */
    public void playVideoAudio(String resourcesPath, int loop) {
        List<String> command = new ArrayList<>();
        command.add(ffplay);
        command.add("-window_title");
        String fileName = resourcesPath.substring(resourcesPath.lastIndexOf(File.separator) + 1);
        command.add(fileName);
        command.add(resourcesPath);
        command.add("-loop");
        command.add(String.valueOf(loop));
        // 播放完后自动退出
        // command.add("-autoexit");
        commandStart(command);
    }

    /**
     * 播放音频和视频并指定宽、高、循环次数
     *
     * @param resourcesPath 文件的路径
     * @param weight        宽度
     * @param height        高度
     * @param loop          循环播放次数
     */
    public void playVideoAudio(String resourcesPath, int weight, int height, int loop) {
        List<String> command = new ArrayList<>();
        command.add(ffplay);
        command.add("-window_title");
        String fileName = resourcesPath.substring(resourcesPath.lastIndexOf(File.separator) + 1);
        command.add(fileName);
        command.add(resourcesPath);
        command.add("-x");
        command.add(String.valueOf(weight));
        command.add("-y");
        command.add(String.valueOf(height));
        command.add("-loop");
        command.add(String.valueOf(loop));
        // 播放完后自动退出
        // command.add("-autoexit");
        commandStart(command);
    }

    /**
     * 从视频中提取音频为mp3
     *
     * @param videoResourcesPath 视频文件的路径
     * @param saveFilePath       保存文件路径
     */
    public void getAudioFromVideo(String videoResourcesPath, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(videoResourcesPath);
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 从视频中去除去音频并保存视频
     *
     * @param videoResourcesPath 视频文件的路径
     * @param saveFilePath       保存文件路径
     */
    public void getVideoFromAudio(String videoResourcesPath, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(videoResourcesPath);
        command.add("-vcodec");
        command.add("copy");
        command.add("-an");
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 无声视频+音频合并为一个视频
     * 若音频比视频长，画面停留在最后一帧，继续播放声音。
     *
     * @param videoResourcesPath 视频文件的路径
     * @param audioResourcesPath 音频文件的路径
     * @param saveFilePath       保存文件路径
     */
    public void mergeSilentVideoAudio(String videoResourcesPath, String audioResourcesPath, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(videoResourcesPath);
        command.add("-i");
        command.add(audioResourcesPath);
        command.add("-vcodec");
        command.add("copy");
        command.add("-acodec");
        command.add("copy");
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 有声视频+音频合并为一个视频。
     * 若音频比视频长，画面停留在最后一帧，继续播放声音,
     * 若要以视频和音频两者时长短的为主，放开注解启用-shortest。
     *
     * @param videoResourcesPath 视频文件的路径
     * @param audioResourcesPath 音频文件的路径
     * @param saveFilePath       保存文件路径
     */
    public void mergeVideoAudio(String videoResourcesPath, String audioResourcesPath, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(videoResourcesPath);
        command.add("-i");
        command.add(audioResourcesPath);
        command.add("-filter_complex");
        command.add("amix");
        command.add("-map");
        command.add("0:v");
        command.add("-map");
        command.add("0:a");
        command.add("-map");
        command.add("1:a");
        // -shortest会取视频或音频两者短的一个为准，多余部分则去除不合并
        // command.add("-shortest");
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 多视频拼接合并(兼容较差)
     *
     * @param videoResourcesPathList 视频文件路径的List
     * @param saveFilePath           保存文件路径
     */
    public void mergeVideosUnstable(List<String> videoResourcesPathList, String saveFilePath) {
        validateInputList(videoResourcesPathList, "videoResourcesPathList");
        removeExisted(saveFilePath);
        // 所有要合并的视频转换为ts格式存到videoList里
        List<String> videoList = new ArrayList<>();
        for (String video : videoResourcesPathList) {
            List<String> command = new ArrayList<>();
            command.add(ffmpeg);
            command.add("-i");
            command.add(video);
            command.add("-c");
            command.add("copy");
            command.add("-bsf:v");
            command.add("h264_mp4toannexb");
            command.add("-f");
            command.add("mpegts");
            String videoTempName = video.substring(video.lastIndexOf(File.separator) + 1, video.lastIndexOf("."))
                    + ".ts";
            command.add(tempMediaPath + videoTempName);
            commandStart(command);
            videoList.add(tempMediaPath + videoTempName);
        }

        List<String> command1 = new ArrayList<>();
        command1.add(ffmpeg);
        command1.add("-i");
        StringBuilder buffer = new StringBuilder("concat:");
        for (int i = 0; i < videoList.size(); i++) {
            buffer.append(videoList.get(i));
            if (i != videoList.size() - 1) {
                buffer.append("|");
            }
        }
        command1.add(String.valueOf(buffer));
        command1.add("-c");
        command1.add("copy");
        command1.add(saveFilePath);
        commandStart(command1);
    }

    /**
     * 多视频拼接合并（兼容性好）
     *
     * @param videoResourcesPathList 视频文件路径的List
     * @param saveFilePath           保存文件路径
     */
    public void mergeVideos(List<String> videoResourcesPathList, String saveFilePath) {
        validateInputList(videoResourcesPathList, "videoResourcesPathList");
        removeExisted(saveFilePath);
        // 将所有要合并的视频路径写入txt文件
        String txtFileName = UUID.randomUUID() + ".txt";
        File txt = new File(tempMediaDir, txtFileName);
        String txtAbsolutePath = txt.getAbsolutePath();
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(txtAbsolutePath, false)))) {
            for (String video : videoResourcesPathList) {
                writer.println("file '" + video + "'");
            }
        } catch (IOException e) {
            log.error("生成txt文件失败", e);
        }
        try {
            List<String> command = new ArrayList<>();
            command.add(ffmpeg);
            command.add("-f");
            command.add("concat");
            command.add("-safe");
            command.add("0");
            command.add("-i");
            command.add(txtAbsolutePath);
            command.add("-c");
            command.add("copy");
            command.add(saveFilePath);
            commandStart(command);
        } finally {
            if (txt.exists()) {
                txt.delete();
            }
        }
    }

    /**
     * 多音频拼接合并为一个音频（在每个音频结尾追加另一个音频，即同一时间只播放一个音频）。
     *
     * @param audioResourcesPathList 音频文件路径的List
     * @param saveFilePath           保存文件路径
     */
    public void mergeAudios(List<String> audioResourcesPathList, String saveFilePath) {
        validateInputList(audioResourcesPathList, "audioResourcesPathList");
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        StringBuilder buffer = new StringBuilder("concat:");
        for (int i = 0; i < audioResourcesPathList.size(); i++) {
            buffer.append(audioResourcesPathList.get(i));
            if (i != audioResourcesPathList.size() - 1) {
                buffer.append("|");
            }
        }
        command.add(String.valueOf(buffer));
        command.add("-acodec");
        command.add("copy");
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 视频格式转换
     *
     * @param videoResourcesPath 视频文件的路径
     * @param saveFilePath       保存文件路径(例如转化为mp4格式，saveFilePath为"e:/test.mp4",转化为avi,saveFilePath为"e:/test.avi")
     */
    public void videoFormatConversion(String videoResourcesPath, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(videoResourcesPath);
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 获取音频或视频信息
     *
     * @param videoAudioResourcesPath 音频或视频文件的路径
     */
    public List<String> videoAudioInfo(String videoAudioResourcesPath) {
        List<String> command = new ArrayList<>();
        command.add(ffprobe);
        command.add("-i");
        command.add(videoAudioResourcesPath);
        // 调用命令行获取视频信息
        String infoStr = commandStart(command);
        String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s";
        String regexVideo = "Video: (.*?) tbr";
        String regexAudio = "Audio: (.*?), (.*?) Hz, (.*?) kb\\/s";
        List<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(regexDuration);
        Matcher matcher = pattern.matcher(infoStr);
        if (matcher.find()) {
            list.add("视频/音频整体信息: ");
            list.add("视频/音频名称：" + videoAudioResourcesPath);
            list.add("开始时间：" + matcher.group(2));
            list.add("结束时间：" + matcher.group(1));
            list.add("比特率: " + matcher.group(3) + " kb/s");
            list.add("------------------------------------ ");
        }

        Pattern patternVideo = Pattern.compile(regexVideo);
        Matcher matcherVideo = patternVideo.matcher(infoStr);
        if (matcherVideo.find()) {
            String videoInfo = matcherVideo.group(1);
            String[] sp = videoInfo.split(",");
            list.add("视频流信息: ");
            list.add("视频编码格式: " + sp[0]);
            int ResolutionPosition = 2;
            if (sp[1].contains("(") && sp[2].contains(")")) {
                list.add("YUV: " + sp[1] + "," + sp[2]);
                ResolutionPosition = 3;
            } else if (sp[1].contains("(") && !sp[2].contains(")") && sp[3].contains(")")) {
                list.add("YUV: " + sp[1] + "," + sp[2] + "," + sp[3]);
                ResolutionPosition = 4;
            } else {
                list.add("YUV: " + sp[1]);
            }
            list.add("分辨率: " + sp[ResolutionPosition]);
            list.add("视频比特率: " + sp[ResolutionPosition + 1]);
            list.add("帧率: " + sp[ResolutionPosition + 2]);
            list.add("------------------------------------ ");
        }
        Pattern patternAudio = Pattern.compile(regexAudio);
        Matcher matcherAudio = patternAudio.matcher(infoStr);
        if (matcherAudio.find()) {
            list.add("音频流信息: ");
            list.add("音频编码格式: " + matcherAudio.group(1));
            list.add("采样率: " + matcherAudio.group(2) + " HZ");
            list.add("声道: " + matcherAudio.group(3).split(",")[0]);
            list.add("音频比特率: " + matcherAudio.group(3).split(",")[2] + " kb/s");
        }
        return list;
    }

    /**
     * 视频或音频剪切
     * 参考@link:https://zhuanlan.zhihu.com/p/27366331
     *
     * @param videoAudioResourcesPath 视频或音频文件的路径
     * @param startTime               开始时间
     * @param endTime                 结束时间
     * @param saveFilePath            保存文件路径
     */
    public void cutVideoAudio(String videoAudioResourcesPath, LocalTime startTime, LocalTime endTime,
            String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-ss");
        command.add(startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        command.add("-t");
        command.add(calculationEndTime(startTime, endTime));
        command.add("-i");
        command.add(videoAudioResourcesPath);
        command.add("-c:v");
        command.add("libx264");
        command.add("-c:a");
        command.add("aac");
        command.add("-strict");
        command.add("experimental");
        command.add("-b:a");
        command.add("98k");
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 视频裁剪大小尺寸（根据leftDistance和topDistance确定裁剪的起始点，再根据finallywidth和finallyHeight确定裁剪的宽和长）
     * <p>
     * <p>
     * 参考@link:https://www.cnblogs.com/yongfengnice/p/7095846.html
     *
     * @param videoAudioResourcesPath 视频文件的路径
     * @param finallyWidth            裁剪后最终视频的宽度
     * @param finallyHeight           裁剪后最终视频的高度
     * @param leftDistance            开始裁剪的视频左边到y轴的距离（视频左下角为原点）
     * @param topDistance             开始裁剪的视频上边到x轴的距离（视频左下角为原点）
     * @param saveFilePath            保存文件路径
     */
    public void cropVideoSize(String videoAudioResourcesPath, String finallyWidth, String finallyHeight,
            String leftDistance, String topDistance, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(videoAudioResourcesPath);
        command.add("-vf");
        // 获取视频信息得到原始视频长、宽
        List<String> list = videoAudioInfo(videoAudioResourcesPath);
        String resolution = list.stream().filter(v -> v.contains("分辨率")).findFirst()
                .orElseThrow(() -> new IllegalStateException("can not parse video resolution"));
        Matcher matcher = Pattern.compile("(\\d+)x(\\d+)").matcher(resolution);
        if (!matcher.find()) {
            throw new IllegalStateException("can not parse video resolution: " + resolution);
        }
        int originalWidth = Integer.parseInt(matcher.group(1));
        int originalHeight = Integer.parseInt(matcher.group(2));
        Integer cropStartWidth = originalWidth - Integer.parseInt(leftDistance);
        Integer cropStartHeight = originalHeight - Integer.parseInt(topDistance);
        command.add("crop=" + finallyWidth + ":" + finallyHeight + ":" + cropStartWidth + ":" + cropStartHeight);
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 计算两个时间的时间差
     *
     * @param starTime 开始时间，如：00:01:09
     * @param endTime  结束时间，如：00:08:27
     * @return 返回xx:xx:xx形式，如：00:07:18
     */
    public String calculationEndTime(LocalTime starTime, LocalTime endTime) {
        Objects.requireNonNull(starTime, "starTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");
        long hour = HOURS.between(starTime, endTime);
        long minutes = MINUTES.between(starTime, endTime);
        long seconds = SECONDS.between(starTime, endTime);
        if (seconds < 0) {
            seconds += 24 * 60 * 60;
            minutes = seconds / 60;
            hour = minutes / 60;
        }
        minutes = minutes > 59 ? minutes % 60 : minutes;
        String hourStr = hour < 10 ? "0" + hour : String.valueOf(hour);
        String minutesStr = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
        long getSeconds = seconds - (hour * 60 + minutes) * 60;
        String secondsStr = getSeconds < 10 ? "0" + getSeconds : String.valueOf(getSeconds);
        return hourStr + ":" + minutesStr + ":" + secondsStr;
    }

    /**
     * 视频截图
     *
     * @param videoResourcesPath 视频文件的路径
     * @param screenshotTime     截图的时间，如：00:01:06
     * @param saveFilePath       保存文件路径
     */
    public void videoScreenshot(String videoResourcesPath, LocalTime screenshotTime, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-ss");
        command.add(screenshotTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        command.add("-i");
        command.add(videoResourcesPath);
        command.add("-f");
        command.add("image2");
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 整个视频截图
     *
     * @param videoResourcesPath 视频文件的路径
     * @param fps                截图的速度。1则表示每秒截一张；0.1则表示每十秒一张；10则表示每秒截十张图片
     * @param targetFileDirPath  文件保存的目标文件夹
     */
    public void videoAllScreenshot(String videoResourcesPath, String fps, String targetFileDirPath) {
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(videoResourcesPath);
        command.add("-vf");
        command.add("fps=" + fps);
        String fileName = videoResourcesPath.substring(videoResourcesPath.lastIndexOf("\\") + 1,
                videoResourcesPath.lastIndexOf("."));
        command.add(targetFileDirPath + fileName + "%d" + ".jpg");
        commandStart(command);
    }

    /**
     * 多图片+音频合并为视频
     *
     * @param pictureResourcesPath 图片文件路径(数字编号和后缀不要)。如：D:\ffmpegMedia\pictur\101-你也不必耿耿于怀1.jpg
     *                             和D:\ffmpegMedia\pictur\101-你也不必耿耿于怀2.jpg。只需传D:\ffmpegMedia\pictur\101-你也不必耿耿于怀
     * @param audioResourcesPath   音频文件的路径
     * @param fps                  帧率,每张图片的播放时间（数值越小则每张图停留的越长）。0.5则两秒播放一张，1则一秒播放一张，10则一秒播放十张
     * @param saveFilePath         保存文件路径
     */
    public void pictureAudioMerge(String pictureResourcesPath, String audioResourcesPath, String fps,
            String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-threads");
        command.add("2");
        command.add("-y");
        command.add("-r");
        // 帧率
        command.add(fps);
        command.add("-i");
        command.add(pictureResourcesPath + "%d.jpg");
        command.add("-i");
        command.add(audioResourcesPath);
        command.add("-absf");
        command.add("aac_adtstoasc");
        // -shortest会取视频或音频两者短的一个为准，多余部分则去除不合并
        command.add("-shortest");
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 绘制音频波形图保存.jpg后缀可改为png
     *
     * @param audioResourcesPath 音频文件的路径
     * @param saveFilePath       保存文件路径
     */
    public void audioWaveform(String audioResourcesPath, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(audioResourcesPath);
        command.add("-filter_complex");
        command.add("showwavespic=s=1280x240");
        command.add("-frames:v");
        command.add("1");
        String fileName = audioResourcesPath.substring(audioResourcesPath.lastIndexOf("\\") + 1,
                audioResourcesPath.lastIndexOf("."));
        // jpg可换为png
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 两个音频混缩合并为一个音频（即同一时间播放两首音频）。
     * 音量参考：@link:https://blog.csdn.net/sinat_14826983/article/details/82975561
     *
     * @param audioResourcesPath1 音频2文件路径的
     * @param audioResourcesPath2 音频资源path2
     * @param saveFilePath        保存文件路径
     */
    public void mergeAudios(String audioResourcesPath1, String audioResourcesPath2, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(audioResourcesPath1);
        command.add("-i");
        command.add(audioResourcesPath2);
        command.add("-filter_complex");
        command.add("amix=inputs=2:duration=longest");
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 两个音频混缩合并为一个音频（即同一时间播放两首音频）。
     * 音量参考：@link:https://blog.csdn.net/sinat_14826983/article/details/82975561
     *
     * @param audioResourcesPath1 音频1文件路径
     * @param number1             音频1的音量，如取 0.4 表示音量是原来的40% ，取1.5表示音量是原来的150%
     * @param audioResourcesPath2 音频2文件路径
     * @param number2             音频2的音量，如取 0.4 表示音量是原来的40% ，取1.5表示音量是原来的150%
     * @param saveFilePath        保存文件路径
     */
    public void mergeAudios(String audioResourcesPath1, String number1, String audioResourcesPath2, String number2,
            String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(audioResourcesPath1);
        command.add("-i");
        command.add(audioResourcesPath2);
        command.add("-filter_complex");
        command.add("[0:a]volume=" + number1 + "[a1];[1:a]volume=" + number2
                + "[a2];[a1][a2]amix=inputs=2:duration=longest");
        command.add(saveFilePath);
        commandStart(command);
    }

    /**
     * 两个音频混缩合并为一个音频的不同声道（即一只耳机播放一个音频）。
     * 声道参考：@link:https://www.itranslater.com/qa/details/2583879740000044032
     *
     * @param audioResourcesPath1 音频1文件路径
     * @param audioResourcesPath2 音频2文件路径
     * @param saveFilePath        保存文件路径
     */
    public void mergeAudiosSoundtrack(String audioResourcesPath1, String audioResourcesPath2, String saveFilePath) {
        removeExisted(saveFilePath);
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-i");
        command.add(audioResourcesPath1);
        command.add("-i");
        command.add(audioResourcesPath2);
        command.add("-filter_complex");
        command.add("amerge=inputs=2,pan=stereo|c0<c0+c1|c1<c2+c3");
        command.add(saveFilePath);
        commandStart(command);
    }

}
