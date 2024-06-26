package com.sharemiracle.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.lang.annotation.Target;

@Component
@ConfigurationProperties(prefix = "share-miracle.jwt")
@Data
//@Target()
public class JwtProperties {
    /**
     * 用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private long redisTtl;
    private String userTokenName;
}
