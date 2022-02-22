package com.gitee.spring.domain.processor;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.gitee.spring.domain.annotation.Root;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LimitedRootInitializingBean implements ApplicationContextAware, InitializingBean {

    private final static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC45/wXnQpCphCYJOS19eYFEVuNOFWtLfhB8NasNRHwoDIchcYm4WvTcDoo+ZY74EhBtHh6sGnWUpNGCf6Fnl1mgy72qvSGsiXJS7ZEgtFs9qneNO84Oki1rrzO75uQYdeAntZXpbNieyABlMon4fU/If1cIbVFSkz+VvIgZs+nswIDAQAB";

    protected final Log logger = LogFactory.getLog(getClass());
    private final Map<String, String> domainPatternMapping;
    private final String sign;
    private ApplicationContext applicationContext;

    public LimitedRootInitializingBean(Map<String, String> domainPatternMapping, String sign) {
        this.domainPatternMapping = domainPatternMapping;
        this.sign = sign;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Root.class);
        String md5Str = getRootServicesMd5Str(beans);
        String signMd5Str = decryptSign(sign);
        if (!md5Str.equals(signMd5Str)) {
            throw new BeanCreationException("The root services are not authorized! md5Str: [" + md5Str + "], signMd5Str: [" + signMd5Str + "]");
        }
    }

    private String getRootServicesMd5Str(Map<String, Object> beans) {
        List<String> typeNames = new ArrayList<>();
        domainPatternMapping.forEach((domain, pattern) -> typeNames.add(domain + ":" + pattern));
        beans.forEach((id, bean) -> typeNames.add(bean.getClass().getName()));
        String string = typeNames.stream().sorted().collect(Collectors.joining(", "));
        String md5Str = SecureUtil.md5(string);
        logger.info("MD5 value of root services is [" + md5Str + "]");
        return md5Str;
    }

    private String decryptSign(String sign) {
        try {
            RSA rsa = new RSA(null, PUBLIC_KEY);
            byte[] bytes = rsa.decrypt(sign, KeyType.PublicKey);
            return StrUtil.str(bytes, "UTF-8");

        } catch (Exception e) {
            logger.error("Decryption failed! Please check the configuration!");
        }
        return null;
    }

    public static void main(String[] args) {
//        KeyPair pair = SecureUtil.generateKeyPair("RSA");
//        System.out.println(Base64.encode(pair.getPrivate().getEncoded()));
//        System.out.println(Base64.encode(pair.getPublic().getEncoded()));
        File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
        String desktopPath = desktopDir.getAbsolutePath();
        String PRIVATE_KEY = FileUtil.readString(desktopPath + "\\PRIVATE_KEY.txt", "UTF-8");
        String md5Str = "adf61045e9b5abb7d00d057ca6ff3ddf";
        RSA rsa = new RSA(PRIVATE_KEY, null);
        byte[] bytes = rsa.encrypt(md5Str, KeyType.PrivateKey);
        System.out.println(Base64.encode(bytes));
    }

}
