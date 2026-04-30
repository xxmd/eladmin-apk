package me.zhengjie.entity.app;

import org.springframework.web.multipart.MultipartFile;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class KeyStoreReader {
    private final KeyStore keyStore;
    private final String alias;
    private X509Certificate x509Certificate;

    public KeyStoreReader(MultipartFile file, String storePass, String alias, String keyPass) {
        this.alias = alias;
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("MultipartFile为空");
        }
        try {
            this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            throw new IllegalArgumentException("获取KeyStore实例异常", e);
        }
        try {
            keyStore.load(file.getInputStream(), storePass.toCharArray());
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("密钥库密码：%s 错误", storePass), e);
        }
        try {
            if (!keyStore.containsAlias(alias)) {
                throw new IllegalArgumentException(String.format("密钥别名: %s 不存在", alias));
            }
        } catch (KeyStoreException e) {
            throw new IllegalArgumentException(String.format("查询别名: %s 异常", alias), e);
        }
        Key key;
        try {
            key = keyStore.getKey(alias, keyPass.toCharArray());
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("密钥密码: %s 错误", keyPass));
        }
        if (key == null) {
            throw new IllegalArgumentException(String.format("根据密钥密码: %s 获取密钥异常", keyPass));
        }
    }

    private X509Certificate getX509Certificate() {
        Certificate cert;
        try {
            cert = keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new RuntimeException(String.format("根据别名: %s 获取证书异常", alias), e);
        }
        if (!(cert instanceof X509Certificate)) {
            throw new RuntimeException(String.format("根据别名: %s 获取到的证书文件非X509格式", alias));
        }
        return (X509Certificate) cert;
    }

    public long getValidityMills() {
        if (x509Certificate == null) {
            x509Certificate = getX509Certificate();
        }
        long diff = x509Certificate.getNotAfter().getTime() - x509Certificate.getNotBefore().getTime();
        return diff;
    }

    public int getValidityDays() {
        return (int) (getValidityMills() / (1000L * 60 * 60 * 24));
    }

    public LdapName getDName() throws InvalidNameException {
        if (x509Certificate == null) {
            x509Certificate = getX509Certificate();
        }
        String dn = x509Certificate.getSubjectX500Principal().getName();
        return new LdapName(dn);
    }
}
