package com.gitee.spring.domain.processor;

import cn.hutool.core.codec.Base64;
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

import java.security.KeyPair;
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
        domainPatternMapping.forEach((domain, pattern) -> typeNames.add(pattern));
        beans.forEach((id, bean) -> typeNames.add(bean.getClass().getName()));
        String string = typeNames.stream().sorted().collect(Collectors.joining(", "));
        String md5Str = SecureUtil.md5(string);
        logger.info("MD5 value of root services is [" + md5Str + "]");
        return md5Str;
    }

    private String decryptSign(String sign) {
        RSA rsa = new RSA(null, PUBLIC_KEY);
        byte[] bytes = rsa.decrypt(sign, KeyType.PublicKey);
        return StrUtil.str(bytes, "UTF-8");
    }

    public static void main(String[] args) {
        KeyPair pair = SecureUtil.generateKeyPair("RSA");
        System.out.println(Base64.encode(pair.getPrivate().getEncoded()));
        System.out.println(Base64.encode(pair.getPublic().getEncoded()));
    }

}
