package cn.cychee.base.jdbc.interceptor;

import cn.cychee.base.jdbc.config.DBContextHolder;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Locale;
import java.util.Properties;


@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class})})
public class MybatisDataSourceInterceptor implements Interceptor {

    /**
     * 判断是插入还是增加还是删除之类的正则, u0020是空格
     */
    private static final String regex = ".*insert\\u0020.*|.*delete\\u0020.*|.update\\u0020.*";

    /**
     * 我们要操作的主要拦截方法，什么情况下去拦截，就看这个了
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //判断当前是否有实际事务处于活动状态 true 是
        boolean synchronizationActive = TransactionSynchronizationManager.isActualTransactionActive();
        //获取sql的资源变量参数（增删改查的一些参数）
        Object[] objects = invocation.getArgs();
        //MappedStatement 可以获取到到底是增加还是删除 还是修改的操作
        MappedStatement mappedStatement = (MappedStatement) objects[0];

        if (!synchronizationActive && !DBContextHolder.isUseMaster()) {
            //读方法,说明是 select 查询操作
            if (mappedStatement.getSqlCommandType().equals(SqlCommandType.SELECT)) {
                //如果selectKey 为自增id查询主键（select last_insert_id（）方法），使用主库，这个查询是自增主键的一个查询
                if (mappedStatement.getId().contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)) {
                    //使用主库
                    DBContextHolder.master();
                } else {
                    //获取到绑定的sql
                    BoundSql boundSql = mappedStatement.getSqlSource().getBoundSql(objects[1]);
                    String sqlStr = boundSql.getSql();
                    //toLowerCase方法用于把字符串转换为小写,replaceAll正则将所有的制表符转换为空格
                    String sql = sqlStr.toLowerCase(Locale.CHINA).replaceAll("[\\t\\n\\r]", " ");

                    //使用sql去匹配正则，看他是否是增加、删除、修改的sql，如果是则使用主库
                    if (sql.matches(regex)) {
                        DBContextHolder.master();
                    } else {
                        //从读库（从库），注意，读写分离后一定不能将数据写到读库中，会造成非常麻烦的问题
                        DBContextHolder.slave();
                    }
                }
            }
        } else {
            //事务管理的用主库
            DBContextHolder.master();
        }
        return invocation.proceed();
    }

    /**
     * 返回封装好的对象，决定返回的是本体还是编织好的代理
     */
    @Override
    public Object plugin(Object target) {
        //Executor是mybatis的，所有的增删改查都会经过这个类
        if (target instanceof Executor) {
            //如果是Executor 那就进行拦截
            return Plugin.wrap(target, this);
        } else {
            //否则返回本体
            return target;
        }
    }

    /**
     * 类初始化的时候做一些操作
     */
    @Override
    public void setProperties(Properties properties) {

    }
}