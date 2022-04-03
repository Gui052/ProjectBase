package cn.cychee.interceptor;

import cn.cychee.config.DBContextHolder;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Properties;

/**
 * 实现mybatis plugin
 * 当前线程使用的数据源对应的key，这个key需要在mybatis plugin根据sql类型来确定
 * 如果这里不是用mybatis，可以手动写切面拦截数据库操作，设置数据源为主库还是从库
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {
                        MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class})})
public class MybatisDataSourceInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        if (!synchronizationActive) {
            Object[] objects = invocation.getArgs();
            MappedStatement ms = (MappedStatement) objects[0];

            //查询走从库，如果标记了强制走主库，则走主库
            if (ms.getSqlCommandType().equals(SqlCommandType.SELECT) && !DBContextHolder.isUseMaster()) {
                DBContextHolder.slave();
            } else {
                DBContextHolder.master();
            }
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}