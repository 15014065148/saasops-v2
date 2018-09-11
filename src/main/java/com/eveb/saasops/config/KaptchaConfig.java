package com.eveb.saasops.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;


/**
 * 生成验证码配置
 *
 */
@Configuration
public class KaptchaConfig {

    @Bean
    public DefaultKaptcha producer() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "160");
        properties.setProperty("kaptcha.image.height", "60");
        properties.setProperty("kaptcha.textproducer.font.size", "45");
        properties.setProperty("kaptcha.textproducer.char.space", "5");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.font.color", "237,81,81");
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
        properties.setProperty("kaptcha.textproducer.font.names", "楷体");
        properties.setProperty("kaptcha.background.impl", "com.google.code.kaptcha.impl.DefaultBackground");
        //取消干扰线
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        
        
        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
