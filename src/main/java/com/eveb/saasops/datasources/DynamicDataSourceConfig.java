package com.eveb.saasops.datasources;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 配置多数据源
 */
@Configuration
@Data
public class DynamicDataSourceConfig {
    private Logger logger = LoggerFactory.getLogger(DynamicDataSourceConfig.class);
    Map<String, DataSource> targetDataSources ;

    @Bean
    @ConfigurationProperties("spring.datasource.druid.manage")
    public DataSource manageDaraSource(){
        return DruidDataSourceBuilder.create().build();
    }

}
