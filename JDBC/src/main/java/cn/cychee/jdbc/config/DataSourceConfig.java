package cn.cychee.jdbc.config;

import cn.cychee.jdbc.enumerate.DBTypeEnum;
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSource masterDataSource() {
        return getDataSource();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.slave1")
    public DataSource slaveOneDataSource() {
        return getDataSource();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.slave2")
    public DataSource slaveTwoDataSource() {
        return getDataSource();
    }

    private DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        Properties properties = new Properties();
        properties.put("socketTimeout", "3000");
        properties.put("connectTimeout", "1200");
        dataSource.setConnectProperties(properties);
        return dataSource;
    }

    @Bean
    public DataSource myRoutingDataSource(@Qualifier("masterDataSource") DataSource masterDataSource,
                                          @Qualifier("slaveOneDataSource") DataSource slaveOneDataSource,
                                          @Qualifier("slaveTwoDataSource") DataSource slaveTwoDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DBTypeEnum.MASTER, masterDataSource);
        targetDataSources.put(DBTypeEnum.SLAVE_ONE, slaveOneDataSource);
        targetDataSources.put(DBTypeEnum.SLAVE_TWO, slaveTwoDataSource);
        MyRoutingDataSource myRoutingDataSource = new MyRoutingDataSource();
        //设置默认数据源
        myRoutingDataSource.setDefaultTargetDataSource(masterDataSource);
        //设置目标数据源
        myRoutingDataSource.setTargetDataSources(targetDataSources);
        return myRoutingDataSource;
    }

}
