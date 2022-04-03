package cn.cychee.jdbc.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;

/**
 * 获取路由key
 */
public class MyRoutingDataSource extends AbstractRoutingDataSource {
    /**
     * 决定使用数据源的方法
     */
    @Nullable
    @Override
    protected Object determineCurrentLookupKey() {
        return DBContextHolder.get();
    }
}