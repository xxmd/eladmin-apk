package me.zhengjie.entity.h5;

import cn.hutool.core.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.entity.app.AppIcon;
import me.zhengjie.entity.app.AppSign;
import me.zhengjie.util.FileConflictUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class H5MiniGameProject {
    private final File projectDir;
    private final File gradlePropertiesFile;
    private final File argumentConfigFile;
    private final File resDir;
    private final File manifestFile;

    public Optional<File> getReleaseApkFile() {
        File releaseDir = new File(projectDir, "app/build/outputs/apk/release");
        return Arrays.stream(Objects.requireNonNull(releaseDir.listFiles()))
                .filter(f -> f.isFile() && f.getName().endsWith(".apk"))
                .findFirst();
    }

    public File getXiaoMiVerifyApk() {
        return new File(projectDir, "com.xiaomi.getapps.signature.verification.apk");
    }

    public H5MiniGameProject(File projectDir) {
        this.projectDir = projectDir;
        this.gradlePropertiesFile = new File(projectDir, "gradle.properties");
        if (!isValidFile(gradlePropertiesFile)) {
            throw new IllegalArgumentException("获取H5小游戏项目中gradle.properties文件异常");
        }
        this.argumentConfigFile = new File(projectDir, "app/src/main/java/com/h5/game/config/LocalConfigProvider.java");
        if (!isValidFile(argumentConfigFile)) {
            throw new IllegalArgumentException("获取H5小游戏项目中参数配置文件异常");
        }
        this.resDir = new File(projectDir, "app/src/main/res");
        if (!isValidDir(resDir)) {
            throw new IllegalArgumentException("获取H5小游戏项目中资源目录异常");
        }
        this.manifestFile = new File(projectDir, "app/src/main/AndroidManifest.xml");
        if (!isValidFile(manifestFile)) {
            throw new IllegalArgumentException("获取H5小游戏项目中清单文件异常");
        }
    }

    public void replaceAll(H5AppInfo h5AppInfo) throws IOException {
        replaceGradleProperties(h5AppInfo.getName(), h5AppInfo.getPackageName(), h5AppInfo.getVersion(), h5AppInfo.getSignature());
        replaceConfigArgument(h5AppInfo.getDlChannelId(), h5AppInfo.getDlProductName(), h5AppInfo.getAttrSDKKey(), h5AppInfo.getWebsiteUrl());
        replaceAppIcon(h5AppInfo.getIcon());
    }

    private void replaceAppIcon(AppIcon appIcon) throws IOException {
        File zipResFile = new File(appIcon.getZipResFile().getPath());
        File unzipDir = Files.createTempDirectory("temp_icon_unzip_").toFile();
        ZipUtil.unzip(zipResFile, unzipDir);
        File srcResDir = new File(unzipDir, "res");
        FileConflictUtil.deleteSameNameFiles(srcResDir, resDir);
        FileUtils.copyDirectory(srcResDir, resDir);

        File anyDpiDir = new File(srcResDir, "mipmap-anydpi-v26");
        File[] files = anyDpiDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File childFile) {
                return StringUtils.isNotBlank(childFile.getName()) && childFile.getName().endsWith(".xml");
            }
        });
        if (files.length > 0) {
            String baseName = FilenameUtils.getBaseName(files[0].getName());
            String xml = Files.readString(manifestFile.toPath());
            xml = xml.replaceAll(
                    "android:icon=\"[^\"]*\"",
                    String.format("android:icon=\"@mipmap/%s\"", baseName)
            );
            xml = xml.replaceAll(
                    "android:roundIcon=\"[^\"]*\"",
                    String.format("android:roundIcon=\"@mipmap/%s\"", baseName)
            );
            Files.writeString(manifestFile.toPath(), xml);
        }
        FileUtils.deleteDirectory(unzipDir);
    }

    private boolean isValidFile(File file) {
        return file != null && file.exists() && file.isFile();
    }

    private boolean isValidDir(File file) {
        return file != null && file.exists() && file.isDirectory();
    }

    private void replaceGradleProperties(String appName, String packageName, String versionName, AppSign appSign) throws IOException {
        String content = Files.readString(gradlePropertiesFile.toPath(), StandardCharsets.ISO_8859_1);
        content = replaceProperty(content, "APP_NAME", appName);
        content = replaceProperty(content, "APPLICATION_ID", packageName);
        content = replaceProperty(content, "VERSION_CODE", String.valueOf(versionNameToCode(versionName)));
        content = replaceProperty(content, "VERSION_NAME", versionName);
        content = replaceProperty(content, "RELEASE_STORE_FILE", appSign.getFileInfo().getPath().replace("\\", "/"));
        content = replaceProperty(content, "RELEASE_STORE_PASSWORD", appSign.getStorePass());
        content = replaceProperty(content, "RELEASE_KEY_ALIAS", appSign.getAlias());
        content = replaceProperty(content, "RELEASE_KEY_PASSWORD", appSign.getKeyPass());
        Files.write(gradlePropertiesFile.toPath(), content.getBytes());
    }

    public int versionNameToCode(String versionName) {
        if (versionName == null || versionName.isEmpty()) {
            throw new IllegalArgumentException("versionName不能为空");
        }
        String[] parts = versionName.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("版本号必须是 x.y.z 格式");
        }
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);
        return major * 10000 + minor * 100 + patch;
    }

    private void replaceConfigArgument(String dlChannelId, String dlProductName, String adjustAppToken, String websiteUrl) throws IOException {
        String content = Files.readString(argumentConfigFile.toPath(), StandardCharsets.UTF_8);
        content = replaceFinalStr(content, "DL_CHANNEL_ID", dlChannelId);
        content = replaceFinalStr(content, "DL_PRODUCT_NAME", String.valueOf(dlProductName));
        content = replaceFinalStr(content, "ADJUST_APP_TOKEN", adjustAppToken);
        content = replaceFinalStr(content, "H5_WEBSITE_URL", websiteUrl);
        Files.write(argumentConfigFile.toPath(), content.getBytes());
    }

    private String replaceXmlValue(String content, String key, String value) {
        if (content == null || key == null || value == null) {
            return content;
        }
        String regex = "(<string\\s+name\\s*=\\s*[\"']" + Pattern.quote(key) + "[\"']\\s*>)(.*?)(</string>)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String escapedValue = escapeXml(value);
            String replacement = matcher.group(1) + escapedValue + matcher.group(3);
            return matcher.replaceFirst(replacement);
        }
        return content;
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String replaceProperty(String content, String key, String value) {
        if (content == null || key == null || value == null) {
            return content;
        }
        String finalValue = value.trim();
        String regex = "(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*.*$";
        return content.replaceAll(regex, Matcher.quoteReplacement(key + "=" + finalValue));
    }

    private String replaceFinalStr(String content, String key, String value) {
        if (content == null || key == null || value == null) {
            return content;
        }
        // 转义新值中的特殊字符（防止正则问题）
        String escapedValue = value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("$", "\\$");

        // 正则表达式：匹配 private static final String KEY = "任意值";
        String regex = "(private\\s+static\\s+final\\s+String\\s+"
                + Pattern.quote(key)
                + "\\s*=\\s*)\"[^\"]*\"";
        String replacement = "$1\"" + escapedValue + "\"";
        return content.replaceAll(regex, replacement);
    }
}
