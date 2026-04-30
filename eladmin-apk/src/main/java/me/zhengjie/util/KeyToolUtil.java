package me.zhengjie.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
public class KeyToolUtil {
    public static File createJksFile(int validityDays, String storePass, String alias, String keyPass, String dName) throws IOException, InterruptedException {
        String fileName = String.format("%s_%s_%s.jks", storePass, alias, keyPass);
        File jksFile = new File(fileName);
        if (jksFile.exists()) {
            jksFile.delete();
        }
        ProcessBuilder processBuilder = new ProcessBuilder(
                "keytool",
                "-J-Dfile.encoding=UTF-8",
                "-genkeypair",
                "-v",
                "-keystore", jksFile.getAbsolutePath(),
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", String.valueOf(validityDays),
                "-storepass", storePass,
                "-alias", alias,
                "-keypass", keyPass,
                "-dname", dName
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("keytool创建签名文件失败: {}", output);
            throw new RuntimeException(String.format("keytool创建签名文件失败"));
        }
        return jksFile;
    }
}