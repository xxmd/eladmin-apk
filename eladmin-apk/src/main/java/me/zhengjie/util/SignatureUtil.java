package me.zhengjie.util;

import com.android.apksig.ApkSigner;
import com.android.apksig.apk.ApkFormatException;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.entity.app.AppSign;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class SignatureUtil {
    public static File signApk(AppSign appSign, File apkFile) throws ApkFormatException, IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, UnrecoverableKeyException, CertificateException, KeyStoreException {
        try {
            ApkSigner.SignerConfig signerConfig = buildSignerConfig(appSign);
            ApkSigner.Builder builder = new ApkSigner.Builder(
                    Collections.singletonList(signerConfig)
            );
            builder.setInputApk(apkFile);
            String srcName = apkFile.getName();
            String signApkFilePath = FilenameUtils.getBaseName(srcName) + "_signed." + FilenameUtils.getExtension(srcName);
            File signApkFile = new File(apkFile.getParentFile(), signApkFilePath);
            builder.setOutputApk(signApkFile);
            builder.setV1SigningEnabled(true);
            builder.setV2SigningEnabled(true);
            builder.setV3SigningEnabled(true);
            ApkSigner apkSigner = builder.build();
            apkSigner.sign();
            return signApkFile;
        } catch (Exception e) {
            log.error(String.format("使用签名文件: {} 给应用: {} 签名异常", appSign.getFileInfo().getPath(), apkFile.getAbsolutePath()), e);
            throw e;
        }
    }

    private static ApkSigner.SignerConfig buildSignerConfig(AppSign appSign) throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        String storePass = appSign.getStorePass();
        String alias = appSign.getAlias();
        String keyPass = appSign.getKeyPass();
        String path = appSign.getFileInfo().getPath();
        File signFile = new File(path);
        try (FileInputStream fis = new FileInputStream(signFile)) {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // 自动判断 JKS 或 PKCS12
            keystore.load(fis, storePass.toCharArray());
            PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, keyPass.toCharArray());
            if (privateKey == null) {
                throw new IllegalArgumentException("别名 '" + alias + "' 不存在或密码错误");
            }
            Certificate[] certChain = keystore.getCertificateChain(alias);
            if (certChain == null || certChain.length == 0) {
                throw new IllegalArgumentException("别名 '" + alias + "' 没有对应的证书链");
            }
            List<X509Certificate> certificates = new ArrayList<>();
            for (Certificate cert : certChain) {
                if (cert instanceof X509Certificate) {
                    certificates.add((X509Certificate) cert);
                } else {
                    throw new IllegalArgumentException("证书链中包含非 X509 证书");
                }
            }
            return new ApkSigner.SignerConfig.Builder(
                    alias,
                    privateKey,
                    certificates
            ).build();
        } catch (Exception e) {
            log.error(String.format("通过签名文件: {} 生成签名配置异常", appSign.getFileInfo().getPath()), e);
            throw e;
        }
    }
}
