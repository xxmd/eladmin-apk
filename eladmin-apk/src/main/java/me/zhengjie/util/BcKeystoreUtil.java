package me.zhengjie.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class BcKeystoreUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static File createJks(
            int validityDays,
            String storePass,
            String alias,
            String keyPass,
            String dName
    ) throws Exception {

        // 1️⃣ 生成 RSA 密钥对
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();

        // 2️⃣ 证书有效期
        Date start = new Date();
        Date end = new Date(System.currentTimeMillis() + validityDays * 86400000L);

        // 3️⃣ 证书主题
        X500Name subject = new X500Name(dName);

        // 4️⃣ 证书构建
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                subject,
                BigInteger.valueOf(System.currentTimeMillis()),
                start,
                end,
                subject,
                keyPair.getPublic()
        );

        // 5️⃣ 签名器
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .build(keyPair.getPrivate());
        X509CertificateHolder holder = builder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(holder);

        // 6️⃣ 创建 KeyStore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, storePass.toCharArray());
        keyStore.setKeyEntry(
                alias,
                keyPair.getPrivate(),
                keyPass.toCharArray(),
                new Certificate[]{cert}
        );

        // 7️⃣ 输出文件
        String fileName = String.format("%s_%s_%s.keystore", storePass, alias, keyPass);
        File file = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            keyStore.store(fos, storePass.toCharArray());
        }
        return file;
    }
}