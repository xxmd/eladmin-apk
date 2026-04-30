package me.zhengjie.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class GradleUtil {
    public static void assembleRelease(File projectDir) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        grantGradlew(projectDir, isWindows);
        ProcessBuilder processBuilder = getProcessBuilder(projectDir, isWindows);
        Process process = processBuilder.start();
        printGradleOut(process);
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            log.info("apk打包成功");
        } else {
            throw new RuntimeException(String.format("gradlew异常结束, exitCode: %d", exitCode));
        }
    }

    private static void printGradleOut(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (StringUtils.isNotBlank(line)) {
                log.info("[Gradle] {}", line);
            }
        }
    }

    private static void grantGradlew(File projectDir, boolean isWindows) throws IOException, InterruptedException {
        if (isWindows) {
            return;
        }
        ProcessBuilder chmod = new ProcessBuilder("chmod", "+x", "gradlew");
        chmod.directory(projectDir);
        Process chmodProcess = chmod.start();
        int chmodCode = chmodProcess.waitFor();
        if (chmodCode != 0) {
            throw new RuntimeException("gradlew chmod 失败");
        }
    }

    private static ProcessBuilder getProcessBuilder(File projectDir, boolean isWindows) {
        ProcessBuilder pb = new ProcessBuilder();
        if (isWindows) {
            pb.command("cmd.exe", "/c", "gradlew.bat", "assembleRelease");
        } else {
            pb.command("./gradlew", "assembleRelease");
        }
        pb.directory(projectDir);
        pb.redirectErrorStream(true);
        Map<String, String> env = pb.environment();
        env.put("GRADLE_OPTS", "-Dfile.encoding=UTF-8");
        env.put("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8 -Duser.language=en");
        env.put("LC_ALL", "en_US.UTF-8");
        return pb;
    }
}