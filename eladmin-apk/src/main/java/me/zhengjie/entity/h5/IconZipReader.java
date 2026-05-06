package me.zhengjie.entity.h5;

import cn.hutool.core.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipInputStream;

@Slf4j
public class IconZipReader {

    private static final String[] requiredDirs = {
            "res/mipmap-mdpi/",
            "res/mipmap-hdpi/",
            "res/mipmap-xhdpi/",
            "res/mipmap-xxhdpi/",
            "res/mipmap-xxxhdpi/",
            "res/mipmap-anydpi-v26/"
    };

    private static final String[] requiredFiles = {
            "play_store_512.png"
    };

    private final File unZipDir;
    private final File androidDir;
    private File androidZipDir;

    public IconZipReader(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename) || !originalFilename.endsWith(".zip")) {
            throw new IllegalArgumentException("文件类型需要为zip压缩文件");
        }
        this.unZipDir = Files.createTempDirectory("icon_unzip").toFile();
        try {
            ZipUtil.unzip(new ZipInputStream(file.getInputStream()), unZipDir);
        } catch (Exception e) {
            throw new RuntimeException("读取压缩包文件流异常", e);
        }
        androidDir = containsAndroidDir(unZipDir) ? new File(unZipDir, "android") : unZipDir;
        for (String requiredFile : requiredFiles) {
            File diskFile = new File(androidDir, requiredFile);
            if (!diskFile.exists()) {
                throw new IllegalArgumentException(String.format("上传压缩包中必须存在 %s 文件", requiredFile));
            }
        }
        for (String requiredDir : requiredDirs) {
            File diskDir = new File(androidDir, requiredDir);
            if (!diskDir.exists()) {
                throw new IllegalArgumentException(String.format("上传压缩包中必须存在 %s 目录", diskDir));
            }
        }
    }

    private boolean containsAndroidDir(File file) {
        if (file == null || !file.exists() || !file.isDirectory()) {
            return false;
        }
        File androidDir = new File(file, "android");
        return androidDir.exists() && androidDir.isDirectory();
    }

    public File getAndroidDir() {
        return androidDir;
    }

    public File getAndroidZipDir() {
        if (androidZipDir == null) {
            androidZipDir = ZipUtil.zip(androidDir);
        }
        return androidZipDir;
    }

    public File getPlayStorePng() {
        return new File(androidDir, "play_store_512.png");
    }

    public void release() throws IOException {
        if (unZipDir.exists() && unZipDir.isDirectory()) {
            FileUtils.deleteDirectory(unZipDir);
        }
        if (androidZipDir != null && androidZipDir.exists() && androidZipDir.isFile()) {
            FileUtils.delete(androidZipDir);
        }
    }
}
